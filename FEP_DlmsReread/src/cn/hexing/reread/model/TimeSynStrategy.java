package cn.hexing.reread.model;
/**
 * 自动对时任务
 * @ClassName:TimeSynTask
 * @Description:TODO
 * @author kexl
 * @date 2012-11-15 下午02:41:23
 *
 */
public class TimeSynStrategy {
	private String dwdm; //单位代码
	
	private String rwlx; //任务类型:0-召测任务，1-补召任务
	
	private String cron; //执行时间
	
	private String xgbj; //读取状态

	public TimeSynStrategy() {
		super();
	}

	public TimeSynStrategy(String dwdm, String rwlx, String cron, String xgbj) {
		super();
		this.dwdm = dwdm;
		this.rwlx = rwlx;
		this.cron = cron;
		this.xgbj = xgbj;
	}


	public String getDwdm() {
		return dwdm;
	}

	public void setDwdm(String dwdm) {
		this.dwdm = dwdm;
	}

	public String getRwlx() {
		return rwlx;
	}

	public void setRwlx(String rwlx) {
		this.rwlx = rwlx;
	}

	public String getCron() {
		return cron;
	}

	public void setCron(String cron) {
		this.cron = cron;
	}

	public String getXgbj() {
		return xgbj;
	}

	public void setXgbj(String xgbj) {
		this.xgbj = xgbj;
	}

	public String toString() {
		return "{dwdm="+dwdm+",rwlx="+rwlx+",cron="+cron+",xgbj="+xgbj+"}";
	}
}
