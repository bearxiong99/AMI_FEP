package cn.hexing.fas.model;

public class FaalGGKZM30Request extends FaalRequest{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8770072600072561889L;

	private long upgradeId;
	
	private byte[] currentContent;

	private int contentNum;
	
	private String fileName;
	
	private int ifseq;
	
	private String logicAddress;
	
	private int fileType;
	
	public FaalGGKZM30Request(){
		type = 0x30;
		protocol="04";
	}
	
	public long getUpgradeId() {
		return upgradeId;
	}

	public void setUpgradeId(long upgradeId) {
		this.upgradeId = upgradeId;
	}
	
	public byte[] getCurrentContent() {
		return currentContent;
	}

	public void setCurrentContent(byte[] currentContent) {
		this.currentContent = currentContent;
	}

	public int getContentNum() {
		return contentNum;
	}

	public void setContentNum(int contentNum) {
		this.contentNum = contentNum;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getIfseq() {
		return ifseq;
	}

	public void setIfseq(int ifseq) {
		this.ifseq = ifseq;
	}

	public String getLogicAddress() {
		return logicAddress;
	}

	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}
	
	
	
}
