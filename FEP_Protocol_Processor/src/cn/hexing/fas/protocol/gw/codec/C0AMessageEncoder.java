package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN0ARequest;
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
 * ��ѯ����(�����룺0AH)������Ϣ������
 * 
 */
public class C0AMessageEncoder extends AbstractMessageEncoder{
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();
		try{
			if(obj instanceof FaalRequest){
				FaalGWAFN0ARequest request=(FaalGWAFN0ARequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sdata="",tp="",param="";
				//ʱ���ǩ������֡������+֡����ʱ��+��������ʱʱ��
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				if (request.getParam()!=null&&request.getParam().length>=1){//������ѯ������Ҫ�Ĵ���Ķ����б�
					param=DataItemCoder.constructor(""+request.getParam().length,"HTB1");//��������
					for(int i=0;i<request.getParam().length;i++)
						param=param+DataItemCoder.constructor(""+request.getParam()[i],"HTB1");
				}
				
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
					String codes="";
					for (FaalRequestParam pm:params){						
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());
						if (pm.getName().equals("04F038")||pm.getName().equals("04F039")){//��������б���Ҫ���⴦�����������ζ���һ���û������{
							if (!param.equals("")){
								param=DataItemCoder.constructor(""+request.getParam()[0],"HTB1");//�û������
								param=param+DataItemCoder.constructor(""+(request.getParam().length-1),"HTB1");//��������
								for(int i=1;i<request.getParam().length;i++)
									param=param+DataItemCoder.constructor(""+request.getParam()[i],"HTB1");
							}
						}else if(pm.getName().equals("04F033") || pm.getName().equals("04F011")
							   ||pm.getName().equals("04F013") || pm.getName().equals("04F014")
							   ||pm.getName().equals("04F015") || pm.getName().equals("04F034")){
							param = DataItemCoder.constructor(""+(request.getParam().length), "HTB1"); //��ѯ��������
							for(int i=0;i<request.getParam().length;i++){
								param=param+DataItemCoder.constructor(""+request.getParam()[i],"HTB1");
							}
							
							
						}else if (pm.getName().equals("04F010")){
							if (!param.equals("")){
								param=DataItemCoder.constructor(""+request.getParam().length,"HTB2");//��������
								for(int i=0;i<request.getParam().length;i++)
									param=param+DataItemCoder.constructor(""+request.getParam()[i],"HTB2");
							}
						}
						else if (pm.getName().equals("04F170")||pm.getName().equals("04F171")
							   ||pm.getName().equals("04F172")||pm.getName().equals("04F173")
							   ||pm.getName().equals("04F174")||pm.getName().equals("04F175")
							   ||pm.getName().equals("04F176")){//�ֳ�pda��������
							if (request.getParam()!=null&&request.getParam().length==2){//������ѯ������Ҫ�Ĵ���Ķ����б�
								param=DataItemCoder.constructor(""+request.getParam()[0],"HTB2");
								param=param+DataItemCoder.constructor(""+request.getParam()[1],"HTB1");
							}
						}
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
					if(rtu==null){
						throw new MessageEncodeException("�ն���Ϣδ�ڻ����б�"+ParseTool.IntToHex4(rtu.getRtua()));
					}
					MessageGwHead head=new MessageGwHead();
					//���ñ���ͷ�����Ϣ
					head.rtua=rtu.getRtua();
				
					MessageGw msg=new MessageGw();
					msg.head=head;
					msg.setAFN((byte)request.getType());
					msg.data=HexDump.toByteBuffer(sdata);
					if (!tp.equals(""))//���д�ʱ���ǩ������Aux
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
