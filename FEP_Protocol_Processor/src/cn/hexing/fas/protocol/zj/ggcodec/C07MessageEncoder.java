package cn.hexing.fas.protocol.zj.ggcodec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalRealTimeWriteParamsRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageEncoder;
import cn.hexing.fas.protocol.zj.parse.DataItemCoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;

/**
 * @info ʱ����д����(07H)
 * @author Administrator
 * @time 2012��12��3��
 */
public class C07MessageEncoder extends AbstractMessageEncoder{
	private final Log log=LogFactory.getLog(C07MessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalRealTimeWriteParamsRequest){
				FaalRealTimeWriteParamsRequest para=(FaalRealTimeWriteParamsRequest)obj;
		        byte rights=0x11;	//Ȩ�޵ȼ� 0x00�ͼ�Ȩ�� 0x11 �߼�Ȩ��		        		        		        
		        byte[] rowdata=new byte[2048];
		        byte[] rowdataHL=new byte[2048];	//�������⴦��,��APN
		        byte[] rowdataHLi=new byte[2048];	//�������⴦��,��APN		        		        
		        Calendar time=para.getCmdTime();	//����ʱ��ʱ��
		        if(time==null){
		        	time=Calendar.getInstance();
		        }
		        int wt=para.getTimeout();			//��ʱʱ��		        		        	        
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){			        	
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
			        	byte[] fdata=null;			        	
			        	if(rtu==null){
			        		log.info("�ն���Ϣȱʧ���ն�ID--"+rtu.getRtuId());
			        		continue;
			        	}
			        	for(int p=0;p<frp.getTn().length;p++){
			        		int point=frp.getTn()[p];
			        		int loc=0;
					        rowdata[0]=(byte)point;
					        rowdata[1]=rights;
					        rowdata[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdata[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdata[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdata[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdata[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdata[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        rowdataHL[0]=(byte)point;
					        rowdataHL[1]=rights;
					        rowdataHL[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdataHL[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdataHL[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdataHL[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdataHL[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdataHL[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        rowdataHLi[0]=(byte)point;
					        rowdataHLi[1]=rights;
					        rowdataHLi[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdataHLi[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdataHLi[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdataHLi[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdataHLi[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdataHLi[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        loc=11;
					        
					        int index=0;	
					        int[] itemlen=new int[frp.getParams().size()];
					        int[] keysinpara=new int[frp.getParams().size()];
					        String[] valsinpara=new String[frp.getParams().size()];
				        	for(int i=0;i<frp.getParams().size();i++){
				        		FaalRequestParam fp=(FaalRequestParam)frp.getParams().get(i);
				        		ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(fp.getName());
					        	if(pdc!=null){	//֧�ֵĲ���
				        			rowdata[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdata[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			rowdataHL[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdataHL[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			rowdataHLi[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdataHLi[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			loc+=2;
				        			int dlen=DataItemCoder.coder(rowdata,loc,fp,pdc);
						        	if(dlen<=0){
						        		//����Ĳ���
						        		throw new MessageEncodeException(fp.getName(),"����Ĳ���:"+fp.getName()+"---"+fp.getValue());
						        	}
						        	
						        	if((pdc.getDataKey() & 0xffff)==0x8015){//apn���⴦��
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);	//����
						        		int zi=16;	//��0����
						        		int si=loc+15;
						        		for(int k=0;k<16;k++){
						        			if((rowdata[si] & 0xff)==0x0){
						        				rowdataHL[loc+k]=0;
						        				rowdataHLi[si]=(byte)0xAA;
						        				si--;
						        				zi--;
						        			}else{				        				
						        				break;
						        			}
						        		}
						        		if(zi>0){
						        			System.arraycopy(rowdata,loc,rowdataHL,loc+16-zi,zi);			        			
						        		}
						        	}else if((pdc.getDataKey() & 0xffff)==0x8902){//���ַ
						        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);	
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
						        		int zi=6;	//��AA����
						        		int si=loc+5;
						        		for(int k=0;k<6;k++){
						        			if((rowdata[si] & 0xff)==0xAA){
						        				rowdataHLi[si]=0;
						        				si--;
						        				zi--;
						        			}else{				        				
						        				break;
						        			}
						        		}
						        	}else{
						        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
						        	}
						        	
						        	itemlen[index]=dlen;
			                        keysinpara[index]=pdc.getDataKey();
			                        valsinpara[index]=fp.getValue();
						        	index++;
				        			loc+=dlen;		        		
					        	}else{
					        		throw new MessageEncodeException(fp.getName(),"�����޷���ȡ�������"+fp.getName());
					        	}				     			        	
				        	}
				        	
				        	//���ұ�������¡11->0087,ǧ��18->0112,�˴�33->0094,��ʤ27->0117,����13->0061,��ҵ31->0098
				        	if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("0087") || rtu.getManufacturer().equalsIgnoreCase("0112") || rtu.getManufacturer().equalsIgnoreCase("0094")|| rtu.getManufacturer().equalsIgnoreCase("0117"))){	//��HL
				        		fdata=rowdataHL;
				        	}else if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("0061") || rtu.getManufacturer().equalsIgnoreCase("0098"))){
				        		fdata=rowdataHLi;
				        	}
				        	else{
				        		fdata=rowdata;
				        	}
				        	String pwd=rtu.getHiAuthPassword();
				        	if(pwd==null){
				        		log.info("�ն�����ȱʧ���ն�ID--"+rtu.getRtuId());
				        		continue;
				        	}		        	
				        	int datamax=DataItemCoder.getDataMax(rtu);	//�ն�ÿ֡�������ֵ
					        int msgcount=0;			        
			        		int dnum=0;
			        		int pos=0;
			        		int curlen=0;		        		
			        		for(int j=0;j<itemlen.length;j++){
			        			if((curlen+11+(2+itemlen[j]))>datamax){//����+1������+1Ȩ������+3����+5����ʱ��+1��Чʱ��
			        				MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());							        
							        if(msg!=null){						        	
								        msgcount++;
							        	rt.add(msg);
							        }
			        				pos+=curlen;
							        dnum=1;
							        curlen=2+itemlen[j];
			        			}else{
			        				dnum+=1;
			        				curlen+=(2+itemlen[j]);
			        				if(keysinpara[j]>0x8100 && keysinpara[j]<=0x81FE){//(��������ز������þͷ�֡����ӦһЩ�ն�ֻ֧��һ����������ÿ֡������)
			        					MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());							        
								        if(msg!=null){							        	
									        msgcount++;
								        	rt.add(msg);							        	
								        }
								        dnum=0;
								        pos+=curlen;
								        curlen=0;
			        				}
			        			}
			        		}
			        		if(dnum>0){
			        			MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());
			        			if(msg!=null){
							        msgcount++;
			        				rt.add(msg);
			        			}
			        		}
			        		//ÿ���������ô˴ε��ն���֡����
				        	setMsgcount(rt,msgcount);
			        	}			        	
	        		}
		        }
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
		return null;
	}
	
	private MessageZjHead createHead(BizRtu rtu){
    	//    	֡ͷ����
		MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//��վ�·�
        head.c_expflag=0;	//�쳣��
        head.c_func=(byte)0x07;	//������
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//������ ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//������ ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//�ն˵�ַ
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//֡�����
        //head.fseq		//֡���???????
        //head.msta=	//��վ��ַ?????
        return head;
    }
	
	private MessageZj createMessageZj(byte[] rowdata,BizRtu rtu,int pos,int dlen,Object cmdid){
		//��֡
		MessageZjHead head=createHead(rtu);
    	head.dlen=(short)(dlen+11);
    	
    	byte[] frameA=new byte[head.dlen];
		System.arraycopy(rowdata,0,frameA,0,11);
		System.arraycopy(rowdata,11+pos,frameA,11,dlen);
		
		String pwd=rtu.getHiAuthPassword();
		ParseTool.HexsToBytesAA(frameA,2,pwd,3,(byte)0xAA);
		
		MessageZj msg=new MessageZj();
		msg.setCmdId((Long)cmdid);
		
        msg.data=ByteBuffer.wrap(frameA);
        msg.head=head;
        return msg;
	}
	
	private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
