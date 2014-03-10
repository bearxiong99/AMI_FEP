/**
 * 每个应用系统都有start 、 shutdown两个脚本。
 * shutdown脚本依赖于ShutdownApp类来实现关闭。
 */
package cn.hexing.fk.monitor.client;

/**
 *
 */
public class ShutdownApp {

	/**
	 * 参数格式 -ip=127.0.0.1 -port=10006 -shutdown
	 * @param args
	 */
	public static void main(String[] args) {
		String ip = "127.0.0.1";
		int port = 10006;
		for(String arg: args ){
			if( arg.startsWith("-ip=")){
				ip = arg.substring(4).trim();
			}
			else if( arg.startsWith("-port=")){
				port = Integer.parseInt(arg.substring(6).trim());
			}
		}
		MonitorClient client = new MonitorClient();
		client.setHostIp(ip);
		client.setHostPort(port);
		client.connect();
		int cnt = 5;
		while( !client.isConnected() && cnt-->0 ){
			try{
				Thread.sleep(1000);
			}catch(Exception e){}
		}
		if( client.isConnected() )
			client.shutdownApplication();
		try{
			Thread.sleep(100);
		}catch(Exception e){}
		client.close();
	}

}
