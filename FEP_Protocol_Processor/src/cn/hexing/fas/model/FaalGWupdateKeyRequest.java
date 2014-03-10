package cn.hexing.fas.model;

/**
 * 
 * @author gaoll
 *
 * @time 2012-12-18 上午8:59:17
 *
 * @info 国网更新密钥请求
 */
public class FaalGWupdateKeyRequest extends FaalRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2015670089624298385L;
	
	/**帧序列号，用于加密*/
	private int[] fseq ;
	
	private long cmdId;
	
	private int flag = 0 ;
	
	private String collectorNo;
	
	private int keyVersion;
	
	public final int[] getFseq() {
		return fseq;
	}

	public final void setFseq(int[] fseq) {
		this.fseq = fseq;
	}

	public final long getCmdId() {
		return cmdId;
	}

	public final void setCmdId(long cmdId) {
		this.cmdId = cmdId;
	}

	public final int getFlag() {
		return flag;
	}

	public final void setFlag(int flag) {
		this.flag = flag;
	}

	public final String getCollectorNo() {
		return collectorNo;
	}

	public final void setCollectorNo(String collectorNo) {
		this.collectorNo = collectorNo;
	}

	public final int getKeyVersion() {
		return keyVersion;
	}

	public final void setKeyVersion(int keyVersion) {
		this.keyVersion = keyVersion;
	} 
	

}
