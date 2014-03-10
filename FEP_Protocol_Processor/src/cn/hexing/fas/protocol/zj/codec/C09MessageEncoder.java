package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.model.FaalReadAlertRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fas.protocol.zj.parse.ZjDateAssistant;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.StringUtil;


public class C09MessageEncoder extends AbstractMessageEncoder{
	private static Log log=LogFactory.getLog(C09MessageEncoder.class);
	
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalReadAlertRequest){
				FaalReadAlertRequest para=(FaalReadAlertRequest)obj;				
		        Calendar stime=para.getStartTime();	//召测的数据开始时间 ????	
		        int num=para.getCount();	//召测的项目数
		        //组帧
		        int len=9;
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){			        				        	
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
			        	if(rtu==null){
			        		log.info("终端信息未在缓存列表："+frp.getRtuId());
			        		continue;
			        	}			        	
						for(int p=0;p<frp.getTn().length;p++){
							int point=frp.getTn()[p];
							int alr=ParseTool.HexToDecimal(((FaalRequestParam)frp.getParams().get(0)).getName());
							//帧头数据
							MessageZjHead head=new MessageZjHead();
						    head.c_dir=0;	//主站下发
						    head.c_expflag=0;	//异常码
						    head.c_func=(byte)0x09;	//功能码				        
						    head.rtua=rtu.getRtua();				        
						    head.iseq=0;	//帧内序号				       
						    head.dlen=(short)len;
						    byte[] frame=new byte[len];
						    frame[0]=(byte)point;	//测量点 0xFF表示所有测量点
						    frame[1]=(byte)(alr & 0xff);
						    frame[2]=(byte)((alr & 0xff00)>>>8);
					        ZjDateAssistant.constructDateFrame(frame, 3, stime); //这一句话替换下面5行
//						    frame[3]=ParseTool.IntToBcd(stime.get(Calendar.YEAR)%100);	//year
//						    frame[4]=ParseTool.IntToBcd(stime.get(Calendar.MONTH)+1);	//month
//						    frame[5]=ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH));	//day
//						    frame[6]=ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY));	//hour
//						    frame[7]=ParseTool.IntToBcd(stime.get(Calendar.MINUTE));	//minute	        
						    frame[8]=(byte)num;	//num
						    
						    MessageZj msg=new MessageZj();
						    msg.data=ByteBuffer.wrap(frame);
						    msg.setCmdId(frp.getCmdId());				        
						    msg.head=head;
						    msg.setMsgCount(1);
						    rt.add(msg);
			        	}
			        	
			        	
	        		} 
		        }
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		if(rt!=null){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;  
	}

}
