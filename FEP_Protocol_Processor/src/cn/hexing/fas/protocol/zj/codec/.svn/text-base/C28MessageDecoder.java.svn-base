package cn.hexing.fas.protocol.zj.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.zj.ErrorCode;
import cn.hexing.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	C28MessageDecoder.java
 * TODO			请求发送短信返回帧解析
 */
public class C28MessageDecoder extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
        HostCommand hc = null;
		try{
    		//RTUReply reply=new RTUReply();
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
        		//应答类型
        		int rtype=(ParseTool.getErrCode(message));
                
        		hc = new HostCommand();
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常应答
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        		}else{
        			//异常应答帧
        			byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
        		}
        		//message
    		}else{
    			//下发帧，调用短信接口发之
    			
    		}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}		
		return hc;
	}

}
