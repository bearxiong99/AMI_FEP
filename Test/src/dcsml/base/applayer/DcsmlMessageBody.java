package dcsml.base.applayer;

import java.io.IOException;
import java.util.HashMap;

import com.hx.dlms.EncodeStream;

import cn.hexing.fk.utils.HexDump;
import dcsml.base.DcsmlChoice;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlType;
import dcsml.base.DcsmlUnsigned32;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-20 ÏÂÎç01:11:53
 *
 * @info messageBody choice
 */
public class DcsmlMessageBody extends DcsmlChoice{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5320517973011544657L;

	private DcsmlOpenRequest openRequest = new DcsmlOpenRequest();
	private DcsmlGetMeterDataRequest getMeterDataRequest = new DcsmlGetMeterDataRequest();
	private DcsmlGetMeterDataResponse getMeterDataResponse = new DcsmlGetMeterDataResponse();
	private DcsmlOpenResponse openResponse = new DcsmlOpenResponse();
	private DcsmlMeterTransparentResponse transparentResponse = new DcsmlMeterTransparentResponse();
	private DcsmlMeterTransparentRequest transparentRequest = new DcsmlMeterTransparentRequest();
	
	
	
	private HashMap<Class<? extends DcsmlType>,Integer> map = new HashMap<Class<? extends DcsmlType>,Integer>();

	@Override
	public String toString(){
		
		if(this.selectedObject ==null ) return null;
		
		return "\t"+selectedObject.toString();
	}
	
	
	
	public DcsmlMessageBody(){
		
		addChoiceMember(openRequest,0x00000100);
		addChoiceMember(getMeterDataRequest, 0x10000800);
		addChoiceMember(getMeterDataResponse, 0x10000801);
		addChoiceMember(openResponse, 0x00000101);
		addChoiceMember(transparentResponse, 0x10001101);
		addChoiceMember(transparentRequest, 0x10001100);

	}
	
	private void addChoiceMember(DcsmlType choiceType,int tagNo){

		if( choiceType.getTagAdjunct() == null ){
			DcsmlTagAdjunct myAdjunct = DcsmlTagAdjunct.contextSpecificImplicit(tagNo);
			myAdjunct.axdrCodec(true);
			choiceType.setTagAdjunct(myAdjunct);
			map.put(choiceType.getClass(), tagNo);
		}
		else
			map.put(choiceType.getClass(), choiceType.getTagAdjunct().identifier() );

		this.addMember(choiceType);
		
	}
	
	
	
	
	public static void main(String[] args) throws IOException {
		DcsmlMessageBody dmb = new DcsmlMessageBody();
		dmb.choose(new DcsmlOpenRequest("xxx".getBytes(), "zzz".getBytes()));
		System.out.println(HexDump.toHex(dmb.encode()));
		
	}

	@Override
	public void encode(EncodeStream output) throws IOException {
		output.write(0x72);//choice
		new DcsmlUnsigned32(selectedObject.identifier()).encode(output);//request id
		selectedObject.encode(output);
		
	}
	
	
}
