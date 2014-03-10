package cn.hexing.fk.fe.rawmsg2db;

import java.util.Date;

import org.apache.cxf.common.util.StringUtils;
import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.batch.event.adapt.BaseLog2DbHandler;
import cn.hexing.fk.fe.cluster.RealtimeSynchronizer;
import cn.hexing.fk.fe.cluster.RtuState;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsMessage;

public class RawMessage2DbHandler extends BaseLog2DbHandler {
	private static final Logger log = Logger.getLogger(RawMessage2DbHandler.class);
	private int defaultLogKey = 5000;
	private int unDocLogKey = 5020;
	
	private int getLogKey(String logicalAddr){
		String dwdm = null;
		int result = defaultLogKey;
		try{
			dwdm = RealtimeSynchronizer.getInstance().getRtuState(logicalAddr).getDwdm();
		}catch(Exception exp){
			log.warn("get RTU dwdm from RTU states exception:"+exp.getLocalizedMessage(),exp);
		}
		if( StringUtils.isEmpty(dwdm)){
			log.error("dwdm is null. so frame save to unDocLogKey.");
			result = unDocLogKey;
		}
		else 
			result = defaultLogKey;
		return result;
	}
	
	@Override
	public void handleLog2Db(AsyncService service, IMessage msg) {
		try{
			MessageLog msgLog=new MessageLog();
			msgLog.setLogicAddress(msg.getLogicalAddress());
			RtuState state = RealtimeSynchronizer.getInstance().getRtuState(msg.getLogicalAddress());
			msgLog.setFwqm(RealtimeSynchronizer.getInstance().getAddressName());
			String regionCode = "";
			if(state==null || "".equals(state.getDwdm())){//TODO:添加时间2012年9月9日15:41:17
				state=RealtimeSynchronizer.getInstance().loadFromDb(msgLog.getLogicAddress());
				if(state!=null){
					RealtimeSynchronizer.getInstance().setRtuState(state);
				}
			}
			if( null != state && null != state.getDwdm() )
				regionCode = state.getDwdm();
			msgLog.setQym( regionCode );
			
			int logKey = this.getLogKey(msg.getLogicalAddress());
			
			boolean dirUp = false;
			byte appFunctionCode = 0;
			if( msg.getMessageType() == MessageType.MSG_ZJ ){
				MessageZj zjmsg = (MessageZj)msg;
				if ( MessageConst.ZJ_FUNC_LOGIN == zjmsg.head.c_func || MessageConst.ZJ_FUNC_HEART ==zjmsg.head.c_func) 	//对控制码为21的登录报文特殊处理，避免当做下行报文保存
					zjmsg.head.c_dir = MessageConst.DIR_UP;
				dirUp = zjmsg.head.c_dir == MessageConst.DIR_UP;
				appFunctionCode = zjmsg.head.c_func;
			}
			else if( msg.getMessageType() == MessageType.MSG_GW_10 ){
				MessageGw gwmsg = (MessageGw)msg;
				dirUp = gwmsg.head.c_dir == MessageConst.DIR_UP;
				appFunctionCode = gwmsg.afn();
			}
			else if ( msg.getMessageType() == MessageType.MSG_DLMS ){
				DlmsMessage dlmsmsg = (DlmsMessage)msg;
				//TODO: use APDU code to determine ditection
				appFunctionCode=dlmsmsg.getFunctionCode();
				dirUp=dlmsmsg.getDirection() == IMessage.DIRECTION_DOWN?false:true;
				
			}
			else if( msg.getMessageType() == MessageType.MSG_ANSI ){
				AnsiMessage ansiMessage=(AnsiMessage)msg;
//				appFunctionCode="";
				dirUp=ansiMessage.getDirection() == IMessage.DIRECTION_DOWN?false:true;
			}
			else if(msg.getMessageType() == MessageType.MSG_BENGAL){
				//TODO:  bengal frame to database
				BengalMessage bengalMsg = (BengalMessage) msg;
				appFunctionCode = (byte) bengalMsg.getFuncCode();
				dirUp = bengalMsg.getDir()!=null&&bengalMsg.getDir().equals("up")?true:false;
			}
			msgLog.setKzm(HexDump.toHex(appFunctionCode));
			
			msgLog.setTxfs(msg.getTxfs());
			if( "01".equals(msg.getTxfs())){
				//UMS通道的消息
				String umsAddr = msg.getServerAddress();
				if( null != umsAddr && umsAddr.length()>0 ){
					int index = msg.getServerAddress().indexOf(',');
					if( index>0 ){
						msgLog.setSrcAddr(msg.getServerAddress().substring(0, index));	//SIM卡号
						msgLog.setDestAddr(msg.getServerAddress().substring(index+1));  //appid+subid
					}
					else{
						msgLog.setSrcAddr(msg.getPeerAddr());
						msgLog.setDestAddr(msg.getServerAddress());
					}
				}
				else{
					msgLog.setSrcAddr(msg.getPeerAddr());
					msgLog.setDestAddr("unknow");
				}
			}
			else if( "02".equals(msg.getTxfs())){		//GPRS or CDMA通道消息
				//对于上行，peerAddr＝终端IP地址；对于下行，peerAddr为业务处理器的IP地址
				String commMode=state.getCommunicationMode();
				if(commMode!=null && !commMode.equals("")){
					msgLog.setTxfs(commMode);
				}
				msgLog.setSrcAddr(msg.getPeerAddr());
				
				//上行和下行，source对应为网关连接client；
				if( null != msg.getSource() ){
					msgLog.setDestAddr(msg.getSource().getPeerAddr());
				}else{
					msgLog.setDestAddr("unknow");
				}
			}
			else{//other txfs
				msgLog.setSrcAddr(msg.getPeerAddr());
				if( null != msg.getSource() ){
					msgLog.setDestAddr(msg.getSource().getPeerAddr());
				}else{
					msgLog.setDestAddr("unknow");
				}
			}

			//取本机当前时间作为原始报文通讯时间
			msgLog.setTime(new Date(System.currentTimeMillis()));
			msgLog.setBody(msg.getRawPacketString());
			msgLog.setSize(msgLog.getBody().length());
			if (dirUp)//上行报文
				service.addToDao(msgLog,logKey);
			else{//下行报文
				if(msg.getStatus()!=null&&msg.getStatus().equals("1"))//下发失败
					msgLog.setResult("1");
				else
					msgLog.setResult("0");
				if(logKey!=unDocLogKey)//非法报文存同一张表
					logKey=logKey+1;
				service.addToDao(msgLog,logKey);
			}
		}catch(Exception ex){
			log.error("Error to processing message log:"+msg, ex);
		}
	}

	public void setDefaultLogKey(int defaultLogKey) {
		this.defaultLogKey = defaultLogKey;
	}

	public void setUnDocLogKey(int unDocLogKey) {
		this.unDocLogKey = unDocLogKey;
	}

}
