package cn.hexing.fk.sockclient.csd;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.SyncSocketClient;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

import com.hx.dlms.DlmsAssistant;
import com.hx.dlms.DlmsAssistant.OP_TYPE;
import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.message.DlmsHDLCMessage;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-22 上午09:50:41
 *
 * @info CSD异步的连接对象
 * 对于DLMS表要进行HDLC,APP连接
 */
public class CSDSyncTcpSocketClient extends SyncSocketClient {

	private IMessage realMessage;
	
	public byte[] authenticationValue;
	
	public static enum CSD_CLIENT_STATUS { DIAL,IDLE,PENDING,BUSY,HDLC_LINK,APP_LINK,SEND_STOC,HANG_UP,DISC_HDLC_LINK,CHANNGE_COMMAND};
	
	//AARQ从HLS_2开始
	public CipherMechanism aaMechanism = CipherMechanism.HLS_2;
	
	public byte[] meterSysTitle;
	
	public DlmsContext context = new DlmsContext();
	
	public ArrayList<Object> blockReplys = new ArrayList<Object>();

	public int controlWord; 
	
	public long lastSendTime;
	
	public byte[] curMsg=null;
	
	public int curFrameLength=0;

	public int pendingTime = 10;
	/**
	 * 标识当前client对象的状态
	 * 0标识空闲
	 */
	private CSD_CLIENT_STATUS status=CSD_CLIENT_STATUS.IDLE;
	
	public String phoneNum=null;

	private int invokeId;
	
	private static final Logger log = Logger.getLogger(CSDSyncTcpSocketClient.class);	

	
	public final byte[] makeInitVector( byte[]cipherText, int offset ){
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(this.meterSysTitle);
		iv.put(cipherText, offset, 4);
		return iv.array();
	}
	
	public CSDSyncTcpSocketClient(SocketChannel c, ISocketServer s) {
		super(c, s);
		DetectTimeOut timeDetect = new DetectTimeOut();
		timeDetect.start();
	}
	
	
	public void processDownMessage(IMessage msg) throws Exception{
		localAddr=msg.getLogicalAddress();
		MessageGate gateMsg = (MessageGate) msg;
		if(this.status == CSD_CLIENT_STATUS.IDLE){
			this.phoneNum=gateMsg.getHead().getAttributeAsString(GateHead.ATT_SIM_NUM);
			dial(this.phoneNum);			
		}else if(this.status==CSD_CLIENT_STATUS.PENDING){
			//说明此时没有挂断,需要将真实数据发送出去
			IMessage innerMsg = gateMsg.getInnerMessage();
			MessageBytes mb = new MessageBytes();
			if(innerMsg instanceof DlmsMessage){
				byte[] apdu = wrapHDLCFrame(((DlmsMessage) innerMsg).getApdu().array(), this);
				mb.setData(apdu);
				this.send(mb);
			}
			log.info("send real msg.");
			this.setStatus(CSD_CLIENT_STATUS.BUSY);
		}
		realMessage = msg;
	}
	
	
	private byte[] wrapHDLCFrame(byte[] apdu,CSDSyncTcpSocketClient csdClient) throws Exception {
		int controlWord = 0xFE;
		if(csdClient.controlWord==0){
			controlWord = 0x10;
		}else{
			int rrr=(csdClient.controlWord>>5)&0x0F;
			int sss=((csdClient.controlWord&0x0F)>>1)&0x0F;
			if(++sss>7){
				sss=0;
			}
			int needResp = 1;
			controlWord = (0xFE & (rrr<<5)|(needResp<<4))|(sss<<1);
		}
		//7E A0|A8 LL ADDR CC HCS_H HCS_L E6 E6 00 APDU 7E
	
		DlmsHDLCMessage hdlc = new DlmsHDLCMessage();
		hdlc.setControlField((byte)controlWord);
		int type = apdu[0];
		OP_TYPE opType = OP_TYPE.OP_NA;
		switch(type){
		case (byte)0xC0:
			opType = OP_TYPE.OP_GET;
			break;
		case (byte)0xC1:
			opType = OP_TYPE.OP_SET;
			break;
		case (byte)0xC3:
			opType = OP_TYPE.OP_ACTION;
			break;
		}
		
		apdu = DlmsAssistant.getInstance().cipher(opType, apdu, csdClient.context);
		hdlc.setControlField((byte)controlWord);
		hdlc.setApdu(apdu);
		//7E A0 LL destAddr srcAddr c HCS E6 E6 00 APDU FCS 7E
		//1+1+1+hdlc.getServerAddr().length+hdlc.getClientAddr().length+1+2+3+apdu.length+2+1
		ByteBuffer buffer = ByteBuffer.allocate(12+hdlc.getServerAddr().length+hdlc.getClientAddr().length+apdu.length);

		hdlc.write(buffer);
		buffer.flip();
		return buffer.array();
	}
	/**
	 * 拨号,要有电话号码
	 */
	public void dial(String phoneNum){
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		log.debug("phoneNum:"+phoneNum+" dialing");
		this.status=CSD_CLIENT_STATUS.DIAL;
		MessageBytes mb = new MessageBytes();
		mb.setData(("ATD"+phoneNum+"\r").getBytes());
		lastSendTime = System.currentTimeMillis();
		send(mb);
	}
	
	/**
	 * 挂断
	 */
	public void hangUp(){
		log.debug("phoneNum:"+phoneNum+"hang up");
		this.status=CSD_CLIENT_STATUS.HANG_UP;
		MessageBytes mb = new MessageBytes();
		mb.setData("ATH\r".getBytes());
		send(mb);
	}
	
	public void channgeCommandMode(){
		log.debug("phoneNum:"+phoneNum+" channge command mode");
		this.status=CSD_CLIENT_STATUS.CHANNGE_COMMAND;
		MessageBytes mb = new MessageBytes();
		mb.setData(HexDump.toArray("2B2B2B"));
		send(mb);
	}

	/**
	 * 成功挂断 
	 */
	public void onHangUp() {
		this.setStatus(CSD_CLIENT_STATUS.IDLE);
		this.realMessage=null;
		blockReplys.clear();
		aaMechanism = CipherMechanism.HLS_2;
		meterSysTitle=null;
		authenticationValue=null;
		controlWord=0;
		localAddr=null;
		lastSendTime=0;
		curFrameLength=0;
		curMsg = null;
		log.info("phoneNum "+this.phoneNum+" hang up");
	}

	public CSD_CLIENT_STATUS getStatus() {
		return status;
	}


	public void setStatus(CSD_CLIENT_STATUS status) {
		this.status = status;
	}


	public String getPhoneNum() {
		return phoneNum;
	}


	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}


	public IMessage getRealMessage() {
		return realMessage;
	}


	public void setRealMessage(IMessage realMessage) {
		this.realMessage = realMessage;
	}


	public int nextFrameCounter() {
		invokeId++;
		if( invokeId> 0x0F )
			invokeId = 1;
		return invokeId;
	}

	public void updateAare(AareApdu aare) {
//		this.setConformance(aare.getNegotiatedResponse().getConformance());
//		setMaxPduSize(aare.getNegotiatedPduSize());
//		setAuthenticationMechanismName(aare.getMechanismName());
		this.authenticationValue=aare.getRespAuthenticationValue().getAuthValue();
		this.meterSysTitle = aare.getRespApTitle();
	
	}
	class DetectTimeOut extends Thread{

		public DetectTimeOut(){
			this.setDaemon(false);
			this.setName("DetectTimeOut-"+peerAddr);
		}
		@Override
		public void run() {
			while(channel!=null){
				long now = System.currentTimeMillis();
				if(status!=CSD_CLIENT_STATUS.IDLE){
					boolean overTime = now-lastSendTime>1000*60;
					if(overTime){
						if( status == CSD_CLIENT_STATUS.CHANNGE_COMMAND){
							hangUp();
						}else if(status == CSD_CLIENT_STATUS.HANG_UP){
							onHangUp();
						}else {
							channgeCommandMode();
						}
					}
				}
				
				if(status==CSD_CLIENT_STATUS.PENDING){
					//如果当前状态处于PENDING,如果超过10秒钟没有进行通讯,则通讯进行挂断。
					boolean overTime = now-lastSendTime>1000*pendingTime;
					if(overTime){
						channgeCommandMode();
					}
				}
				
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					log.error(StringUtil.getExceptionDetailInfo(e));
				}
			}
		}
		
	}
}
