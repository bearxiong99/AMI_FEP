package cn.hexing.fas.protocol.gw.codec;


import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

/**
 * 设置参数(功能码：04H)下行消息编码器
 * 
 */
public class C04MessageEncoder  extends AbstractMessageEncoder {
	@SuppressWarnings("unused")
	private static Log log=LogFactory.getLog(C04MessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();		
		try{
			if(obj instanceof FaalRequest){
				FaalGWNoParamRequest request=(FaalGWNoParamRequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sDADT="",sValue="",sdata="",tp="",pw="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
					for (int i=0;i<tn.length;i++){
						for (FaalRequestParam pm:params){
							if (pm.getName().equals("04F010") //主站把F10作为测量点参数处理，下发测量点为实际测量点号，需要特殊处理
									|| pm.getName().substring(0, 2).equals("C0"))//固件升级作为特殊处理
								tn[i]=0;
							sDADT=DataItemCoder.getCodeFrom1To1(tn[i],pm.getName());//数据单元标识
							ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());
							sValue=DataItemCoder.coder(pm.getValue(),pdc.getFormat());//数据值
							sdata=sdata+sDADT+sValue;
						} 
					}
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
					if(rtu==null){
						throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
					}
					if (rtu.getHiAuthPassword()!=null&&rtu.getHiAuthPassword().length()==32)
						pw=DataSwitch.ReverseStringByByte(rtu.getHiAuthPassword());//终端密码
					else{
						log.warn("Terminal "+rp.getRtuId()+" hiAuthPassword is null,use default password.");
						pw="00000000000000000000000000000000";
					}
					MessageGwHead head=new MessageGwHead();
					//设置报文头相关信息
					head.rtua=rtu.getRtua();
					
					MessageGw msg=new MessageGw();
					msg.head=head;
					msg.setAFN((byte)request.getType());
					msg.data=HexDump.toByteBuffer(sdata+pw);
					if (!tp.equals(""))//下行带时间标签则设置Aux
						msg.setAux(HexDump.toByteBuffer(tp), true);
					msg.setCmdId(rp.getCmdId());
					msg.setMsgCount(1);
					rt.add(msg);
				}				
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null&&rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;  
	}
}	
