


import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

/********************************
 *
 *@author:高亮亮
 *@date  :2012-3-26:下午04:42:47
 *@info  :
 *
 ********************************/
public class Server extends Thread
{

	String ip="localhost";
	
	int port=1024;
	
	ServerSocketChannel serverChannel;
	
	Selector selector;
	
	public static void main(String[] args) {
		Server s = new Server();
		s.start();
	}
	
	public Server()
	{
		
		InetSocketAddress isa = new InetSocketAddress(ip,port);
	
		try {
			selector = Selector.open();
			
			serverChannel = ServerSocketChannel.open();
			
			serverChannel.configureBlocking(false);
			
			serverChannel.socket().bind(isa);
			
			serverChannel.register(selector, SelectionKey.OP_ACCEPT);
			
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	@Override
	public void run()
	{
		while(true)
		{
			try {
				int n = selector.select(1000);
				
				if(n==0)
				{
					continue;
				}
				
				Iterator<SelectionKey> it = selector.selectedKeys().iterator();
				while(it.hasNext())
				{
					SelectionKey key = it.next();
				
					if(key.isValid() && key.isAcceptable())
					{
						ServerSocketChannel schannel=(ServerSocketChannel) key.channel();
						SocketChannel channel = null;
						try {
							channel = schannel.accept();
							/**注册本次连接的通道信息**/
							channel.configureBlocking(false);
							channel.register(selector, SelectionKey.OP_READ);
							
						} catch (IOException e) {
							e.printStackTrace();
						}
					
					}else if(key.isValid() &&key.isReadable()  )
					{
						SocketChannel channel = (SocketChannel) key.channel();
						ByteBuffer buffer = ByteBuffer.allocate(1000);
						channel.read(buffer );
					}
				
				}
				
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
}
