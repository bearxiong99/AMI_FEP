package dcsml.base;

public class ListOfSmlValueEntry extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2206594614120924393L;
	
	public ListOfSmlValueEntry(){
		factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new SmlValue();
			}
		};
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		for(DcsmlType type:members){
			SmlValue value = (SmlValue) type;
			sb.append("\n\t\t\t\t"+value);
		}
		return sb.toString();
	}

}
