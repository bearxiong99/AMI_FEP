package cn.hexing.fk.bp.model;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-4 ����10:13:50
 *
 * @info Dlms������ʵ���࣬���ݱ����ʹ���
 */
public class DlmsMultiScale {
	
	/**������*/
	private String meterType;
	/**�ӹ�Լ*/
	private String subProtocol;
	/**OBIS*/
	private String itemId;
	/**����*/
	private int scale;

	public String getMeterType() {
		return meterType;
	}

	public void setMeterType(String meterType) {
		this.meterType = meterType;
	}

	public String getSubProtocol() {
		return subProtocol;
	}

	public void setSubProtocol(String subProtocol) {
		this.subProtocol = subProtocol;
	}

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}
}
