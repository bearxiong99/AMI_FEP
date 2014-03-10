package cn.hexing.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.meter.BbMeterFrame;
import cn.hexing.fas.protocol.meter.IMeterParser;
import cn.hexing.fas.protocol.meter.MeterParserFactory;
import cn.hexing.fas.protocol.meter.ZjMeterFrame;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;

/**
 * ���м�(�����룺00H)��Ӧ��Ϣ������
 *
 */
public class C00MessageDecoder  extends AbstractMessageDecoder{
	
	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();		
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��                                      
				int rtype=(ParseTool.getErrCode(message));
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//�ն�����Ӧ��
					byte[] data=ParseTool.getData(message);	//ȡӦ������
					if((data!=null) && (data.length>1)){											
			        	
			        	//��ȡ���Լ,��Ϊ���б��Ĳ����������,���Խ�����������������
			        	String pm=getMeterProtocol(data,1,data.length-1);			        	
			        	IMeterParser mparser=MeterParserFactory.getMeterParser(pm);
			        	if(mparser==null){
			        		throw new MessageDecodeException("��֧�ֵı��Լ��"+pm);
			        	}
			        	Object[] dis=mparser.parser(data,1,data.length-1);
			        	if((dis!=null) && (dis.length>0)){		//���˽����--�������Ľ�
			        		for(int i=0;i<dis.length;i++){
			        			DataItem di=(DataItem)dis[i];
			        			String key=(String)di.getProperty("datakey");
			        			if(key==null || key.length()<4){
			        				continue;
			        			}
			        			boolean called=true;
			        			if(called){
			        				HostCommandResult hcr=new HostCommandResult();
	        						hcr.setCode(key);
	        						if(di.getProperty("value")==null){
	        							hcr.setValue(null);
	        						}else{
	        							hcr.setValue(di.getProperty("value").toString());
	        						}	        						
	        						hcr.setCommandId(hc.getId());
	        						value.add(hcr);
			        			}
			        		}
			        	}
			        	hc.setStatus(HostCommand.STATUS_SUCCESS);
			        	hc.setResults(value);
					}else{
						//������������Ϊ�ն�������ͨѶ						
						hc.setStatus(HostCommand.STATUS_RTUAMTCOM_ERROR);
						hc.setResults(null);
					}
				}else{
					//�쳣Ӧ��֡
					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
					hc.setResults(null);
				}
			}else{
				//��վ�ٲ�
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}		
		return hc;
	}
		
	private String getMeterProtocol(byte[] data,int loc,int len){
		String protocol="";
		BbMeterFrame bbFrame=new BbMeterFrame();
		bbFrame.parse(data,loc,len);
		if(bbFrame.getDatalen()>0)
			protocol=Protocol.BBMeter97;
		else{
			ZjMeterFrame zjFrame=new ZjMeterFrame();
			zjFrame.parse(data, loc, len);
			if (zjFrame.getDatalen()>0){
				protocol=Protocol.ZJMeter;
			}
		}
    	return protocol;
	}
}
