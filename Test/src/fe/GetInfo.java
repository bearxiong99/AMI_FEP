package fe;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.fk.clientmod.feintf.FeIntfClient;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.model.Master2FeRequest;

public class GetInfo {
	public static void main(String[] args) {
		
		Master2FeRequest req = new Master2FeRequest();
		
		MessageGate mg = new MessageGate();
		mg.getHead().setCommand(MessageGate.MASTER_FE_CMD);
		mg.setDataObject(req);

		FeIntfClient fif = new FeIntfClient();
		fif.setIp("192.168.2.167");
		fif.setPort(20002);
		fif.init();
		List<String> str = new ArrayList<String>();
		str.add("000013011629");
		fif.saveHeart2Db(str);
//		System.out.println(fif.getSaveHeart2DbList());
	}
}
