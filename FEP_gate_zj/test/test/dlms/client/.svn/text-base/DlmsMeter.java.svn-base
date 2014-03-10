package test.dlms.client;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.EventType;
import cn.hexing.fk.common.spi.IEvent;
import cn.hexing.fk.common.spi.IEventHandler;
import cn.hexing.fk.sockserver.event.ReceiveMessageEvent;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.InitiateResponse;
import com.hx.dlms.message.DlmsMessage;
import com.hx.dlms.message.DlmsMessageCreator;

public class DlmsMeter {
	private ClientModule client = new ClientModule();
	private String meterId = "00000000303030303132303330323632";
	private IEventHandler handler = new IEventHandler(){
		@Override
		public void handleEvent(IEvent event) {
			if( event.getType() == EventType.MSG_RECV ){
				ReceiveMessageEvent evt = (ReceiveMessageEvent)event;
				DlmsMessage msg = (DlmsMessage)evt.getMessage();
				System.out.println("RECV >> " + msg);
				ByteBuffer apdu = msg.getApdu();
				int applicationTag = apdu.get(0) & 0xFF ;
				switch( applicationTag ){
				case 0x60: //AARQ
				{
					try {
						InitiateResponse resp = new InitiateResponse();
						resp.getMaxRecvPduSize().setValue(0x01f4);
						resp.getConformance().setInitValue(new byte[]{0,(byte)0x50,(byte)0x1F});
						AareApdu aare = new AareApdu();
						aare.setInitResponse(resp);
						aare.getAaResult().setValue(0);
						aare.getDiagnostics()[0].setValue(0);
						aare.getDiagnostics()[0].setBerCodec();
						aare.getAaResultDiagnostic().choose(aare.getDiagnostics()[0]);
						DlmsMessage msgAARE = new DlmsMessage();
						byte[] aareApdu = aare.encode();
						ByteBuffer buf = ByteBuffer.wrap(aareApdu);
						System.out.println("AARE: "+HexDump.hexDump(buf));
						msgAARE.setApdu(buf);
						client.sendMessage(msgAARE);
					} catch (IOException exp) {
						exp.printStackTrace();
						break;
					}
					break;
				}
				case 192:	//get-request
				case 193: 	//set-request
				case 194: 	//event-notification-request
				case 195: 	//action-request
					break;
				case 196:	//get-response
				{
					//handleGetResponse(apdu,evt);
					break;
				}
				case 197: 	//set-response
					break;
				case 199:	//action-response
					break;
				case 200:	//with global ciphering, glo-get-request
				case 201:	//with global ciphering, glo-set-request
				case 202:	//with global ciphering, glo-event-notification-request
				case 203:	//with global ciphering, glo-action-request
					System.out.println("Up-link message should not be request.");
					break;
				case 204:	//with global ciphering, glo-get-response
					break;
				case 205:	//with global ciphering, glo-set-response
					break;
				case 207:	//with global ciphering, glo-action-response
					break;
				case 216:	//exception-response
					break;
				case 0xDD:  //Heart-beat, decimal value is 221
					break;
				default:{
					System.out.println("APDU="+HexDump.hexDump(apdu));
				}
				}
			}
			else if( event.getType() == EventType.CLIENT_CONNECTED ){
				// do on-connection
				byte[] meterAddr = new byte[16];
				byte[] mid = HexDump.toByteBuffer(meterId).array();
				int zeroCnt = meterAddr.length - mid.length;
				int j= 0;
				for(int i=0; i<meterAddr.length; i++ ){
					if( zeroCnt>0 ){
						meterAddr[i] = 0;
						zeroCnt--;
					}
					else{
						meterAddr[i] = mid[j++];
					}
				}
				DlmsMessage msg = new DlmsMessage();
				ByteBuffer buf = ByteBuffer.allocate(meterAddr.length+2);
				buf.put((byte)0xDD); buf.put((byte)meterAddr.length);
				buf.put(meterAddr);
				buf.flip();
				msg.setApdu(buf);
				client.sendMessage(msg);
			}
		}
	};
	
	private void init(){
		client.setHeartInterval(-1);
		client.setBufLength(1024);
		client.setHostIp("127.0.0.1");
		client.setHostPort(20002);
		client.setEventHandler(handler);
		client.setMessageCreator(new DlmsMessageCreator());
		client.init();
		client.start();
	}
	
	public static void main(String[] args) {
		DlmsMeter meter = new DlmsMeter();
		meter.init();
	}

}
