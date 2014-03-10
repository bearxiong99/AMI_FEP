package cn.hexing.fas.protocol.zj.codec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGGKZM33Request;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;
/**
 * 网络预付费信息类 （控制码  C=33H） 消息编码器
 * @author Administrator
 *
 */


public class C33MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C33MessageEncoder.class);

    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalGGKZM33Request){	
        		FaalGGKZM33Request para=(FaalGGKZM33Request)obj;
        		rt=new ArrayList<MessageZj>();
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
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
				        String code="",name="",value="",meterAddr="";
				        //  取测量点号
	        			int tn[]=frp.getTn();
			        	//  通过测量点号查找电表通信地址
	        			MeasuredPoint mp=rtu.getMeasuredPoint(String.valueOf(tn[0]));     
	        			meterAddr=DataSwitch.ReverseStringByByte(mp.getTnAddr());
	        			List<FaalRequestParam> parmas = frp.getParams();
	        			for(FaalRequestParam param:parmas){
	        				name = param.getName();
	        				value=DataSwitch.ReverseStringByByte(param.getValue());
	    					ProtocolDataItemConfig pdc = (ProtocolDataItemConfig) super.dataConfig.getDataItemConfig(name);
	    					if (pdc != null) {
	    						if (pdc.getParentCode() != null) // 测量点数据,主站使用内部标识
	    							code = code+ DataSwitch.ReverseStringByByte(pdc.getParentCode());
	    					}
	    					break;
	        			}
				        String data=meterAddr+code+value;
				        log.info("FaalGGKZM33Request encode data="+data);			    
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
        head.c_func=(byte)0x33;	//功能码
        head.rtua=rtu.getRtua();       
        head.iseq=0;	//帧内序号
        return head;
    }
}

