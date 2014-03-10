package dcsml.base.applayer;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;
import dcsml.base.DcsmlOctetString;
import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlType;
import dcsml.base.DcsmlUnsigned8;

public class DcsmlMeterTransparentRequest extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3980384172982287861L;
	
	private DcsmlUnsigned8 priority = new DcsmlUnsigned8();
	
	private DcsmlOctetString serverId = new DcsmlOctetString();
	
	private DcsmlOctetString rawData = new DcsmlOctetString();
	
	public DcsmlMeterTransparentRequest(){
		adjunct = DcsmlTagAdjunct.contextSpecificExplicit(0x10001100);

		members = new DcsmlType[]{priority,serverId,rawData};
		
		memberSize = 3;
	}
	
	
	
	@Override
	public String toString(){
		return super.toString();
	}
	
	public static void main(String[] args) throws IOException {
		DcsmlMeterTransparentRequest request = new DcsmlMeterTransparentRequest();
		request.setPriority(4);
		request.setServerId("HXE00001".getBytes());
		request.setRawData(HexDump.toArray("C0014100030100010800FF0200"));
		SmlMessage sm = new SmlMessage();
		sm.chooseMessageBody(request);
		System.out.println(HexDump.toHex(sm.encode()));
	}



	public void setPriority(int value) {
		this.priority.setValue(value);
	}



	public void setServerId(byte[] value) {
		this.serverId.setValue(value);
	}



	public void setRawData(byte[] rawData) {
		this.rawData.setValue(rawData);
	}
	

}
