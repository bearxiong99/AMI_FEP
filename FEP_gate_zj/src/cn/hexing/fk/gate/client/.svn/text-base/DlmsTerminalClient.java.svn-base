package cn.hexing.fk.gate.client;

import com.hx.dlms.aa.AareApdu;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 œ¬ŒÁ2:55:32
 *
 * @info Dlms÷’∂À∂‘œÛ
 */
public class DlmsTerminalClient extends TerminalClient implements Comparable<DlmsTerminalClient>{

	public enum STATUS {HDLC_LINK,APP_LINK,SEND_STOC,APP_OK};
	
	public STATUS status=STATUS.HDLC_LINK;
	
	public CipherMechanism aaMechanism = CipherMechanism.HLS_2;
	
	private int invokeId;
	
	public DlmsContext context = new DlmsContext();
	
	public byte[] meterSysTitle;
	
	public byte[] authenticationValue;
	
	public DlmsMessage realMessage;
	
	@Override
	public boolean start() {
		return super.start();
	}
	
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
	
	@Override
	public int compareTo(DlmsTerminalClient o) {
		return 0;
	}
}
