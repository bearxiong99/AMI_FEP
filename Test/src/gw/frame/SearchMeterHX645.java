package gw.frame;

import msg.send.Client;
import cn.hexing.fas.model.FaalHX645RelayRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;

public class SearchMeterHX645 {
	public static void main(String[] args) {
		FaalHX645RelayRequest faal = new FaalHX645RelayRequest();
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setCmdId((long)0);
		frrp.setRtuId("66554433");
		frrp.addParam("10F001", "");
		faal.addRtuParam(frrp);
		faal.setId("EA10");
//		faal.setDataArea("12345678901234567890");
		faal.setOp("01");
		faal.setType(16);
		faal.setFixAddre("19860811");
		faal.setPort(31); //¶Ë¿ÚºÅ
		frrp.setTn(new int[]{3});
		faal.setProtocol("02");
		Client.getInstance().sendMsg(faal);
		System.exit(0);
	}
}
