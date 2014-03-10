package cn.hexing.fk.sockserver;

import java.nio.channels.SocketChannel;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.simpletimer.ITimerFunctor;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.message.DlmsHDLCMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-4-2 上午09:29:07
 *
 * @info 处理外置模块的专用client,其实主要用于Dlms (╰_╯)#
 * 
 * 有一个定时器,间隔为60秒钟
 * 在定时器内有一个超时时间,如果如果一次请求超时了，根据当前的状态进行处理.
 * ex:
 * 1.当前状态是hdlc_Link,超时之后再次发送链路连接的请求
 * 2.当前状态是READY,一段时间没有通讯,则发送读表号的请求
 * 
 */
public class AsyncExHdlcDlmsSocketClient extends AsyncSocketClient implements ITimerFunctor{
	private static final Logger log = Logger.getLogger(AsyncExHdlcDlmsSocketClient.class);

	public enum CLIENTSTATUS{NULL,HDLC_LINK,APP_LINK,SEND_STOC,READ_NUM,READY,BUSY};
	
	public CLIENTSTATUS status = CLIENTSTATUS.NULL;
	
	public long lastCommunicateTime = System.currentTimeMillis();
	
	public String logicAddress = null;
	
	public byte[] acceptAARE=null;
	
	public long overTime = 10*1000;
	
	public long heartInterval = 60*1000;
	/**模块是否有心跳上送*/
	public boolean autoHeartBeat = false;
	
	public AsyncExHdlcDlmsSocketClient(SocketChannel c,ISocketServer s){
		super(c, s);
		try {
			String strHeartInterval=System.getProperty("heartInterval");
			if(strHeartInterval!=null){
				heartInterval=Integer.parseInt(strHeartInterval)*1000;
			}
		} catch (NumberFormatException e) {}
		try {
			String strOvertime=System.getProperty("overtime");
			if(strOvertime!=null){
				overTime = Integer.parseInt(strOvertime)*1000;
			}
		} catch (NumberFormatException e) {}
		TimerScheduler.getScheduler().addTimer(new TimerData(this,1,10));	
	}
	
	public void removeTimeScheduler(){
		TimerScheduler.getScheduler().removeTimer(this,1);
	}
	
	public CipherMechanism aaMechanism = CipherMechanism.HLS_2;
	
	private int invokeId;
	
	public DlmsContext context = new DlmsContext();
	
	public byte[] meterSysTitle;
	
	public byte[] authenticationValue;
	
	public int nextFrameCounter() {
		invokeId++;
		if( invokeId> 0x0F )
			invokeId = 1;
		return invokeId;
	}

	public void updateAare(AareApdu aare){
		this.authenticationValue=aare.getRespAuthenticationValue().getAuthValue();
		this.meterSysTitle = aare.getRespApTitle();
	}
	
	public void readMeterNo(){
		this.status = CLIENTSTATUS.READ_NUM;
		byte[] apdu = HexDump.toArray("C0018100010000600100FF0200");
		//C0018100010000600187FF0200
		DlmsHDLCMessage msg = new DlmsHDLCMessage();
		msg.setApdu(apdu);
		msg.setControlField((byte) 0x10);
		this.send(msg);
		log.debug("read meterNo,logicAddress:"+logicAddress);
	}

	public void sendHdlcLink(){
		this.status = CLIENTSTATUS.HDLC_LINK;
		MessageBytes mb = new MessageBytes();
		mb.setData(HexDump.toArray("7EA0230002FEFF0393E4B0818014050207D0060207D00704000000010804000000013AF27E"));
		this.send(mb);
	}
	
	@Override
	public void onTimer(int id) {
		long diffTime=System.currentTimeMillis()-lastCommunicateTime;
		if(this.status == CLIENTSTATUS.NULL) return;
		if(id==1){
			if(diffTime>overTime && (this.status == CLIENTSTATUS.HDLC_LINK||this.status==CLIENTSTATUS.APP_LINK ||this.status == CLIENTSTATUS.READ_NUM)){
				this.sendHdlcLink();
			}else if(diffTime>overTime && (this.status == CLIENTSTATUS.BUSY||this.status == CLIENTSTATUS.SEND_STOC) ){
				this.status = CLIENTSTATUS.READY;
			}else if(diffTime > heartInterval && this.status == CLIENTSTATUS.READY ){
				readMeterNo();
			}
		}
	}


}
