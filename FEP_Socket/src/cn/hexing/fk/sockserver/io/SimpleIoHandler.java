/**
 * SimpleIoHandler类实现简单的数据收、发。数据不作实际处理。
 * 用于异步TCP Socket服务器进行IO处理。
 */
package cn.hexing.fk.sockserver.io;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;

import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.exception.SocketClientCloseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;
import cn.hexing.fk.tracelog.TraceLog;

/**
 *	ByteBuffer操作注意事项：先写（put），flip(),然后才可以读(get)；
 */
public class SimpleIoHandler implements IClientIO {
	private static final Logger log = Logger.getLogger(SimpleIoHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(SimpleIoHandler.class);

	/**
	 * called when socket channel receive data.
	 * 特别注意，bufRead永远处于fill data状态，即 limit==capacity;
	 */
	public boolean onReceive(IServerSideChannel client) throws SocketClientCloseException{
		int msgCount = 0;
		ByteBuffer readBuf = client.getBufRead();
		int bytesRead = 0,n=0;
		if( readBuf.remaining() == 0 ){
			log.info("SimpleIoHandler.onReceive error. readBuf empty:pos="+readBuf.position()+",limit="+readBuf.limit()+",capacity="+readBuf.capacity());
			readBuf.clear();
		}
		while(true){	//该循环务必保留。不理解的，继续研究算法。
			try{
				n = client.getChannel().read(readBuf);
			}catch(IOException e){
				log.warn("client.getChannel().read(readBuf)异常:"+e.getLocalizedMessage());
				throw new SocketClientCloseException(e);
			}
			if( n<0 ){ //对方主动关闭
				String info = "client close socket:"+client.toString();
				log.info(info);
				throw new SocketClientCloseException(info);
			}
			bytesRead += n;
			if( n==0 ){
				if( !readBuf.hasRemaining() ){
					//readBuf满了。需要消息对象处理缓冲区数据，使得缓冲区空闲出来继续接收数据
					readBuf.flip();
//					log.warn("dump readbuf:"+HexDump.hexDump(readBuf));
					msgCount = processBuffer(readBuf,client,msgCount);
					if( msgCount<0 ){
						//过度读取。暂时放弃读取数据。
						if( log.isDebugEnabled() )
							log.debug("过度读取。暂时放弃读取数据");
						return false;
					}
				}
				else{
					//readBuf还有数据，但socket缓冲区没有数据了。
					break;
				}
			}
		}
		if( bytesRead ==0 ){
			//读取0字节数据，socket关闭
			return true;
		}
		readBuf.flip();		//注意，readBuf刚刚'put'进去一些数据，flip然后才能读取处理
//		log.warn("dump readbuf:"+HexDump.hexDump(readBuf));
		msgCount = processBuffer(readBuf,client,msgCount);
		return  msgCount>= 0 ;
	}
	
	/**
	 * 处理读缓冲的数据，生成相应的报文对象。
	 * @param buf
	 * @param client
	 */
	private int processBuffer(ByteBuffer buf,IServerSideChannel client, int count) throws SocketClientCloseException{
		/** 从技术上讲，buf内容可能包含多个帧的数据，因此需要循环处理。
		 *  缓冲区读取数据后（可能部分数据，还有少量剩余数据），需要删除已经被处理的数据，
		 *  然后允许buf继续写。
		 */
		while(buf.hasRemaining()){ //该循环务必保留。不理解的，继续研究算法。
			IMessage msg = client.getCurReadingMsg();
			if( null == msg ){
				//client当前没有消息对象，需要创建新消息
				int rem1 = buf.remaining();
				msg = client.getServer().createMessage(buf);
				int rem2 = buf.remaining();
				if( null == msg ){
					if( rem1>=9 && rem1 == rem2 ){
						//大于13字节，一定可以识别国网 浙规的报文。即使不能识别，也需要丢弃非法数据。
						//否则表示服务器配置的messageCreator不能创建对象。
						String info = "消息对象类型配置错误,server port="+client.getServer().getPort();
						log.fatal(info);
						buf.clear();
						throw new SocketClientCloseException(info);
					}
					else{
						if( buf.hasRemaining() )
							buf.compact();
						else
							buf.clear();
						return 0;
					}
				}
				client.setCurReadingMsg(msg);
				msg.setSource(client);
				msg.setServerAddress(client.getServer().getServerAddress());
			}
			boolean down = false;
			try{
				down = msg.read(buf);
			}catch(MessageParseException mpe){
				String expInfo = mpe.getLocalizedMessage();
				log.warn("Read Message Exception:"+expInfo,mpe);//log.warn("读消息异常："+expInfo,mpe);
				
				//在测试模式下，把信息直接发送给client，以便测试工具知道错误原因。
/*				if( FasSystem.getFasSystem().isTestMode() ){
					SocketChannel channel = client.getChannel();
					if( null == channel ){
						if( buf.hasRemaining() )
							buf.compact();
						else
							buf.clear();
						return 0;
					}
					ByteBuffer writeBuf = client.getBufWrite();
					writeBuf.clear();
					byte[] expBytes = expInfo.getBytes();
					int len = Math.min(expBytes.length, writeBuf.remaining());
					writeBuf.put(expBytes,0,len);
					writeBuf.flip();
					flush(channel,writeBuf);
					client.setCurWritingMsg(null);
				}
*/				
				//消息读取异常，因此需要重新读取。考虑到程序健壮性，下次读取新的消息。
				client.setCurReadingMsg(null);
				if( buf.hasRemaining() )
					buf.compact();
				else
					buf.clear();
				throw new SocketClientCloseException(mpe.getLocalizedMessage());
				//return 0;
			}
			if( down ){		//消息已经完整读取。
				count++;
				client.setCurReadingMsg(null);
				ReceiveMessageEvent ev = new ReceiveMessageEvent(msg,client);
				msg.setIoTime(System.currentTimeMillis());
				msg.setPeerAddr(client.getPeerAddr());
				msg.setTxfs(client.getServer().getTxfs());
				GlobalEventHandler.postEvent( ev );
				//防止过渡读取
				int maxCanRead = client.getServer().getMaxContinueRead();
				int sendReqCount = client.sendQueueSize();
				if( maxCanRead > 0 && sendReqCount>0 && count>= maxCanRead ){
					//暂时放弃读取数据。
					if( buf.hasRemaining() )
						buf.compact();
					else
						buf.clear();
					return -1;
				}
			}
			else
				break;
		}
		//注意，缓冲区可能还有剩余数据没有被处理。剩余数据移到前面，可以继续put数据。
		//如果缓冲区所有数据都被处理完，那么compact相当于clear。满足要求.
		if( buf.hasRemaining() )
			buf.compact();
		else
			buf.clear();
		return count;
	}

	/**
	 * called when socket buffer can put more data to send.
	 * 如果client对象所有内容都被发送完毕，则返回true, false otherwise.
	 */
	public boolean onSend(IServerSideChannel client) throws SocketClientCloseException{
		//1.先发送缓冲区剩余的数据。
		ByteBuffer writeBuf = client.getBufWrite();
		IMessage msg = client.getCurWritingMsg();
		boolean sent = false;
		//Modified by bhw 2009-1-17 13:44 in that message is write done but buffer is not sent.
		if( client.bufferHasRemaining() ){
			sent = flush(client.getChannel(),writeBuf);
			if( ! sent ){									//缓冲区数据没有发送完毕
				log.debug("flush(client.getChannel(),writeBuf),buff area unfinish:msg="+msg.getRawPacketString());
				return false;
			}
		}
		//end modified
		
		if( null != msg ){
			//当前有消息正在发送，继续从消息读取数据发送
			sent = sendMessage(msg,client);
			if( !sent ){	//消息没有发送完，剩余数据在缓冲区
				log.debug("sendMessage(msg,client),unfinish,remaining data in buff area:msg="+msg.getRawPacketString());
				return false;
			}
		}
		//检查待发送队列，继续发送消息对象。
		while( null != (msg=client.getNewSendMessage()) ){
			client.setCurWritingMsg(msg);
			sent = sendMessage(msg,client);
			if( !sent ){	//消息没有发送完，剩余数据在缓冲区
				log.debug("sendMessage(msg,client),unfinish,remaining data in buff area:msg="+msg.getRawPacketString());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * 发送单个消息。
	 * @param msg
	 * @return true 如果整个消息发送完毕；false otherwise。
	 * @throws SocketClientCloseException
	 */
	private boolean sendMessage(IMessage msg,IServerSideChannel client)throws SocketClientCloseException{
		ByteBuffer writeBuf = client.getBufWrite();
		boolean done = false,sent = false;
		//增加死循环检测功能
		int deadloop = 0;
		while( !done ){
			done = msg.write(writeBuf);
			writeBuf.flip();
			sent = flush(client.getChannel(),writeBuf);
			
			//Modified by bhw 2009-1-17 13:44 in that message is write done but buffer is not sent.
			if( done ){
				client.setCurWritingMsg(null);
				msg.setIoTime(System.currentTimeMillis());
				msg.setPeerAddr(client.getPeerAddr());
				msg.setSource(client);
				msg.setTxfs(client.getServer().getTxfs());
				//通知，该消息已经完整发送出去啦
				GlobalEventHandler.postEvent(new SendMessageEvent(msg,client));
				if (client.getServer().getClientSize()<=100){//避免大量客户端存在时打印出异常
					StringBuffer sb = new StringBuffer();
					sb.append("server port="+client.getServer().getPort()).append(",clients=");
					for( IServerSideChannel c: client.getServer().getClients()){
						sb.append(c.toString()).append(",");
					}
					trace.trace(sb.toString());
				}
			}
			//end modified
			
			if( !sent ){
				//缓冲区数据没有发送完毕
				client.setBufferHasRemaining(true);
				return false;
			}
			client.setBufferHasRemaining(false);
			if( ++deadloop > 1000 ){
				log.fatal("Message.write方法死循环错误："+msg.getClass().getName());
				return true;			//return true，丢失该消息对象。以免系统崩溃。
			}
		}
		return true;
	}

	private boolean flush(SocketChannel channel,ByteBuffer buf) throws SocketClientCloseException{
		int bytesWritten = 0;
		while( buf.hasRemaining() ){
			try{
				bytesWritten = channel.write(buf);
			}catch(IOException exp){
				String s = "channel.write()异常，原因"+exp.getLocalizedMessage();
				log.warn(s,exp);
				throw new SocketClientCloseException(exp);
			}
			if( 0 == bytesWritten )
				return false;		//socket buffer full，但是还有数据没有发送完
		}
		//缓冲区buf数据全部写到socket buffer
		buf.clear();		//缓冲区清空以便下次写
		return true;
	}
}
