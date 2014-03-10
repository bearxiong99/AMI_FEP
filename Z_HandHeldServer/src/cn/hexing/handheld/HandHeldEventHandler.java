package cn.hexing.handheld;

import cn.hexing.fas.model.handheld.HandHeldRequest;
import cn.hexing.fas.model.handheld.HandHeldResult;
import cn.hexing.fas.protocol.handheld.HandHeldMessageDecoder;
import cn.hexing.fas.protocol.handheld.HandHeldMessageEncoder;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.AsyncHandheldSocketClient;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-3 ����03:11:47
 *
 * @info �ƻ��¼�������
 */
public class HandHeldEventHandler extends BasicEventHook{

	
	private static int ACK_SUCCESS =0;
	private static int ACK_ERROR=1;
	
	private static int  k = 0;
	
	@Override
	public void handleEvent(IEvent event) {
		if(event.getType() == EventType.MSG_RECV ){
			//�յ���Ϣ
			MessageBytes message = (MessageBytes) event.getMessage();
			ReceiveMessageEvent recvEvent = (ReceiveMessageEvent) event;
			IChannel client = recvEvent.getClient();
			AsyncHandheldSocketClient handHeldClient = (AsyncHandheldSocketClient) client;
			if(handHeldClient.read(message)==null) return;
			
			HandHeldResult result = (HandHeldResult) HandHeldMessageDecoder.getInstance().decode(message);

			byte ci_field = result.getCi_filed();
			switch(ci_field){
			case 2://��������
				String value =result.getValue();
				//value ��#�ŷָ�,ÿһ��#�ű�ʾ����˼�鿴��Լ
				//����ȷ��֡
				String meterNo = result.getMeterId();
				client.send(createAckMessage(meterNo,ACK_SUCCESS));
				break;
			case 0:{
				String handHeldId=result.getValue();
				//����Ӧ�� handHeld �ϴη��͵ı������Ϊ�ѷ���
				break;
				}
			case 1:{
				//���ϲ�������
				break;
				}
			case 4:  //�յ���ȡ��ŵ�����
				//����ǰ�ı����Ϣ
				HandHeldRequest request = new HandHeldRequest();
				request.setMeterId(result.getMeterId()); 
				//��һ������0��ʾ����δ���͵ı�1��ʾ���ݿ�����δ���͵ı�
				//���+����+refrence����
				//ÿ����෢��7�������Ϣ
				//request.setValue("0#3798000001#00000000#00000000000000A#3798000002#00000000#00000000000000A#3798000003#00000000#00000000000000A#3798000004#00000000#00000000000000A#3798000005#00000000#00000000000000A#3798000006#00000000#00000000000000A#3798000007#00000000#00000000000000A");
				//���ʣ��ı����Ϣ����7��������Ĳ���Ҫ��֤�����߸����м䲻Ҫ������
				//request.setValue("1#3798000010#00000000#00000000000000A##################");
				request.setCiField(ci_field);
				IMessage[] msg = HandHeldMessageEncoder.getInstance().encode(request);
				if(msg!=null && msg.length>0){
					client.send(msg[0]);
				}
				break;
			}
		}else if(event.getType() == EventType.ACCEPTCLIENT ){
			System.out.println(event);
		}
	}
	
	private IMessage createAckMessage(String meterNo, int ack) {
		HandHeldRequest hhr = new HandHeldRequest();
		hhr.setCiField((byte) ack);
		hhr.setMeterId(meterNo);
		IMessage[] msg = HandHeldMessageEncoder.getInstance().encode(hhr);
		
		if(msg!=null && msg.length>0){
			return msg[0];
		}
		return  null;
	}

}
