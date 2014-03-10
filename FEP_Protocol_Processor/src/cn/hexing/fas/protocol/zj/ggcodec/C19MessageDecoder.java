package cn.hexing.fas.protocol.zj.ggcodec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;

/**
 * 事件告警数据（C=19H）解码
 * @author Administrator
 * @time 2012年12月11日10
 * 
 */
public class C19MessageDecoder  extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C19MessageDecoder.class);

    public Object decode(IMessage message) {
    	List<RtuAlert> rt=new ArrayList<RtuAlert>();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//是终端应答
        		//应答类型
        		int rtype=(ParseTool.getErrCode(message));  
    			BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//正常终端应答
        			//取应答数据
        			String data=ParseTool.getDataString(message);
        			// 取告警数量（ALRN）1字节16进制
        			int alrnNumber=Integer.parseInt(data.substring(0, 2));
        			data=data.substring(2);	//消去告警数量1个字节
        			log.info("C19MessageDecoder 事件告警数量="+alrnNumber);
        			if(alrnNumber==0){
        				log.info("没有事件需要处理。。。");
        			}
        			if(data.length()%66!=0){
        				log.info("帧长度不符合规范");
        				return null;
        			}
        			for(int i =0;i<alrnNumber;i++){
        				//获取电表编号
        				String meterNo= DataSwitch.ReverseStringByByte(data.substring(0, 12));
        				//通过meterNo查找测量点号
        				MeasuredPoint mp=rtu.getMeasuredPointByTnAddr(meterNo);
            			data=data.substring(12);//消去电表通信地址6个字节
            			String stime="20"+data.substring(0, 10);
            			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            			Date date=sdf.parse(stime);
            			data=data.substring(10);//消去时间6个字节
            			String alertCode=DataSwitch.ReverseStringByByte(data.substring(0, 4));
            			RtuAlert ra=new RtuAlert();	 
            			ra.setAlertCode(Integer.parseInt(alertCode,16));
            			ra.setAlertTime(date);
            			ra.setDataSaveID(mp.getDataSaveID());
            			ra.setTn(mp.getTn());
            			ra.setReceiveTime(new Date(((MessageZj) message).getIoTime()));
            			ra.setCorpNo(rtu.getDeptCode());
            			ra.setRtuId(rtu.getRtuId());
            			ra.setCustomerNo(mp.getCustomerNo());
            			ra.setStationNo(mp.getTn());
            			data=data.substring(44);//消去2个字节的编码和20字节的内容
            			rt.add(ra);
        			}
        		}else{
        			//异常应答
        		}
        	}
    		else{
    			//事件告警的主动上报处理
    		}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }     
        return rt;
    }      

}
