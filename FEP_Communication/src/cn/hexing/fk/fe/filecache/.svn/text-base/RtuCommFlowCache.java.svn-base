/**
 * 终端流量本地缓存。
 */
package cn.hexing.fk.fe.filecache;

import java.io.File;

import org.apache.log4j.Logger;

import cn.hexing.fk.model.ComRtu;

/**
 * 格式: rtua(4)+ upGprsFlowmeter + upSmsCount + downGprsFlowmeter + downSmsCount + upGprsCount + downGprsCount
 * 		lastGprsTime(8) + lastSmsTime(8) + taskCount(2) + heartbeatCount(2)
 * 
 * line1 4*7=28 bytes; line2 8*2+ 2 + 2 = 20;   total 48 bytes
 */
public class RtuCommFlowCache {
	private static final Logger log = Logger.getLogger(RtuCommFlowCache.class);
	private static RtuCommFlowCache instance;
	
	static {
		//检测是否存在data目录
		try{
			File file = new File("data");
			file.mkdirs();
			instance = new RtuCommFlowCache();
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}

	//可配置
	private int batchSize = 1000;
	
	private RtuCommFlowCache(){
	}

	public static final RtuCommFlowCache getInstance(){
		return instance;
	}
	
	/**
	 * 在终端通信参数初始化之后，进行流量初始化。
	 */
	public void initOnStartup(){
	}
	
	/**
	 * 当终端的流量发生变更时候调用本函数。
	 * 流量变化的触发情况：（1）GateMessageEventHandler; （2）SmsMessageEventhandler
	 * @param rtu
	 */
	public void addRtu(ComRtu rtu){
	}
	
	//当系统退出，需要保存缓存。
	public void dispose(){
	}

	public int getBatchSize() {
		return batchSize;
	}

	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	
}
