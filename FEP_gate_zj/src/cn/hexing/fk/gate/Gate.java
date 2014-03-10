/**
 * ���ض���ͨ��spring���ö���ն���������TCP/UDP����
 * ͬʱֻ�ܰ���Ψһǰ�û��ӿڷ���
 * ÿ��Socket���񣬶���Ҫ����һ���¼���������
 * �����¼��������Ѿ�������Socket������ˣ�Gate������Ҫ�ٰ���SocketServer����
 * һ����˵��ÿ��Socket�����¼����������ܹ��ã���Ϊ�յ����Ķ���ҵ����ͬ��
 * 
 */
package cn.hexing.fk.gate;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.socket.abstra.BaseSocketServer;
import cn.hexing.fk.gate.config.ApplicationPropertiesConfig;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 */
public class Gate {
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-monitor.xml",
				"classpath*:applicationContext-gate.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		ApplicationPropertiesConfig config = (ApplicationPropertiesConfig)context.getBean("applicationPropertiesConfig");
		config.parseConfig();
		for(BaseSocketServer sockServer: config.getSocketServers())
			fasSystem.addModule(sockServer);
		for(BaseSocketServer sockServer: config.getExSocketServers())
			fasSystem.addModule(sockServer);
		for(BaseSocketServer sockServer: config.getCsdSocketServers())
			fasSystem.addModule(sockServer);
		for(BasicEventHook eventHandler: config.getEventHandlers() )
			fasSystem.addEventHook(eventHandler);
		for(BasicEventHook eventHandler: config.getExEventHandlers() )
			fasSystem.addEventHook(eventHandler);
		for(BasicEventHook eventHandler:config.getCsdEventHandlers()){
			fasSystem.addEventHook(eventHandler);
		}
		fasSystem.startSystem();
	}
}
