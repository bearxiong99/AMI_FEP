package cn.hexing.fas.protocol.zj.codec;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.protocol.codec.MessageDecoder;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;

/**
 * 浙江规约消息解码器抽象类
 */
public abstract class AbstractMessageDecoder implements MessageDecoder {

    /** 协议数据配置 */
    protected ProtocolDataConfig dataConfig;
    
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageDecoder#setDataConfig(cn.hexing.fas.protocol.conf.ProtocolDataConfig)
     */
    public void setDataConfig(ProtocolDataConfig dataConfig) {
        this.dataConfig = dataConfig;
    }
    
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageDecoder#decode(cn.hexing.fas.framework.IMessage)
     */
    public abstract Object decode(IMessage message);
}
