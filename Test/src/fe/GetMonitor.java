package fe;

import cn.hexing.fk.monitor.client.MonitorClient;

public class GetMonitor {
	public static void main(String[] args) {
		MonitorClient mc = new MonitorClient("127.0.0.01", 10022);
		mc.init();
		mc.cmdTraceRTUs("12653101");
//		mc.cmdGetFile("./applicationContext-monitor.xml");
//		mc.cmdListLog();
//		mc.cmdListConfig();
//		mc.cmdGatherProfile();
	}
}
