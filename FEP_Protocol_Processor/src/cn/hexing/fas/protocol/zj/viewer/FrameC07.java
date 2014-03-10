package cn.hexing.fas.protocol.zj.viewer;

import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	FrameC07.java
 * TODO
 */
public class FrameC07 extends AbstractFrame{
	public static final String FUNC_NAME="实时设置终端参数";
	public FrameC07(){
		//
	}
	
	public FrameC07(byte[] frame){
		super(frame);
	}
	
	public FrameC07(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("命令类型--").append(FUNC_NAME);
			sb.append("\n");
			if(direction>0){	//终端应答
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){
				if(length>1){
					parseErr(buffer);
				}else{
					buffer.append("异常应答--").append(errCode(frame[11]));					
				}
			}else{
				parseErr(buffer);				
			}
		}catch(Exception e){
			//
		}
	}
	
	private void parseErr(StringBuffer buffer){
		buffer.append("设置的测量点--");
		int point=frame[11] & 0xFF;
		buffer.append(point).append("\n");
		
		int index=12;
		int tail=length+11;
		
		while(index<tail){
			if(2<(tail-index)){
				buffer.append(ParseTool.BytesToHexC(frame, index, 2));
				buffer.append("设置结果:").append(errCode(frame[index+2])).append("\n");
				index+=3;
			}else{
				break;
			}
		}
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("设置的测量点--");
			int point=frame[11] & 0xFF;
			buffer.append(point).append("    ");
			buffer.append("使用的权限等级--");
			buffer.append((frame[12] & 0xFF)==0x11?"高级":"低级").append("    ");
			buffer.append("密码--");
			buffer.append(ParseTool.BytesToHexC(frame, 13, 3)).append("\n");
			buffer.append("命令下发时间---");
			buffer.append("20").append(Util.ByteToHex(frame[16])).append("-").append(Util.ByteToHex(frame[17]))
					.append("-").append(Util.ByteToHex(frame[18])).append(" ").append(Util.ByteToHex(frame[19]))
					.append(":").append(Util.ByteToHex(frame[20])).append(":00    ");
			buffer.append("命令有效时间---").append(Util.ByteToHex(frame[21])).append("min\n");
			buffer.append("设置的数据项---");
			int index=22;
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
						
						itemlen=parseBlockData(frame,loc,dic,point,buffer);
						loc+=itemlen;
						if(ParseTool.isTask(datakey)){
							loc=tail;
							break;//高科终端只能单独召测任务配置，并且返回的数据为任务配置+垃圾数据，目前简单处理为单独召测任务配置
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
				buffer.append(pdc.getCode()).append("=");				
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
