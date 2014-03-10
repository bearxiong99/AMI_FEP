package cn.hexing.dp.model;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

public class RtuTaskRequest{
	private static final Logger log = Logger.getLogger(RtuTaskRequest.class);
	/** 终端局号ID */
    private String rtuId;
    /** 终端逻辑地址（HEX） */
    private String logicAddress;
    /** 任务通道号 */
    private String taskNo;
    /** 发送时间 */
    private Date sendTime=null;
    /** 发送次数 */
    private int sendCount=0;
    /** 任务时间 */
    private String taskDate;
    
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getTaskNo() {
		return taskNo;
	}
	public void setTaskNo(String taskNo) {
		this.taskNo = taskNo;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public Date getSendTime() {
		return sendTime;
	}
	public void setSendTime(Date sendTime) {
		this.sendTime = sendTime;
	}
	public int getSendCount() {
		return sendCount;
	}
	public void setSendCount(int sendCount) {
		this.sendCount = sendCount;
	}
	public String getTaskDateStr(){
		return taskDate;
	}
	public void setTaskDate(String taskDate) {
		this.taskDate = taskDate;
	}
	public Date getTaskDate() {
    	Date time=null;
    	if (taskDate!=null){
    		try{
    			if (taskDate.trim().length()==16){
    				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    				time = df.parse(taskDate);
    			}
    			else if (taskDate.trim().length()==13){
    				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH");
    				time = df.parse(taskDate);
    			}
    			else if (taskDate.trim().length()==10){
    				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
    				time = df.parse(taskDate);
    			}
    			else if (taskDate.trim().length()==7){
    				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM");
    				time = df.parse(taskDate);
    			}
    		}
    		catch(Exception ex){		
    			log.error("getTimeValue error,dt="+taskDate+" error:"+ex.getLocalizedMessage());
    		}
    	}				
		return time;
	}
    
}
