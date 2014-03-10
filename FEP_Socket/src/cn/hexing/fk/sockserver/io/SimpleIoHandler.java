/**
 * SimpleIoHandler��ʵ�ּ򵥵������ա��������ݲ���ʵ�ʴ���
 * �����첽TCP Socket����������IO����
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
 *	ByteBuffer����ע�������д��put����flip(),Ȼ��ſ��Զ�(get)��
 */
public class SimpleIoHandler implements IClientIO {
	private static final Logger log = Logger.getLogger(SimpleIoHandler.class);
	private static final TraceLog trace = TraceLog.getTracer(SimpleIoHandler.class);

	/**
	 * called when socket channel receive data.
	 * �ر�ע�⣬bufRead��Զ����fill data״̬���� limit==capacity;
	 */
	public boolean onReceive(IServerSideChannel client) throws SocketClientCloseException{
		int msgCount = 0;
		ByteBuffer readBuf = client.getBufRead();
		int bytesRead = 0,n=0;
		if( readBuf.remaining() == 0 ){
			log.info("SimpleIoHandler.onReceive error. readBuf empty:pos="+readBuf.position()+",limit="+readBuf.limit()+",capacity="+readBuf.capacity());
			readBuf.clear();
		}
		while(true){	//��ѭ����ر����������ģ������о��㷨��
			try{
				n = client.getChannel().read(readBuf);
			}catch(IOException e){
				log.warn("client.getChannel().read(readBuf)�쳣:"+e.getLocalizedMessage());
				throw new SocketClientCloseException(e);
			}
			if( n<0 ){ //�Է������ر�
				String info = "client close socket:"+client.toString();
				log.info(info);
				throw new SocketClientCloseException(info);
			}
			bytesRead += n;
			if( n==0 ){
				if( !readBuf.hasRemaining() ){
					//readBuf���ˡ���Ҫ��Ϣ�������������ݣ�ʹ�û��������г���������������
					readBuf.flip();
//					log.warn("dump readbuf:"+HexDump.hexDump(readBuf));
					msgCount = processBuffer(readBuf,client,msgCount);
					if( msgCount<0 ){
						//���ȶ�ȡ����ʱ������ȡ���ݡ�
						if( log.isDebugEnabled() )
							log.debug("���ȶ�ȡ����ʱ������ȡ����");
						return false;
					}
				}
				else{
					//readBuf�������ݣ���socket������û�������ˡ�
					break;
				}
			}
		}
		if( bytesRead ==0 ){
			//��ȡ0�ֽ����ݣ�socket�ر�
			return true;
		}
		readBuf.flip();		//ע�⣬readBuf�ո�'put'��ȥһЩ���ݣ�flipȻ����ܶ�ȡ����
//		log.warn("dump readbuf:"+HexDump.hexDump(readBuf));
		msgCount = processBuffer(readBuf,client,msgCount);
		return  msgCount>= 0 ;
	}
	
	/**
	 * �������������ݣ�������Ӧ�ı��Ķ���
	 * @param buf
	 * @param client
	 */
	private int processBuffer(ByteBuffer buf,IServerSideChannel client, int count) throws SocketClientCloseException{
		/** �Ӽ����Ͻ���buf���ݿ��ܰ������֡�����ݣ������Ҫѭ������
		 *  ��������ȡ���ݺ󣨿��ܲ������ݣ���������ʣ�����ݣ�����Ҫɾ���Ѿ�����������ݣ�
		 *  Ȼ������buf����д��
		 */
		while(buf.hasRemaining()){ //��ѭ����ر����������ģ������о��㷨��
			IMessage msg = client.getCurReadingMsg();
			if( null == msg ){
				//client��ǰû����Ϣ������Ҫ��������Ϣ
				int rem1 = buf.remaining();
				msg = client.getServer().createMessage(buf);
				int rem2 = buf.remaining();
				if( null == msg ){
					if( rem1>=9 && rem1 == rem2 ){
						//����13�ֽڣ�һ������ʶ����� ���ı��ġ���ʹ����ʶ��Ҳ��Ҫ�����Ƿ����ݡ�
						//�����ʾ���������õ�messageCreator���ܴ�������
						String info = "��Ϣ�����������ô���,server port="+client.getServer().getPort();
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
				log.warn("Read Message Exception:"+expInfo,mpe);//log.warn("����Ϣ�쳣��"+expInfo,mpe);
				
				//�ڲ���ģʽ�£�����Ϣֱ�ӷ��͸�client���Ա���Թ���֪������ԭ��
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
				//��Ϣ��ȡ�쳣�������Ҫ���¶�ȡ�����ǵ�����׳�ԣ��´ζ�ȡ�µ���Ϣ��
				client.setCurReadingMsg(null);
				if( buf.hasRemaining() )
					buf.compact();
				else
					buf.clear();
				throw new SocketClientCloseException(mpe.getLocalizedMessage());
				//return 0;
			}
			if( down ){		//��Ϣ�Ѿ�������ȡ��
				count++;
				client.setCurReadingMsg(null);
				ReceiveMessageEvent ev = new ReceiveMessageEvent(msg,client);
				msg.setIoTime(System.currentTimeMillis());
				msg.setPeerAddr(client.getPeerAddr());
				msg.setTxfs(client.getServer().getTxfs());
				GlobalEventHandler.postEvent( ev );
				//��ֹ���ɶ�ȡ
				int maxCanRead = client.getServer().getMaxContinueRead();
				int sendReqCount = client.sendQueueSize();
				if( maxCanRead > 0 && sendReqCount>0 && count>= maxCanRead ){
					//��ʱ������ȡ���ݡ�
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
		//ע�⣬���������ܻ���ʣ������û�б�����ʣ�������Ƶ�ǰ�棬���Լ���put���ݡ�
		//����������������ݶ��������꣬��ôcompact�൱��clear������Ҫ��.
		if( buf.hasRemaining() )
			buf.compact();
		else
			buf.clear();
		return count;
	}

	/**
	 * called when socket buffer can put more data to send.
	 * ���client�����������ݶ���������ϣ��򷵻�true, false otherwise.
	 */
	public boolean onSend(IServerSideChannel client) throws SocketClientCloseException{
		//1.�ȷ��ͻ�����ʣ������ݡ�
		ByteBuffer writeBuf = client.getBufWrite();
		IMessage msg = client.getCurWritingMsg();
		boolean sent = false;
		//Modified by bhw 2009-1-17 13:44 in that message is write done but buffer is not sent.
		if( client.bufferHasRemaining() ){
			sent = flush(client.getChannel(),writeBuf);
			if( ! sent ){									//����������û�з������
				log.debug("flush(client.getChannel(),writeBuf),buff area unfinish:msg="+msg.getRawPacketString());
				return false;
			}
		}
		//end modified
		
		if( null != msg ){
			//��ǰ����Ϣ���ڷ��ͣ���������Ϣ��ȡ���ݷ���
			sent = sendMessage(msg,client);
			if( !sent ){	//��Ϣû�з����꣬ʣ�������ڻ�����
				log.debug("sendMessage(msg,client),unfinish,remaining data in buff area:msg="+msg.getRawPacketString());
				return false;
			}
		}
		//�������Ͷ��У�����������Ϣ����
		while( null != (msg=client.getNewSendMessage()) ){
			client.setCurWritingMsg(msg);
			sent = sendMessage(msg,client);
			if( !sent ){	//��Ϣû�з����꣬ʣ�������ڻ�����
				log.debug("sendMessage(msg,client),unfinish,remaining data in buff area:msg="+msg.getRawPacketString());
				return false;
			}
		}
		return true;
	}
	
	/**
	 * ���͵�����Ϣ��
	 * @param msg
	 * @return true ���������Ϣ������ϣ�false otherwise��
	 * @throws SocketClientCloseException
	 */
	private boolean sendMessage(IMessage msg,IServerSideChannel client)throws SocketClientCloseException{
		ByteBuffer writeBuf = client.getBufWrite();
		boolean done = false,sent = false;
		//������ѭ����⹦��
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
				//֪ͨ������Ϣ�Ѿ��������ͳ�ȥ��
				GlobalEventHandler.postEvent(new SendMessageEvent(msg,client));
				if (client.getServer().getClientSize()<=100){//��������ͻ��˴���ʱ��ӡ���쳣
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
				//����������û�з������
				client.setBufferHasRemaining(true);
				return false;
			}
			client.setBufferHasRemaining(false);
			if( ++deadloop > 1000 ){
				log.fatal("Message.write������ѭ������"+msg.getClass().getName());
				return true;			//return true����ʧ����Ϣ��������ϵͳ������
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
				String s = "channel.write()�쳣��ԭ��"+exp.getLocalizedMessage();
				log.warn(s,exp);
				throw new SocketClientCloseException(exp);
			}
			if( 0 == bytesWritten )
				return false;		//socket buffer full�����ǻ�������û�з�����
		}
		//������buf����ȫ��д��socket buffer
		buf.clear();		//����������Ա��´�д
		return true;
	}
}
