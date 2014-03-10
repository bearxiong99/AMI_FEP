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
 * 读中继(功能码：00H)响应消息编码器
 *
 */
public class C00MessageDecoder  extends AbstractMessageDecoder{
	
	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();		
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答                                      
				int rtype=(ParseTool.getErrCode(message));
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//终端正常应答
					byte[] data=ParseTool.getData(message);	//取应答数据
					if((data!=null) && (data.length>1)){											
			        	
			        	//获取表规约,因为上行报文不带测量点号,所以解析结果不带测量点号
			        	String pm=getMeterProtocol(data,1,data.length-1);			        	
			        	IMeterParser mparser=MeterParserFactory.getMeterParser(pm);
			        	if(mparser==null){
			        		throw new MessageDecodeException("不支持的表规约："+pm);
			        	}
			        	Object[] dis=mparser.parser(data,1,data.length-1);
			        	if((dis!=null) && (dis.length>0)){		//过滤结果集--方法待改进
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
						//数据区错误认为终端与表计无通讯						
						hc.setStatus(HostCommand.STATUS_RTUAMTCOM_ERROR);
						hc.setResults(null);
					}
				}else{
					//异常应答帧
					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
					hc.setResults(null);
				}
			}else{
				//主站召测
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
