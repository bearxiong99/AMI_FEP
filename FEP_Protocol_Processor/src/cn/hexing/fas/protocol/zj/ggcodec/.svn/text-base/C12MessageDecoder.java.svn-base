package cn.hexing.fas.protocol.zj.ggcodec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.model.RtuDataItem;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.HexDump;


/**
 *	集中器读取各个表的日冻结数据(功能码：12H)响应消息解码器
 * @author Administrator
 *
 */
public class C12MessageDecoder extends AbstractMessageDecoder{	
	private static Log log=LogFactory.getLog(C12MessageDecoder.class);

    public Object decode(IMessage message) {
    	List<RtuData> tasks = new ArrayList<RtuData>();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
        		//应答类型
        		int rtype=(ParseTool.getErrCode(message));  
        		HostCommand hc=new HostCommand();
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//取应答数据
        			String data=ParseTool.getDataString(message);
        			log.info("C12MessageDecoder data="+data);
        			BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());  
        			MessageZj zjMsg=(MessageZj) message;
        			byte fseq = zjMsg.head.fseq;
        			byte iseq=zjMsg.head.iseq ;
        			String taskNo=(String) rtu.getParamFromMap(fseq);
        			if(iseq==7||iseq==0){
            			rtu.removeParamFromMap(fseq);
        			}
        			if (rtu!=null&&data!=null&&data.length()>=42){
        				String totalFrame=data.substring(0, 2); //帧总数 1 字节HEX
        				String startNo=DataSwitch.ReverseStringByByte(data.substring(2, 6));//起始电表顺序号 2字节
        				String currentSEQ=data.substring(6, 8); //当前帧序号  1字节HEX
        				String code=data.substring(8, 12);
        				String sdate= data.substring(12, 18);
        				int datakey=Integer.parseInt(code,16);
    					ProtocolDataItemConfig dic=getDataItemConfig(datakey);
    					int dlen=dic.getLength();
    					data=data.substring(18);   //1字节帧总数+ 2字节 起始电表顺序号+ 1字节 当前帧序号 + 2字节选项域 + 3个字节时间YYMMdd  将这9个字节取出来，剩下的都是数据区：包含有每一块电表的数据。
    					//电表数据格式： 6字节电表编号 + 1字节数据长度 +2字节电笔状态字+ 5字节数据时标 + dlen字节数据块内容
    					int len =(6+1+2+5+dlen)*2;
    					byte[] datas=null;
    					String sdata="";
    					String meterNo="";
    					String stime="";
    					int count = data.length()/len; // 数据区中带有count块电表的数据
    					for(int i=0;i<count ;i++){
    		            	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
    						sdata=data.substring(0, len);
    						meterNo=DataSwitch.ReverseStringByByte(sdata.substring(0, 12));
    						MeasuredPoint mp=rtu.getMeasuredPointByTnAddr(meterNo);
    						if(mp==null){
    							log.error("表计档案不存在。。。");
    							continue;
    						}
    						//数据长度和状态字暂时没用到 
//    						int dataLen=Integer.parseInt(sdata.substring(12, 14));
//    						String status=sdata.substring(14, 18);
    					    stime=sdata.substring(18, 28);
    					    stime="13"+stime+"00";
    					    SimpleDateFormat sd=new SimpleDateFormat("yyyyMMddHHmmss");
    					    SimpleDateFormat sdff=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    					    stime=DateConvert.iranToGregorian(sdff.format(sd.parse(stime)));
    						datas=HexDump.toArray(sdata);
    						int index=14;
    	        			while(index<datas.length){
    	        				if(2<(datas.length-index)){	//至少要有3字节数据（2字节数据标示+至少1字节数据）
    	        					if(dic!=null){
    	        						int loc=index;
    	        						int itemlen=0;
            							itemlen=parseBlockData(datas,loc,dic,mp.getTn(),new Long(0),value);
            							loc+=itemlen;
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
    	        			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    	        			Date time = sdf.parse(stime);
    	        			if(mp!=null){
    	        				RtuData task=new RtuData();
    	        				task.setTn(mp.getTn());
    	        				task.setLogicAddress(rtu.getLogicAddress());
    	        				task.setTime(time);
    	        				task.setTaskNum(taskNo);		
    	        				for(int j=0;j<value.size();j++){					
            						RtuDataItem item=new RtuDataItem();
            						item.setCode(value.get(j).getCode());
            						item.setValue(value.get(j).getValue());    
            						task.addDataList(item);
            					}
            					tasks.add(task);
    	        			}
    	        		  data=data.substring(len);
    					}
        			}
        		}
    		}	
    	}
        			catch(Exception e){
    			        	throw new MessageDecodeException(e);
    			        }     
        	return tasks;
    			  
    	
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
    private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,String point,Long cmdid,List<HostCommandResult> result){
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
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,String point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{
    		int datakey=pdc.getDataKey();
    		int itemlen=0;   		
			itemlen=pdc.getLength();			
			if(itemlen<=(data.length-loc)){	//有足够数据				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());
				HostCommandResult hcr=new HostCommandResult();
				hcr.setCode(pdc.getCode());
				if(di!=null){
					hcr.setValue(di.toString());
				}
				hcr.setCommandId(cmdid);
				hcr.setTn(point);
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
