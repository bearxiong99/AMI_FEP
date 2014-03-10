package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalRequestParam;
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
 * 一类数据(功能码：0CH)下行消息编码器
 * 
 */
public class C0CMessageEncoder extends AbstractMessageEncoder{
	
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();
		try{
			if(obj instanceof FaalRequest){
				FaalGWNoParamRequest request=(FaalGWNoParamRequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sdata="",tp="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
					String codes="";
					for (FaalRequestParam pm:params){						
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());							
						if (pdc==null){//如果找不到，则用通配符查找
							if (!pm.getName().substring(0,2).equals("0C")&&pm.getName().length()==10)//小项查询
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
					String[] sDADTList=DataItemCoder.getCodeFromNToN(tn,codeList);
					for (int i=0;i<sDADTList.length;i++)
						sdata=sdata+sDADTList[i];
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
					if(rtu==null){
						throw new MessageEncodeException("终端信息未在缓存列表："+rp.getRtuId());
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
