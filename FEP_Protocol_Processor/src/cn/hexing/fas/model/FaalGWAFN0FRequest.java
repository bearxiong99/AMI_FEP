package cn.hexing.fas.model;

public class FaalGWAFN0FRequest extends FaalRequest {

    private static final long serialVersionUID = -562982183452485616L; 
    
    /** 文件标志  
    00H：清除传输文件，恢复到升级前状态。
	01H：终端升级文件。
	02H：远程（上行）通讯模块升级文件。
	03H：本地通讯模块升级文件。
	04H：采集器升级的采集器地址文件。
	05H：采集器升级的采集器程序文件。
	06H：采集器通信模块升级的地址文件。
	07H：采集器通信模块升级的程序文件。
	FBH:表计模块升级
	FCH:表计白名单地址文件
	FDH：表计升级文件
	FEH:表计地址文件
	ffH：代表主站下发任意文件程序（其中文件的第一帧中包含文件的相关信息，目前采用该格式升级集中器程序和表计的程序）
     */
    private String fileTag;
    /**   */
    private String fileAttribute;
    /**	文件指令 00H：报文方式传输；01H：FTP方式传输；02H：启动组地址升级。 */
    private String fileCommand;
    /** 版本控制*/
    private String softVersion;
    /**	用户名	*/
    private String userName;
    /**	用户密码	*/
    private String password;
    /**	文件IP地址	*/
    private String fileIPAddr;
    /**	端口号	*/
    private int port;
    /**	文件路径*/
    private String filePath;
    /**	文件名	*/
    private String fileName;
    /** 下行发送时间 */
    private String tpSendTime;
    /** 下行发送传输超时时间 */
    private int tpTimeout;
    
    private long softUpgradeID;
    
    private int currentMessage=0;
    private int totalMessageCount=0;
    
    public FaalGWAFN0FRequest() {
    }
    
	public String getFileTag() {
		return fileTag;
	}

	public void setFileTag(String fileTag) {
		this.fileTag = fileTag;
	}

	public String getFileAttribute() {
		return fileAttribute;
	}

	public void setFileAttribute(String fileAttribute) {
		this.fileAttribute = fileAttribute;
	}

	public String getFileCommand() {
		return fileCommand;
	}

	public void setFileCommand(String fileCommand) {
		this.fileCommand = fileCommand;
	}
	
	public String getTpSendTime() {
		return tpSendTime;
	}
	public void setTpSendTime(String tpSendTime) {
		this.tpSendTime = tpSendTime;
	}
	public int getTpTimeout() {
		return tpTimeout;
	}
	public void setTpTimeout(int tpTimeout) {
		this.tpTimeout = tpTimeout;
	}

	public String getSoftVersion() {
		return softVersion;
	}

	public void setSoftVersion(String softVersion) {
		this.softVersion = softVersion;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFileIPAddr() {
		return fileIPAddr;
	}

	public void setFileIPAddr(String fileIPAddr) {
		this.fileIPAddr = fileIPAddr;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getSoftUpgradeID() {
		return softUpgradeID;
	}

	public void setSoftUpgradeID(long softUpgradeID) {
		this.softUpgradeID = softUpgradeID;
	}

	public int getCurrentMessage() {
		return currentMessage;
	}

	public void setCurrentMessage(int currentMessage) {
		this.currentMessage = currentMessage;
	}

	public int getTotalMessageCount() {
		return totalMessageCount;
	}

	public void setTotalMessageCount(int totalMessageCount) {
		this.totalMessageCount = totalMessageCount;
	}

}