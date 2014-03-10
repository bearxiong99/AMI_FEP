package cn.hexing.fk.clientmod;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;

public class FaalRequestMessage extends MessageGate {
	private static final Logger log = Logger.getLogger(FaalRequestMessage.class);
	private static Class<?> FaalRequestClass = null;
	
	public FaalRequestMessage(Object obj){
		if( null == FaalRequestClass ){
			try{
				FaalRequestClass = Class.forName("cn.hexing.fas.model.FaalRequest");
			}catch(Exception exp){
				log.error("Can not find class:cn.hexing.fas.model.FaalRequest");
				throw new RuntimeException(exp);
			}
		}
        if ( FaalRequestClass.isAssignableFrom(obj.getClass() )) {
            getHead().setCommand(CMD_GATE_REQUEST);
        }
        else {
            getHead().setCommand( 0xFF );
        }
		this.getHead().setAttribute(GateHead.ATT_MSGSEQ, obj.hashCode());
		try{
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ObjectOutputStream serializer = new ObjectOutputStream(out);
			serializer.writeObject(obj);
			this.data = ByteBuffer.wrap(out.toByteArray());
			this.head.setIntBodylen(this.data.remaining());
		}catch(Exception exp){
			throw new RuntimeException("Error encoding request to byte array. reason="+exp.getLocalizedMessage());
		}
	}
	
	public static final Object getReply(MessageGate gm){
		if( gm.getData().remaining()>10 ){
			try{
				ByteArrayInputStream in = new ByteArrayInputStream(gm.getData().array());
				ObjectInputStream deserializer = new ObjectInputStream(in);
				return deserializer.readObject();
			}catch(Exception exp){
				throw new RuntimeException("Error decoding gate message to FaalRequest. reason="+exp.getLocalizedMessage());
			}
		}
		return null;
	}
}
