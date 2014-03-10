package cn.hexing.fk.sockserver;

import java.math.BigInteger;
import java.nio.channels.SocketChannel;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.HexDump;
/**
 * 
 * @author gaoll
 *
 * @time 2013-5-2 ����02:43:05
 *
 * @info POS����Ϣ�ͻ���
 */
public class AsyncPosSocketClient extends AsyncSocketClient{
	
	
	
	
	public byte[] currentMsg = null;
	public AsyncPosSocketClient(SocketChannel c,ISocketServer s){
		super(c, s);
	}
	
	public IMessage read(MessageBytes message){
		if(message ==null || (message.data.length<3 && this.currentMsg==null)){
			return null;
		}
		
		if(this.currentMsg == null){
			this.currentMsg = message.data;
		}else{
			this.currentMsg=HexDump.cat(this.currentMsg, message.data);					
		}
		byte[] lengthArray = new byte[2];
		System.arraycopy(this.currentMsg, 1, lengthArray, 0, 2);
		BigInteger bi = new BigInteger(lengthArray);
		int length=bi.intValue();
		if(length > this.currentMsg.length){
			//�����ǰ���ȴ��ڵ�ǰ����Ϣ���ȣ�˵����û�н�����ϣ��ȴ���������
			return null;
		}
		if(length == this.currentMsg.length-3){
			message.data = this.currentMsg;
			this.currentMsg = null;
			return message;
		}else{
			//˵�����Ȳ���
			this.currentMsg = null;
			return null;
		}
	}

	
}
