package dcsml.base;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Constants;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.EncodeStream;

public class DcsmlOctetString extends DcsmlType {
	private static final long serialVersionUID = 1561673472159826574L;

	public DcsmlOctetString(){
		this(ASN1Constants.TAG_OCTETSTRING,-1);
	}
	
	public DcsmlOctetString(int size){
		this(ASN1Constants.TAG_OCTETSTRING,size);
	}
	
	protected DcsmlOctetString(int tagNum,int fixedLen){
		super(tagNum,fixedLen);
	}
	
	public DcsmlOctetString(byte[] octValue){
		this();
		value = octValue;
	}
	
	public DcsmlOctetString(int size,byte[] octValue){
		this(size);
		value = octValue;
	}

	public void setValue(byte[] initValue){
		value = initValue;
	}
	
	public byte[] getValue(){
		return value;
	}

	@Override
	protected void onDecodeConstructedComplete(DecodeStream _input) {
		DecodeStream input = DecodeStream.wrap(ByteBuffer.wrap(value));
		ByteArrayOutputStream outStream = new ByteArrayOutputStream(value.length);
		try {
			while( input.available()>2 ){
				DcsmlOctetString octs = new DcsmlOctetString();
				octs.decode(input);
				outStream.write(octs.value);
			}
			value = outStream.toByteArray();
			length = value.length;
		} catch (IOException e) {
		}
	}
	
	public static void main(String[] args) throws IOException {
		DcsmlOctetString dos = new DcsmlOctetString("123321".getBytes());
		System.out.println(HexDump.toHex(dos.encode()));
	}

	@Override
	public void encodeLength(EncodeStream output) throws IOException {
		if( null == value ){
			if(!isOptional())
				output.write(0);
			return ;  //ASN1 NULL ?
		}
		length = value.length+1;
		doEncodeLength(length,output);
	}


}
