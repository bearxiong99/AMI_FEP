package dcsml.base;


public class DcsmlServerIdData extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8816515873914083706L;

	
	private DcsmlOctetString serverId = new DcsmlOctetString();
	
	//headList
	private ListOfSmlProfObjHeaderEntry headerEntry = new ListOfSmlProfObjHeaderEntry();
	//periodList
	private ListOfSmlProfObjPeriodEntry periodEntry = new ListOfSmlProfObjPeriodEntry();
	
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\n\t\t serverId:"+new String(serverId.getValue()));
		sb.append("\n\t\t headerEntry:"+headerEntry.toString());
		sb.append("\n\t\t periodEntry:"+periodEntry.toString());
		return sb.toString();
	}
	
	
	public DcsmlServerIdData(){
		members = new DcsmlType[]{serverId,headerEntry,periodEntry};
		memberSize = 3;
	}
	
	
	
}
