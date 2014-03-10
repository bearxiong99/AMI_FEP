package cn.hexing.fas.protocol.zj.viewer.gg;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.model.RtuAlertArg;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.RtuManage;

/**
 * @info 异常，告警。
 */
public class C09MessageDecoder extends AbstractMessageDecoder{
	private static final Log log=LogFactory.getLog(C09MessageDecoder.class);
	
	public Object decode(IMessage message) {
		List<RtuAlert> rt=null;
		try{
			if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
				int rtype=(ParseTool.getErrCode(message));
				if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
					//取应答数据
        			byte[] data=ParseTool.getData(message);
        			
        			if(data!=null){
        				int num=(data[0] & 0xff);	//告警数量
        				int loc=1;
        				//取终端对象
        				int rtu=((MessageZj)message).head.rtua;
        				NumberFormat nf=NumberFormat.getInstance();
        				nf.setMaximumFractionDigits(4);	        				
        				rt=new ArrayList<RtuAlert>();
        				Calendar date=Calendar.getInstance();
                        //用IO时间作为接收时间        				
        				date.setTimeInMillis(((MessageZj)message).getIoTime());        				
        				while(loc<data.length){
        					if(8<=(data.length-loc)){	        						
        						int point=(data[loc] & 0xff);	//测量点
        						Calendar stime=getTime(data,loc+1);	//告警发生时间
        						int alr=(data[loc+6] & 0xff)+((data[loc+7] & 0xff)<<8);	//告警编码
        						loc+=8;	        						
        						List rac=getRtuAlertCode(alr);	 //取告警配置	
        						if(rac!=null){
        							RtuAlert ra=new RtuAlert();	        							
                                    String tn = String.valueOf(point);
                                    ra.setTn(tn);
                                    ra.setAlertCode(alr);
                                    ra.setAlertTime(stime.getTime());
                                    //用IO时间作为接收时间
                                    ra.setReceiveTime(new Date(((MessageZj) message).getIoTime()));                                    
        							List<RtuAlertArg> alertdatas=new ArrayList<RtuAlertArg>();
	        						String olddi="";
        							Hashtable<String,RtuAlertArg> databag=new Hashtable<String,RtuAlertArg>();	//存放临时数据，为了匹配对应量
	        						for(Iterator iter=rac.iterator();iter.hasNext();){
        								String di=(String)iter.next();
        								boolean is8ffe=false;	        								
        								if(di.equals("8FFE") || di.equals("8ffe")){
        									//取前一个数据id
        									if(olddi.length()<=0){
        										//配置错误
        										throw new MessageDecodeException("告警配置错误：终端--"+ParseTool.IntToHex4(rtu)+" 告警编码--"+ParseTool.IntToHex(alr) );
        									}
        									is8ffe=true;
        									di=olddi;
        								}else{
        									is8ffe=false;
        									olddi=di;
        								}
        								ProtocolDataItemConfig pdc=super.dataConfig.getDataItemConfig(di);
        								if(pdc!=null){
        									if(pdc.getLength()<=(data.length-loc)){
        										List childs=pdc.getChildItems();
        										if(childs!=null && childs.size()>0){
        											for(Iterator iterc=childs.iterator();iterc.hasNext();){
        												ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)iterc.next();
        												Object dt=null;
        												try{
        													dt=DataItemParser.parsevalue(data,loc,cpdc.getLength(),cpdc.getFraction(),cpdc.getParserno());			        										
        												}catch(Exception e){
        													//忽略数据解析错误
        													log.error("告警数据解析错误",e);
        												}
        												RtuAlertArg arg=null;
        												if(dt!=null){
		        											if(is8ffe){
		        												arg=(RtuAlertArg)databag.get(cpdc.getCode());
		        												arg.setCorrelValue(dt.toString());
		        											}else{
		        												arg=new RtuAlertArg();
				        										arg.setCode(cpdc.getCode());
		        												arg.setValue(dt.toString());
		        												databag.put(cpdc.getCode(),arg);
		        												alertdatas.add(arg);
		        											}
		        											
		        										}else{
		        											if(!is8ffe){
			        											arg=new RtuAlertArg();
				        										arg.setCode(cpdc.getCode());
			        											arg.setValue(null);
			        											alertdatas.add(arg);
			        											databag.put(cpdc.getCode(),arg);
		        											}
		        										}			        										
		        										loc+=cpdc.getLength();
        											}
        										}else{
        											Object dt=DataItemParser.parsevalue(data,loc,pdc.getLength(),pdc.getFraction(),pdc.getParserno());
	        										RtuAlertArg arg=null;		        										
	        										if(dt!=null){
	        											if(is8ffe){
	        												arg=(RtuAlertArg)databag.get(pdc.getCode());
	        												arg.setCorrelValue(dt.toString());		        												
	        											}else{
	        												arg=new RtuAlertArg();
			        										arg.setCode(pdc.getCode());				        										
	        												arg.setValue(dt.toString());
	        												alertdatas.add(arg);
	        												databag.put(pdc.getCode(),arg);
	        											}
	        										}else{
	        											if(!is8ffe){
		        											arg=new RtuAlertArg();
			        										arg.setCode(pdc.getCode());	
		        											arg.setValue(null);
		        											alertdatas.add(arg);
		        											databag.put(pdc.getCode(),arg);
	        											}
	        										}
	        										loc+=pdc.getLength();
        										}
        									}else{
        										//数据长度不对        										    									
        										log.info("数据长度不对,数据项："+pdc.getCode()+" 期望数据长度："+pdc.getLength()+" 解析长度："+(data.length-loc));
        										loc=data.length;	//如果告警数据解析错误（比如告警数据上送错误，数据库内配置错误），正常记录告警
        										break;
        									}
        								}else{
        									//无法识别的数据项
        									throw new MessageDecodeException("无法识别的数据项");
        								}
        							}
	        						ra.setArgs(alertdatas);
	        						rt.add(ra);
        						}else{
	        						throw new MessageDecodeException("无法获取告警配置"+" 告警编码--"+ParseTool.IntToHex(alr));
        						}
        					}else{
        						//数据长度不对
        						throw new MessageDecodeException("数据长度不对");
        					}
        					num--;
        					if((num<=0) && (loc<data.length)){
        						//数据长度不对
        						//throw new MessageDecodeException("告警解析错误");	        						
								log.info("数据长度不对,预期长度："+loc+" 解析长度："+data.length);
								loc=data.length;	//如果告警数据解析错误（比如告警数据上送错误，数据库内配置错误），正常记录告警
								break;
        					}
        				}
        			}else{
        				//数据缺失
        				throw new MessageDecodeException("数据长度不对");
        			}
				}else{
				}
			}else{
				//主站召测				
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		//把异常携带的数据项转换成字符串,用于保存
		if (rt!=null){
			for(int i=0;i<rt.size();i++){
				RtuAlert rtuAlert=(RtuAlert)rt.get(i);
				rtuAlertSetSbcs(rtuAlert);
			}
		}		
		return rt;
	}
	private void rtuAlertSetSbcs(RtuAlert rtuAlert){
		try{
			StringBuffer sb=new StringBuffer();
	        List args = rtuAlert.getArgs();
	        if(args!=null && args.size()>0){
	        	RtuAlertArg arg = (RtuAlertArg) args.get(0);
	        	sb.append(arg.getCode());
	        	sb.append("=");
	        	if(arg.getValue()!=null){
	        		sb.append(arg.getValue());
	        	}
	        	sb.append("@");
	        	if(arg.getCorrelValue()!=null){
	        		sb.append(arg.getCorrelValue());
	        	}
	        	for (int i = 1; i < args.size(); i++) {
	        		arg = (RtuAlertArg) args.get(i);
	        		sb.append(";");
	        		sb.append(arg.getCode());
	            	sb.append("=");
	            	if(arg.getValue()!=null){
	            		sb.append(arg.getValue());
	            	}
	            	sb.append("@");
	            	if(arg.getCorrelValue()!=null){
	            		sb.append(arg.getCorrelValue());
	            	}
	        	}
	        	rtuAlert.setSbcs(sb.toString());
	        }
		}catch(Exception ex){
			
		}
	}
	
	/**
     * 解析数据时间
     * @param data
     * @param offset
     * @return
     */
    private Calendar getTime(byte[] data,int offset){
    	Calendar rt=null;
    	try{
    		int month=ParseTool.BCDToDecimal(data[1+offset]);
    		int year=ParseTool.BCDToDecimal(data[offset]);
    		if(month>0 && year>=0){
	    		if(ParseTool.isValidMonth(data[1+offset]) && ParseTool.isValidDay(data[2+offset],month,year+2000)
	    				&& ParseTool.isValidHHMMSS(data[3+offset]) && ParseTool.isValidHHMMSS(data[4+offset]))
	    		{		    	
			    	rt=Calendar.getInstance();		    		
			    	rt.set(Calendar.YEAR,year+2000);			    	
			    	rt.set(Calendar.MONTH,month-1);			    	
			    	int num=ParseTool.BCDToDecimal((byte)(data[2+offset] & 0x3f));
			    	rt.set(Calendar.DAY_OF_MONTH,num);
			    	num=ParseTool.BCDToDecimal((byte)(data[3+offset] & 0x3f));
			    	rt.set(Calendar.HOUR_OF_DAY,num);
			    	num=ParseTool.BCDToDecimal((byte)(data[4+offset] & 0x7f));
			    	if(num>=60){
			    		rt.add(Calendar.HOUR_OF_DAY,1);
			    		num=0;
			    	}
			    	rt.set(Calendar.MINUTE,num);
			    	rt.set(Calendar.SECOND,0);
			    	rt.set(Calendar.MILLISECOND,0);
	    		}
    		}
    	}catch(Exception e){
    		log.error("alert decode",e);
    	}    	
    	return rt;
    }
    
	/**
	 * 取终端告警配置
	 * @param rtu
	 * @param alert
	 */
	private List getRtuAlertCode(int alert){
		List rt=null;
		try{
			rt=RtuManage.getInstance().getRtuAlertCode(alert).getArgs();
		}catch(Exception e){
			log.error("get alert code",e);
		}
		return rt;
	}
		
}
