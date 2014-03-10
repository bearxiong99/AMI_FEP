package cn.hexing.fas.protocol.codec;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;

/**
 * ��Ϣ����������ҵ����������ʺ��ڿ������������Ϣ����
 */
public interface MessageEncoder {

    /**
     * ����Э����������
     * @param dataConfig
     */
    public void setDataConfig(ProtocolDataConfig dataConfig);

    /**
     * ��ҵ����������ʺ��ڿ������������Ϣ����
     * @param obj ҵ�����
     * @return ��Ϣ��������
     */
    public IMessage[] encode(Object obj);
}
