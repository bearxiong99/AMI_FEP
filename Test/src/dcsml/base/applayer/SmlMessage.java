package dcsml.base.applayer;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.EncodeStream;

import dcsml.base.DcsmlCrc16;
import dcsml.base.DcsmlOctetString;
import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlType;
import dcsml.base.DcsmlUnsigned8;

public class SmlMessage extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = 7835409533750920604L;

	
	private DcsmlOctetString transactionID = new DcsmlOctetString("0000".getBytes());
	
	private DcsmlUnsigned8 groupId = new DcsmlUnsigned8(1);
	
	private DcsmlUnsigned8 abortOnError = new DcsmlUnsigned8(2);
	
	private DcsmlMessageBody messageBody = new DcsmlMessageBody();
	
	private DcsmlCrc16 crc16 = new DcsmlCrc16(12);
	
	//end of message
	
	public SmlMessage(){
		members = new DcsmlType[]{transactionID,groupId,abortOnError,messageBody};
		memberSize = 6;
	}
	
	public void chooseMessageBody(DcsmlType obj){
		messageBody.choose(obj);
	}

	@Override
	public void encode(EncodeStream output) throws IOException {
		encodeTLFeiled(output);
		if( null == value )
			value = encodeMembers();
		output.write(value);
		crc16.encode(output);//comput crc16
		output.write(0x00);//end of message
	}
	
	
	public static void main(String[] args) throws IOException {
		SmlMessage sm = new SmlMessage();
		DcsmlOpenRequest sdf = new DcsmlOpenRequest();
		sdf.setClientId("12323".getBytes());
		sm.chooseMessageBody(sdf);
		System.out.println(HexDump.toHex(sm.encode()));
		sm=  new SmlMessage();
		sm.decode(new DecodeStream(HexDump.toArray("7607000000000001620062007265000001017211434C49454E542D4C364842454356435209303030303030303163cccc00")));
		DcsmlMessageBody body = sm.getMessageBody();
		DcsmlType resultObj = body.getDecodedObject();
		
		System.out.println(resultObj);
		
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("SmlMessage:");
		sb.append("\n\t transactionID:"+HexDump.toHex(transactionID.getValue()));
		sb.append("\n\t messageBody:"+messageBody.toString());
		sb.append("\n\t crc16:"+HexDump.toHex(crc16.getValue()));
		return sb.toString();
	}
	

	@Override
	public boolean decode(DecodeStream input) throws IOException {
		super.decode(input);
	 
		crc16.decode(input);
		
		input.read();//end of message
		
		return true;
	}

	public DcsmlMessageBody getMessageBody() {
		return messageBody;
	}

	
	
}
