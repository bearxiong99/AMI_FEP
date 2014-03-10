package cn.hexing.fas.protocol.conf;

import java.util.List;

/**
 * 协议提供者配置
 */
public class ProtocolProviderConfig {

    /** 协议处理器列表 */
    private List handlers;
    
    /**
     * 取得协议处理器配置
     * @param messageType 消息类型
     * @return 协议处理器配置
     */
    public ProtocolHandlerConfig getProtocolHandlerConfig(String messageType) {
        if (handlers == null) {
            return null;
        }
        
        for (int i = 0; i < handlers.size(); i++) {
            ProtocolHandlerConfig handler = (ProtocolHandlerConfig) handlers.get(i);
            if (handler.getMessageType().equals(messageType)) {
                return handler;
            }
        }
        
        return null;
    }

    /**
     * @return Returns the handlers.
     */
    public List getHandlers() {
        return handlers;
    }

    /**
     * @param handlers The handlers to set.
     */
    public void setHandlers(List handlers) {
        this.handlers = handlers;
    } 
}
