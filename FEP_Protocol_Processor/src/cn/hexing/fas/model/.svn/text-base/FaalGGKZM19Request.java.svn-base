package cn.hexing.fas.model;

import java.util.Date;
import java.util.List;


/**
 * 广规控制码=12的请求命令：集中器抄收日冻结数据
 */
public class FaalGGKZM19Request extends FaalRequest {
	private static final long serialVersionUID = 4740138509881420068L;

	/**起始电表顺序号*/
	private int startMeterNo;
	/**连续电表数*/
	private int num;
    /** 数据起始时间 */
    private Date startTime;
    /** 数据结束时间 */
    private Date endTime;
    /**事件任务点数*/
   private int eventNum;
   /**事件ID*/
    private List<String> eventIDs;
    
    
    
    public FaalGGKZM19Request() {
        super();
        this.setProtocol("04");
        type = FaalRequest.TYPE_Event;
    }

	
    public int getStartMeterNo(){
    	return startMeterNo;
    }

    public void setStartMeterNo(int startMeterNo){
    	this.startMeterNo=startMeterNo;
    }
    public int getNum(){
    	return num;
    }

    public void setNum(int num){
    	this.num=num;
    }
	public Date getSrartTime() {
		return startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}


	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}


	public int getEventNum() {
		return eventNum;
	}


	public void setEventNum(int eventNum) {
		this.eventNum = eventNum;
	}


	public List<String> getEventIDs() {
		return eventIDs;
	}


	public void setEventIDs(List<String> eventIDs) {
		this.eventIDs = eventIDs;
	}


	public Date getStartTime() {
		return startTime;
	}


 
}
