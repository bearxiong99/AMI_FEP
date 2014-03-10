package cn.hexing.fas.protocol.zj.handler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;
import cn.hexing.exception.ProtocolHandleException;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.protocol.codec.MessageCodecFactory;
import cn.hexing.fas.protocol.codec.MessageDecoder;
import cn.hexing.fas.protocol.codec.MessageEncoder;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;


/**
 * 浙江规约处理器
 */
public class PrototalHandlerImpl implements ProtocolHandler {   
    private static final Log log = LogFactory.getLog(PrototalHandlerImpl.class);    
    /** 消息编码/解码器工厂 */
	private MessageCodecFactory codecFactory;
	
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.handler.ProtocolHandler#setCodecFactory(cn.hexing.fas.protocol.codec.MessageCodecFactory)
     */
    public void setCodecFactory(MessageCodecFactory codecFactory) {
        this.codecFactory = codecFactory;
    }

    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.handler.ProtocolHandler#getCodecFactory()
     */
    public MessageCodecFactory getCodecFactory() {        
        return codecFactory;
    }

    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.handler.ProtocolHandler#process(cn.hexing.fas.framework.IMessage)
     */
    public Object process(IMessage message) {
    	Object value;
        if (!(message instanceof MessageZj)) {
            throw new ProtocolHandleException("Unsupported message type: " + message.getClass());
        }
	    // 一定是浙江规约帧，否则不会进入本规约解析器
    	MessageZj msg = (MessageZj) message;
        int funCode = msg.head.c_func & 0xff;
        // 其它消息，需要解码后处理
    	MessageDecoder decoder = codecFactory.getDecoder(funCode);
        if (decoder == null) {
            throw new ProtocolHandleException("Can't find decoder for function code: " + funCode);
        }      
        try {                       
        	value = decoder.decode(msg);       	
            if (log.isDebugEnabled()) {
                log.debug("Message decoded");
            }           
        }
        catch (MessageDecodeException ex) {
            //报文解析失败
            throw ex;
        }
        catch (Exception ex) {            
        	throw new ProtocolHandleException("Error to process message", ex);
        }
        return value;
    }

	/* (non-Javadoc)
	 * @see cn.hexing.fas.protocol.handler.ProtocolHandler#createMessage(cn.hexing.fas.model.FaalRequest)
	 */
	public IMessage[] createMessage(FaalRequest request) {
        MessageEncoder encoder = codecFactory.getEncoder(request.getType());
        if (encoder == null) {
            throw new ProtocolHandleException("Can't find encoder for function code: " + request.getType());
        }
        
        try {
            return encoder.encode(request);
        }
        catch (MessageEncodeException ex) {
            throw ex;
        }
        catch (Exception ex) {
            throw new ProtocolHandleException("Error to encoding message", ex);
        }
	}
    
 
   
    
   
}
