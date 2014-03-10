package gw;

import java.util.List;

import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.startup.ProtocolDecoder;
import cn.hexing.fk.bp.model.AutoRegister;

public class TestAlarm {
	public static void main(String[] args) {
		
		ProtocolDecoder pd =new ProtocolDecoder();
		@SuppressWarnings("unchecked")
		List<RtuAlert> alerts = (List<RtuAlert>) pd.messageDecoder("68CA00CA0068C422221111000E64000001000100000127201110130912020002009F3212120088000048584501353E0400000000000000112416");
		RtuAlert alert = alerts.get(0);
		String value=alert.getSbcs();
		String[] params = value.split("#");
		String logicAddress = "22114433";
		AutoRegister ar = new AutoRegister();
		ar.setLogicAddress(logicAddress);
		ar.setRegisterTime(alert.getAlertTime());
		ar.setMeasurePoint(params[1]);
		ar.setStatus(0);
	
		ar.setValue(value);
	}
}
