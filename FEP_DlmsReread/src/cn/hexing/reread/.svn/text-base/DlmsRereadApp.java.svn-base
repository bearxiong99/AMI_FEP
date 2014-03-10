
package cn.hexing.reread;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.utils.ApplicationContextUtil;
/**
 * 启动补召程序，加载Spring的xml文件
 * 由Spring负责补召程序主程序的启动
 * @ClassName:DlmsRereadApp
 * @author kexl
 * @date 2012-9-24 上午10:27:01
 *
 */
public class DlmsRereadApp {
	public static String dataBaseName_oracle = "oracle";
	public static String dataBaseName_mysql = "mysql";
	public static String dataBaseName = System.getProperty("reread.database.name").trim();
	
	public static void main(String[] args) {
		String xmlExt = (null==dataBaseName || "".equals(dataBaseName) || dataBaseName_oracle.equals(dataBaseName))?"":"-"+dataBaseName;
		String[] path= new String[] { 
			"classpath*:applicationContext-common.xml",
			"classpath*:applicationContext-socket.xml",
			"classpath*:applicationContext-monitor.xml",
			"classpath*:applicationContext"+xmlExt+"-db-batch.xml",
			"classpath*:applicationContext"+xmlExt+"-reread.xml"
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
