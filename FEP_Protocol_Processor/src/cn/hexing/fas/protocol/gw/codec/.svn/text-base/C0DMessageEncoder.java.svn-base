package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN0DRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

/**
 * 二类数据(功能码：0DH)下行消息编码器
 * 
 */
public class C0DMessageEncoder extends AbstractMessageEncoder{
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();
		try{
			if(obj instanceof FaalRequest){
				FaalGWAFN0DRequest request=(FaalGWAFN0DRequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sdata="",tp="",date="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				if (request.getStartTime()==null)
					throw new MessageEncodeException("C0DMessageEncoder startTime is null!");
				if (request.getInterval()>0&&request.getCount()>=1){//曲线数据时标
					String interval="";
					switch (request.getInterval()){
						case 1:interval="FF";break;
						case 5:interval="FE";break;
						case 15:interval="01";break;
						case 30:interval="02";break;
						case 60:interval="03";break;
						default: interval="00";
					}						
					date=DataItemCoder.constructor(request.getStartTime(),"A15")+interval+DataItemCoder.constructor(""+request.getCount(),"HTB1");
				}
				else{//冻结时标
//					if(request.getStartTime().trim().length()==10)	//日冻结
//						date=DataItemCoder.constructor(request.getStartTime(),"A20");
//					else									//月冻结
//						date=DataItemCoder.constructor(request.getStartTime(),"A21");
					//这个地方调整,不应该根据时间判断死.现在的想法是根据数据标识判断,在protocol-data-config.xml里获得是日冻结，还是月冻结
					for(FaalRequestRtuParam rp:rtuParams){
						List<FaalRequestParam> params=rp.getParams();
						for(FaalRequestParam pm:params){
							ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());
							if(pdc ==null){
								throw new MessageEncodeException("can not find cmd:"+pm.getName());
							}
							if("日冻结".equals(pdc.getType())){
								date=DataItemCoder.constructor(request.getStartTime(),"A20");
							}else{
								date=DataItemCoder.constructor(request.getStartTime(),"A21");
							}
							if(!"".equals(date)) break;
						}
						if(!"".equals(date)) break;
					}
					
				}
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
					String codes="";
					for (FaalRequestParam pm:params){						
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());							
						if (pdc==null){//如果找不到，则用通配符查找
							if (!pm.getName().substring(0,2).equals("0D")&&pm.getName().length()==10)//小项查询
								pm.setName(pm.getName().substring(0,8)+"XX");//消去数据项编码中的子项,改用XX表示，因为配置文件没有配子项
							pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());
							if (pdc==null)
								throw new MessageEncodeException("can not find cmd:"+pm.getName());
						}					
						if (codes.indexOf(pdc.getParentCode())<0)
							codes=codes+","+pdc.getParentCode();
					}
					if (codes.startsWith(","))
						codes=codes.substring(1);
					String[] codeList=codes.split(",");
					for (int i=0;i<tn.length;i++){//国网宣贯：数据标识带数据单元时，数据标识只能表示唯一的信息点和信息类
						for(int j=0;j<codeList.length;j++){
							sdata=sdata+DataItemCoder.getCodeFrom1To1(tn[i], codeList[j])+date;
						}
					}
					/*String[] sDADTList=DataItemCoder.getCodeFromNToN(tn,codeList);
					for (int i=0;i<sDADTList.length;i++){
						int[] mps=DataItemParser.measuredPointParser(sDADTList[i].substring(0,4));
						codeList=DataItemParser.dataCodeParser(sDADTList[i].substring(4,8), "0D");
						sdata=sdata+sDADTList[i];
						for (int j=0;j<mps.length;j++)			//多个pn
							for (int k=0;k<codeList.length;k++)	//多个fn
								sdata=sdata+date;				//累加时标		
					}*/
						
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
					if(rtu==null){
						throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
					}
					MessageGwHead head=new MessageGwHead();
					//设置报文头相关信息
					head.rtua=rtu.getRtua();
					
					MessageGw msg=new MessageGw();
					msg.head=head;
					msg.setAFN((byte)request.getType());
					msg.data=HexDump.toByteBuffer(sdata);
					if (!tp.equals(""))//下行带时间标签则设置Aux
						msg.setAux(HexDump.toByteBuffer(tp), true);
					msg.setCmdId(rp.getCmdId());
					msg.setMsgCount(1);
					rt.add(msg);
				}				
			}
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null&&rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
		else
			return null;  
	}

}
