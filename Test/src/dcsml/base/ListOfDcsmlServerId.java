package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;



public class ListOfDcsmlServerId extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5577787377066420555L;

	public ListOfDcsmlServerId(){
		factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new DcsmlOctetString();
			}
		};
	}
	
	public ListOfDcsmlServerId(String[] serverIds){
		this();
		DcsmlOctetString[] ids = new DcsmlOctetString[serverIds.length];
		for(int i=0;i<ids.length;i++){
			ids[i] = new DcsmlOctetString(serverIds[i].getBytes());
		}
		setServerIds(ids);
	}
	
	
	public ListOfDcsmlServerId(DcsmlOctetString[] serverIds){
		this();
		setServerIds(serverIds);
	}
	
	public void setServerIds(DcsmlOctetString[] serverIds){
		if(serverIds==null){
			throw new RuntimeException("DcsmlSequenceOf can't be null");
		}
		members = new DcsmlType[serverIds.length];
		System.arraycopy(serverIds, 0, members, 0,serverIds.length);
		memberSize = members.length;
	}
	
	

	public static void main(String[] args) throws IOException {
		ListOfDcsmlServerId lods = new ListOfDcsmlServerId(
				new DcsmlOctetString[]{
				new DcsmlOctetString("abcd444".getBytes()),
				new DcsmlOctetString("efgh3".getBytes()),
				new DcsmlOctetString("ijko2".getBytes()),
				new DcsmlOctetString("mnop1".getBytes())
				});
	
		System.out.println(HexDump.toHex(lods.encode()));
	}
	
	
}
