package cn.hexing.dlms.meter.client;

import java.nio.ByteBuffer;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.common.simpletimer.TimerData;
import cn.hexing.fk.common.simpletimer.TimerScheduler;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.sockclient.JSocket;

/**
 * 
 * @author gaoll
 *
 * @time 2012-12-12 上午10:08:32
 *
 * @info 电表客户端
 */
public class MeterClient extends ClientModule{
	
	private String logicAddress;
	long lastReceiveTime =0;
	private int heartBeatInterval= 2;
	@Override
	public boolean start(){
		super.start();
		//启动定时服务
		super.getSocket().setMessageCreator(new MessageBytesCreator());
		TimerScheduler.getScheduler().addTimer(new TimerData(this,3,heartBeatInterval*60));
		return true;
	}
	
	@Override
	public void onTimer(int id){
		if(id==3){
			//心跳
			long now=System.currentTimeMillis();
			long timeDiff = now-lastReceiveTime;
			if(timeDiff>heartBeatInterval*60*1000){
				sendMessage(createHeartBeat());
			}
		}
	}
	
	
	@Override
	public void onConnected(JSocket client) {
		super.onConnected(client);
		if(!client.send(createHeartBeat())){
			System.out.println("连接时,发送心跳失败.");
		}
	}
	@Override
	public void onReceive(JSocket client, IMessage msg) {
		super.onReceive(client, msg);
		lastReceiveTime = System.currentTimeMillis();
	}
	
	public MessageBytes createHeartBeat(){
		
		MessageBytes heartBeat = new MessageBytes();
		ByteBuffer heart=ByteBuffer.allocate(26);
		//--------帧头------------
		heart.putShort((short) 0x0001);
		heart.putShort((short) 0x0001);
		heart.putShort((short) 0x0010);
		//--------帧头------------
		heart.putShort((short)0x0012); //长度
		heart.put((byte) 0xDD);
		heart.put((byte)0x10);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		heart.put((byte)0x00);
		byte[] b_logic=logicAddress.getBytes();
		heart.put(b_logic);
		heart.flip();
		heartBeat.setData(heart.array());
		return heartBeat;
		
	}
	
	public final void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}

	public final int getHeartBeatInterval() {
		return heartBeatInterval;
	}

	public final void setHeartBeatInterval(int heartBeatInterval) {
		this.heartBeatInterval = heartBeatInterval;
	}

}
