package cn.hexing.fk.model;
/**
 * Dlms�������� ����������Ĺ�ϵ
 * @author Administrator
 *
 */
public class DlmsItemRelated {
	
	/**
	 * classId+obis+attr;
	 */
	String attribute;
	
	String code;

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
}
