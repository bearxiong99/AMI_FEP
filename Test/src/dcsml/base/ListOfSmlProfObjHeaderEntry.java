package dcsml.base;

public class ListOfSmlProfObjHeaderEntry extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6019984216363223930L;

	
	public ListOfSmlProfObjHeaderEntry(){
		
		factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new SmlProfObjHeaderEntry();
			}
		};
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		
		for(DcsmlType type:members){
			SmlProfObjHeaderEntry entry = (SmlProfObjHeaderEntry)type;
			sb.append("\t\t\t"+entry);
		}
		return sb.toString();
	}
	
	
}
