package cn.hexing.fk.bp.dlms.time;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.BUSINESS_CATEGORY;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.persisit.MasterDbServiceAssistant;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.utils.DateConvert;
/**
 * 	自动对时组req
 * @author Administrator
 *
 */


public class TimeAutomatic {
	
	//DLMS 规约自动对时请求req
	public static void timeAutomatic(DlmsEventProcessor processor, 
			DlmsRequest request) {
		boolean isDaySaving = false;
		if(request.containsKey("isDaySaving")){
			isDaySaving =  (Boolean) request.getAppendParam("isDaySaving");			
		}
		boolean isIranTime = DlmsConstant.getInstance().isIranTime;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=new Date();
		String sdate="";
		if(isIranTime){
			 sdate = DateConvert.gregorianToIran(sdf.format(date));
		}else{
			 sdate=sdf.format(date);
		}
		DlmsRequest req= new DlmsRequest();
		DlmsObisItem[] params =new DlmsObisItem[1];
		params[0] = new DlmsObisItem();
		params[0].classId = 8;
		params[0].obisString = "0.0.1.0.0.255";
		params[0].attributeId = 2;
		params[0].data.setDlmsDateTime(sdate,isDaySaving);
		req.setParams(params);
		req.setProtocol("03");
		req.setOperator("TimeSyn");
		req.setOpType(DLMS_OP_TYPE.OP_SET);
		if(request.isRelay()){
			req.setRelayParam(request.getRelayParam());
			req.setCategory(BUSINESS_CATEGORY.BIZ_RELAY);
		}else{
		req.setMeterId(MasterDbServiceAssistant.getInstance().getRtuId(request));
		}
		processor.postWebRequest(req, null);
	}
	
	
	
	
	
	
	
	
	
}
