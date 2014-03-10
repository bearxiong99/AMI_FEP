package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.ParseTool;

/**
 * ȷ������(�����룺00H)��Ӧ��Ϣ������
 * 
 */
public class C00MessageDecoder extends AbstractMessageDecoder {	
	
    public Object decode(IMessage message) {    
		HostCommand hc=new HostCommand();
		try{			
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpVλ��1��ʾ��������ʱ���ǩ
				data=data.substring(0,data.length()-12);//��ȥ6���ֽڵ�ʱ���ǩ
			if (msg.head.c_acd==1)//ACDλ��1��ʾ���������¼�������
				data=data.substring(0,data.length()-4);//��ȥ2���ֽڵ��¼�������
			//int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
			String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), "00");
			data=data.substring(8);//��ȥ���ݵ�Ԫ��ʶ
			if (codes.length==1){
				//ȫ��ȷ�ϻ�ȫ������
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
				else{//����ȷ�Ϸ���
					String sAFN=data.substring(0,2);
					data=data.substring(2);
					while(data.length()>=10){
						int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
						codes= DataItemParser.dataCodeParser(data.substring(4,8), sAFN);
						data=data.substring(8);//��ȥ���ݵ�Ԫ��ʶ
						String sValue=data.substring(0,2);//�������
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
    
    