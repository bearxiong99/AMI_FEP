package cn.hexing.fk.bp.model;

import java.util.Date;
/**
 * �Ƿ�������־��ӳ����
 */
public class MessageLogErr {
	/** �ն��߼���ַ */
    private String logicAddress;
    /** ������(�ն��߼���ַǰ��λ) */
    private String qym;
    /** ������ */
    private String kzm;
    /** ͨѶʱ�� */
    private Date time;   
    /** ԭʼ���� */
    private String body;
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		//���ƷǷ�������ⳤ�ȣ����Ᵽ��������Ƴ��������ݿ���ֶγ���һ��
		if (body.length()>=4000)
			body=body.substring(0,3999);
		this.body = body;
	}
	public String getKzm() {
		return kzm;
	}
	public void setKzm(String kzm) {
		this.kzm = kzm;
	}
	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getQym() {
		return qym;
	}
	public void setQym(String qym) {
		this.qym = qym;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
}
