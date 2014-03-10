package test.hzjbbis.fk.simulator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.clientmod.feintf.FeIntfClient;
import cn.hexing.fk.utils.ApplicationContextUtil;

public class WebSideClientTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml"};
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		FeIntfClient client = new FeIntfClient();
		client.setIp("127.0.0.1");
		client.setPort(20002);
		client.init();
		try{
			Thread.sleep(1000*2);
		}catch(Exception e){}
		client.getProfile();
		try{
			Thread.sleep(1000*100);
		}catch(Exception e){}
	}

}
