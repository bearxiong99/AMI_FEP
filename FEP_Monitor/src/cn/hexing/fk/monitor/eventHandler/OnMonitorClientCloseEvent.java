package cn.hexing.fk.monitor.eventHandler;

import cn.hexing.fk.monitor.biz.HandleRtuTrace;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.adapt.ClientCloseEventAdapt;

/**
 * 监控管理需要处理监控客户端连接断开事件。
 *
 */
public class OnMonitorClientCloseEvent extends ClientCloseEventAdapt {

	@Override
	protected void process(ClientCloseEvent event) {
		super.process(event);
		HandleRtuTrace.getHandleRtuTrace().onClientClose(event);
	}

}
