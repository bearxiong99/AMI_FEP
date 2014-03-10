package cn.hexing.fas.model;

/**
 * 读中继请求
 */
public class FaalReadForwardDataRequest extends FaalRequest {

    private static final long serialVersionUID = -6840264166731727088L;
    /** 超时时间 */
    private int timeout;
    /** 是否为广播命令 */
    private boolean broadcast = false;
    /** 表计广播地址 */
    private String broadcastAddress;
    
    private String fixProto;	/** 指定表计规约 */
    private String fixAddre;	/** 指定表计地址 */
    private String fixPort;		/** 指定表计端口号 */
    
    public FaalReadForwardDataRequest() {
        super();
        type = FaalRequest.TYPE_READ_FORWARD_DATA;
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
    /**
     * @return Returns the broadcast.
     */
    public boolean isBroadcast() {
        return broadcast;
    }
    /**
     * @param broadcast The broadcast to set.
     */
    public void setBroadcast(boolean broadcast) {
        this.broadcast = broadcast;
    }
    /**
     * @return Returns the broadcastAddress.
     */
    public String getBroadcastAddress() {
        return broadcastAddress;
    }
    /**
     * @param broadcastAddress The broadcastAddress to set.
     */
    public void setBroadcastAddress(String broadcastAddress) {
        this.broadcastAddress = broadcastAddress;
    }

	public String getFixAddre() {
		return fixAddre;
	}

	public String getFixProto() {
		return fixProto;
	}

	public void setFixAddre(String fixAddre) {
		this.fixAddre = fixAddre;
	}

	public void setFixProto(String fixProto) {
		this.fixProto = fixProto;
	}

	public String getFixPort() {
		return fixPort;
	}

	public void setFixPort(String fixPort) {
		this.fixPort = fixPort;
	}
}
