package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.protocol.codec.MessageEncoder;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;

/**
 * 国网规约消息编码器抽象类
 */
public abstract class AbstractMessageEncoder implements MessageEncoder {

    /** 协议数据配置 */
    protected ProtocolDataConfig dataConfig;

    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageEncoder#setDataConfig(cn.hexing.fas.protocol.conf.ProtocolDataConfig)
     */
    public void setDataConfig(ProtocolDataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }
    
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageEncoder#encode(java.lang.Object)
     */
    public abstract IMessage[] encode(Object obj);
}
