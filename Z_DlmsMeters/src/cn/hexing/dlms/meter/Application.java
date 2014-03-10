package cn.hexing.dlms.meter;


import cn.hexing.dlms.meter.client.MeterClient;
import cn.hexing.dlms.meter.protocol.MessageHandler;
import cn.hexing.fk.utils.ClassLoaderUtil;

public class Application {
	public static void main(String[] args) throws InterruptedException {
		ClassLoaderUtil.initializeClassPath();
		String gateIp=System.getProperty("gate.ip");
		String gatePort=System.getProperty("gate.port");
		String meterCount=System.getProperty("meter.count");
		String meterStartNo=System.getProperty("meter.address.no");
		String meterHeartInterval=System.getProperty("meter.heart.interval");
		String channelData =System.getProperty("meter.channelData");
		String isEncript = System.getProperty("meter.isEncript");
		boolean isIran = Boolean.parseBoolean(System.getProperty("meter.isIran"));
		int i_meterCount = Integer.parseInt(meterCount);
		int	i_meterStartNo = Integer.parseInt(meterStartNo);
		int i_meterHeartInterval = Integer.parseInt(meterHeartInterval);
		boolean b_isEncript = Boolean.parseBoolean(isEncript);
		
		for(int i = 0 ;i<i_meterCount;i++){
			MessageHandler handler =new MessageHandler();
			handler.setValue(channelData);
			handler.setEncript(b_isEncript);
			handler.setIran(isIran);
			MeterClient meterClient = new MeterClient();
			meterClient.setBufLength(1000);
			meterClient.setHostIp(gateIp);
			meterClient.setHostPort(Integer.parseInt(gatePort));
			meterClient.setLogicAddress("10"+"0000000000".substring((""+(i+i_meterStartNo)).length())+(i+i_meterStartNo));
			meterClient.setBufLength(10240);
			meterClient.setHeartBeatInterval(i_meterHeartInterval);
			meterClient.setTimeout(4);
			meterClient.setRequestNum(500);
			meterClient.setEventHandler(handler);
			meterClient.start();
			Thread.sleep(20);
		}
		
	}
}
