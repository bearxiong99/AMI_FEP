package dcsml.base.applayer;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;

import dcsml.base.DcsmlOctetString;
import dcsml.base.DcsmlSequence;
import dcsml.base.DcsmlTagAdjunct;
import dcsml.base.DcsmlType;

public class DcsmlMeterTransparentResponse extends DcsmlSequence{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3980384172982287861L;
	
	
	private DcsmlOctetString serverId = new DcsmlOctetString();
	
	private DcsmlOctetString rawData = new DcsmlOctetString();
	
	public DcsmlMeterTransparentResponse(){
		adjunct = DcsmlTagAdjunct.contextSpecificExplicit(0x10001101);

		members = new DcsmlType[]{serverId,rawData};
		
		memberSize = 2;
	}
	
	@Override
	public String toString(){
		
		StringBuilder sb = new StringBuilder();
		sb.append("serverId:"+new String(serverId.getValue()));
		sb.append("\nrawData:"+HexDump.toHex(rawData.getValue()));
		
		return sb.toString();
	}
	
	
	public static void main(String[] args) throws IOException {
		SmlMessage sm = new SmlMessage();
		sm.decode(new DecodeStream(HexDump.toArray("760700000000000262006200726510001101720948584530303030310606C401810063001200")));
		System.out.println(sm);
	
	}

}
