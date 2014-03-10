package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;

/**
 * 查询参数(功能码：0AH)响应消息解码器
 * 
 */
public class C0AMessageDecoder  extends AbstractMessageDecoder{
	public Object decode(IMessage message) {
		HostCommand hc=new HostCommand();
		try{			
			String data=ParseTool.getMsgData(message);
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器		
			DataValue dataValue=new DataValue();
			while(data.length()>=8){
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "04");
				data=data.substring(8);//消去数据单元标识
				for (int i=0;i<tn.length;i++){
					for (int j=0;j<codes.length;j++){
						ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(codes[j]);							
						dataValue=DataItemParser.parser(data, pdc.getFormat(),false);
						data=data.substring(dataValue.getLen());
						HostCommandResult hcr=new HostCommandResult();
						hcr.setCode(codes[j]);
						hcr.setTn(""+tn[i]);
						hcr.setValue(dataValue.getValue());																
						hc.addResult(hcr);		
					}
				}
			}
			hc.setStatus(HostCommand.STATUS_SUCCESS);																
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;	
	}
	
	
}
