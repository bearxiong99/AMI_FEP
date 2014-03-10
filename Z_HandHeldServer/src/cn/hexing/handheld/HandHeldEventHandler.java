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
 * @time 2013-5-3 下午03:11:47
 *
 * @info 掌机事件处理器
 */
public class HandHeldEventHandler extends BasicEventHook{

	
	private static int ACK_SUCCESS =0;
	private static int ACK_ERROR=1;
	
	private static int  k = 0;
	
	@Override
	public void handleEvent(IEvent event) {
		if(event.getType() == EventType.MSG_RECV ){
			//收到消息
			MessageBytes message = (MessageBytes) event.getMessage();
			ReceiveMessageEvent recvEvent = (ReceiveMessageEvent) event;
			IChannel client = recvEvent.getClient();
			AsyncHandheldSocketClient handHeldClient = (AsyncHandheldSocketClient) client;
			if(handHeldClient.read(message)==null) return;
			
			HandHeldResult result = (HandHeldResult) HandHeldMessageDecoder.getInstance().decode(message);

			byte ci_field = result.getCi_filed();
			switch(ci_field){
			case 2://结算数据
				String value =result.getValue();
				//value 以#号分割,每一个#号表示的意思查看规约
				//返回确认帧
				String meterNo = result.getMeterId();
				client.send(createAckMessage(meterNo,ACK_SUCCESS));
				break;
			case 0:{
				String handHeldId=result.getValue();
				//将对应得 handHeld 上次发送的表号设置为已发送
				break;
				}
			case 1:{
				//否认不做处理
				break;
				}
			case 4:  //收到读取表号的请求
				//将当前的表号信息
				HandHeldRequest request = new HandHeldRequest();
				request.setMeterId(result.getMeterId()); 
				//第一个参数0表示还有未发送的表，1表示数据库内无未发送的表
				//表号+密码+refrence号码
				//每次最多发送7个表计信息
				//request.setValue("0#3798000001#00000000#00000000000000A#3798000002#00000000#00000000000000A#3798000003#00000000#00000000000000A#3798000004#00000000#00000000000000A#3798000005#00000000#00000000000000A#3798000006#00000000#00000000000000A#3798000007#00000000#00000000000000A");
				//如果剩余的表计信息不足7个，后面的参数要保证到达七个，中间不要有数据
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
