package cn.hexing.fk.bp.webapi;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import cn.hexing.exception.MessageDecodeException;

/**
 * Web通讯消息解码器
 */
public class WebMessageDecoder {

    /**
     * 将 Web 通讯消息解码为原始的 Java 对象
     * @param message Web 通讯消息
     * @return 原始的 Java 对象
     */
    public Object decode(MessageWeb message) {
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
}
