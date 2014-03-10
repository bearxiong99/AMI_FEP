package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import cn.hexing.fas.protocol.zj.FunctionCode;


/**
 * FAAL 通讯请求
 */
public abstract class FaalRequest implements Serializable {
    private static final long serialVersionUID = 2937756926363569712L;
    
    /** 请求类型：读中继 */
    public static final int TYPE_READ_FORWARD_DATA = FunctionCode.READ_FORWARD_DATA;
    /** 请求类型：读当前数据 */
    public static final int TYPE_READ_CURRENT_DATA = FunctionCode.READ_CURRENT_DATA;
    /** 请求类型：读任务数据 */
    public static final int TYPE_READ_TASK_DATA = FunctionCode.READ_TASK_DATA;
    /** 请求类型：读编程日志 */
    public static final int TYPE_READ_PROGRAM_LOG = FunctionCode.READ_PROGRAM_LOG;
    /** 请求类型：实时写对象参数 */
    public static final int TYPE_REALTIME_WRITE_PARAMS = FunctionCode.REALTIME_WRITE_PARAMS;
    /** 请求类型：写对象参数 */
    public static final int TYPE_WRITE_PARAMS = FunctionCode.WRITE_PARAMS;
    /** 请求类型：读告警数据 */
    public static final int TYPE_READ_ALERT = FunctionCode.READ_ALERT;
    /** 请求类型：告警确认 */
    public static final int TYPE_CONFIRM_ALERT = FunctionCode.CONFIRM_ALERT;
    /** 请求类型：发送短信 */
    public static final int TYPE_SEND_SMS = FunctionCode.SEND_SMS;
    /** 自定义请求类型：刷新通讯服务缓存 */
    public static final int TYPE_REFRESH_CACHE = FunctionCode.REFRESH_CACHE;
    /** 自定义请求类型：其它操作 */
    public static final int TYPE_OTHER = FunctionCode.OTHER;
    /** 请求类型：点抄日（月）冻结数据 */
    public static final int TYPE_READ_HISTORY_DATA11 = FunctionCode.READ_HISTORY_DATA11;
    /** 请求类型：集抄日（月）冻结数据 */
    public static final int TYPE_READ_HISTORY_DATA12 = FunctionCode.READ_HISTORY_DATA12;
    /** 请求类型：集中器操作类 */
    public static final int TYPE_Action = FunctionCode.Action;
    /** 请求类型：自动注册类 */
    public static final int TYPE_AutoRegistered = FunctionCode.AutoRegistered;
    /** 请求类型：事件类 */
    public static final int TYPE_Event = FunctionCode.Event;
    /** 请求类型：远程升级 */
    public static final int TYPE_RemoteUpgrade = FunctionCode.RemoteUpgrade;
    /** 请求类型：预付费类 */
    public static final int TYPE_pay_token = FunctionCode.pay_token;

    /** 单位代码 */
    protected String dwdm;
    /** 用户类型 */
    protected String yhlx; 
    
    /** 规约类型 */
    protected String protocol;
    /** 命令类型 */
    protected int type;   
    /** 请求参数列表 */
    private List<FaalRequestRtuParam> rtuParams;
    private int txfs=0; 
    /** 通讯请求发起人 */
    private String operator;

    private long timetag;
   
    /**
     * 添加国网请求参数
     * @param param 请求参数
     */
    public void addRtuParam(FaalRequestRtuParam rtuParam) {
        if (rtuParams == null) {
            rtuParams = new ArrayList<FaalRequestRtuParam>();
        }
        rtuParams.add(rtuParam);
    }
    
    public List<FaalRequestRtuParam> getRtuParams() {
		return rtuParams;
	}
	public void setRtuParams(List<FaalRequestRtuParam> rtuParams) {
		this.rtuParams = rtuParams;
	}
	
   
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(", type=").append(type)
            .append(", rtuCount=").append(rtuParams == null ? 0 : rtuParams.size())
            .append("]");
        
        return sb.toString();
    }
    
    /**
     * @return Returns the type.
     */
    public int getType() {
        return type;
    }
    /**
     * @return Returns the protocol.
     */
    public String getProtocol() {
        return protocol;
    }
    /**
     * @param protocol The protocol to set.
     */
    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    /**
     * @return Returns the operation.
     */

    /**
     * @return Returns the operator.
     */
    public String getOperator() {
        return operator;
    }
    /**
     * @param operator The operator to set.
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }
   

	public long getTimetag() {
		return timetag;
	}

	public void setTimetag(long timetag) {
		this.timetag = timetag;
	}
	public void setType(int type) {
		this.type = type;
	}
	public int getTxfs() {
		return txfs;
	}
	public void setTxfs(int txfs) {
		this.txfs = txfs;
	}
	public String getDwdm() {
		return dwdm;
	}
	public void setDwdm(String dwdm) {
		this.dwdm = dwdm;
	}
	public String getYhlx() {
		return yhlx;
	}
	public void setYhlx(String yhlx) {
		this.yhlx = yhlx;
	}
    
    
}