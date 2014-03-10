/**
 * 业务处理器系统对象。
 */
package cn.hexing.fk.bp;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.db.managertu.ManageRtu;
import cn.hexing.fk.FasSystem;
import cn.hexing.fk.bp.processor.BPLatterProcessor;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 */
public class BizProcessor {
	private static final Logger log = Logger.getLogger(BizProcessor.class);
	public static void main(String[] args) {
		String dataBaseName = System.getProperty("bp.database.name");
		dataBaseName = (null==dataBaseName || "".equals(dataBaseName.trim()))?"":"-"+dataBaseName;
		String[] path= new String[] { 
					"classpath*:applicationContext-common.xml",
					"classpath*:applicationContext-socket.xml",
					"classpath*:applicationContext-monitor.xml",
					"classpath*:applicationContext"+dataBaseName+"-db-batch.xml",
					"classpath*:applicationContext"+dataBaseName+"-bp.xml"
					};
		try{
			ApplicationContext context = new ClassPathXmlApplicationContext(path);
			ApplicationContextUtil.setContext(context);
			
			ManageRtu manageRtu = (ManageRtu)context.getBean("manageRtu");
			manageRtu.loadBizRtu();
			
			MasterDbService master = (MasterDbService)context.getBean("master.dbservice");
			BPLatterProcessor.getInstance().setMasterDbService(master);
			BPLatterProcessor.getInstance().start();
			
			FasSystem fas = (FasSystem)context.getBean("fasSystem");
			
			/*try{
				TelnetServer ts = new TelnetServer(2324);
				fas.addModule(ts);
			}catch(Exception ex){
				log.error(ex);
			}*/
			
			fas.startSystem();
		}catch(Exception ex){
			log.error("start bp err:"+ex.getLocalizedMessage(),ex);
		}
	}
}
