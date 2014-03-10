/**
 * 每个线程维护一个Selector。每个SocketChannel只能归属于某个Thread。
 * 
 * 对于网关终端服务来说，下行命令优先级最高，因此发送事件优先；
 * 而对于网关前置机应用来说，接收下行命令优先级高，因此读事件优先。
 * 因此需要进行读、写优先级控制机制。
 *    控制机制：通过interestOps。如果存在发送没有完成，则设置一个连续发送多少次，才允许读。
 *    		  次数只能控制在client对象属性中。
 */
package cn.hexing.fk.sockserver.io;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.EventQueue;
import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;

import cn.hexing.fk.exception.SocketClientCloseException;
import cn.hexing.fk.sockclient.async.event.ClientConnectedEvent;
import cn.hexing.fk.sockserver.event.AcceptEvent;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ClientWriteReqEvent;
import cn.hexing.fk.tracelog.TraceLog;

/**
 */
public class SocketIoThread extends Thread {
	private static final Logger log = Logger.getLogger(SocketIoThread.class);
	private static final TraceLog tracer = TraceLog.getTracer();
	private volatile cn.hexing.fk.utils.State state = cn.hexing.fk.utils.State.STOPPED;
	private IClientIO ioHandler;
	private Selector selector;
	//每个线程维护SocketClient列表，使得多个线程均衡工作。
	private List<IServerSideChannel> list = Collections.synchronizedList(new LinkedList<IServerSideChannel>());
	private EventQueue queue = new EventQueue();
	
	public SocketIoThread(int port,IClientIO ioHandler,int index){
		super("io-"+port+"-"+index);
		this.ioHandler = ioHandler;
		super.start();
	}
	
	public void stopThread(){
		state = cn.hexing.fk.utils.State.STOPPING;
		this.interrupt();
	}
	
	public boolean isRunning(){
		return state == cn.hexing.fk.utils.State.RUNNING;
	}
	
	public void acceptClient(IServerSideChannel client){
		client.setIoThread(this);
		list.add(client);

		//事件系统来说，事件是不能丢弃的。但是考虑到系统的正常运行，这里提供1秒中的插入尝试时间。
		//事件处理应该是高效的。如果失败，则需要跟踪日志，以便调整事件队列大小。
		IEvent event = new AcceptEvent(client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//事件插入队列失败，是非常严重的事情。
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void addConnectedClient(IServerSideChannel client){
		client.setIoThread(this);
		list.add(client);

		IEvent event = new ClientConnectedEvent(client.getServer(),client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//事件插入队列失败，是非常严重的事情。
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void closeClientRequest(IServerSideChannel client){
		list.remove(client);
		
		IEvent event = new ClientCloseEvent(client);
		int cnt = 20;
		while( cnt-- > 0 ){
			try{
				queue.offer( event );
			}catch(Exception exp){
				//事件插入队列失败，是非常严重的事情。
				String info = "SocketIoThread can not offer event. reason is "+exp.getLocalizedMessage()+". event is"+event.toString();
				log.fatal(info,exp);
				tracer.trace(info,exp);
				try{
					Thread.sleep(50);
				}catch(Exception te){}
			}
		}

		selector.wakeup();
	}
	
	public void clientWriteRequest(IServerSideChannel client){
		try{
			queue.offer(new ClientWriteReqEvent(client));
			selector.wakeup();
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}
	
	public int getClientSize(){
		return list.size();
	}
	
	public void run(){
		try{
			selector = Selector.open();
		}
		catch(IOException e){
			log.error("Selector.open()",e);
			return;
		}
		state = cn.hexing.fk.utils.State.RUNNING;
		while( state != cn.hexing.fk.utils.State.STOPPING ){
			try{
				//JDK存在缺陷：selector()可能检测不够迅速，因此采用selector(100)。请不要修改这里代码。
				int n = selector.select(100);
				if( state == cn.hexing.fk.utils.State.STOPPING )
					break;
				//先处理异步关闭socket client等事件。
				if( handleEvent() )
					continue;
				
				Set<SelectionKey> set = selector.selectedKeys();
				if( null== set )
					continue;
				if( 0==n && set.size()<=0 )
					continue;
				Iterator<SelectionKey> it = set.iterator();
				while(it.hasNext()){
					SelectionKey key = it.next();
					it.remove();
					IServerSideChannel client = (IServerSideChannel)key.attachment();
					if( null == client ){
						key.cancel();	key.attach(null);
						log.warn("null==key.attachment()");
						continue;
					}
					if( !key.isValid() ){
						key.cancel();	key.attach(null);
						log.warn("!key.isValid()");
						continue;
					}
					if( key.isReadable() ){		//持续的读取数据。
						client.setLastReadTime() ;
						try{
							//JDK缺陷：缓冲区读取后，在生成接收消息过程中，如果对方继续把数据发送到socket缓冲区，
							//那么select不会检测到可读事件。这将导致部分数据不能及时处理。采用心跳可以"激活"数据。
							//最好办法是，生成一次读取socket数据内部事件。
							//在发送优先情况下，读取内部事件可以滞后生成；发送不优先情况下，再次调用onRecieve。
							//返回是否全部读取完毕。如果发送优先级高，那么读取部分消息后，应该把控制权交给发送。
							boolean readDone = ioHandler.onReceive(client);
							client.getServer().setLastReceiveTime(System.currentTimeMillis());
							//如果readDone=true，表示读优先，可以继续读。false，表示控制权交给写。
							if( readDone ){
								ioHandler.onReceive(client);		//再次读，防止丢包
								//如果有消息需要发送，则等发送完毕，再继续读。
								if( client.sendQueueSize()>1 ){
									int interest = key.interestOps();
									interest &= ~SelectionKey.OP_READ;
									interest |= SelectionKey.OP_WRITE;
									key.interestOps( interest );
								}
							}
							else{
								int interest = key.interestOps();
								interest &= ~SelectionKey.OP_READ;
								interest |= SelectionKey.OP_WRITE;
								key.interestOps( interest );
							}
						}catch(SocketClientCloseException cce){
							//对方关闭Socket连接。
							client.getServer().removeClient(client);
							key.cancel();
							client.close();
							list.remove(client);
							GlobalEventHandler.postEvent(new ClientCloseEvent(client));
							continue;
						}
						catch(Exception exp){
							//读取数据异常
							log.warn("server.getIoHandler().onReceive(client):"+exp.getLocalizedMessage(),exp);
						}
					}
					if( key.isWritable() ){
						//缓冲区空闲，可以发送。但是数据发送完毕，则取消写
						try{
							doWrite(client);
						}catch(SocketClientCloseException cce){
							//对方关闭Socket连接。
							client.getServer().removeClient(client);
							key.cancel();
							client.close();
							list.remove(client);
							GlobalEventHandler.postEvent(new ClientCloseEvent(client));
							if(log.isDebugEnabled())
								log.debug("server["+client.getServer().getPort()+"]doWrite exp. client closed.",cce);
							continue;
						}
						catch(Exception exp){
							//写数据异常
							log.warn("server.getIoHandler().onSend(client):"+exp.getLocalizedMessage(),exp);
						}
					}
				}
			}
			catch(Exception e){
				log.error("select(500) exception:"+e,e);
			}
		}
		try{
			selector.close();
		}
		catch(IOException e){
			log.error("Selector.close()",e);
		}
		finally{
			selector = null;
			state = cn.hexing.fk.utils.State.STOPPED;
		}
	}
	
	private void doWrite(IServerSideChannel client) throws SocketClientCloseException{
		client.setLastIoTime();
		boolean finishWrite = false;
		finishWrite = ioHandler.onSend(client);
		SelectionKey key = client.getChannel().keyFor(selector);
		int interest = key.interestOps();

		//写优先级控制机制
		if( finishWrite && client.sendQueueSize()==0 ){
			client.setLastingWrite(0);
			interest &= ~SelectionKey.OP_WRITE;		//去掉可写检测
			interest |= SelectionKey.OP_READ;
			client.getServer().setLastSendTime(System.currentTimeMillis());
			//如果全部数据发送完毕，则需要读一下数据。防止socket缓冲区数据遗漏，select可能不会检测到。
			ioHandler.onReceive(client);
		}
		else
			client.setLastingWrite(client.getLastingWrite()+1);
		if( client.getLastingWrite() > client.getServer().getWriteFirstCount() ){
			interest |= SelectionKey.OP_READ;
			client.setLastingWrite(0);
		}
		key.interestOps(interest);
	}
	
	private boolean handleEvent(){
		IEvent ee = null;
		boolean processed = false;
		while( null !=( ee=queue.poll()) ){
			processed = true;
			if( ee.getType() == EventType.ACCEPTCLIENT ){
				//当服务器接受客户端连接后，需要注册“读”
				try{
					AcceptEvent e = (AcceptEvent) ee;
					IServerSideChannel client = e.getClient();
					client.getChannel().configureBlocking(false);
					client.getChannel().register(selector,SelectionKey.OP_READ, client);
					//SocketServer已经调用过，不要再次调用。GlobalEventHandler.postEvent(ee);
				}catch(Exception exp){
					log.error("accept client后处理异常:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENTCLOSE ){
				try{//强制关闭事件
					ClientCloseEvent e = (ClientCloseEvent)ee;
					IServerSideChannel client = e.getClient();
					if( null == client.getChannel() )
						continue;
					SelectionKey key = client.getChannel().keyFor(selector);
					if( null != key )
						key.cancel();
					client.close();
					//socket client关闭事件
					GlobalEventHandler.postEvent(ee);
				}catch(Exception exp){
					log.error("外部调用关闭client处理异常:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENT_WRITE_REQ ){
				try{
					ClientWriteReqEvent e = (ClientWriteReqEvent)ee;
					IServerSideChannel client = e.getClient();
					//if client is closed.
					if( null == client.getChannel() )
						continue;
					SelectionKey key = client.getChannel().keyFor(selector);
					int interest = key.interestOps();
					interest |= SelectionKey.OP_WRITE;		//增加可写检测
					key.interestOps(interest);
				}catch(Exception exp){
					log.error("client写请求处理异常:"+exp.getLocalizedMessage(),exp);
				}
			}
			else if( ee.getType() == EventType.CLIENT_CONNECTED ){
				ClientConnectedEvent e = (ClientConnectedEvent)ee;
				IServerSideChannel client = (IServerSideChannel)e.getClient();
				try{
					client.getChannel().configureBlocking(false);
					client.getChannel().register(selector,SelectionKey.OP_READ, client);
				}catch(Exception exp){
					log.error("connected client后处理异常:"+exp.getLocalizedMessage(),exp);
				}
			}
		}
		return processed;
	}
}
