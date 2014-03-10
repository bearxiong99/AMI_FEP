package dcsml.base;

import java.io.IOException;
import java.util.Arrays;

import com.hx.dlms.DecodeStream;

public class DcsmlSequenceOf extends DcsmlSequence{
	protected int fixedSize = -1;
	protected DcsmlObjectFactory factory = null;

	/**
	 * 
	 */
	private static final long serialVersionUID = -8496534848415756097L;

	@Override
	public boolean decodeContent(DecodeStream input) throws IOException {

		if(null == members){
			int tempSize = fixedSize>0 ? fixedSize : size;
			members = new DcsmlType[tempSize];
			Arrays.fill(members, null);
		}
		boolean result = true;
		int i=0;
		for(;result && i<members.length;i++){
			if( null == members[i] )
				members[i] = factory.create();
			if( members[i].isDecodeDone() )
				continue;
			result = result && (members[i].decode(input));
		}
		
		if( ! result ){
			String msg = "SEQUENCEOF.AXDR.decode exp: size="+members.length+",decoded Index="+i+",input remain="+input.available();
			log.error(msg);
			input.position(0);
			log.warn("SEQUENCEOF.content="+input.toString());
			if( i>0 ){
				members = Arrays.copyOf(members, i);
			}
			//throw new IOException(msg);
		}
		decodeState = DecodeState.DECODE_DONE;
		return true;
	}


}
