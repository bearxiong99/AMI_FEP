package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.Map;


/**
 * FAAL 通讯响应
 */
public class FaalRequestResponse implements Serializable{
	private static final long serialVersionUID = 1L;
	
    /** 主站操作请求对应的命令ID */
    private Long cmdId;
    /** 终端局号ID */
    private String rtuId;
    /** 终端业务类型:01专变；02集抄*/
    private String rtuType;
    /** FAAL 通讯请求类型 */
    private String cmdStatus;
    /** FAAL 通讯请求返回参数结果 */
    private Map<String,String> params;
    
	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public String getCmdStatus() {
		return cmdStatus;
	}
	public void setCmdStatus(String cmdStatus) {
		this.cmdStatus = cmdStatus;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public Map<String, String> getParams() {
		return params;
	}
	public void setParams(Map<String, String> params) {
		this.params = params;
	}
	public String getRtuType() {
		return rtuType;
	}
	public void setRtuType(String rtuType) {
		this.rtuType = rtuType;
	}

       
}
