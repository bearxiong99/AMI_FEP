package cn.hexing.fas.protocol.zj.viewer.gg;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;

public class C33MessageDecoder extends AbstractMessageDecoder{	
	private static Log log=LogFactory.getLog(C33MessageDecoder.class);

    public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
        		//应答类型
        		int rtype=(ParseTool.getErrCode(message));  
        		
            	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//取应答数据
        			String data=ParseTool.getDataString(message);
        			log.info("C33MessageDecoder data="+data);
        				//获取电表编号
        				String meterNo= DataSwitch.ReverseStringByByte(data.substring(0, 12));
        				//通过meterNo查找测量点号
        				data=data.substring(12);
        				//取 数据项编码 code
        				String code=data.substring(2, 4)+data.substring(0, 2);
        				data=data.substring(4);	
        				HostCommandResult hcr = new HostCommandResult();
        				String svalue=data.substring(0, 2);
        				hcr.setValue(""+Integer.parseInt(svalue,16));
        				hcr.setCode(code);
        				hcr.setMeterAddr(meterNo);
        				value.add(hcr);
        			hc.setResults(value);      		
        		}
        			
        	}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }     
        return hc;
    }      
}
