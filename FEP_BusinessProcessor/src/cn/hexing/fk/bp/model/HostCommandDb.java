package cn.hexing.fk.bp.model;


/**
 * 主站操作命令列表类
 */
public class HostCommandDb  {   
    /** 命令ID */
    private Long id;
    /** 终端业务类别 */
    private String rtuType;
    /** 消息数量。命令可能被拆分成多个消息下发 */
    private int messageCount;
    /** 命令状态 */
    private String status;
    /** 错误码 */
    private String errcode;		/*操作返回码*/
      
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
          
	public String getErrcode() {
		return errcode;
	}

	public void setErrcode(String errcode) {
		this.errcode = errcode;
		this.status=this.errcode;
	}
	public String getRtuType() {
		return rtuType;
	}
	public void setRtuType(String rtuType) {
		this.rtuType = rtuType;
	}	
	
}
