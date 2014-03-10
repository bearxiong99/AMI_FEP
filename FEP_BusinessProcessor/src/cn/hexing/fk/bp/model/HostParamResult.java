package cn.hexing.fk.bp.model;
/**
 * 主站操作命令设置结果类
 */
public class HostParamResult {
	/** 终端局号 */
    private String rtuId;
    /** 终端业务类别 */
    private String rtuType;
    /** 终端规约类型 */
    private String rtuProtocol;
    /** 测量点号 */
    private String tn;
    /** 参数项 */
    private String code;
    /** 状态 */
    private int status;
    /**失败原因 */
    private String sbyy;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public String getSbyy() {
		return sbyy;
	}
	public void setSbyy(String sbyy) {
		this.sbyy = sbyy;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	public String getRtuProtocol() {
		return rtuProtocol;
	}
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}
	public String getRtuType() {
		return rtuType;
	}
	public void setRtuType(String rtuType) {
		if (rtuType==null)
			this.rtuType="";
		else
			this.rtuType = rtuType;
	}
	
}
