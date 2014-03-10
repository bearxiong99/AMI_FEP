package send;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;

import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.sockserver.SyncSocketClient;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-22 ����09:50:41
 *
 * @info CSD�첽�����Ӷ���,Ҫ���ϳ�ʱ�ж�,����һ��ʱ���û�з��أ��͹Ҷ�
 * ����DLMS��Ҫ����HDLC,APP����
 */
public class CSDSyncTcpSocketClient extends SyncSocketClient{

	private IMessage realMessage;
	
	public byte[] authenticationValue;
	
	public static enum CSD_CLIENT_STATUS { DIAL,IDLE,BUSY,HDLC_LINK,APP_LINK,SEND_STOC,HANG_UP};
	
	//AARQ��HLS_2��ʼ
	public CipherMechanism aaMechanism = CipherMechanism.HLS_2;
	
	public byte[] meterSysTitle;
	
	public DlmsContext context = new DlmsContext();
	
	public ArrayList<Object> blockReplys = new ArrayList<Object>();

	public int controlWord; 
	
	public long lastSendTime;

	/**
	 * ��ʶ��ǰclient�����״̬
	 * 0��ʶ����
	 */
	private CSD_CLIENT_STATUS status=CSD_CLIENT_STATUS.IDLE;
	
	private String phoneNum=null;

	private int invokeId;
	
	public final byte[] makeInitVector( byte[]cipherText, int offset ){
		ByteBuffer iv = ByteBuffer.allocate(12);
		iv.put(this.meterSysTitle);
		iv.put(cipherText, offset, 4);
		return iv.array();
	}
	
	public CSDSyncTcpSocketClient(SocketChannel c, ISocketServer s) {
		super(c, s);
	}
	
	
	public void processDownMessage(IMessage msg){
		dial();
		realMessage = msg;
	}
	
	/**
	 * ����,Ҫ�е绰����
	 */
	private void dial(){
		System.out.println("dial");
		this.status=CSD_CLIENT_STATUS.DIAL;
		//sendMsg();
		MessageBytes mb = new MessageBytes();
		mb.setData("abc".getBytes());
		send(mb);
	}
	
	/**
	 * �Ҷ�
	 */
	public void hangUp(){
		System.out.println("hang up");
		this.status=CSD_CLIENT_STATUS.HANG_UP;
		//sendMsg();
	}

	/**
	 * �ɹ��Ҷ� 
	 */
	public void onHangUp() {
		this.setStatus(CSD_CLIENT_STATUS.IDLE);
		this.realMessage=null;
		blockReplys.clear();
		aaMechanism = CipherMechanism.HLS_2;
		meterSysTitle=null;
		authenticationValue=null;
		controlWord=0;
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
			this.setDaemon(true);
		}
		@Override
		public void run() {
			while(true){
				if(status!=CSD_CLIENT_STATUS.IDLE){
					long now = System.currentTimeMillis();
					if(now-lastSendTime>1000*60){//Ҫ���ó�ʱʱ��
						hangUp();
					}
				}
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
	}

}
