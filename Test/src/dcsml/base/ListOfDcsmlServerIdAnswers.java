package dcsml.base;

public class ListOfDcsmlServerIdAnswers extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = 594854791287742932L;
	
	public ListOfDcsmlServerIdAnswers(){
		this.factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new DcsmlServerIdData();
			}
		};
	}
	
	@Override
	public String toString(){
		if(members==null) return "";
		StringBuilder sb = new StringBuilder();
		for(DcsmlType type:members){
			DcsmlServerIdData str = (DcsmlServerIdData) type;
			sb.append("\t"+str.toString());
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();
	}

}
