package cn.hexing.fk.bp.model;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-4 上午10:13:50
 *
 * @info Dlms多量纲实体类，根据表类型处理
 */
public class DlmsMultiScale {
	
	/**表类型*/
	private String meterType;
	/**子规约*/
	private String subProtocol;
	/**OBIS*/
	private String itemId;
	/**量纲*/
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
