package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fas.protocol.meter.BbMeterFrame;
import cn.hexing.fas.protocol.meter.HX645MeterFrame;
import cn.hexing.fas.protocol.meter.IMeterParser;
import cn.hexing.fas.protocol.meter.MeterParserFactory;
import cn.hexing.fas.protocol.meter.ZjMeterFrame;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.HexDump;

/**
 * 中继转发(功能码：10H)响应消息编码器
 * 
 */
public class C10MessageDecoder  extends AbstractMessageDecoder{
	List<String> setOpKey = new ArrayList<String>();
	{
		setOpKey.add("0700001400");
		setOpKey.add("0700001300");
		setOpKey.add("0700001301");
		setOpKey.add("0700001401");
		setOpKey.add("0700001600");
		setOpKey.add("0700000100");
		setOpKey.add("0800000100");
		setOpKey.add("0500000701");
	}
	
	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
    	List<HostCommandResult> value=new ArrayList<HostCommandResult>();		
		try{				
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "10");
			data=data.substring(8);//消去数据单元标识			
			if (codes!=null&&codes.length==1&&codes[0].equals("10F009")){//转发主站直接对电表的抄读数据命令上行
				data=data.substring(2);//消去通讯端口号	
				String addr=DataSwitch.ReverseStringByByte(data.substring(0,12));//表地址
				int tag=Integer.parseInt(data.substring(12,14),16); //转发结果标志
				data=data.substring(14);
				int len=Integer.parseInt(data.substring(0,2),16);//转发内容字节长度
				data=data.substring(2);
				if(len>4){//转发有数据内容
					String key=DataSwitch.ReverseStringByByte(data.substring(0,8));//数据项标识
					IMeterParser mparser;
					if (key.substring(0,4).equals("0000")){//97版表规约标识2个字节
						key=key.substring(4,8);
						mparser=MeterParserFactory.getMeterParser(Protocol.BBMeter97);
					}
					else{//07版电表规约标识4个字节
						mparser=MeterParserFactory.getMeterParser(Protocol.BBMeter07);
					}
					data=data.substring(8,8+(len-4)*2);
					
					Object[] dis=mparser.parser(key,data,addr); 
					if((dis!=null) && (dis.length>0)){		//过滤结果集--方法待改进
		        		for(int i=0;i<dis.length;i++){
		        			DataItem di=(DataItem)dis[i];
		        			key=(String)di.getProperty("datakey");
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
				}
				else{
					//数据区错误认为终端与表计无通讯						
					hc.setStatus(HostCommand.STATUS_RTUAMTCOM_ERROR);
					hc.setResults(null);
				}
			}
			if (codes!=null&&codes.length==1&&codes[0].equals("10F010")){//转发主站直接对电表的遥控跳闸和允许合闸的命令
				if(data.length()>=16){
				BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
				data=data.substring(2);//消去通讯端口号	
				String addr=DataSwitch.ReverseStringByByte(data.substring(0,12));//表地址
				MeasuredPoint mp=rtu.getMeasuredPointByTnAddr(addr);
				String tag=data.substring(12,14); //转发结果标志
				String RelayStatus=data.substring(14, 16);
				data=data.substring(16);
		        HostCommandResult hcr=new HostCommandResult();
		        hcr.setMeterAddr(addr);
	    		hcr.setCode("10F010");
	    		hcr.setValue(tag);
	    		hcr.setTn(mp.getTn());
	    		hcr.setCommandId(hc.getId());
	    		value.add(hcr);
		        hc.setStatus(HostCommand.STATUS_SUCCESS);
		        hc.setResults(value);
				}
				else{
					//数据区错误认为终端与表计无通讯						
					hc.setStatus(HostCommand.STATUS_RTUAMTCOM_ERROR);
					hc.setResults(null);
				}
			}
			else{//通明转发上行
				data=data.substring(2);//消去通讯端口号			
				int len=Integer.parseInt(DataItemParser.parseValue(data.substring(0,4),"HTB2").getValue());
				data=data.substring(4);//消去转发字节数
				data=data.substring(0,len*2);//取得电表帧
				byte[] datas= HexDump.toByteBuffer(data).array();
				if((datas!=null) && (datas.length>0)){											        				        	
		        	//获取表规约,因为上行报文不带测量点号,所以解析结果不带测量点号
		        	String pm=getMeterProtocol(datas,0,datas.length);			        	
		         	IMeterParser mparser=MeterParserFactory.getMeterParser(pm);  
		        	if(mparser==null){
		        		throw new MessageDecodeException("不支持的表规约："+pm);
		        	}
		        	if(!pm.equals(Protocol.HX645)){
		        		BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
		        		String meterAddr="";
		        		MeasuredPoint mp=null;
			        	Object[] dis=mparser.parser(datas,0,datas.length,rtu);
			        	if((dis!=null) && (dis.length>0)){		//过滤结果集--方法待改进
			        		for(int i=0;i<dis.length;i++){
			        			DataItem di=(DataItem)dis[i];
			        			String key=(String)di.getProperty("datakey");
			        			if(key==null || key.length()<4){
			        				continue;
			        			}
			        			if(key.equals("8902")){
			        				meterAddr=di.getProperty("value").toString();
			        				mp=rtu.getMeasuredPointByTnAddr(meterAddr);
			        				continue;
			        			}
			        			boolean called=true;
			        			//继电器操作，为写,
			        			if(setOpKey.contains(key)){
			        				if(key.equals("0500000701")){//如果是对时返回，是set
			        					if(di.getProperty("value").toString().length()==2){
				        					hc.setSetTag(true);
			        					}
			        				}else{
			        					hc.setSetTag(true);
			        				}
			        			}
			        			if(called){
			        				HostCommandResult hcr=new HostCommandResult();
		    						hcr.setCode(key);
		    						hcr.setTn(mp.getTn());
		    						if(di.getProperty("value")==null){
		    							hcr.setValue(null);
		    						}else{
		    							if(key.equals("0400102603")){//电表状态字  1路门节点+2路门接点+继电器状态
		    								String meterStatus=di.getProperty("value").toString();
		    								meterStatus=meterStatus.substring(2, 3)+"#"+meterStatus.substring(3, 4)+"#"+meterStatus.substring(9, 10);
		    								hcr.setValue(meterStatus);
		    							}else{
		    								hcr.setValue(di.getProperty("value").toString());
		    							}
		    							
		    						}	        						
		    						hcr.setCommandId(hc.getId());
		    						value.add(hcr);
			        			}
			        		}
			        	}
		        	}else{
		        		HostCommandResult hcr = new HostCommandResult();
		        		hcr.setCode("10F001");
		        		HX645MeterFrame hx645Frame=new HX645MeterFrame();
						hx645Frame.parse(datas, 0, datas.length);
						hc.setSetTag(hx645Frame.isSetFlag());
						hcr.setValue(hx645Frame.getValue());
		        		BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
		        		MeasuredPoint mp = rtu.getMeasuredPointByTnAddr(hx645Frame.getMeteraddr());
		        		if(mp==null){
		        			throw new RuntimeException("not this measure point :"+hx645Frame.getMeteraddr());
		        		}
		        		hcr.setTn(mp.getTn());
		        		value.add(hcr);
		        	}
		         	hc.setStatus(HostCommand.STATUS_SUCCESS);
		        	hc.setResults(value);
				}else{
					//数据区错误认为终端与表计无通讯						
					hc.setStatus(HostCommand.STATUS_RTUAMTCOM_ERROR);
					hc.setResults(null);
				}
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
		if(bbFrame.getDatalen()>=0)//数据区有为0的情况
			protocol=Protocol.BBMeter97;
		else{
			ZjMeterFrame zjFrame=new ZjMeterFrame();
			zjFrame.parse(data, loc, len);
			if (zjFrame.getDatalen()>0){
				protocol=Protocol.ZJMeter;
			}
			if("".equals(protocol)){
				HX645MeterFrame hx645Frame=new HX645MeterFrame();
				hx645Frame.parse(data, loc, len);
				if(hx645Frame.getDatalen()>0){
					protocol = Protocol.HX645;
				}
			}
		}
    	return protocol;
	}
}
