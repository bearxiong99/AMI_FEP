package dcsml.base.applayer;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import dcsml.base.DcsmlOctetString;
import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlType;

public class DcsmlOpenResponse extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = -7547562021080557435L;

	private DcsmlOctetString clientId = new DcsmlOctetString();
	
	private DcsmlOctetString serverId = new DcsmlOctetString();//Optional
	
	public DcsmlOpenResponse(){
		members = new DcsmlType[]{clientId,serverId};
		serverId.setOptional(true);
		adjunct = DcsmlTagAdjunct.contextSpecificExplicit(0x00000101);
		memberSize = 2;
	}
	
	public DcsmlOpenResponse(byte[] clientId){
		this();
		this.clientId.setValue(clientId);
		memberSize=1;
	}
	
	public DcsmlOpenResponse(byte[] clientId,byte[] serverID){
		this(clientId);
		this.serverId.setValue(serverID);
		this.memberSize=2;
	}
	
	public void setClientId(byte[] clientId){
		this.clientId.setValue(clientId);
	}
	
	public void setServerId(byte[] serverId){
		this.serverId.setValue(serverId);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlOpenResponse dor = new DcsmlOpenResponse("abc".getBytes(),"123a".getBytes());
		System.out.println(HexDump.toHex(dor.encode()));
	}
	
	
}
