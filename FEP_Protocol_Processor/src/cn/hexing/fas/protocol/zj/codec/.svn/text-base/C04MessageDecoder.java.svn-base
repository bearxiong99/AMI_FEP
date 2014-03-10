package cn.hexing.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.Calendar;

import java.util.List;


import cn.hexing.exception.MessageDecodeException;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;

import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.zj.ErrorCode;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;


/**]
 * 读编程日志(功能码：03H)响应消息解码器
 *
 */
public class C04MessageDecoder  extends AbstractMessageDecoder{

	public Object decode(IMessage message) {
		List<HostCommandResult> datas = null;
		HostCommand hc=new HostCommand();
		
        try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
				int rtype=(ParseTool.getErrCode(message));
				//by yangjie 屏蔽下行请求命令ID,需要在数据库查询获得
				/*HostCommand hcmd=(HostCommand)message.getAttachment();
        		CopyUtil.copyProperties(hc,hcmd);
        		Long cmdid=hcmd.getId();*/
				
				if(DataMappingZJ.ERROR_CODE_OK==rtype){		//终端正常应答
					byte[] data=ParseTool.getData(message);	//取应答数据
					if((data!=null) && (data.length>17)){	//1字节测量点+5字节编程时间+9字节通讯通道+至少一个数据项数据（不少于3字节）
						
						datas=new ArrayList<HostCommandResult>();
						//Long cmdid=(Long)message.getAttachment();	//主站命令ID，just for debug
						//hc.setId(cmdid);
						hc.setStatus(HostCommand.STATUS_SUCCESS);
						int point =ParseTool.BCDToDecimal(data[0]);						
						Calendar optime=ParseTool.getTime(data,1);		//编程时间							
						Object comm=DataItemParser.parsevalue(data,6,5,0,14);	//通讯通道
						
						int loc=15;						
						while(loc<data.length){	//解析数据
							int datakey=((data[loc+1] & 0xff)<<8)+(data[loc] & 0xff);	//数据项标识
							loc+=2;							
							ProtocolDataItemConfig dic=getDataItemConfig(datakey);		//数据项设置
							if(dic!=null){
								
								int itemlen=0;
        						if((0x8100<datakey) && (0x81fe>datakey)){//是任务配置
        							int tasktype=(data[loc] & 0xff);	//????任务有三类 普通 中继 异常，要分类型计算
        							if(tasktype==DataItemParser.TASK_TYPE_NORMAL){
        								if(16<(data.length-loc)){
	        								itemlen=(data[loc+15] & 0xff)*2+16;	
	        							}else{
	        								throw new MessageDecodeException("帧数据太少");	        								
	        							}
        							}
        							if(tasktype==DataItemParser.TASK_TYPE_RELAY){
        								if(21<(data.length-loc)){
	        								itemlen=(data[loc+20] & 0xff)+21;	
	        							}else{
	        								throw new MessageDecodeException("帧数据太少");
	        							}
        							}
        							if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
        								if(7<(data.length-loc)){
	        								itemlen=(data[loc+6] & 0xff)*3+8;	
	        							}else{
	        								throw new MessageDecodeException("帧数据太少");
	        							}
        							}
        						}else{
        							itemlen=dic.getLength();
        						}
								if(itemlen<=(data.length-loc)){
									Object di=DataItemParser.parsevalue(data,loc,itemlen,dic.getFraction(),dic.getParserno());
									
									HostCommandResult hcr=new HostCommandResult();
									hcr.setChannel((String)comm);
									hcr.setCode(dic.getCode());
									hcr.setCommandId(new Long(0));
									hcr.setProgramTime(optime.getTime());
									hcr.setTn(String.valueOf(point));
									if(di!=null){
										hcr.setValue(di.toString());
									}
									datas.add(hcr);
									
									loc+=itemlen;
								}else{
									//数据缺失
									throw new MessageDecodeException("帧数据太少");
								}
							}else{
								//无法识别的数据
								throw new MessageDecodeException("未配置的数据项");
							}
						}
						
					}else{
						//错误数据
						if(data.length>0){
							throw new MessageDecodeException("帧数据太少");
						}else{
							datas=null;	//空的数据
						}
					}
				}else{
					//异常应答
					byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
				}
			}else{
				//主站召测
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		hc.setResults(datas);
		return hc;
	}
		
	/**
	 * 取数据项设置
	 * @param datakey
	 * @return
	 */
	private ProtocolDataItemConfig getDataItemConfig(int datakey){    	
    	return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
    }
}
