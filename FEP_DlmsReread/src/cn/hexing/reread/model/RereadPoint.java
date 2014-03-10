package cn.hexing.reread.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 漏点
 * @ClassName:RereadPoint
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午11:02:24
 *
 */
public class RereadPoint {
	private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	//终端逻辑地址
	private String terminalAddr;
	//表计通讯地址
	private String commAddr;
	//任务号（通道号）
	private String taskNo;
	//数据时间（时间点）
	private String timePoint;
	//数据项编码
	private String dataItemId;
	//发送成功标志
	private boolean sendFlag = false;
	//测量点号
	private int cldh;
	//任务类型：01-抄表日冻结任务 ,02-日冻结
	private String taskType; 
	
	public String getDataItemId() {
		return dataItemId;
	}
	public void setDataItemId(String dataItemId) {
		this.dataItemId = dataItemId;
	}
	public String getTaskNo() {
		return taskNo;
	}
	public void setTaskNo(String taskNo) {
		this.taskNo = taskNo;
	}
	public String getTimePoint() {
		return timePoint;
	}
	public void setTimePoint(Date timePoint) {
		this.timePoint = df.format(timePoint);
	}
	public String getCommAddr() {
		return commAddr;
	}
	public void setCommAddr(String commAddr) {
		this.commAddr = commAddr;
	}
	public boolean isSendFlag() {
		return sendFlag;
	}
	public void setSendFlag(boolean sendFlag) {
		this.sendFlag = sendFlag;
	}
	public final String getTerminalAddr() {
		return terminalAddr;
	}
	public final void setTerminalAddr(String terminalAddr) {
		this.terminalAddr = terminalAddr;
	}
	public int getCldh() {
		return cldh;
	}
	public void setCldh(int cldh) {
		this.cldh = cldh;
	}
	
	public String getTaskType() {
		return taskType;
	}
	public void setTaskType(String taskType) {
		this.taskType = taskType;
	}
	
	public String toString(){
		return "RereadPoint[commAddr:" + commAddr
		+",taskNo:" + taskNo
		+",timePoint:" + timePoint
		+",dataItemId:" + dataItemId
		//+",sendFlag:" + sendFlag
		+",cldh:" + cldh
		+",taskType:" + taskType
		+"]";
	}
}
