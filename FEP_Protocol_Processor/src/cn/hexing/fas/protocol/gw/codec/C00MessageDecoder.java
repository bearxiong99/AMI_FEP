package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.ParseTool;

/**
 * 确认命令(功能码：00H)响应消息解码器
 * 
 */
public class C00MessageDecoder extends AbstractMessageDecoder {	
	
    public Object decode(IMessage message) {    
		HostCommand hc=new HostCommand();
		try{			
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			//int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
			String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "00");
			data=data.substring(8);//消去数据单元标识
			if (codes.length==1){
				//全部确认或全部否认
				if (codes[0].equals("00F001")||codes[0].equals("00F002")){
					HostCommandResult hcr=new HostCommandResult();
					hcr.setCode(codes[0]);
					hcr.setTn("0");
					hcr.setValue("00");
					hc.addResult(hcr);
					if (codes[0].equals("00F001"))
						hc.setStatus(HostCommand.STATUS_SUCCESS);
					else
						hc.setStatus(HostCommand.STATUS_RTU_FAILED);
				}					
				else{//部分确认否认
					String sAFN=data.substring(0,2);
					data=data.substring(2);
					while(data.length()>=10){
						int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
						codes= DataItemParser.dataCodeParser(data.substring(4,8), sAFN);
						data=data.substring(8);//消去数据单元标识
						String sValue=data.substring(0,2);//错误编码
						data=data.substring(2);
						for (int i=0;i<tn.length;i++){
							for (int j=0;j<codes.length;j++){
								HostCommandResult hcr=new HostCommandResult();
								hcr.setCode(codes[j]);
								hcr.setTn(""+tn[i]);
								hcr.setValue(sValue);																
								hc.addResult(hcr);								
							}
						}
					}	
					hc.setStatus(HostCommand.STATUS_SUCCESS);
				}														
			}
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;	
    }
}
    
    