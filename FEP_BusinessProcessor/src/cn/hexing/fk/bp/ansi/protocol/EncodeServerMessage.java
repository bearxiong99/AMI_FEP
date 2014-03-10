package cn.hexing.fk.bp.ansi.protocol;

import org.apache.log4j.Logger;

import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.element.CalledAPTitleElement;
import com.hx.ansi.element.CallingAPInvocationIdElement;
import com.hx.ansi.element.CallingAPTitleElement;
import com.hx.ansi.element.UserInformationElement;
import com.hx.ansi.message.AnsiMessage;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-3-21 上午10:05:27
 * @version 1.0 
 */

public class EncodeServerMessage {
	private static final Logger log = Logger.getLogger(EncodeServerMessage.class);
	private static final TraceLog trace = TraceLog.getTracer("ANSI_LOGON");
	private static final EncodeServerMessage instance = new EncodeServerMessage();
	public static final EncodeServerMessage getInstance(){ return instance; }

	/**
	 * 
	 * @param peerAddr
	 * @param localAddr
	 * @param userInformation
	 * @return
	 */
	public AnsiMessage encodeLogonMessage(AnsiContext context,String userInformation){
		if(null==context.acseTitle){//如果context里面没有带有acseTitle的信息，则要组一个acseTitle
			CalledAPTitleElement calledAPTitle=new CalledAPTitleElement();
			calledAPTitle.setPeerAddr(context.peerAddr);
			calledAPTitle.encode();
			CallingAPTitleElement callingAPTitle=new CallingAPTitleElement();
			callingAPTitle.setPeerAddr(context.localAddr);
			callingAPTitle.encode();
			String apduTitle=calledAPTitle.getCalledAPTitleElement()+callingAPTitle.getCallingAPTitleElement();
			context.acseTitle=apduTitle;
		}
		CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
		callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
		callingAPInvocationId.encode();
		UserInformationElement userInf=new UserInformationElement();
		userInf.encode(userInformation,context, "50");
		String apdu=context.acseTitle+callingAPInvocationId.getCallingAPInvocationIdElement()+userInf.getUserInformationElement();
		apdu="60"+parseInt2HexString(apdu.length()/2)+apdu;
		AnsiMessage msg = new AnsiMessage();
		msg.setApdu(HexDump.toArray(apdu));
		return msg;
	}
	/**
	 * 
	 * @param context
	 * @param userInformation
	 * @return
	 */
	public AnsiMessage encodeTerminateMessage(AnsiContext context,String userInformation) {
		if(null==context.acseTitle){//如果context里面没有带有acseTitle的信息，则要组一个acseTitle
			CalledAPTitleElement calledAPTitle=new CalledAPTitleElement();
			calledAPTitle.setPeerAddr(context.peerAddr);
			calledAPTitle.encode();
			CallingAPTitleElement callingAPTitle=new CallingAPTitleElement();
			callingAPTitle.setPeerAddr(context.localAddr);
			callingAPTitle.encode();
			String apduTitle=calledAPTitle.getCalledAPTitleElement()+callingAPTitle.getCallingAPTitleElement();
			context.acseTitle=apduTitle;
		}
		String acse="";
		CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
		callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
		callingAPInvocationId.encode();
		UserInformationElement userInf=new UserInformationElement();
		userInf.encode(userInformation,context, "21");
		acse=context.acseTitle+callingAPInvocationId.getCallingAPInvocationIdElement()+userInf.getUserInformationElement();
		acse="60"+parseInt2HexString(acse.length()/2).toUpperCase()+acse;
		AnsiMessage msg = new AnsiMessage();
		msg.setApdu(HexDump.toArray(acse));
		return msg;
	}
	
	public String  parseInt2HexString(int i){
		String ss=Integer.toHexString(i);
		if(1==(ss.length()%2)){
			ss=0+ss;
		 }
		return ss;
	}

}
