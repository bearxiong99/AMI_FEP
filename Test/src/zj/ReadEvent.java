package zj;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import msg.send.Client;

import cn.hexing.fas.model.FaalReadAlertRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class ReadEvent {
	public static void main(String[] args) throws ParseException, InterruptedException {
		FaalReadAlertRequest frar = new FaalReadAlertRequest();
		frar.setProtocol("04");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date date = sdf.parse("2012-11-10 00:00:00");
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		frar.setStartTime(c);
		frar.setCount(1);
		FaalRequestRtuParam param = new FaalRequestRtuParam();
		param.addParam("8067", null);
		param.setTn(new int[]{255});
		param.setRtuId("12142086");
		param.setCmdId((long) 0);
		frar.addRtuParam(param);
		Client.getInstance().sendMsg(frar);
		Thread.sleep(1000);
		System.exit(0);
	}
}
