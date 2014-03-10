package cn.hexing.fas.model;


/**
 * 刷新通讯服务缓存 通讯请求
 */
public class FaalRefreshCacheRequest extends FaalRequest {

    private static final long serialVersionUID = -5642795098417472194L;
    
    /** 任务号。可选，如果指定任务号，则只刷新该任务配置 */
    private String taskNum;
    /** 终端局号 */
    private String[] rtuIds;
    
    /**表地址*/
    private String[] meterAddrs;
    
    public FaalRefreshCacheRequest() {
        super();
        type = FaalRequest.TYPE_REFRESH_CACHE;
    }

    /**
     * @return Returns the taskNum.
     */
    public String getTaskNum() {
        return taskNum;
    }
    /**
     * @param taskNum The taskNum to set.
     */
    public void setTaskNum(String taskNum) {
        this.taskNum = taskNum;
    }

	public String[] getRtuIds() {
		return rtuIds;
	}

	public void setRtuIds(String[] rtuIds) {
		this.rtuIds = rtuIds;
	}

	public final String[] getMeterAddrs() {
		return meterAddrs;
	}

	public final void setMeterAddrs(String[] meterAddrs) {
		this.meterAddrs = meterAddrs;
	}

    
}
