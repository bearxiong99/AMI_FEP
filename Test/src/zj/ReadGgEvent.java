package zj;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import msg.send.Client;

import cn.hexing.fas.model.FaalGGKZM19Request;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class ReadGgEvent {
	public static void main(String[] args) throws ParseException {
		FaalGGKZM19Request request = new FaalGGKZM19Request();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<String> ids = new ArrayList<String>();
		ids.add("0171");
		request.setEventIDs(ids);
		request.setEventNum(1);
		request.setStartTime(sdf.parse("2013-10-08 00:00:00"));
		request.setEndTime(sdf.parse("2013-10-16 00:00:00"));
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setRtuId("12649602");
		frrp.setCmdId(251454L);
		request.addRtuParam(frrp);
		
		Client.getInstance().sendMsg(request);
		System.exit(0);
	}
}
