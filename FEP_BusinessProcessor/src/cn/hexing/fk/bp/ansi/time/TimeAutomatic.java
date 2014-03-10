package cn.hexing.fk.bp.ansi.time;

import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;

import com.hx.ansi.ansiElements.AnsiDataItem;

public class TimeAutomatic {
	//ANSI 规约自动对时请求req
	public static void timeAutomatic(AnsiEventProcessor processor, 
			AnsiRequest request) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date=new Date();
		String sdate="";
		sdate=sdf.format(date);
		AnsiRequest req =new AnsiRequest();
		req.setMeterId(request.getMeterId());
		req.setProtocol("06");
		req.setOpType(ANSI_OP_TYPE.OP_ACTION);
		req.setTable(7);
		req.setServiceTag("40");
		req.setFull(true);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="00005200";
		datas[0].data=sdate;
		req.setDataItem(datas);
		req.setOperator("TimeSyn");
		processor.postWebRequest(req, null);
	}

}
