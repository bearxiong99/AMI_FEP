package cn.hexing.fas.model;

import java.util.Calendar;

/**
 * 实时写对象参数请求
 */
public class FaalRealTimeWriteParamsRequest extends FaalWriteParamsRequest {

    private static final long serialVersionUID = 2234597531372736773L;
    
    /** 命令时间 */
    private Calendar cmdTime;
    /** 命令有效时间（分） */
    private int timeout;
    
    public FaalRealTimeWriteParamsRequest() {
        super();
        type = FaalRequest.TYPE_REALTIME_WRITE_PARAMS;
    }
    
    /**
     * @return Returns the cmdTime.
     */
    public Calendar getCmdTime() {
        return cmdTime;
    }
    /**
     * @param cmdTime The cmdTime to set.
     */
    public void setCmdTime(Calendar cmdTime) {
        this.cmdTime = cmdTime;
    }
    /**
     * @return Returns the timeout.
     */
    public int getTimeout() {
        return timeout;
    }
    /**
     * @param timeout The timeout to set.
     */
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }
}
