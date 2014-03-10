package cn.hexing.fas.protocol.handheld;

import java.nio.ByteBuffer;

import cn.hexing.fas.model.handheld.HandHeldResult;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.pos.parse.DataItemParser;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.FCS;
import cn.hexing.fk.utils.HexDump;

public class HandHeldMessageDecoder extends AbstractMessageDecoder{
	private static HandHeldMessageDecoder instance = new HandHeldMessageDecoder();
	public  static HandHeldMessageDecoder getInstance(){return instance;}

	@Override
	public Object decode(IMessage message) {
		
		if(!(message instanceof MessageBytes)) return null;
		MessageBytes msg = (MessageBytes) message;
		ByteBuffer allFrame=ByteBuffer.wrap(msg.data);
		if(allFrame.remaining()<12) return null;
		
		//计算cs
		String calcCs = FCS.fcs(HexDump.toHex(msg.data).substring(0,( msg.data.length-3)*2));
		
		//将头取出来
		byte[] head = new byte[12];
		allFrame.get(head, 0, 12);

		//将数据区取出来
		byte[] data = new byte[allFrame.remaining()-3];
		allFrame.get(data);
		
		byte[] cs = new byte[2];
		allFrame.get(cs);
		if(!DataSwitch.ReverseStringByByte(calcCs).equalsIgnoreCase(HexDump.toHex(cs))){
			//校验码错误
			return null;			
		}
		byte ciField = head[10];
		byte packetNum=head[11];
		String s_data=HexDump.toHex(data);
		HandHeldResult value=parse(ciField,s_data);
		
		if(value==null) return null;
		
		value.setCi_filed(ciField);
		value.setPacketNum(packetNum);
		value.setMeterId(DataSwitch.ReverseStringByByte(HexDump.hexDump(head, 5, 5).replaceAll(" ","")));
		return value;
	}
	public static void main(String[] args) {
		MessageBytes message = new MessageBytes();
		message.data = HexDump.toArray("681B1B68080000000000040031313233000000000000000000000000000000F18716");
		System.out.println(HandHeldMessageDecoder.getInstance().decode(message));
	}
	private HandHeldResult parse(byte ciField, String data) {
		
		HandHeldResult hhr = new HandHeldResult();
		if(ciField == 0x05){ //																												05特殊处理。不提供数据区长度，只能如此,掌机规约，实在不想做。-_-!!
			hhr.setValue( new String(HexDump.toArray(data)));
		}else{
			String format = HandHeldItemConfig.itemUpMap.get(""+ciField);
			DataValue value = DataItemParser.parser(data, format, false);
			hhr.setValue(value.getValue());
		}
		return hhr;
	}
}
