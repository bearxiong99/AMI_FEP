package cn.hexing.fas.protocol.handler;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.protocol.codec.MessageCodecFactory;

/**
 * Э�鴦�������������ض�Э�����Ϣ
 */
public interface ProtocolHandler {
    
    /**
     * ���ñ���/����������
     * @param codecFactory ����/����������
     */
    public void setCodecFactory(MessageCodecFactory codecFactory);
    
    /**
     * ȡ�ñ���/����������
     * @return ����/����������
     */
    public MessageCodecFactory getCodecFactory();
    
    /**
     * ������Ϣ
     * @param message ��Ϣ
     * @return Ӧ����Ϣ���������ҪӦ���򷵻� null
     */
    public Object process(IMessage message);
    
    /**
     * ��װ��Ϣ
     * @param request ͨѶ����
     * @return �����ض���Լ����Ϣ����
     */
    public IMessage[] createMessage(FaalRequest request);
}
