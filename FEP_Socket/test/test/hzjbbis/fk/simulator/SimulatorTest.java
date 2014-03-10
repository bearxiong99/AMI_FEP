package test.hzjbbis.fk.simulator;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.sockclient.async.AsyncSocketPool;
import cn.hexing.fk.sockclient.async.eventhandler.AsyncSocketEventHandler;
import cn.hexing.fk.sockclient.async.simulator.SimulatorManager;

public class SimulatorTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket-client.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		//模拟器事件处理器
		AsyncSocketEventHandler simulatorHandler = (AsyncSocketEventHandler)context.getBean("eventhook.simulator");
		simulatorHandler.start();
		AsyncSocketPool sockPool = (AsyncSocketPool)context.getBean("asyncpool.simulator");
		sockPool.start();
		SimulatorManager.startHeart();	SimulatorManager.startTask();
	}

}
