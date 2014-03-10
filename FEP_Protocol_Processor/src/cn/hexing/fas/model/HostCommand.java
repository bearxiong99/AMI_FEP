package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * 主站操作命令
 */
public class HostCommand implements Serializable {

    private static final long serialVersionUID = -4703256789407620876L;
    
    /** 主站操作命令状态: 进行中 */
    public static final String STATUS_RUNNING = "0";
    /** 主站操作命令状态: 已结束 */
    public static final String STATUS_SUCCESS = "1";
    /** 主站操作命令状态: 终端执行失败 */
    public static final String STATUS_RTU_FAILED = "2";
    /** 主站操作命令状态: 通道错误 */
    public static final String STATUS_COMM_FAILED = "3";
    /** 主站操作命令状态: 终端超时无应答 */
    public static final String STATUS_TIMEOUT = "4";
    /** 主站操作命令状态: 中继命令无应答 */
    public static final String STATUS_FWD_CMD_NO_RESPONSE = "5";
    /** 主站操作命令状态: 设置内容非法 */
    public static final String STATUS_PARA_INVALID = "6";
    /** 主站操作命令状态: 权限不足 */
    public static final String STATUS_PERMISSION_DENIDE = "7";
    /** 主站操作命令状态: 无此数据项 */
    public static final String STATUS_ITEM_INVALID = "8";
    /** 主站操作命令状态: 命令时间失效 */
    public static final String STATUS_TIME_OVER = "9";
    /** 主站操作命令状态: 目标地址不存在 */
    public static final String STATUS_TARGET_UNREACHABLE = "10";
    /** 主站操作命令状态: 发送失败 */
    public static final String STATUS_SEND_FAILURE = "11";
    /** 主站操作命令状态: 短信帧太长 */
    public static final String STATUS_SMS_OVERFLOW = "12";    
    /** 主站操作命令状态: 主站传入参数错误，组帧失败 */
    public static final String STATUS_PRAR_ERROR = "13";   
    /** 主站操作命令状态: 前置机解析失败 */
    public static final String STATUS_PARSE_ERROR = "14";    
    /** 主站操作命令状态: 结果保存入库失败 */
    public static final String STATUS_TODB_ERROR = "15";
    /** 主站操作命令状态: 终端与表计无通讯 */
    public static final String STATUS_RTUAMTCOM_ERROR = "16";
    
    
    /** 命令ID */
    private Long id;
    /** 主站操作任务ID */
    private Long taskId;
    /** 终端ID */
    private String rtuId;
    /** 参数个数 */
    private int paramCount;
    /** 消息数量。命令可能被拆分成多个消息下发 */
    private int messageCount;
    /** 下发时间 */
    private Date requestTime;
    /** 回应时间 */
    private Date responseTime;
    /** 命令状态 */
    private String status;
    /** 是否需要更新到任务数据或告警表。用于在召测历史任务数据或告警时使用 */
    private boolean doUpdate = false;
    /** 是否为写参数命令。如果是写参数命令，通讯服务在收到应答后需要进一步的处理 */
    private boolean writeParams = false;
    /** 发起这个命令的 FAAL 请求对象 */
    private FaalRequest request;

    /** 命令结果列表[HostCommandResult] */
    private List<HostCommandResult> results;
    /** 测量点数据[HostCommandMeasurePoint] */
    private List measurePoints;
    private HashMap mpLines;
    private String errcode;		/*操作返回码*/
    private boolean setTag=false;
    
    /**
     * 添加命令结果
     * @param result 命令结果
     */
    public void addResult(HostCommandResult result) {
        if (results == null) {
            results = new ArrayList();
        }
        results.add(result);
    }
    
    /**
     * @return Returns the id.
     */
    public Long getId() {
        return id;
    }
    /**
     * @param id The id to set.
     */
    public void setId(Long id) {
        this.id = id;
    }
    /**
     * @return Returns the taskId.
     */
    public Long getTaskId() {
        return taskId;
    }
    /**
     * @param taskId The taskId to set.
     */
    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }
    /**
     * @return Returns the rtuId.
     */
    public String getRtuId() {
        return rtuId;
    }
    /**
     * @param rtuId The rtuId to set.
     */
    public void setRtuId(String rtuId) {
        this.rtuId = rtuId;
    }
    /**
     * @return Returns the paramCount.
     */
    public int getParamCount() {
        return paramCount;
    }
    /**
     * @param paramCount The paramCount to set.
     */
    public void setParamCount(int paramCount) {
        this.paramCount = paramCount;
    }
    /**
     * @return Returns the messageCount.
     */
    public int getMessageCount() {
        return messageCount;
    }
    /**
     * @param messageCount The messageCount to set.
     */
    public void setMessageCount(int messageCount) {
        this.messageCount = messageCount;
    }
    /**
     * @return Returns the requestTime.
     */
    public Date getRequestTime() {
        return requestTime;
    }
    /**
     * @param requestTime The requestTime to set.
     */
    public void setRequestTime(Date requestTime) {
        this.requestTime = requestTime;
    }
    /**
     * @return Returns the responseTime.
     */
    public Date getResponseTime() {
        return responseTime;
    }
    /**
     * @param responseTime The responseTime to set.
     */
    public void setResponseTime(Date responseTime) {
        this.responseTime = responseTime;
    }
    /**
     * @return Returns the status.
     */
    public String getStatus() {
        return status;
    }
    /**
     * @param status The status to set.
     */
    public void setStatus(String status) {
        this.status = status;
        this.errcode=status;
    }
    /**
     * @return Returns the doUpdate.
     */
    public boolean isDoUpdate() {
        return doUpdate;
    }
    /**
     * @param doUpdate The doUpdate to set.
     */
    public void setDoUpdate(boolean doUpdate) {
        this.doUpdate = doUpdate;
    }
    /**
     * @return Returns the writeParams.
     */
    public boolean isWriteParams() {
        return writeParams;
    }
    /**
     * @param writeParams The writeParams to set.
     */
    public void setWriteParams(boolean writeParams) {
        this.writeParams = writeParams;
    }
    /**
     * @return Returns the request.
     */
    public FaalRequest getRequest() {
        return request;
    }
    /**
     * @param request The request to set.
     */
    public void setRequest(FaalRequest request) {
        this.request = request;
    }
   
    /**
     * @return Returns the results.
     */
    public List<HostCommandResult> getResults() {
        return results;
    }
    /**
     * @param results The results to set.
     */
    public void setResults(List<HostCommandResult> results) {
        this.results = results;
    }
    /**
     * @return Returns the measurePoints.
     */
    public List getMeasurePoints() {
        return measurePoints;
    }
    /**
     * @param measurePoints The measurePoints to set.
     */
    public void setMeasurePoints(List measurePoints) {
        this.measurePoints = measurePoints;
    }

	public HashMap getMpLines() {
		return mpLines;
	}

	public void setMpLines(HashMap mpLines) {
		this.mpLines = mpLines;
	}
    
	/**
	 * add mp to line map
	 * @param mp		终端测量点号
	 * @param line		路号
	 */
    public void addLine(String mp,String line){
    	if(mpLines==null){
    		mpLines=new HashMap();
    	}
    	mpLines.put(line, mp);
    }
    
    /**
     * get mp by line
     * @param line
     */
    public String getMpByLine(String line){
    	if(mpLines!=null){
    		if(mpLines.containsKey(line)){
    			return (String)mpLines.get(line);
    		}
    	}
    	return null;
    }

	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
		this.status=this.errcode;
	}

	public final boolean isSetTag() {
		return setTag;
	}

	public final void setSetTag(boolean setTag) {
		this.setTag = setTag;
	}
}
