package cn.hexing.fas.protocol.codec;


import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;

/**
 * 消息编码/解码工具。提供几个常规的编码/解码方法
 */
public abstract class MessageCodecUtil {

    /** 协议处理器工厂 */
    private static final ProtocolHandlerFactory handlerFactory = ProtocolHandlerFactory.getInstance();    
 
    /**
     * 取得相应的编码器
     * @param messageType 消息类型
     * @param funCode 功能码
     * @return 匹配的编码器。若没有匹配的编码器，则返回 null
     */
    private static MessageEncoder getEncoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getEncoder(funCode);
    }
    
    /**
     * 取得相应的解码器
     * @param messageType 消息类型
     * @param funCode 功能码
     * @return 匹配的解码器。若没有匹配的解码器，则返回 null
     */
    private static MessageDecoder getDecoder(Class messageType, int funCode) {
        ProtocolHandler handler = handlerFactory.getProtocolHandler(messageType);
        return handler.getCodecFactory().getDecoder(funCode);
    }
}
