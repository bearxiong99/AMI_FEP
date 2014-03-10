package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fas.model.FaalReadTaskDataRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.parse.DataItemCoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fas.protocol.zj.parse.TaskSetting;

/**
 * @info ����������(02H)
 */
public class C02MessageEncoder extends AbstractMessageEncoder  {
	private static Log log=LogFactory.getLog(C02MessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalReadTaskDataRequest){
				FaalReadTaskDataRequest para=(FaalReadTaskDataRequest)obj;				
		        Calendar ptime=Calendar.getInstance();	//�ٲ����ʷ���ݿ�ʼʱ��
		        ptime.setTime(para.getStartTime());		        
		        int num=para.getCount();	//�ٲ����
		        int taskno=Integer.parseInt(para.getTaskNum());	//�����
		        int rate=para.getFrequence();		//�ϱ����ݵ�����������ݵ����ı���		      
		        //��֡
		        int len=8;
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			Calendar stime=Calendar.getInstance();	//�ٲ����ʷ���ݿ�ʼʱ��
				        stime.setTime(ptime.getTime());
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));	        	
			        	if(rtu==null){
			        		//δ�ҵ��ն���Ϣ
			        		log.info("�ն���Ϣδ�ڻ����б�"+frp.getRtuId());
			        		continue;
			        	}			        				        
				        //get task setting
				        TaskSetting ts=new TaskSetting(rtu.getRtua(),taskno,super.dataConfig);
				        if(ts==null||ts.getRtu()==null){
				        	log.info("�ն�δ�������ն�--"+ParseTool.IntToHex4(rtu.getRtua())+" �����--"+String.valueOf(taskno));
				        	continue;
				        }
				        else if (ts.getRtask()==null){
				        	log.info("�ն�����δ���ã��ն�--"+ParseTool.IntToHex4(rtu.getRtua())+" �����--"+String.valueOf(taskno));
				        	continue;
				        }
				        int tsu=ts.getTIUnit();
				        int tsn=ts.getTI();
				        //get how many bytes in one point of data
				        List datacs=ts.getDI();
				        int tsbytes=0;
				        for(int guard=0;guard<datacs.size();guard++){
				        	ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)datacs.get(guard);
				        	if(pdc==null){
				        		throw new MessageEncodeException("������ն���������--"+ts.getDataCodes());
				        	}
				        	tsbytes+=pdc.getLength();
				        }
				        if(tsbytes<=0){
				        	log.info("�����������������㽭��Լ���ݼ���");
				        	continue;
				        }
				        //get frame capacity
				        int datamax=DataItemCoder.getDataMax(rtu);	//�ն�ÿ֡�������ֵ
				        
				        int pointnum=datamax/tsbytes;	//һ֡���Ӧ�����ݵ���
				        if(pointnum<=0){
				        	pointnum=1;	//�޷�һ֡�ٲ⣬һ����֡�����ù��ͣ��Ժ�Ҫ����������
				        }
				        int curnum=num;				        
				        int msgcount=0;	                 
				        while(curnum>0){
				        	int realp=0;
				        	if(curnum>pointnum){
				        		realp=pointnum;
				        	}else{
				        		realp=curnum;
				        	}
				        	
				        	//֡ͷ����
				        	MessageZjHead head=createHead(rtu);
					        byte[] frame=new byte[len];
					        frame[0]=(byte)taskno;	//task no
					        frame[1]=ParseTool.IntToBcd(stime.get(Calendar.YEAR)%100);	//year
					        frame[2]=ParseTool.IntToBcd(stime.get(Calendar.MONTH)+1);	//month
					        frame[3]=ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH));	//day
					        frame[4]=ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY));	//hour
					        frame[5]=ParseTool.IntToBcd(stime.get(Calendar.MINUTE));	//minute	        
					        frame[6]=(byte)realp;
					        frame[7]=(byte)rate;	//rate
					        
					        MessageZj msg=new MessageZj();
					        msg.data=ByteBuffer.wrap(frame);
	                        msg.head=head;
	                        rt.add(msg);	                        
					        msg.setCmdId(frp.getCmdId());
					       	msgcount++;
				        	int ti=getTimeInterval((byte)tsu);
				        	if(ti<=1440){
								stime.add(Calendar.MINUTE,ti*tsn*realp*rate);
							}else{
								//ʱ����Ϊ��
								stime.add(Calendar.MONTH,realp*tsn*rate);
							}
				        	curnum-=pointnum;
				        }
			        	//ÿ���������ô˴ε��ն���֡����
			        	setMsgcount(rt,msgcount);
	        		}
		        }		         
			}
		}catch(Exception e){
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
        head.c_func=(byte)0x02;	//������        
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//֡�����
        //head.fseq		//֡���???????
        //head.msta=	//��վ��ַ?????
        return head;
    }
	
	/**
     * ʱ������ͳһ��λΪ�֣��±Ƚ����⣬�����������������̣�
     * @param type
     * @return
     */
    private int getTimeInterval(byte type){
    	int rt=0;
    	switch(type){
	    	case 2:
	    		rt=1;
	    		break;
	    	case 3:
	    		rt=60;
	    		break;
	    	case 4:
	    		rt=1440;
	    		break;
	    	case 5:
	    		rt=43200;
	    		break;
	    	default:
	    		break;
    	}    	
    	return rt;
    }
    private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
