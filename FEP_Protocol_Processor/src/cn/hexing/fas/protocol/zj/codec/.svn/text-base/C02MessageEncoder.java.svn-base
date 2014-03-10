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
 * @info 读任务数据(02H)
 */
public class C02MessageEncoder extends AbstractMessageEncoder  {
	private static Log log=LogFactory.getLog(C02MessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalReadTaskDataRequest){
				FaalReadTaskDataRequest para=(FaalReadTaskDataRequest)obj;				
		        Calendar ptime=Calendar.getInstance();	//召测的历史数据开始时间
		        ptime.setTime(para.getStartTime());		        
		        int num=para.getCount();	//召测点数
		        int taskno=Integer.parseInt(para.getTaskNum());	//任务号
		        int rate=para.getFrequence();		//上报数据点间隔与采样数据点间隔的倍率		      
		        //组帧
		        int len=8;
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			Calendar stime=Calendar.getInstance();	//召测的历史数据开始时间
				        stime.setTime(ptime.getTime());
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));	        	
			        	if(rtu==null){
			        		//未找到终端信息
			        		log.info("终端信息未在缓存列表："+frp.getRtuId());
			        		continue;
			        	}			        				        
				        //get task setting
				        TaskSetting ts=new TaskSetting(rtu.getRtua(),taskno,super.dataConfig);
				        if(ts==null||ts.getRtu()==null){
				        	log.info("终端未建档！终端--"+ParseTool.IntToHex4(rtu.getRtua())+" 任务号--"+String.valueOf(taskno));
				        	continue;
				        }
				        else if (ts.getRtask()==null){
				        	log.info("终端任务未配置！终端--"+ParseTool.IntToHex4(rtu.getRtua())+" 任务号--"+String.valueOf(taskno));
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
				        		throw new MessageEncodeException("错误的终端任务配置--"+ts.getDataCodes());
				        	}
				        	tsbytes+=pdc.getLength();
				        }
				        if(tsbytes<=0){
				        	log.info("数据描述错误，请检查浙江规约数据集！");
				        	continue;
				        }
				        //get frame capacity
				        int datamax=DataItemCoder.getDataMax(rtu);	//终端每帧数据最大值
				        
				        int pointnum=datamax/tsbytes;	//一帧最多应答数据点数
				        if(pointnum<=0){
				        	pointnum=1;	//无法一帧召测，一定是帧长配置过低，以后要按厂家配置
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
				        	
				        	//帧头数据
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
								//时间间隔为月
								stime.add(Calendar.MONTH,realp*tsn*rate);
							}
				        	curnum-=pointnum;
				        }
			        	//每个报文设置此次单终端组帧总数
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
    	//    	帧头数据
		MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//主站下发
        head.c_expflag=0;	//异常码
        head.c_func=(byte)0x02;	//功能码        
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//帧内序号
        //head.fseq		//帧序号???????
        //head.msta=	//主站地址?????
        return head;
    }
	
	/**
     * 时间间隔（统一单位为分，月比较特殊，单独处理，见解析过程）
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
