package dcsml.base;

public class ListOfSmlProfObjPeriodEntry extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6019984216363223930L;

	public ListOfSmlProfObjPeriodEntry(){
		factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new SmlProfObjPeriodEntry();
			}
		};
	}
	
	@Override
	public String toString(){

		
		StringBuilder sb = new StringBuilder();
		
		for(DcsmlType type:members){
			SmlProfObjPeriodEntry entry = (SmlProfObjPeriodEntry)type;
			sb.append(""+entry);
		}
		return sb.toString();
	
	}
	
	
}
