package cn.hexing.fas.protocol.codec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.conf.CodecConfig;
import cn.hexing.fas.protocol.conf.CodecFactoryConfig;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;
import cn.hexing.util.CastorUtil;

/**
 * 消息编码/解码器工厂的缺省实现
 */
public class DefaultMessageCodecFactory implements MessageCodecFactory {
	private static final Log log=LogFactory.getLog(DefaultMessageCodecFactory.class);
    /** 协议数据项配置 */
    private ProtocolDataConfig dataConfig;
    /** 编码器列表 */
    private Map encoders;
    /** 解码器列表 */
    private Map decoders;
    
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageCodecFactory#setConfig(cn.hexing.fas.protocol.conf.CodecFactoryConfig)
     */
    public void setConfig(CodecFactoryConfig config) {
        init(config);
    }

    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageCodecFactory#getEncoder(int)
     */
    public MessageEncoder getEncoder(int funCode) {
        return (MessageEncoder) encoders.get(new Integer(funCode));
    }

    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageCodecFactory#getDecoder(int)
     */
    public MessageDecoder getDecoder(int funCode) {
        return (MessageDecoder) decoders.get(new Integer(funCode));
    }

    /**
     * 初始化
     * @param config 
     */
    private void init(CodecFactoryConfig config) {
        try{
	    	dataConfig = (ProtocolDataConfig) CastorUtil.unmarshal(
	                config.getDataConfigMapping(), config.getDataConfigResource());
	        dataConfig.fillMap();
	        encoders = new HashMap();
	        decoders = new HashMap();
	        List codecConfigs = config.getCodecs();
	        if(codecConfigs!=null){
		        for (int i = 0; i < codecConfigs.size(); i++) {
		            CodecConfig codecConfig = (CodecConfig) codecConfigs.get(i);
		            Integer funCode = new Integer(codecConfig.getFunCode());
		            
		            if (codecConfig.getEncoderClass() != null) {
		                MessageEncoder encoder = (MessageEncoder) newInstance(
		                        codecConfig.getEncoderClass());
		                encoder.setDataConfig(dataConfig);
		                encoders.put(funCode, encoder);
		            }
		            
		            if (codecConfig.getDecoderClass() != null) {
		                MessageDecoder decoder = (MessageDecoder) newInstance(
		                        codecConfig.getDecoderClass());
		                decoder.setDataConfig(dataConfig);
		                decoders.put(funCode, decoder);
		            }
		        }
	        }
        }catch(Exception e){
        	log.error("load protocol setting",e);
        }
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
