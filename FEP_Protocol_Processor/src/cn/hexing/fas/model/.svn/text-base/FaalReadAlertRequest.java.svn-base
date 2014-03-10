package cn.hexing.fas.model;

import java.util.Calendar;

/**
 * 读终端告警数据请求
 */
public class FaalReadAlertRequest extends FaalRequest {

    private static final long serialVersionUID = -654318745767829688L;
    /** 告警起始时间 */
    private Calendar startTime;
    /** 告警数据点数 */
    private int count;
    
    public FaalReadAlertRequest() {
        super();
        type = FaalRequest.TYPE_READ_ALERT;
    }
    
    /**
     * @return Returns the startTime.
     */
    public Calendar getStartTime() {
        return startTime;
    }
    /**
     * @param startTime The startTime to set.
     */
    public void setStartTime(Calendar startTime) {
        this.startTime = startTime;
    }
    /**
     * @return Returns the count.
     */
    public int getCount() {
        return count;
    }
    /**
     * @param count The count to set.
     */
    public void setCount(int count) {
        this.count = count;
    }
}
