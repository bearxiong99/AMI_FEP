package cn.hexing.fas.protocol.zj.ggcodec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.zj.ErrorCode;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;

/**
 * 	读当前数据(功能码：01H)响应消息解码器
 *  控制码（C=01H）
 * @author luolb
 */
public class C01MessageDecoder extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C01MessageDecoder.class);
	/* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageDecoder#decode(cn.hexing.fas.framework.IMessage)
     */
    public Object decode(IMessage message) {
    	HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
        		//应答类型
        		int rtype=(ParseTool.getErrCode(message));
        		Long cmdid=new Long(0);        		
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//取应答数据
        			byte[] data=ParseTool.getData(message);
	        		if(data!=null && data.length>10){	//返回数据不可能少于6个byte（8字节TNM+至少一个测量点数据）	        									
	        			//解析应答数据
	        			byte points[]=new byte[64];		//测量点号数组（最多64个测量点，定义见规约TNM）
	        			byte pn=0;
	        			byte pnum=0;	//测量点个数
	        			for(int i=0;i<8;i++){	//测量点分析
	        				int flag=0x1;
	        				int tnm=(data[i] & 0xff);
	        				for(int j=0;j<8;j++){        					
	        					if((tnm & (flag<<j))>0){
	        						points[pnum]=pn;
	        						pnum++;
	        					}
	        					pn++;
	        				}
	        			}
	        			if(pnum>0){		        			
	        				int index=8;	//解析测量点数据
		        			while(index<data.length){
		        				if(2<(data.length-index)){	//至少要有3字节数据（2字节数据标示+至少1字节数据）
		        					int datakey=((data[index+1] & 0xff)<<8)+(data[index] & 0xff); //数据标示号		        					
		        					ProtocolDataItemConfig dic=getDataItemConfig(datakey);
		        					if(dic!=null){
		        						int loc=index+2;
		        						int itemlen=0;

		        						for(int j=0;j<pnum;j++){
		        							itemlen=parseBlockData(data,loc,dic,points[j],cmdid,value);
		        							loc+=itemlen;
		        							if(ParseTool.isTask(datakey)){
		        								loc=data.length;
		        								break;//高科终端只能单独召测任务配置，并且返回的数据为任务配置+垃圾数据，目前简单处理为单独召测任务配置
		        							}
		        						}
		        						index=loc;
		        					}else{
		        						//不支持的数据		        							
		        						log.info("不支持的数据:"+ParseTool.IntToHex(datakey));	
		        						break;	//高科的任务数据比较特殊，暂时做如此处理
		        					}
		        				}else{
		        					//错误帧数据
		        					throw new MessageDecodeException("帧数据太少");	
		        				}
		        			}		        			
	        			}else{
	        				throw new MessageDecodeException("帧内容错误，未指定测量点");
	        			}
        			}else{        				
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
        		}else{
    				//异常应答帧
        			byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				if(data.length==1){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        				}else if(data.length==9){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[8]));
        				}else{
        					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        				}
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
    			}
        	}else{
    			//主站召测帧
    			
    		}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }
        hc.setResults(value);        
        return hc;
    }
    
    /**
     * 解析块数据
     * @param data		数据帧
     * @param loc		解析开始位置
     * @param pdc		数据项配置
     * @param points	召测的测量点数组
     * @param pnum		召测的测量点个数
     * @param result	结果集合
     */
    private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,byte point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{    		
    		List children=pdc.getChildItems();
    		int index=loc;
    		if((children!=null) && (children.size()>0)){	//数据块召测    			
    			for(int i=0;i<children.size();i++){
    				ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)children.get(i);
    				int dlen=parseBlockData(data,index,cpdc,point,cmdid,result);
    				index+=dlen;
    				rt+=dlen;
    			}    			
    		}else{
    			int dlen=parseItem(data,loc,pdc,point,cmdid,result);
    			rt+=dlen;
    		}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,byte point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{
    		int datakey=pdc.getDataKey();
    		int itemlen=0;
    		if((0x8100<datakey) && (0x81fe>datakey)){//是任务配置
				int tasktype=(data[loc] & 0xff);	//????任务有三类 普通 中继 异常，要分类型计算
				if(tasktype==DataItemParser.TASK_TYPE_NORMAL){
					if(16<(data.length-loc)){
						itemlen=(ParseTool.BCDToDecimal(data[loc+15]))*2+16;	
					}else{
						throw new MessageDecodeException(
								"错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>16"+" 解析长度："+(data.length-loc));
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_RELAY){
					if(21<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+20])+21;	
					}else{
						throw new MessageDecodeException(
								"错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>21"+" 解析长度："+(data.length-loc));
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
					if(7<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+6])*3+8;	
					}else{
						throw new MessageDecodeException(
								"错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>7"+" 解析长度："+(data.length-loc));
					}
				}
			}else{
				itemlen=pdc.getLength();
			}
			if(itemlen<=(data.length-loc)){	//有足够数据				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());
				HostCommandResult hcr=new HostCommandResult();
				hcr.setCode(pdc.getCode());
				if(di!=null){
					if("8030".equals(pdc.getCode())){
						String date="13"+di.toString().substring(2);
						date=DateConvert.iranToGregorian(date);
						hcr.setValue(date);
					}else{
						hcr.setValue(di.toString());
					}
				}
				hcr.setCommandId(cmdid);
				hcr.setTn(point+"");
				result.add(hcr);
				rt=itemlen;
			}else{
				//错误数据
				if((data.length-loc)==0){
					//没有更多字节解析，可能是终端中块数据不全，或者数据丢失
					
				}else{
					throw new MessageDecodeException(
							"错误数据长度，数据项："+pdc.getCode()+" 期望数据长度："+itemlen+" 解析长度："+(data.length-loc));
				}				      							
			}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private ProtocolDataItemConfig getDataItemConfig(int datakey){    	
    	return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
    }
}
