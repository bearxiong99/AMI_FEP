/**
 * 管理多个SocketIoThread对象，实现socket IO线程池的功能。非通用线程池实现。
 * 所有socketChannel对象的IO操作。每个SocketChannel只能归属于某个特定线程。
 */
package cn.hexing.fk.sockserver.io;

import java.util.ArrayList;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;

/**
 *
 */
public class SocketIoThreadPool {
	private static final Logger log = Logger.getLogger(SocketIoThreadPool.class);
	private int ioThreadSize;
	private int port;				//一般对应Server的端口
	private IClientIO ioHandler;
	private ArrayList<SocketIoThread> threads = new ArrayList<SocketIoThread>();
	
	public SocketIoThreadPool(int port, int ioSize,IClientIO ioHandler){
		this.port = port;
		ioThreadSize = ioSize;
		this.ioHandler = ioHandler;
	}
	
	public void start(){
		for(int i=0; i<ioThreadSize; i++ )
			threads.add(new SocketIoThread(port,ioHandler,i));
		//等待SocketIoThread执行完成。
		Thread.yield();
	}
	
	public void stop(){
		for(int i=0; i<ioThreadSize; i++ ){
			threads.get(i).stopThread();
		}
		threads.clear();
	}
	
	/**
	 * 异步Socket服务器把Socket client均匀的分配到各个IO工作线程。
	 * @param client
	 */
	public void acceptNewClient(IServerSideChannel client){
		scheduleClient(client,true);
	}
	
	public void addConnectedClient(IServerSideChannel client){
		scheduleClient(client,false);
	}
	
	private void scheduleClient(IServerSideChannel client, boolean acceptMode){
		//冒泡排序算法，查找client数量最小的线程。
		SocketIoThread cur=null,q=null;
		int min = Integer.MAX_VALUE;
		for(int i=0; i<ioThreadSize; i++ ){
			q = threads.get(i);
			if(q.getClientSize()<min ){
				cur = q;
				min = cur.getClientSize();
			}
		}
		if( null == cur ){
			log.error("scheduleClient failed. cur thread == null.");
			return;
		}
		if( acceptMode )
			cur.acceptClient(client);
		else
			cur.addConnectedClient(client);
	}
}
