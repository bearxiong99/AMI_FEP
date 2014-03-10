package zj;

import msg.send.Client;
import cn.hexing.fas.model.FaalGGKZM30Request;

public class Upgrade {
	public static void main(String[] args) {
//		FaalGGKZM30Request fr = new FaalGGKZM30Request();
//		fr.setUpgradeId(1152);
//		fr.setLogicAddress("12079802");
//		Client.getInstance().sendMsg(fr);
//		System.exit(0);
		
		System.out.println(Runtime.getRuntime().totalMemory()>>>20);
		System.out.println(Runtime.getRuntime().freeMemory()>>>20);

		System.out.println(Runtime.getRuntime().maxMemory()>>>20);

	}
}
