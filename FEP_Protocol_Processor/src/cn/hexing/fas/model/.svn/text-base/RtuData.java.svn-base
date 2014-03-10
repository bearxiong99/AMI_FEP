package cn.hexing.fas.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 终端任务数据
 */
public class RtuData {
	private static final Logger log = Logger.getLogger(RtuData.class);
	/** 单位代码 用于任务保存*/
    //private String deptCode;
	/** 数据保存ID */
    //private String dataSaveID;
    /** 终端任务号 */
    private String taskNum;
    /** 终端任务数据属性 */
    //private String taskProperty;
    /** 终端逻辑地址（HEX） */
    private String logicAddress;
    /** 测量点号 */
    private String tn;
    /** 数据时间 */
    private Date time;
    
    /** 数据列表 */
    private List<RtuDataItem> dataList=new ArrayList<RtuDataItem>();


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[logicAddress=").append(getLogicAddress())
        	.append(", taskNum=").append(getTaskNum())
            .append(", time=").append(getTime()).append("]");
        return sb.toString();
    }
    /**
     * 添加告警参数
     * @param arg 告警参数
     */
    public void addDataList(RtuDataItem rtuDataItem) {
    	dataList.add(rtuDataItem);
    }

	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public void setTime(String dt) {
		try{
			SimpleDateFormat df = null ;
			if (dt.trim().length()==16){
				df= new SimpleDateFormat("yyyy-MM-dd HH:mm");
			}
			else if (dt.trim().length()==10){
				df = new SimpleDateFormat("yyyy-MM-dd");
			}else if (dt.trim().length()==7){
				df = new SimpleDateFormat("yyyy-MM");
			}else if(dt.trim().length()==19){
				df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			this.time = df.parse(dt);
		}
		catch(Exception e){		
			log.error("setTime time="+dt+" error:"+e);
		}
	}
	public String getTimeStr() {
		String sDate="";
		try{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sDate=df.format(time);
		}catch(Exception ex){			
		}
		return sDate;
	}
	public List<RtuDataItem> getDataList() {
		return dataList;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	//取特殊处理过的任务时间，当前+1天
	public Date getNextday() {
		Calendar cl = Calendar.getInstance();
		cl.setTime(time);
		cl.add(Calendar.DATE, +1);
		return cl.getTime();
	}
	//获得下一个月，为了统一国网和DLMS月冻结
	public Date getNextMonth(){
		Calendar cl = Calendar.getInstance();
		cl.setTime(time);
		cl.add(Calendar.MONTH, +1);
		return cl.getTime();
	}
	//广规任务时间为集中器冻结时间  比表计实际冻结数据的时间晚一天，当前-1天
	public Date getDateBefore(){
		Calendar calendar = Calendar.getInstance(); //得到日历
		calendar.setTime(time);//把当前时间赋给日历
		calendar.add(Calendar.DAY_OF_MONTH, -1);  //设置为前一天
		return  calendar.getTime();   //得到前一天的时间
	}
}
