/**
 *
 */
package cn.hexing.dp;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 *
 */
public class TaskPollingApp {

	public static void main(String[] args) {
		String dataBaseName = System.getProperty("task.database.name");
		dataBaseName = (null==dataBaseName || "".equals(dataBaseName.trim()))?"":"-"+dataBaseName;
		String[] path= new String[] { 
					"classpath*:applicationContext-common.xml",
					"classpath*:applicationContext-socket.xml",
					"classpath*:applicationContext-monitor.xml",
					"classpath*:applicationContext"+dataBaseName+"-db-batch.xml",
					"classpath*:applicationContext"+dataBaseName+"-tp.xml"
					};
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		ApplicationContextUtil.setContext(context);
		//1. 加载FasSystem，负责所有模块的启动和停止。
		ManageRtu.getInstance().loadBizRtu();
		FasSystem fasSystem = (FasSystem)context.getBean("fasSystem");
		fasSystem.setApplicationContext(context);
		
		fasSystem.startSystem();
	}

}
