package cn.hexing.fas.protocol.zj.ggcodec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageEncoder;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.util.HexDump;

/**
 *	集中器读取各个表的日冻结数据(功能码：12H)响应消息编码器
 * @author Administrator
 *
 */
public class C12MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C12MessageEncoder.class);

    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalGGKZM12Request){	
        		FaalGGKZM12Request para=(FaalGGKZM12Request)obj;
        		rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
		        	//主站下发的时间是公历 需要转换为伊朗历下发
//		        	SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					String startTime=df.format(para.getSrartTime());
//					String endTime=df.format(para.getEndTime());
//					String date=DateConvert.gregorianToIran(endTime);
//					SimpleDateFormat sdf=new  SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//					Date ddate=sdf.parse(date);
					SimpleDateFormat sf= new SimpleDateFormat("yyMMdd");
					String date=sf.format(para.getEndTime());
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			String taskNo=para.getTaskNo();
	        			if(frp.getRtuId()==null){
	        				log.info("rtuId="+frp.getRtuId());
				        	continue;
				        }
				        if(frp.getCmdId()==null){
				        	log.info("cmdId="+frp.getCmdId());
				        	continue;
				        }
				        BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
				        if(rtu==null){
			        		log.info("rtu can not find in cache,rtuId=："+frp.getRtuId());
			        		continue;
			        	}	
				        String code="",name="";
				        for(int i=0;i<frp.getParams().size();i++){	
				        	name=((FaalRequestParam)frp.getParams().get(i)).getName();
				        	
			        		ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(name);
			        		if (pdc!=null){
			        			if (pdc.getParentCode()!=null)	//测量点数据,主站使用内部标识
			        				code=code+pdc.getParentCode();			        		
			        		}	
			        		log.info("name="+name+",code="+code);
				        }
				        String data=code+"0000FFFF"+date;
				        log.info("FaalGGKZM12Request encode data="+data);			    
				        MessageZjHead head=createHead(rtu);			        						        						        		
	        			head.dlen=(short)(data.length()/2);
				        MessageZj msg=new MessageZj();
				        msg.setCmdId(frp.getCmdId());						        
				        msg.data=HexDump.toByteBuffer(data);
				        msg.head=head;
				        msg.setMsgCount(1);
				        rt.add(msg);
	        		}
		        }
		        else{
	        		throw new MessageEncodeException("未传值");
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
        head.c_func=(byte)0x12;	//功能码
        head.rtua=rtu.getRtua();       
        head.iseq=0;	//帧内序号
        return head;
    }
}
