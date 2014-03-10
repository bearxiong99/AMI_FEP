package cn.hexing.fas.model;

/**
 * ����AFN=10����������
 */
public class FaalGWAFN10Request extends FaalRequest {

    private static final long serialVersionUID = -462982183058685616L; 
    /** ���з���ʱ�� */
    private String tpSendTime;
    /** ���з��ʹ��䳬ʱʱ�� */
    private int tpTimeout;
    /** �ն�ͨ�Ŷ˿ں� */
    private int port=2;
    /** ͸��ת��ͨ�ſ����� */
    private String kzz="01101011";
    /** ͸��ת�����յȴ����ĳ�ʱʱ�� */
    private String msgTimeout="133";
    /** ͸��ת�����յȴ��ֽڳ�ʱʱ�� */
    private int byteTimeout=100;
    /** ָ����ƹ�Լ */
    private String fixProto;	
    /** ָ����Ƶ�ַ */
    private String fixAddre;	
    /** �Ƿ�Ϊ�㲥���� */
    private boolean broadcast = false;
    /** ��ƹ㲥��ַ */
    private String broadcastAddress;
    /** ������Ϣ */
    private String endata;
    /** ת������:F1͸��ת��;F9ת����վֱ�ӶԵ��ĳ������������Ӧ��  F10ת����վֱ�ӶԵ���ң����բ�����բ����*/
    private String transmitType;
    
    public FaalGWAFN10Request() {
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
	public int getPort() {
		return port;
	}
	public void setPort(int port) {
		this.port = port;
	}
	public String getKzz() {
		return kzz;
	}
	public void setKzz(String kzz) {
		this.kzz = kzz;
	}
	public String getMsgTimeout() {
		return msgTimeout;
	}
	public void setMsgTimeout(String msgTimeout) {
		this.msgTimeout = msgTimeout;
	}
	public int getByteTimeout() {
		return byteTimeout;
	}
	public void setByteTimeout(int byteTimeout) {
		this.byteTimeout = byteTimeout;
	}
	public String getFixProto() {
		return fixProto;
	}
	public void setFixProto(String fixProto) {
		this.fixProto = fixProto;
	}
	public String getFixAddre() {
		return fixAddre;
	}
	public void setFixAddre(String fixAddre) {
		this.fixAddre = fixAddre;
	}
	public String getBroadcastAddress() {
		return broadcastAddress;
	}
	public void setBroadcastAddress(String broadcastAddress) {
		this.broadcastAddress = broadcastAddress;
	}
	public boolean getBroadcast() {
		return broadcast;
	}
	public void setBroadcast(boolean broadcast) {
		this.broadcast = broadcast;
	}
	public String getEndata() {
		return endata;
	}
	public void setEndata(String endata) {
		this.endata = endata;
	}
	public String getTransmitType() {
		return transmitType;
	}
	public void setTransmitType(String transmitType) {
		this.transmitType = transmitType;
	}

	
    
}