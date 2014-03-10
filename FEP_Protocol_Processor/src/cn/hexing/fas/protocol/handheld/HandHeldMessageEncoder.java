package cn.hexing.fas.protocol.handheld;

import java.nio.ByteBuffer;

import cn.hexing.fas.model.handheld.HandHeldRequest;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.handheld.parse.DataItemCoder;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.FCS;
import cn.hexing.fk.utils.HexDump;

public class HandHeldMessageEncoder extends AbstractMessageEncoder{

	private static HandHeldMessageEncoder instance = new HandHeldMessageEncoder();
	
	public static HandHeldMessageEncoder getInstance(){
		return instance;
	}
	
	private HandHeldMessageEncoder(){}
	
	public static void main(String[] args) {
		HandHeldRequest request = new HandHeldRequest();
		request.setMeterId("00000000");
		request.setCiField((byte) 4);
		request.setValue("123123#123##");
		HandHeldMessageEncoder.getInstance().encode(request);
	}
	
	@Override
	public IMessage[] encode(Object obj) {
		
		if(!(obj instanceof HandHeldRequest)) return null;
		
		HandHeldRequest request = (HandHeldRequest) obj;
		byte ciField=request.getCiField();
		String format = HandHeldItemConfig.itemMap.get(""+ciField);
		int payloadLen = 0;
		String payLoad = "";
		if(format!=null){
			payLoad = DataItemCoder.coder(request.getValue(), format);
			payloadLen = payLoad.length()/2;
		}
		ByteBuffer buffer=ByteBuffer.allocate(15+payloadLen);
		buffer.put((byte) 0x68);
		buffer.put((byte)(8+payloadLen));buffer.put((byte)(8+payloadLen));
		buffer.put((byte) 0x68);
		buffer.put(request.getcField());
		buffer.put(HexDump.toArray(DataSwitch.ReverseStringByByte(request.getMeterId()+"0000000000".substring(request.getMeterId().length()))));
		buffer.put(request.getCiField());
		buffer.put(request.getPacketNum());
		buffer.put(HexDump.toArray(payLoad));
		byte[] data = new byte[12+payloadLen];
		System.arraycopy(buffer.array(), 0, data, 0, data.length);
		buffer.put(HexDump.toArray(DataSwitch.ReverseStringByByte(FCS.fcs(HexDump.toHex(data)))));
		buffer.put((byte) 0x16);
		buffer.flip();
		MessageBytes mb = new MessageBytes();
		mb.setData(buffer.array());
		return new IMessage[]{mb};
	}

}
