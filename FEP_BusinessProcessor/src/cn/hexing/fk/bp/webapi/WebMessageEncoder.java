package cn.hexing.fk.bp.webapi;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalRequest;

/**
 * Web通讯消息编码器
 */
public class WebMessageEncoder {

    /**
     * 将一个 Java 对象编码成 WebMessage 对象
     * @param obj Java 对象
     * @return WebMessage 对象
     */
    public MessageWeb encode(Object obj) {
        assert obj != null;
        MessageWeb msg = new MessageWeb();
        if (obj instanceof FaalRequest) {
            FaalRequest request = (FaalRequest) obj;
            msg.getHead().setCommand(request.getType());
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
            throw new MessageEncodeException("Error to encode object to web message: " + obj, ex);
        }
        finally {
            // 对于 ByteArrayOutputStream, 调用 close() 没有任何效果
        }
        
        return msg;
    }
}
