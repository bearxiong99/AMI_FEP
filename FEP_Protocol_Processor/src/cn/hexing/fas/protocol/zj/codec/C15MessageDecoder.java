package cn.hexing.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;

/**
 * 读取表计档案(功能码：15H)消息解码器
 * @author Administrator
 * 2012年10月29日
 *
 */
public class C15MessageDecoder extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C15MessageDecoder.class);

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
        			if(data.length()%42!=0){
        				log.info("帧长度不符合规范");
        			}
        			String sdata="";
        			int count=data.length()/42;    //上行message里面含有的表计档案的标记数量
        			for(int i=0;i<count;i++){
        				HostCommandResult hcr = new HostCommandResult();
        				sdata=data.substring(0,42);
        				String svalue=parseMessage(sdata);	
        				data=data.substring(42);
        				hcr.setValue(svalue);
        				hcr.setCode("5004");
        				hcr.setTn("0");
        				value.add(hcr);
        			}
        			hc.setResults(value);      		
        		}
        	}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }     
        return hc;
    }      
    class MeterInf{
    		String Optype=null;
    		String CollectorNo=null;
    		String PLC=null;
    		String MeterCom=null;
    		String MeterType=null;
    		String UserFocus=null;
    		String MeterNo=null;
    		String CT=null;
    		String PT=null;
    		String MAC=null;   		
    }
    private String parseMessage(String data){
    	MeterInf inf=new MeterInf();
    	inf.Optype=data.substring(0, 2);
    	inf.CollectorNo=data.substring(2, 10);
    	inf.PLC=data.substring(10, 12);
    	inf.MeterCom=data.substring(12, 14);
    	inf.MeterType=data.substring(14, 16);
    	inf.UserFocus=data.substring(16, 18);
    	inf.MeterNo=DataSwitch.ReverseStringByByte(data.substring(18, 30));
    	inf.CT=data.substring(30, 34);
    	inf.PT=data.substring(34, 38);
    	inf.MAC=data.substring(38, 42);
    	String sMeterInfo=inf.Optype+"#"+inf.CollectorNo+"#"+inf.PLC+"#"+inf.MeterCom+"#"+inf.MeterType+"#"+inf.UserFocus+"#"+inf.MeterNo+"#"+inf.CT+"#"+inf.PT+"#"+inf.MAC;
    	return sMeterInfo;
    }
}
