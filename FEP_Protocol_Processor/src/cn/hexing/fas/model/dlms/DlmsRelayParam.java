package cn.hexing.fas.model.dlms;

import java.io.Serializable;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-13 ����03:11:08
 *
 * @info Dlms�м��������
 */
public class DlmsRelayParam implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = -1965276955090438340L;

	//id
	private String itemId;
	
	//deviceId
	private String deviceId;
	
	//op.  set read
	public enum RELAY_OPERATION{OP_GET,OP_SET};
	
	//relay operation
	private RELAY_OPERATION operation = RELAY_OPERATION.OP_GET;
	
	public enum RELAY_PROTOCOL{METER_97,MODBUS};
	
	private RELAY_PROTOCOL relayProtocol = RELAY_PROTOCOL.METER_97;
	
	//relay params
	private String params ;
	
	private String upRelayMessage;
	
	private String resultValue;

	//��ʼ��ַ�������ҵ���ǰ��item��������֡�ͽ�֡
	private String startPos;
	//����Ҫ������ʼ��ַ
	private int offset;
	//һ������ĸ�����������������������
	private int requestNum=1;
	

	public String getItemId() {
		return itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}

	public String getDeviceId() {
		return deviceId;
	}

	public void setDeviceId(String deviceId) {
		this.deviceId = deviceId;
	}

	public RELAY_OPERATION getOperation() {
		return operation;
	}

	public void setOperation(RELAY_OPERATION operation) {
		this.operation = operation;
	}

	public String getParams() {
		return params;
	}

	public void setParams(String params) {
		this.params = params;
	}

	public RELAY_PROTOCOL getRelayProtocol() {
		return relayProtocol;
	}

	public void setRelayProtocol(RELAY_PROTOCOL relayProtocol) {
		this.relayProtocol = relayProtocol;
	}

	public String getUpRelayMessage() {
		return upRelayMessage;
	}

	public void setUpRelayMessage(String upRelayMessage) {
		this.upRelayMessage = upRelayMessage;
	}

	public String getResultValue() {
		return resultValue;
	}

	public void setResultValue(String resultValue) {
		this.resultValue = resultValue;
	}

	public String getStartPos() {
		return startPos;
	}

	public void setStartPos(String startPos) {
		this.startPos = startPos;
	}

	public int getOffset() {
		return offset;
	}

	public void setOffset(int offset) {
		this.offset = offset;
	}

	public int getRequestNum() {
		return requestNum;
	}

	public void setRequestNum(int requestNum) {
		this.requestNum = requestNum;
	}
	
	
}
