package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;

/**
 * (�����룺04H,05H)�������б���֡����
 * 
 */
public class C04MessageDecoder extends AbstractMessageDecoder {	
	
    public Object decode(IMessage message) {    
		HostCommand hc=new HostCommand();
		try{			
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpVλ��1��ʾ��������ʱ���ǩ
				data=data.substring(0,data.length()-12);//��ȥ6���ֽڵ�ʱ���ǩ
			if (msg.head.c_acd==1)//ACDλ��1��ʾ���������¼�������
				data=data.substring(0,data.length()-4);//��ȥ2���ֽڵ��¼�������
			String sAFN=Integer.toString(msg.getAFN()& 0xff,16).toUpperCase();
			sAFN=DataSwitch.StrStuff("0", 2, sAFN, "left");
			if (sAFN.equals("04")||sAFN.equals("05"))//����������
				data=data.substring(0,data.length()-32);
			DataValue dataValue=new DataValue();
			while(data.length()>=8){
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), sAFN);
				data=data.substring(8);//��ȥ���ݵ�Ԫ��ʶ
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
    
    