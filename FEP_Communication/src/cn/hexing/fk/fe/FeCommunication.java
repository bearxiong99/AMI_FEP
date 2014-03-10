/**
 * 通信前置机主程序。
 */
package cn.hexing.fk.fe;

import java.util.ArrayList;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.spi.IEventHook;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.fe.config.ApplicationPropertiesConfig;
import cn.hexing.fk.telnetserver.TelnetServer;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 * @author bhw
 * 2008-10-31 15:57
 */
public class FeCommunication {

	public static void main(String[] args) {
		String dataBaseName = System.getProperty("fe.database.name");
		dataBaseName = (null==dataBaseName || "".equals(dataBaseName.trim()))?"":"-"+dataBaseName;
		String[] path= new String[] { 
					"classpath*:applicationContext-common.xml",
					"classpath*:applicationContext-socket.xml",
					"classpath*:applicationContext-monitor.xml",
					"classpath*:applicationContext"+dataBaseName+"-db-batch.xml",
					"classpath*:applicationContext"+dataBaseName+"-fec.xml"
					};
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		//1. 加载FasSystem，负责所有模块的启动和停止。
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		fasSystem.setApplicationContext(context);
		
		//2. 确保所有网关通道client对象被加载到ChannelManage。
		ChannelManage channelManage = ChannelManage.getInstance();
		
		ApplicationPropertiesConfig config = ApplicationPropertiesConfig.getInstance();
		config.parseConfig();
		for(ClientModule mod: config.getGprsClientModules() ){
			fasSystem.addModule(mod);
			channelManage.addGprsClient(mod);
		}
		if( null != config.getUmsGateModule() ){
			fasSystem.addModule(config.getUmsGateModule());
			channelManage.setUmsGateClient(config.getUmsGateModule());
		}
			
		//先设置bpServer以及MonitorServer模块
//		fasSystem.setModules(new ArrayList<IModule>());
		for(IModule mod : config.getSocketServers() ){
			fasSystem.addModule(mod);
		}
		
		fasSystem.setEventHooks(new ArrayList<IEventHook>());
		for(IEventHook hook: config.getEventHandlers() ){
			fasSystem.addEventHook(hook);
		}
		
//		DbMonitor mastDbMonitor = (DbMonitor)context.getBean("master.dbMonitor");
//		mastDbMonitor.testDbConnection();
		
		//4. 加载数据库接口模块: 批量保存接口、业务处理DB接口
//		AsyncService asyncService = (AsyncService)context.getBean("asyncService");
//		fasSystem.addUnMonitoredModules(asyncService);

		//6. 启动telnetServer
		TelnetServer ts = TelnetServer.getInstance();
		fasSystem.addModule(ts);

		//7. 最后一步：启动系统（启动所有模块以及事件处理钩子
		fasSystem.startSystem();
	}
}
