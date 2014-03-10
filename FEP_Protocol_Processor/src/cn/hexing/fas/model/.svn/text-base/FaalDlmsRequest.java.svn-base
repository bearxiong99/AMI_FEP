package cn.hexing.fas.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.model.Operator;
import cn.hexing.util.HexDump;

/**
 * 国网AFN=0A命令请求类
 */
public class FaalDlmsRequest extends FaalRequest{
	private static final long serialVersionUID = -3196538871708911008L;
	//private static final Logger log = Logger.getLogger(FaalDlmsRequest.class);
	private String data; 		//页面传入数据区
	private String zdljdz;		//前置机上行回传主站
	private long id;			//前置机上行回传主站
	
    public FaalDlmsRequest() {
    }
	
	public String getData() {
		return data;
	}
	public ByteBuffer getDataBuffer() {
		return HexDump.toByteBuffer(data);
	}
	public void setData(String data) {
		this.data = data;
	}
	public void setData(ByteBuffer buffer) {
		this.data = HexDump.hexDumpCompact(buffer);
	}
	/**
     * 将FaalRequest编码成 MessageGate 对象并转化成ByteBuffer返回
     * @param FaalRequest 对象
     * @return ByteBuffer 对象
     */
    public static ByteBuffer encode(Object obj) {
        assert obj != null;
        MessageGate msg = new MessageGate();
        if (obj instanceof FaalRequest) {
            FaalRequest request = (FaalRequest) obj;
            msg.getHead().setCommand(request.getType());
            msg.getHead().setAttribute(GateHead.ATT_MSGSRC, Operator.ZZ_DLMS);
            request.setProtocol(Protocol.DLMS);
        }
        else {
            msg.getHead().setCommand(FaalRequest.TYPE_OTHER);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream serializer = new ObjectOutputStream(out);
            serializer.writeObject(obj);
            byte[] buf = out.toByteArray();
            msg.getHead().setIntBodylen(buf.length);
            msg.setData( ByteBuffer.wrap(buf) );
        }
        catch (Exception ex) {
            throw new MessageEncodeException("Error to encode to message: " + obj, ex);
        }
        finally {
            // 对于 ByteArrayOutputStream, 调用 close() 没有任何效果
        }
        ByteBuffer buffer = ByteBuffer.allocate(10240);
        try{
        	msg.write(buffer);
            buffer.flip();
        }
        catch (Exception ex) {
            throw new MessageEncodeException("Error to msg to buffer: " + msg, ex);
        }       
        return buffer;
    }
    /**
     * 将FaalRequest编码成 MessageGate 对象并转化成ByteBuffer返回
     * @param FaalRequest 对象
     * @return ByteBuffer 对象
     */
    public MessageGate encodeMsg(Object obj) {
        assert obj != null;
        MessageGate msg = new MessageGate();
        if (obj instanceof FaalRequest) {
            FaalRequest request = (FaalRequest) obj;
            msg.getHead().setCommand(request.getType());
            msg.getHead().setAttribute(GateHead.ATT_MSGSRC, Operator.ZZ_DLMS);
        }
        else {
            msg.getHead().setCommand(FaalRequest.TYPE_OTHER);
        }
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            ObjectOutputStream serializer = new ObjectOutputStream(out);
            serializer.writeObject(obj);
            byte[] buf = out.toByteArray();
            msg.getHead().setIntBodylen(buf.length);
            msg.setData( ByteBuffer.wrap(buf) );
        }
        catch (Exception ex) {
            throw new MessageEncodeException("Error to encode to message: " + obj, ex);
        }
        finally {
            // 对于 ByteArrayOutputStream, 调用 close() 没有任何效果
        }
        return msg;
    }
    /**
     * 将 Web 通讯消息解码为原始的 Java 对象
     * @param message Web 通讯消息
     * @return 原始的 Java 对象
     */
    public static Object decode(ByteBuffer buffer) {
    	MessageGate message=new MessageGate();
    	try{
    		message.read(buffer);
		}catch(Exception e){
			throw new MessageEncodeException("msg read error:"+e.getLocalizedMessage(),e);
		}
        assert message != null;
        int length = message.getHead().getIntBodylen();
        byte[] buf = new byte[length];
        message.getData().get(buf);
        ByteArrayInputStream in = new ByteArrayInputStream(buf);
        try {
            ObjectInputStream deserializer = new ObjectInputStream(in);
            return deserializer.readObject();
        }
        catch (Exception ex) {
            throw new MessageDecodeException("Error to decode web message", ex);
        }
        finally {
            // 对于 ByteArrayInputStream, 调用 close() 没有任何效果
        }
    }

	public String getZdljdz() {
		return zdljdz;
	}

	public void setZdljdz(String zdljdz) {
		this.zdljdz = zdljdz;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
    
}