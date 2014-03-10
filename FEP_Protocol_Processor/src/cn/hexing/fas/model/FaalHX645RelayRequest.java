package cn.hexing.fas.model;

public class FaalHX645RelayRequest extends FaalGWAFN10Request{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String dataArea;
	//01¶Á£¬04Ð´
	private String op;
	
	private String id;

	public final String getDataArea() {
		return dataArea;
	}

	public final void setDataArea(String dataArea) {
		this.dataArea = dataArea;
	}

	public final String getOp() {
		return op;
	}

	public final void setOp(String op) {
		this.op = op;
	}

	public final String getId() {
		return id;
	}

	public final void setId(String id) {
		this.id = id;
	}

}
