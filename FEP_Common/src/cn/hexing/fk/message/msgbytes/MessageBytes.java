/**
 * �ֽ�������Ϣ��һ����Ҫsocket���ӵ�session���ƹ�����ҵ�����ݡ�
 * �ֽ�����Ϣ�����ڿͻ����������֮�䴫��δԤ�ȶ��屨�ĸ�ʽ�����ݡ�
 */
package cn.hexing.fk.message.msgbytes;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.MessageBase;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.utils.HexDump;

/**
 *
 */
public class MessageBytes extends MessageBase{
	private static final Logger log = Logger.getLogger(MessageBytes.class);
	public static final byte[] EMPTY_DATA = new byte[0];
	
	public byte[] data = EMPTY_DATA;
	public int offset = 0;			//�´ο�ʼд��λ�ã��Ա�֧�����ݳ��ȳ�������������������͡�
	
	public byte[] getRawPacket() {
		return data;
	}

	public String getRawPacketString() {
		return HexDump.hexDumpCompact(data, 0, data.length);
	}

	public int length() {
		return data.length;
	}

	public boolean read(ByteBuffer readBuffer) throws MessageParseException {
		int len = readBuffer.remaining();
		if( len >0 ){
			data = new byte[len];
			readBuffer.get(data);
		}
		return true;
	}
	
	public void setData( byte[] msgData ){
		offset = 0;
		if( null != msgData )
			data = msgData;
	}

	public boolean write(ByteBuffer writeBuffer) {
		if( data.length<= offset )	//����Ϣ�����Ѿ����������Ϣ����ʾ��Ϣ������ϡ�
		{
			if( offset <=0 )	//Empty message
				return true;
			else{
				offset = 0;
				log.warn("MessageBytes resend.");
			}
		}
		while( offset < data.length ){
			if( writeBuffer.hasRemaining() )
				writeBuffer.put(data[offset++]);
			else
				return false;
		}
		return true;
	}

	@Override
	public String toString(){
		return this.getRawPacketString();
	}
	@Override
	public MessageType getMessageType() {
		return MessageType.MSG_BYTES;
	}

}
