package dcsml.base;


public class SmlTreePath extends DcsmlSequenceOf{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1764977962941246957L;
	
	
	
	public SmlTreePath(){
		factory = new DcsmlObjectFactory() {
			
			@Override
			public DcsmlType create() {
				return new DcsmlOctetString();
			}
		};
	}
	

	@Override
	public String toString(){
		if(members==null) return "";
		StringBuilder sb = new StringBuilder();
		for(DcsmlType type:members){
			DcsmlOctetString str = (DcsmlOctetString) type;
			sb.append("\n\t\t\t\t"+new String(str.getValue()));
		}
		return sb.toString();
	}
	
	public SmlTreePath(DcsmlOctetString[] treePath){
		members=treePath;
		memberSize=treePath.length;
	}
	
}
