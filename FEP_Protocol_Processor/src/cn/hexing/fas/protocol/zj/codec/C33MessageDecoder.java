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
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;

public class C33MessageDecoder extends AbstractMessageDecoder{	
	private static Log log=LogFactory.getLog(C33MessageDecoder.class);

    public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
        		//Ӧ������
        		int rtype=(ParseTool.getErrCode(message));  
        		
            	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//�����ն�Ӧ��
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//ȡӦ������
        			String data=ParseTool.getDataString(message);
        			log.info("C33MessageDecoder data="+data);
        			BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
        				//��ȡ�����
        				String meterNo= DataSwitch.ReverseStringByByte(data.substring(0, 12));
        				//ͨ��meterNo���Ҳ������
        				MeasuredPoint mp=rtu.getMeasuredPointByTnAddr(meterNo);
        				data=data.substring(12);
        				//ȡ ��������� code
        				String code=data.substring(2, 4)+data.substring(0, 2);
        				data=data.substring(4);	
        				HostCommandResult hcr = new HostCommandResult();
        				String svalue=data.substring(0, 2);
        				hcr.setValue(""+Integer.parseInt(svalue,16));
        				hcr.setCode(code);
        				hcr.setTn(mp.getTn());
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
