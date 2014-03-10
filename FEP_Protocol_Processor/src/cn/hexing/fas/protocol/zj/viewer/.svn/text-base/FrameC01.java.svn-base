package cn.hexing.fas.protocol.zj.viewer;

import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	FrameC01.java
 * TODO			召测终端当前数据
 */
public class FrameC01 extends AbstractFrame{
	public static final String FUNC_NAME="召测终端当前数据";
	public FrameC01(){
		//
	}
	
	public FrameC01(byte[] frame){
		super(frame);
	}
	
	public FrameC01(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("命令类型--").append(FUNC_NAME);
			sb.append("\n");
			if(direction>0){	//终端应答
				if(fexp>0){
					sb.append("异常应答--").append(errCode(frame[11]));
				}else{
					descRtuReply(sb);
				}				
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("召测的测量点--");
			byte points[]=new byte[64];		//测量点号数组（最多64个测量点，定义见规约TNM）
			byte pn=0;
			byte pnum=0;	//测量点个数
			for(int i=0;i<8;i++){	//测量点分析
				int flag=0x1;
				int tnm=(frame[11+i] & 0xff);
				for(int j=0;j<8;j++){        					
					if((tnm & (flag<<j))>0){
						points[pnum]=pn;
						pnum++;
						buffer.append(pn+",");
					}
					pn++;
				}
			}
			buffer.append("\n");
			buffer.append("召测的数据项---");
			//召测的数据项			
			for(int pos=19;pos<(length+11);pos+=2){
				buffer.append(Util.ByteToHex(frame[pos+1])).append(Util.ByteToHex(frame[pos])).append(",");
			}
		}catch(Exception e){
			//
		}
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			//buffer.append("召测的测量点--");
			byte points[]=new byte[64];		//测量点号数组（最多64个测量点，定义见规约TNM）
			byte pn=0;
			byte pnum=0;	//测量点个数
			for(int i=0;i<8;i++){	//测量点分析
				int flag=0x1;
				int tnm=(frame[11+i] & 0xff);
				for(int j=0;j<8;j++){        					
					if((tnm & (flag<<j))>0){
						points[pnum]=pn;
						pnum++;					
					}
					pn++;
				}
			}
			
			buffer.append("召测的数据项---");
			
			if(pnum>0){			
				int index=19;	//解析测量点数据
				int tail=length+11;			
				while(index<tail){
					if(2<(tail-index)){	//至少要有3字节数据（2字节数据标示+至少1字节数据）
						int datakey=((frame[index+1] & 0xff)<<8)+(frame[index] & 0xff); //数据标示号
						ProtocolDataItemConfig dic=DataConfigZj.getInstance().getDataConfig().getDataItemConfig(ParseTool.IntToHex(datakey));
						if(dic==null){
							ProtocolDataItemConfig di=DataConfigZjpb.getInstance().getDataConfig().getDataItemConfig(ParseTool.IntToHex(datakey));
							if(di!=null)
								dic = di;
						}
						if(dic!=null){
							int loc=index+2;
							int itemlen=0;
	
							for(int j=0;j<pnum;j++){
								itemlen=parseBlockData(frame,loc,dic,points[j] & 0xFF,buffer);
								loc+=itemlen;
								if(ParseTool.isTask(datakey)){
									loc=tail;
									break;//高科终端只能单独召测任务配置，并且返回的数据为任务配置+垃圾数据，目前简单处理为单独召测任务配置
								}
							}
							index=loc;
						}else{
							//不支持的数据						
							buffer.append("\n");
							buffer.append("不支持的数据:"+ParseTool.IntToHex(datakey));	
							break;	//高科的任务数据比较特殊，暂时做如此处理
						}
					}else{
						break;
					}
				}
			}
		}catch(Exception e){
			//
		}
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
    private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,int point,StringBuffer buffer){
    	int rt=0;
    	try{    		
    		List children=pdc.getChildItems();
    		int index=loc;
    		if((children!=null) && (children.size()>0)){	//数据块召测    			
    			for(int i=0;i<children.size();i++){
    				ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)children.get(i);
    				int dlen=parseBlockData(data,index,cpdc,point,buffer);
    				if(dlen<=0){
    					return -1;
    				}
    				index+=dlen;
    				rt+=dlen;
    			}    			
    		}else{
    			int dlen=parseItem(data,loc,pdc,point,buffer);
    			if(dlen<=0){
					return -1;
				}
    			rt+=dlen;
    		}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,int point,StringBuffer buffer){
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
						buffer.append("错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>16"+" 解析长度："+(data.length-loc));
						return -1;
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_RELAY){
					if(21<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+20])+21;	
					}else{
						buffer.append("错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>21"+" 解析长度："+(data.length-loc));
						return -1;
					}
				}
				if(tasktype==DataItemParser.TASK_TYPE_EXCEPTION){
					if(7<(data.length-loc)){
						itemlen=ParseTool.BCDToDecimal(data[loc+6])*3+8;	
					}else{
						buffer.append("错误数据长度，数据项："+pdc.getCode()+" 期望数据长度：>7"+" 解析长度："+(data.length-loc));
						return -1;
					}
				}
			}else{
				itemlen=pdc.getLength();
			}
			if(itemlen<=(data.length-loc)){	//有足够数据				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());
				buffer.append("测量点"+point).append("/");
				buffer.append("数据项"+pdc.getCode()).append("=");				
				if(di!=null){
					buffer.append(di.toString());
				}
				buffer.append("\n");				
				rt=itemlen;
			}else{
				//错误数据
				if((data.length-loc)==0){
					//没有更多字节解析，可能是终端中块数据不全，或者数据丢失
					
				}else{
					buffer.append("错误数据长度，数据项："+pdc.getCode()+" 期望数据长度："+itemlen+" 解析长度："+(data.length-loc));
					return -1;
				}				      							
			}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
}
