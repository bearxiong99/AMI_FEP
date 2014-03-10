package cn.hexing.pos;


import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.fas.model.pos.PosCommandRequest;
import cn.hexing.fas.model.pos.PosCommandResult;
import cn.hexing.fas.protocol.pos.PosItemConfig;
import cn.hexing.fas.protocol.pos.PosMessageDecoder;
import cn.hexing.fas.protocol.pos.PosMessageEncoder;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.events.BasicEventHook;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.AsyncPosSocketClient;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.sockserver.event.SendMessageEvent;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-13 上午10:25:04
 *
 * @info pos机服务处理器
 */
public class PosServerEventHandler extends BasicEventHook{
	/**成功*/
	public static final String SUCCESS="0#";
	/**操作失败*/
	public static final String OP_FAILED="1#";
	/**无效请求*/
	public static final String INVALID_REQUEST="2#";
	/**用户信息异常*/
	public static final String USER_INFORMATION_ABNORMAL="3#";
	/**安全信息异常*/
	public static final String SECURITY_INFORMATION_ABNORMAL="4#";
	/**生成token出错*/
	public static final String CREATE_TOKEN_ERROR="5#";
	/**生成订单出错*/
	public static final String CREATE_ORDER_ERROR="6#";
	/**修改债务信息出错*/
	public static final String MODIFY_DEBET_FAILED="7#";
	/**系统错误*/
	public static final String SYSTEM_ERROR="8#";
	/**网络异常*/
	public static final String NETWORK_ERROR="9#";
	/**售电用户异常*/
	public static final String CDU_ERROR="10#";
	
	@Override
	public boolean start() {
		return super.start();
	}

	@Override
	public void stop() {
		super.stop();
	}

	@Override
	public void handleEvent(IEvent event) {
		
		if(event.getType() == EventType.MSG_RECV ){
			
			MessageBytes message = (MessageBytes) event.getMessage();
			ReceiveMessageEvent event_receive = (ReceiveMessageEvent) event;
			IChannel client = event_receive.getClient();
			AsyncPosSocketClient posClient = (AsyncPosSocketClient) client;
			
			//要将当前的消息读取完毕，才可以进行下面的操作
			if(null == posClient.read(message)) return;
			
			System.out.println("recvMsg from "+client+"\nmsg Content:"+message);
			
			Object result = PosMessageDecoder.getInstance().decode(message);
			if(result instanceof PosCommandResult){
				PosCommandResult posResult = (PosCommandResult) result;
				
				System.out.println("value:"+posResult.getValue());
				
				PosCommandRequest request = new PosCommandRequest();
				SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
				String param = "";
				request.setSeq(posResult.getSeq());
				IMessage[] msgs = null;
				//所有受到的请求在posResult里获得
				switch(posResult.getFun_c()){
				case PosItemConfig.FUNC_5://请求登陆
					//收到格式:用户名#密码POSID
					//1.如果登陆成功,发送登陆响应
					request.setFun_c(PosItemConfig.FUNC_6);
					Date time = new Date();
					String s_time=sdf.format(time);
					request.setParam("0#"+s_time);
					//2.否则生成错误帧   createFailedResponse
					msgs=PosMessageEncoder.getInstance().encode(request);
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_7://请求撤销交易
					//收到格式：订单号#表号#用户名#密码#security_id  (目前这个功能未使用)
					//生成成功信息或失败信息
					//msgs = createSuccessResponse(request);
					msgs = createFailedResponse(request, INVALID_REQUEST, "invalid_request");
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_F://请求购电
					//收到格式:表号#请求购电数量#请求购电类型#支付方式#支票号码#标示是否有卡返回数据#写卡时间#余额#正向有功电量#状态#原因#历史事件#充值金额#充值金额#CHGKEY_TOKEN1_RESULT#CHGKEY_TOKEN2#CHGKEY_TOKEN2_RESULT#CHGKEY_TOKEN3#CHGKEY_TOKEN3_RESULT#SECURITY_ID
					//1.生成购电请求帧或错误帧。
					//2.param:   订单号#表号#请求购电数量#Energy Cost#Levies#vat#MD Charge#PF Charge#Service Charge#Debt#Subsidy#Total Cost#CreditToken#ChangeKey TOKEN1#ChangeKey TOKEN2#TranferOptin
					request.setFun_c(PosItemConfig.FUNC_4);
					request.setParam("8888888888888888#666666666666#12#12#12#12#12#12#12#12#12#12#121114000000#11111111111111#11111111111111#11111111111111#1");
					msgs=PosMessageEncoder.getInstance().encode(request);
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_9://请求注销登陆
					//收到格式:SECURITY_ID#POSID
					//返回成功或失败
					msgs = createSuccessResponse(request);
					//msgs= createFailedResponse(request, NETWORK_ERROR, "net work error");
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_B: //请求下发token到表里
					//收到格式  订单号#表号#security_id
					//返回成功、或失败
					msgs = createSuccessResponse(request);
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_D: 
					//收到格式：表号#security_id
					//请求获得最后一次未传送的Token,返回04,或者FF
					request.setFun_c(PosItemConfig.FUNC_4);
					request.setParam("8888888888888888#666666666666#12#12#12#12#12#12#12#12#12#12#121114000000###12568965681256896568#1");
					msgs=PosMessageEncoder.getInstance().encode(request);
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_11: //请求打印交班小票d
					//收到格式:  时间#security_id
					//返回数据格式：订单数量#买电用钱#买电量#总共税费#补卡费用#收回债务#现金总计#起始时间#结束时间#security_id(随便)
					request.setFun_c(PosItemConfig.FUNC_12);
					param= "2#12#3#4#3#5#6#121114000000#121115000000#1";
					request.setParam(param);
					msgs=PosMessageEncoder.getInstance().encode(request);
					client.send(msgs[0]);
					break;
				case PosItemConfig.FUNC_13:
					request.setFun_c(PosItemConfig.FUNC_14);
					param="13123#11111#2222#15#21#11#12#14#2#3#2#2#2#130427000000#123#123123#12312#0";
					msgs=PosMessageEncoder.getInstance().encode(request);
					client.send(msgs[0]);
					break;
				default:
					msgs=createFailedResponse(request, INVALID_REQUEST, "invalid request");
					client.send(msgs[0]);
					break;
				}
			}
			
		}if(event.getType() == EventType.MSG_SENT){
			SendMessageEvent sendEvt= (SendMessageEvent) event;
			IChannel client = sendEvt.getClient();
			System.out.println("sendMsg success:"+client+"\nmsg content:"+sendEvt.getMessage());
		}
		else{
			super.handleEvent(event);
		}
	}
	
	/**
	 * 生成错误响应
	 * @param request   
	 * @param errorType  错误类型
	 * @param errorInfo  错误信息
	 * @return
	 */
	private IMessage[] createFailedResponse(PosCommandRequest request,String errorType,String errorInfo){
		request.setFun_c(PosItemConfig.FUNC_FF);
		request.setParam(errorType+errorInfo); 
		return PosMessageEncoder.getInstance().encode(request);
	}
	/**
	 * 生成成功响应
	 * @param request
	 * @return msgs
	 */
	private IMessage[] createSuccessResponse(PosCommandRequest request){
		request.setFun_c(PosItemConfig.FUNC_FF);
		request.setParam(SUCCESS); 
		return PosMessageEncoder.getInstance().encode(request);
	}
}
