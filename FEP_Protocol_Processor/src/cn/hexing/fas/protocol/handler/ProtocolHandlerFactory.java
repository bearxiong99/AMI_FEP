package cn.hexing.fas.protocol.handler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hexing.fas.protocol.codec.MessageCodecFactory;
import cn.hexing.fas.protocol.conf.CodecFactoryConfig;
import cn.hexing.fas.protocol.conf.ProtocolHandlerConfig;
import cn.hexing.fas.protocol.conf.ProtocolProviderConfig;
import cn.hexing.fas.protocol.meter.MeterProtocolFactory;
import cn.hexing.util.CastorUtil;

/**
 * 协议处理器工厂。可以根据协议类别提供合适的协议处理器
 */
public class ProtocolHandlerFactory {

    /** 配置映射文件 */
    private static final String CONFIG_MAPPING = "/cn/hexing/fas/protocol/conf/protocol-provider-config-mapping.xml";
    /** 配置文件 */
    private static final String CONFIG_RESOURCE = "/cn/hexing/fas/protocol/conf/protocol-provider-config.xml";
    
    /** 单例 */
    private static ProtocolHandlerFactory instance;
    
    /** 协议处理器列表 */
    private Map handlers = new HashMap();
    
    /**
     * 构造一个协议处理器工厂
     *
     */
    private ProtocolHandlerFactory() {
        init();
    }
    
    /**
     * 得到一个协议处理器工厂的实例
     * @return 协议处理器工厂
     */
    public static ProtocolHandlerFactory getInstance() {
        if (instance == null) {
            synchronized (ProtocolHandlerFactory.class) {
                if (instance == null) {
                    instance = new ProtocolHandlerFactory();
                }
            }
        }
        return instance;
    }
    
    /**
     * 取得适合于处理特定类别消息的协议处理器
     * @param messageType 消息类型
     * @return 协议处理器
     */
    public ProtocolHandler getProtocolHandler(Class messageType) {
        return (ProtocolHandler) handlers.get(messageType.getName());
    }
    
    /**
     * 初始化协议处理器工厂
     */
    private void init() {
        ProtocolProviderConfig config = (ProtocolProviderConfig) CastorUtil.unmarshal(
                CONFIG_MAPPING, CONFIG_RESOURCE);
        List handlerConfigs = config.getHandlers();
        for (int i = 0; i < handlerConfigs.size(); i++) {
            ProtocolHandlerConfig handlerConfig = (ProtocolHandlerConfig) handlerConfigs.get(i);
            ProtocolHandler handler = (ProtocolHandler) newInstance(handlerConfig.getHandlerClass());            
            CodecFactoryConfig codecFactoryConfig = handlerConfig.getCodecFactory();
            if (codecFactoryConfig != null) {
                MessageCodecFactory codecFactory = (MessageCodecFactory) newInstance(
                        codecFactoryConfig.getFactoryClass());
                codecFactory.setConfig(codecFactoryConfig);
                handler.setCodecFactory(codecFactory);
            }
            
            handlers.put(handlerConfig.getMessageType(), handler);
        }
        
        //init meter protocol
        MeterProtocolFactory.createMeterProtocolDataSet("ZJMeter");
        MeterProtocolFactory.createMeterProtocolDataSet("BBMeter");
    }
    
    /**
     * 创建一个实例
     * @param className 类名
     * @return 类的实例
     */
    private Object newInstance(String className) {
        try {
            Class clazz = Class.forName(className);
            return clazz.newInstance();
        }
        catch (Exception ex) {
            throw new RuntimeException("Error to instantiating class: " + className, ex);
        }
    }
}
