package cn.hexing.fas.model;

import java.io.Serializable;

public class DataItem  implements Serializable{
	private static final long serialVersionUID = 4L;
	/** ������� */
    private String tn;
	/** ���ݱ�ʶ */
    private String code;
    /** ����ֵ */
    private String value;
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	
}
