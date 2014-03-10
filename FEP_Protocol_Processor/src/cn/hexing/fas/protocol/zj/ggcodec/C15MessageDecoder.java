package cn.hexing.fas.protocol.zj.ggcodec;

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

/**
 * 读取表计档案(功能码：15H)消息解码器
 * @author Administrator
 * 2012年11月26日19:53:32
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
        			log.info("C15MessageDecoder data="+data);
        			if(data.length()%68!=0){
        				log.info("帧长度不符合规范");
        			}
        			String sdata="";
        			int count=data.length()/68;    //上行message里面含有的表计档案的标记数量
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
    		String serial=null;//序号（HEX）
    		String MeterNo=null;
    		String MeterType=null;
    		String Relay_1=null;
    		String Relay_2=null;
    		String Relay_3=null;
    		String Relay_4=null;
    		String CT=null;
    		String TN_quality=null;
    		String AutoCondition=null;   		
    }
    private  String parseMessage(String data){
    	MeterInf inf=new MeterInf();
    	inf.Optype=data.substring(0, 2);
    	inf.serial=DataSwitch.ReverseStringByByte(data.substring(2, 6));
    	inf.MeterNo=DataSwitch.ReverseStringByByte(data.substring(6, 18));
    	inf.MeterType=data.substring(18, 20);
    	inf.Relay_1=data.substring(20, 24);
    	inf.Relay_2=data.substring(24, 28);
    	inf.Relay_3=data.substring(28, 32);
    	inf.Relay_4=data.substring(32, 36);
    	inf.CT=data.substring(36, 40);
    	inf.TN_quality=data.substring(40, 42);
    	inf.AutoCondition=data.substring(42, 68);
    	String sMeterInfo=inf.Optype+"#"+Integer.parseInt(inf.serial)+"#"+inf.MeterNo+"#"+inf.MeterType
    					+"#"+inf.Relay_1+"#"+inf.Relay_2+"#"+inf.Relay_3+"#"+inf.Relay_4+"#"+inf.CT+"#"
    					+inf.TN_quality+"#"+inf.AutoCondition;
    	return sMeterInfo;
    }
    
    public static void main(String[] args) {
    	C15MessageDecoder d=new C15MessageDecoder();
    	String	sdata="00010004000100000000000000000000000001100900000000000000000000000000";
    	String svalue=d.parseMessage(sdata);
    	System.out.println(svalue);
	}
    
}
