package cn.hexing.fk.monitor.biz;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.common.spi.IEventHook;
import cn.hexing.fk.monitor.biz.eventtrace.EventTracer;
import cn.hexing.fk.sockserver.event.ClientCloseEvent;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.HexDump;
/**
 * 考虑到系统性能，只能维持一个rtuTrace对象。
 *
 */
public class HandleRtuTrace {
	private static final Logger log = Logger.getLogger(HandleRtuTrace.class);
	private static final HandleRtuTrace handleRtuTrace = new HandleRtuTrace();
	private static EventTracer tracer = null;
	public static final HandleRtuTrace getHandleRtuTrace(){
		return handleRtuTrace;
	}

	/**
	 * 设置RTU事件跟踪。
	 * @param event
	 * @param inputBody：输入参数格式：Int类型RTUA，允许多个。
	 * @return
	 */
	public boolean startTraceRtu(ReceiveMessageEvent event,ByteBuffer inputBody){
		int count = inputBody.remaining()/4;
		if( count == 0 )
			return false;
		int[]rtus  = new int[count];
		for(int i=0; i<count; i++){
			rtus[i] = inputBody.getInt();
			log.info("跟踪：RTUA="+HexDump.toHex(rtus[i]));
		}
		//根据需要创建tracer
		if( null == tracer ){
			synchronized(this){
				tracer = new EventTracer(event.getClient());
				//向所有事件处理器注册eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(tracer);
				}
			}
		}
		else
			tracer.addClient(event.getClient());
		tracer.traceRtus(rtus);
		return true;
	}
	
	public boolean stopTrace(ReceiveMessageEvent event){
		if( null == tracer )
			return false;
		synchronized(this){
			int monitorCount = tracer.removeClient(event.getClient());
			if( 0 == monitorCount ){
				//向所有事件处理器取消eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(null);
				}
			}
		}
		return true;
	}
	
	public void onClientClose(ClientCloseEvent event){
		if( null == tracer )
			return;
		synchronized(this){
			int monitorCount = tracer.removeClient(event.getClient());
			if( 0 == monitorCount ){
				//向所有事件处理器取消eventTracer
				for(IEventHook hook: FasSystem.getFasSystem().getEventHooks() ){
					hook.setEventTrace(null);
				}
				tracer = null;
			}
		}
	}

}
