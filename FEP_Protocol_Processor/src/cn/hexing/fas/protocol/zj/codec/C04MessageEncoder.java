package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalReadProgramLogRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.protocol.zj.parse.ZjDateAssistant;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.StringUtil;


public class C04MessageEncoder  extends AbstractMessageEncoder {
	private static final Logger log = Logger.getLogger(C04MessageEncoder.class);

	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;
		
		try{
			if(obj instanceof FaalRequest){
				FaalReadProgramLogRequest para=(FaalReadProgramLogRequest)obj;
				
		        Calendar stime;//=Calendar.getInstance();	//召测的编程数据开始时间
		        stime=para.getStartTime();
		        if(stime==null){
		        	stime=Calendar.getInstance();
		        }
		        int point=Integer.parseInt(para.getTn());	//测量点号
		        int num=para.getCount();	//召测的编程项目数		        
		        //组帧
		        int len=7;	        
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
			        	//帧头数据
			        	MessageZjHead head=new MessageZjHead();
				        head.c_dir=0;	//主站下发
				        head.c_expflag=0;	//异常码
				        head.c_func=(byte)0x04;	//功能码
				        head.rtua=rtu.getRtua();
				        
				        head.iseq=0;	//帧内序号
				        head.dlen=(short)len;
				        byte[] frame=new byte[len];
				        frame[0]=(byte)point;	//测量点 0xfe表示所有测量点 0xff表示所有测量点和终端
//				        frame[1]=ParseTool.IntToBcd(stime.get(Calendar.YEAR)%100);	//year
//				        frame[2]=ParseTool.IntToBcd(stime.get(Calendar.MONTH)+1);	//month
//				        frame[3]=ParseTool.IntToBcd(stime.get(Calendar.DAY_OF_MONTH));	//day
//				        frame[4]=ParseTool.IntToBcd(stime.get(Calendar.HOUR_OF_DAY));	//hour
//				        frame[5]=ParseTool.IntToBcd(stime.get(Calendar.MINUTE));	//minute
				        ZjDateAssistant.constructDateFrame(frame, 1, stime);
				        frame[6]=(byte)num;	//num
				        
				        MessageZj msg=new MessageZj();
				        msg.data=ByteBuffer.wrap(frame);
				        HostCommand hcmd=new HostCommand();
				        hcmd.setId(frp.getCmdId()); 
				        hcmd.setMessageCount(1);				        
				        msg.setCmdId(hcmd.getId());
				        msg.head=head;
				        rt.add(msg);
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
