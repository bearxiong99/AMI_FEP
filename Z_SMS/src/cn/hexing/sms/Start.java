package cn.hexing.sms;

import cn.hexing.sms.processor.DefaultSmsMsgProcessor;



public class Start {
	public static void main(String[] args) throws Exception {
		SmsService.getInstance().setSmsInfos("+8613827341049#modem.com3#COM5#9600#Simcom#+8613800571500");
	
		SmsService.getInstance().setProcessor(new DefaultSmsMsgProcessor());
		SmsService.getInstance().startService();
	}
	
}
