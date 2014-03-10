package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.EsamUtil;

/**
 * ���������·ǶԳ���Կ���ݽ���
 * @author Administrator
 *
 */
public class C06MessageDecoder extends AbstractMessageDecoder {

	/**������վ��Կ*/
	private static final String UPDATE_MASTER_PUBKEY = "06F020";
	
	/**���¼�������Կ*/
	private static final String UPDATE_TERMINAL_PUBKEY = "06F021";
	
	@Override
	public Object decode(IMessage message) {
		HostCommand hc = new HostCommand();
		try{
			String data=ParseTool.getMsgData(message);
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpVλ��1��ʾ��������ʱ���ǩ
				data=data.substring(0,data.length()-12);//��ȥ6���ֽڵ�ʱ���ǩ
			if (msg.head.c_acd==1)//ACDλ��1��ʾ���������¼�������
				data=data.substring(0,data.length()-4);//��ȥ2���ֽڵ��¼�������
			
			BizRtu rtu = RtuManage.getInstance().getBizRtuInCache(message.getLogicalAddress());
			
			String[] codes = DataItemParser.dataCodeParser(data.substring(4,8), "06");
			
			data=data.substring(8); //��ȥ��Ԫ����
			if(codes.length>0){
				if(UPDATE_MASTER_PUBKEY.equals(codes[0])){
					//�Ƚ���־λȡ����,0��ʾȡ�������1��ʾ���½��
					DataValue value = DataItemParser.parser(data, "HTB1", false);
					data=data.substring(2);
					String sValue="";
					if(value.getValue().equals("0")){//ȡ�����
						sValue = data.substring(0,32);
					}else if(value.getValue().equals("1")){
						data = data.substring(8);
						sValue =data.substring(0, 4);
						sValue = EsamUtil.getInstance().decript(rtu, sValue, msg.getFseq(),0,"");
						//���³ɹ���ʧ�ܣ�֪ͨ��վ
						HostCommandResult hcr=new HostCommandResult();
						hcr.setCode(codes[0]);
						hcr.setTn("0");
						hcr.setValue("01");
						if("6108".equals(sValue)){//������վ��Կ�ɹ�
							hcr.setValue("00");
						}
						hc.setStatus(HostCommand.STATUS_SUCCESS);
						//���ܸ��³ɹ���ʧ��
						hc.addResult(hcr);
						return hc;
					}
					return sValue;
				}else if(UPDATE_TERMINAL_PUBKEY.equals(codes[0])){
					//format: �Ƿ��״�FLAG+SEQ+48�ֽڹ�Կ[+48�ֽڹ�ԿTAG]
					//���ܸ��³ɹ���ʧ��
					boolean updateSuccess = false;
					String sValue = "";
					boolean isFirstTime=Integer.parseInt(data.substring(0, 2))==0?true:false;
					data = data.substring(2+8);//��FLAG��SEQȥ��
					if(isFirstTime){//ֻ��48���ֽڵĹ�Կ
						sValue = data.substring(0,48*2);
						//���ܼ�������Կ
						sValue=EsamUtil.getInstance().decript(rtu, sValue, msg.getFseq(),0,"");
						updateSuccess=true;
					}else{
						sValue = data.substring(0,48*2);
						data=data.substring(48*2);
						String signature=data.substring(0, 48*2);
						data=data.substring(48*2);
						//��ǩ
						updateSuccess=EsamUtil.getInstance().pubKeyVerify(rtu,sValue, signature,sValue);
					}
					
					//���ܣ���ǩ
					HostCommandResult hcr = new HostCommandResult();
					hcr.setValue("01");
					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
					if(updateSuccess){
						hcr.setValue(sValue);	
						hc.setStatus(HostCommand.STATUS_SUCCESS);
					}
					hcr.setCode(codes[0]);
					hcr.setTn("0");
					hc.addResult(hcr);

				}
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		
		return hc;
	}

//	private String getSystitle(String logicalAddress, int fseq) {
//		ByteBuffer buf = ByteBuffer.allocate(12);
//		buf.put((byte)0x48).put((byte)0x58).put((byte)0x45).put((byte)0x11);
//		byte[] bytes = HexDump.toArray(logicalAddress);
//		bytes[0] = (byte) (bytes[0] & 0x0F);
//		buf.put(bytes);
//		buf.putInt( fseq );
//		buf.flip();
//		return HexDump.toHex(buf.array());
//	}

}
