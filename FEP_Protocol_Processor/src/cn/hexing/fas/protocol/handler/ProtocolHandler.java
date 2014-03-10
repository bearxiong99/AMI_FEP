package cn.hexing.fas.protocol.handler;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.protocol.codec.MessageCodecFactory;

/**
 * 协议处理器。负责处理特定协议的消息
 */
public interface ProtocolHandler {
    
    /**
     * 设置编码/解码器工厂
     * @param codecFactory 编码/解码器工厂
     */
    public void setCodecFactory(MessageCodecFactory codecFactory);
    
    /**
     * 取得编码/解码器工厂
     * @return 编码/解码器工厂
     */
    public MessageCodecFactory getCodecFactory();
    
    /**
     * 处理消息
     * @param message 消息
     * @return 应答消息。如果不需要应答，则返回 null
     */
    public Object process(IMessage message);
    
    /**
     * 组装消息
     * @param request 通讯请求
     * @return 符合特定规约的消息数组
     */
    public IMessage[] createMessage(FaalRequest request);
}
