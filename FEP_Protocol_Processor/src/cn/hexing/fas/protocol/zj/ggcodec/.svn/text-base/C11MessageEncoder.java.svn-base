package cn.hexing.fas.protocol.zj.ggcodec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGGKZM11Request;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageEncoder;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.DateConvert;
import cn.hexing.util.HexDump;

/**
 * 集中器抄日冻结数据(功能码：11H)消息编码器
 */
public class C11MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C11MessageEncoder.class);

    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalGGKZM11Request){	
        		FaalGGKZM11Request para=(FaalGGKZM11Request)obj;
        		rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
		        	String meterStartNo=DataItemCoder.constructor(""+para.getMeterNo(),"HTB2");
		        	if(para.getDataTime()==null){
		        		para.setDataTime(new Date());
		        	}
					//读取日冻结 主站下发的时间是伊朗历，如果不是伊朗历得转换
//					String time=DateConvert.iranToGregorian(dataTime);
					SimpleDateFormat sd=new SimpleDateFormat("ddMMyy");
					String dataTime=sd.format(para.getDataTime());
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
				        //  取测量点号
	        			int tn[]=frp.getTn();
			        	String meterAddr="";
			        	//  通过测量点号查找电表通信地址
	        			MeasuredPoint mp=rtu.getMeasuredPoint(String.valueOf(tn[0]));     
	        			meterAddr=DataSwitch.ReverseStringByByte(mp.getTnAddr());
	        			String Tn=DataSwitch.ReverseStringByByte("0000".substring(mp.getTn().length())+mp.getTn());
				        String code="",name="",data="";boolean isFreeze=false;//要求将点抄日月冻结而和点抄其他的分开
				        for(int i=0;i<frp.getParams().size();i++){	
				        	name=((FaalRequestParam)frp.getParams().get(i)).getName();	
				        	ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(name);
				        	int iCode=Integer.parseInt(pdc.getParentCode(),16);
				        	if(iCode<37520&&iCode>37391){
				        		isFreeze=true;
				        	}
			        		if (pdc!=null){
			        			if (pdc.getParentCode()!=null)	//测量点数据,主站使用内部标识
			        				code=code+DataSwitch.ReverseStringByByte(pdc.getParentCode());			        		
			        		}	
			        		log.info("name="+name+",code="+code);
					        if(isFreeze){
					        	 data =meterAddr+Tn+"01"+dataTime+"000000"+"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"+code;
					        }else{
					        	 data =meterAddr+Tn+"01"+"FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF"+code;
					        }
					        log.info("FaalGGKZM11Request encode data="+data);			    
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
        head.c_func=(byte)0x11;	//功能码
        head.rtua=rtu.getRtua();       
        head.iseq=0;	//帧内序号
        return head;
    }
}
