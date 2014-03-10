package cn.hexing.fk.model;

import java.io.Serializable;
import java.util.Date;


/**
 * 
 * @author gaoll
 *
 * @time 2013-2-17 ����2:06:43
 *
 * @info ������Ϣ
 * 
 */
public class UpgradeInfo implements Serializable{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 278785093401013791L;
	/**����״̬:�����ɹ�=0,�ȴ�����1,��������=2,������ֹ=3,�ȴ�����=4,���ڲ���=5,������ֹ=6,���λͼ���ʧ��=7,��֤������ʧ��=8,���������ʧ��=9,������Чʱ��ʧ��=10,����ʧ��=255*/
	private int status;
	/**�ն��߼���ַ*/
	private String logicAddr;
	/**������*/
	private int tn;
	/**�ļ�ͷ��Ϣ*/
	private String fileHead;
	/**������*/
	private String reissueBlock;
	/**������*/
	private int blockCount;
	/**��ǰ���Ϳ��*/
	private int curBlockNum;
	/**FtpĿ¼*/
	private String ftpDir;
	/**Ftp�û���*/
	private String ftpUserName;
	/**Ftp����*/
	private String ftpPassword;
	/**Ftp�˿�*/
	private int ftpPort;
	/**FtpIp*/
	private String ftpIp;
	/**�ļ���*/
	private String fileName;
	/**��Чʱ��*/
	private	Date effectDate;
	/**ÿ֡��������ֽ���,���ڼ����ܹ�����֡*/
	private int maxSize;
	
	private String protocol;
	
	private long softUpgradeID;
	
	public String getProtocol() {
		return protocol;
	}

	public void setProtocol(String protocol) {
		this.protocol = protocol;
	}

	/**�����ɹ�*/
	public static final int SUCCESS=0;
	/**�ȴ�����*/
	public static final int WAIT_UPGRADE=1;
	/**������*/
	public static final int UPGRADEING=2;
	/**������ֹ*/
	public static final int UPGRADE_PAUSE=3;
	/**�ȴ�����*/
	public static final int WAIT_RESSIUE=4;
	/**������*/
	public static final int RESSIUEING=5;
	/**������ֹ*/
	public static final int RESSIUE_PAUSE=6;
	/**���λͼû�з���*/
	public static final int CHECK_MAP_FAIL=7;
	/**��֤������û�з���*/
	public static final int VERFIY_FILE_FAIL=8;
	/**���������û�з���*/
	public static final int CHECK_FILE_FAIL=9;
	/**������Чʱ��û�з���*/
	public static final int SET_EFFECTTIME_FAIL=10;
	/**��ȡ״̬û�з���*/
	public static final int READ_STATUS_FAIL=12;
	/**������ʼ��û�з���*/
	public static final int WAIT_UPGARDEINIT=13; 
	/**����ʧ��*/
	public static final int FAIL=255;

	public final String getLogicAddr() {
		return logicAddr;
	}

	public final void setLogicAddr(String logicAddr) {
		this.logicAddr = logicAddr;
	}

	public final int getTn() {
		return tn;
	}

	public final void setTn(int tn) {
		this.tn = tn;
	}

	public final String getFileHead() {
		return fileHead;
	}

	public final void setFileHead(String fileHead) {
		this.fileHead = fileHead;
	}

	public final int getStatus() {
		return status;
	}

	public final void setStatus(int status) {
		this.status = status;
	}

	public final String getReissueBlock() {
		return reissueBlock;
	}

	public final void setReissueBlock(String reissueBlock) {
		this.reissueBlock = reissueBlock;
	}

	public final int getBlockCount() {
		return blockCount;
	}

	public final void setBlockCount(int blockCount) {
		this.blockCount = blockCount;
	}

	public final int getCurBlockNum() {
		return curBlockNum;
	}

	public final void setCurBlockNum(int curBlockNum) {
		this.curBlockNum = curBlockNum;
	}

	public final String getFtpDir() {
		return ftpDir;
	}

	public final void setFtpDir(String ftpDir) {
		this.ftpDir = ftpDir;
	}

	public final String getFtpUserName() {
		return ftpUserName;
	}

	public final void setFtpUserName(String ftpUserName) {
		this.ftpUserName = ftpUserName;
	}

	public final String getFtpPassword() {
		return ftpPassword;
	}

	public final void setFtpPassword(String ftpPassword) {
		this.ftpPassword = ftpPassword;
	}

	public final int getFtpPort() {
		return ftpPort;
	}

	public final void setFtpPort(int ftpPort) {
		this.ftpPort = ftpPort;
	}

	public final String getFtpIp() {
		return ftpIp;
	}

	public final void setFtpIp(String ftpIp) {
		this.ftpIp = ftpIp;
	}

	public final String getFileName() {
		return fileName;
	}

	public final void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public final Date getEffectDate() {
		return effectDate;
	}

	public final void setEffectDate(Date effectDate) {
		this.effectDate = effectDate;
	}

	public final int getMaxSize() {
		return maxSize;
	}

	public final void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public long getSoftUpgradeID() {
		return softUpgradeID;
	}

	public void setSoftUpgradeID(long softUpgradeID) {
		this.softUpgradeID = softUpgradeID;
	}


}
