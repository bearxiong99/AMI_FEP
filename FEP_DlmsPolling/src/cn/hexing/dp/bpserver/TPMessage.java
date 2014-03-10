package cn.hexing.dp.bpserver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;

/**
 * dlms任务轮招消息对象。适用所有Web类型下行。
 * @author Administrator
 *
 */
public class TPMessage extends MessageGate {
	public TPMessage(){
		
	}
	
	public TPMessage(FaalRequest request){
		this.getHead().setCommand(request.getType());
		this.getHead().setAttribute(GateHead.ATT_MSGSEQ, request.hashCode());
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream serializer = new ObjectOutputStream(out);
			serializer.writeObject(request);
			this.data = ByteBuffer.wrap(out.toByteArray());
			this.head.setIntBodylen(this.data.remaining());
		}catch(Exception exp){
			throw new MessageEncodeException("Error encoding request to byte array. reason="+exp.getLocalizedMessage());
		}
	}
	
	public static final Object getReply(MessageGate gm){
		if( gm.getData().remaining()>10 ){
			try{
				ByteArrayInputStream in = new ByteArrayInputStream(gm.getData().array());
				ObjectInputStream deserializer = new ObjectInputStream(in);
				return deserializer.readObject();
			}catch(Exception exp){
				throw new MessageDecodeException("Error decoding gate message to FaalRequest. reason="+exp.getLocalizedMessage());
			}
		}
		return null;
	}
	
}
