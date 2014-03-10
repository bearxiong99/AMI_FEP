package cn.hexing.fas.protocol.zj.codec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGGKZM19Request;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

/**
 * 事件告警（C=19H）编码
 * @author Administrator
 *
 */
public class C19MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C19MessageEncoder.class);

    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalGGKZM19Request){	
        		FaalGGKZM19Request para=(FaalGGKZM19Request)obj;
        		rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
		        	SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
					String startTime=df.format(para.getSrartTime());
					String endTime=df.format(para.getEndTime());
					String eventNum = HexDump.toHex((byte)para.getEventNum());
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			List<String> eventIDs=para.getEventIDs();
	        			for(String eventID : eventIDs){
	        				eventID=DataSwitch.ReverseStringByByte(eventID);
		        			if(eventID.equals("")){
		        				eventID="FFFF";
		        			}
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
					        String data=startTime+endTime+eventNum+eventID;
					        log.info("FaalGGKZM19Request encode data="+data);			    
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
        head.c_func=(byte)0x19;	//功能码
        head.rtua=rtu.getRtua();       
        head.iseq=0;	//帧内序号
        return head;
    }
}
