/**
 * UDPͨ���������ա��������ࡣ
 * UDP���������õ��̶߳����ݡ�
 */
package cn.hexing.fk.sockserver.io;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.events.GlobalEventHandler;
import cn.hexing.fk.common.spi.socket.IClientIO;
import cn.hexing.fk.common.spi.socket.IServerSideChannel;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.exception.SocketClientCloseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

/**
 */
public class SimpleUdpIoHandler implements IClientIO {
	private static final Logger log = Logger.getLogger(SimpleUdpIoHandler.class);

	public boolean onSend(IServerSideChannel client)
			throws SocketClientCloseException {
		return false;
	}

	public boolean onReceive(IServerSideChannel client) throws SocketClientCloseException 
	{
		/** �Ӽ����Ͻ���buf���ݿ��ܰ������֡�����ݣ������Ҫѭ������
		 *  ��������ȡ���ݺ󣨿��ܲ������ݣ���������ʣ�����ݣ�����Ҫɾ���Ѿ�����������ݣ�
		 *  Ȼ������buf����д��
		 */
		ByteBuffer buf = client.getBufRead();
		while(buf.hasRemaining()){ //��ѭ����ر����������ģ������о��㷨��
			IMessage msg = client.getCurReadingMsg();
			if( null == msg ){
				//client��ǰû����Ϣ������Ҫ��������Ϣ
				int rem1 = buf.remaining();
				msg = client.getServer().createMessage(buf);
				int rem2 = buf.remaining();
				if( null == msg ){
					if( rem1>13 && rem1 == rem2 ){
						//����13�ֽڣ�һ������ʶ����� ���ı��ġ���ʹ����ʶ��Ҳ��Ҫ�����Ƿ����ݡ�
						//�����ʾ���������õ�messageCreator���ܴ�������
						String info = "��Ϣ�����������ô���,UDP server port="+client.getServer().getPort();
						log.fatal(info);
						buf.clear();
						throw new SocketClientCloseException(info);
					}
					else{
						if( buf.hasRemaining() )
							buf.compact();
						else
							buf.clear();
						return true;
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
				//�ڲ���ģʽ�£�����Ϣֱ�ӷ��͸�client���Ա���Թ���֪������ԭ��
				if( FasSystem.getFasSystem().isTestMode() ){
					SocketAddress sa = client.getSocketAddress();
					if( null == sa )
						return false;
					byte[] expBytes = expInfo.getBytes();
					try{
						DatagramSocket ds = new DatagramSocket();
						DatagramPacket dp = new DatagramPacket(expBytes,expBytes.length,sa);
						ds.send(dp);
					}catch(Exception e){
						log.warn("����ģʽ��UDPӦ���쳣:"+e.getLocalizedMessage(),e);
					}
				}
				//��Ϣ��ȡ�쳣�������Ҫ���¶�ȡ�����ǵ�����׳�ԣ��´ζ�ȡ�µ���Ϣ��
				client.setCurReadingMsg(null);
				return false;
			}
			if( down ){		//��Ϣ�Ѿ�������ȡ��
				client.setCurReadingMsg(null);
				msg.setIoTime(System.currentTimeMillis());
				msg.setPeerAddr(client.getPeerAddr());
				msg.setTxfs(client.getServer().getTxfs());
				ReceiveMessageEvent ev = new ReceiveMessageEvent(msg,client);
				GlobalEventHandler.postEvent( ev );
			}
			else
				break;
		}
		//ע�⣬���������ܻ���ʣ������û�б�����ʣ�������Ƶ�ǰ�棬���Լ���put���ݡ�
		//����������������ݶ��������꣬��ôcompact�൱��clear������Ҫ��.
		if( buf.hasRemaining() )
			buf.compact();
		else
			buf.clear();
		return true;
	}

}
