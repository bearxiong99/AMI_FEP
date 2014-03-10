package cn.hexing.fk.bp.webapi;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;

import cn.hexing.exception.MessageDecodeException;

/**
 * WebͨѶ��Ϣ������
 */
public class WebMessageDecoder {

    /**
     * �� Web ͨѶ��Ϣ����Ϊԭʼ�� Java ����
     * @param message Web ͨѶ��Ϣ
     * @return ԭʼ�� Java ����
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
            // ���� ByteArrayInputStream, ���� close() û���κ�Ч��
        }
    }
}
