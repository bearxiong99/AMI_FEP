package cn.hexing.fas.protocol.codec;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;

/**
 * ��Ϣ���������ѿ�����ת����������Ϣ�����ҵ�����
 */
public interface MessageDecoder {

    /**
     * ����Э����������
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);
    
    /**
     * ����Ϣ����ɸ��߼���ҵ�����
     * @param message ��Ϣ
     * @return ҵ�����
     */
    public Object decode(IMessage message);
}
