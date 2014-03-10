package dcsml.base;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.EncodeStream;

/**
 * ASN.1 SEQUENCE type.
 * Sequence type contain 1 or more type.
 * If a type is optional, any type behind it must unique. 
 * @author hbao
 *
 */
public class DcsmlSequence extends DcsmlType {
	private static final long serialVersionUID = 7682083975430928180L;
	protected DcsmlType[] members = null;
	protected boolean optionalMembers = false;
	
	protected int type=7;
	
	protected int memberSize;
	
	public DcsmlSequence( ){
		super(CLASS_UNIVERSAL,TAG_SEQUENCE,PC_CONSTRUCTED,0,false);
	}
	
	public DcsmlSequence(int tagNumber){
		super(CLASS_UNIVERSAL,tagNumber,PC_CONSTRUCTED,0,false);
	}
	
	/**
	 * Used by SequenceOf type
	 * @param tagNumber
	 * @param fixedLen
	 */
	public DcsmlSequence(int tagNumber,int fixedLen){
		super(CLASS_UNIVERSAL,tagNumber,PC_CONSTRUCTED,fixedLen,false);
	}
	
	public DcsmlSequence(DcsmlType[] types){
		this();
		
		if( null== types || types.length == 0 )
			throw new RuntimeException("SEQUENCE contains no type.");
		//Validation types.
		int optIndex = -1;
		for(int i=0; i<types.length; i++ ){
			if( optIndex>=0 ){
				//Make sure that any type must unique after optIndex
				for(int j=optIndex; j<i; j++ ){
					if( types[j].identifier() == types[i].identifier() )
						throw new RuntimeException("Sequence contain duplicated id:"+types[i].identifier());
				}
			}
			else if( types[i].isOptional() ){
				optIndex = i;
			}
		}
		members = types;
	}

	@Override
	public DcsmlType codec(int myCodec) {
		super.codec(myCodec);
		if( null != members ){
			for(int i=0; i<members.length; i++ ){
				if( null != members[i] )
					members[i].codec(myCodec);
			}
		}
		return this;
	}

	protected byte[] encodeMembers() throws IOException{
		if( null == members || members.length == 0 )
			return null;
		EncodeStream tmpEncoder = new EncodeStream();
		for(int i=0; i<members.length; i++)
			members[i].encode(tmpEncoder);
		byte[] result = tmpEncoder.dump();
		if( result.length == 0 )
			return null;
		return result;
	}
	
	@Override
	public void encode(EncodeStream output) throws IOException{
		encodeTLFeiled(output);
		if( null == value )
			value = encodeMembers();
		super.encode(output);
	}
	
	public void encodeTLFeiled(EncodeStream output){
		output.write((byte)type<<4|memberSize);
		
		//¶à¸öTLField
	}
	
	@Override
	public boolean decodeLength(DecodeStream input) throws IOException{
		if( isAxdrCodec() ){
			decodeState = DecodeState.DECODE_VALUE;
			return true;
		}
		return super.decodeLength(input);
	}

	private void decodeSequenceMembers(DecodeStream _input) throws IOException {
		//It is called when content is ready.
		DecodeStream input = _input;
		if(null != value && value.length>0 )
			input = DecodeStream.wrap(ByteBuffer.wrap(value));
		boolean decodeMember = true;
		int i=0;
		for(; i<members.length && input.available()>0 && decodeMember; i++){
			DcsmlType t = members[i];
			decodeMember = t.decode(input);
		}
		if( i != members.length && !optionalMembers ){
			String msg = "SEQUENCE decode error: at i="+i+",remain data="+input.available();
			log.error(msg);
			throw new IOException(msg);
		}
	}
	
	@Override
	protected void onDecodeConstructedComplete(DecodeStream input) throws IOException {
		decodeSequenceMembers(input);
	}

	public final DcsmlType[] getMembers() {
		return members;
	}
	
	public final void setOptionalMembers(boolean optMembers){
		optionalMembers = optMembers;
	}
	
	public final boolean isOptionalMembers(){
		return optionalMembers;
	}

	@Override
	public void assignValue(DcsmlType srcObj) {
		super.assignValue(srcObj);
		if(srcObj instanceof DcsmlSequence){
			this.members = ((DcsmlSequence) srcObj).getMembers();
			this.memberSize = ((DcsmlSequence) srcObj).memberSize;
		}
	}
	
}
