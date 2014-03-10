/**
 * 完成终端对象初始化过程。
 * 如果从数据库加载失败，则从本地文件加载。
 */
package cn.hexing.fk.fe.init;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.fe.filecache.HeartbeatPersist;
import cn.hexing.fk.fe.msgqueue.BpBalanceFactor;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.tracelog.TraceLog;

/**
 *
 */
public class Initialize {
	//private static final Logger log = Logger.getLogger(Initialize.class);

	public void initRtus(){
		TraceLog.getTracer().trace("initRtus called");
//		ManageRtu.getInstance().loadComRtu();
		//为了支持多业务处理器，统计每个地市终端数量，以便均衡分发
		BpBalanceFactor.getInstance().travelRtus(RtuManage.getInstance().getAllComRtu());
		
		//心跳缓存信息的定位
		HeartbeatPersist.getInstance().initOnStartup();
		
		//通信参数缓存，由RealtimeSynchronizer完成缓存加载以及写缓存文件。
		//流量缓存由BatchSynchronizer完成加载与写缓存文件。
		
		//系统退出时候，需要保存终端状态到本地文件。
		FasSystem.getFasSystem().addShutdownHook(new Runnable(){
			public void run() {
				shutdownWork();
			}
		});
	}

	private void shutdownWork(){
		HeartbeatPersist.getInstance().dispose();
	}
}
