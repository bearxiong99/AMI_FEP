package cn.hexing.reread.model;

/**
 * 补召策略
 * @ClassName:RereadStrategy
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午11:02:40
 *
 */
public class RereadStrategy {
	 /** 任务模版ID */
    private String taskTemplateID;
    /** 采样间隔时间 */
    private int sampleInterval;
    /** 采样间隔时间单位 单位02：分，03：时，04：日，05：月*/
    private String sampleIntervalUnit;
    /** Cron表达式**/
    private String cron;
    /**统计漏点开始时间（相对于当前时间的间隔小时数）**/
    private int dataBegin ;
    /**统计漏点截止时间（相对于当前时间的间隔小时数）**/
    private int dataEnd;
    
    private String xgbj;
    
    private String rwsx;
    
    private String rwlx;//01-终端任务，02-主站任务
    
	public String getTaskTemplateID() {
		return taskTemplateID;
	}
	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}
	public int getSampleInterval() {
		return sampleInterval;
	}
	public void setSampleInterval(int sampleInterval) {
		this.sampleInterval = sampleInterval;
	}
	public String getSampleIntervalUnit() {
		return sampleIntervalUnit;
	}
	public void setSampleIntervalUnit(String sampleIntervalUnit) {
		this.sampleIntervalUnit = sampleIntervalUnit;
	}
	public String getCron() {
		return cron;
	}
	public void setCron(String cron) {
		this.cron = cron;
	}
	
	public int getDataBegin() {
		return dataBegin;
	}
	public void setDataBegin(int dataBegin) {
		this.dataBegin = dataBegin;
	}
	public int getDataEnd() {
		return dataEnd;
	}
	public void setDataEnd(int dataEnd) {
		this.dataEnd = dataEnd;
	}
	public String getRwsx() {
		return rwsx;
	}
	public void setRwsx(String rwsx) {
		this.rwsx = rwsx;
	}
	public String getRwlx() {
		return rwlx;
	}
	public void setRwlx(String rwlx) {
		this.rwlx = rwlx;
	}
	
	public String getXgbj() {
		return xgbj;
	}
	public void setXgbj(String xgbj) {
		this.xgbj = xgbj;
	}
	public String toString(){
		return "RereadStrategy[taskTemplateID:" + taskTemplateID
		+",sampleInterval:" + sampleInterval
		+",sampleIntervalUnit:" + sampleIntervalUnit
		+",cron:" + cron
		+",dataBegin:" + dataBegin
		+",dataEnd:" + dataEnd
		+",rwsx:" + rwsx
		+",rwlx:" + rwlx
		+",xgbj:" + xgbj
		+"]";
	}
    
}	
