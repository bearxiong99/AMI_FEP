package msg.request;

import msg.send.Client;
import cn.hexing.fas.model.FaalRefreshCacheRequest;

public class RefreshTerminal {
	public static void main(String[] args) {
		FaalRefreshCacheRequest refresh = new FaalRefreshCacheRequest();
		refresh.setRtuIds(new String[]{"19860946"});
		Client.getInstance().sendMsg(refresh);
		System.exit(0);
	}
}
