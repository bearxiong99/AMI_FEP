package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN0ERequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

/**
 * 三类数据(功能码：0EH)下行消息编码器
 * 
 */
public class C0EMessageEncoder extends AbstractMessageEncoder{
	
	public IMessage[] encode(Object obj){
		List<MessageGw> rt=new ArrayList<MessageGw>();
		try{
			if(obj instanceof FaalRequest){
				FaalGWAFN0ERequest request=(FaalGWAFN0ERequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sdata="",tp="",param="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				if (request.getPm()>=0&&request.getPn()>=0){//事件起始指针和事件结束指针
					param=DataItemCoder.constructor(""+request.getPm(),"HTB1")+DataItemCoder.constructor(""+request.getPn(),"HTB1");			
				}				
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
					String codes="";
					for (FaalRequestParam pm:params){
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());							
						if (codes.indexOf(pdc.getParentCode())<0)
							codes=codes+","+pdc.getParentCode();
					}
					if (codes.startsWith(","))
						codes=codes.substring(1);
					String[] codeList=codes.split(",");
					String[] sDADTList=DataItemCoder.getCodeFromNToN(tn,codeList);
					for (int i=0;i<sDADTList.length;i++)
						sdata=sdata+sDADTList[i]+param;
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
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
