package cn.hexing.fk.sockclient.async.simulator;

import org.apache.log4j.Logger;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.sockclient.async.JAsyncSocket;
import cn.hexing.fk.utils.HexDump;

public class ZjSimulator implements IRtuSimulator {
	public static final Logger log = Logger.getLogger(ZjSimulator.class);
	private JAsyncSocket client;
	private String strTask = "6899053806C11668811000010000000000000030805520211205047616";
	private MessageZj taskTemplate;
	private int rtua = 0;
	private String pwd = "123456";
	private byte fseq = 0;
	private byte getFseq(){
		synchronized(this){
			fseq++;
			if( fseq > 0x7F | fseq<=0 )
				fseq = 0x01;
			return fseq;
		}
	}

	public ZjSimulator(){
		taskTemplate = new MessageZj();
		try{
			taskTemplate.read(HexDump.toByteBuffer(strTask));
		}catch(Exception e){}
	}
	
	private MessageZj createHeart(){
		MessageZj msg = new MessageZj();
		msg.head.rtua = rtua;
		msg.head.c_func = MessageConst.ZJ_FUNC_HEART;
		msg.head.c_dir = MessageConst.DIR_UP;
		msg.head.fseq = getFseq();
		return msg;
	}
	
	private MessageZj createLogin(){
		MessageZj msg = new MessageZj();
		msg.head.rtua = rtua;
		msg.head.c_func = MessageConst.ZJ_FUNC_LOGIN;
		msg.head.c_dir = MessageConst.DIR_UP;
		msg.head.fseq = getFseq();
		msg.data = HexDump.toByteBuffer(pwd);
		return msg;
	}
	
	private MessageZj createTask(){
		MessageZj msg = new MessageZj();
		msg.head.rtua = rtua;
		msg.head.c_func = MessageConst.ZJ_FUNC_READ_TASK;
		msg.head.c_dir = MessageConst.DIR_UP;
		msg.head.fseq = getFseq();
		msg.data = taskTemplate.data;
		return msg;
	}
	
	public void onClose(JAsyncSocket client) {
		log.info("client closed. "+client);
		this.client = null;
	}

	public void onConnect(JAsyncSocket client) {
		this.client = client;
		sendLogin();
		log.info("client connected. "+client);
	}

	public void onReceive(JAsyncSocket client, IMessage message) {
		log.info("recv msg: "+message+" ,client:"+client);
	}

	public void onSend(JAsyncSocket client, IMessage message) {
		log.info("send msg: "+message+" ,client:"+client);
	}
	
	public void sendLogin(){
		if( null != client && client.isConnected() )
			client.send(createLogin());
	}

	public void sendHeart(){
		if( null != client && client.isConnected() )
			client.send(createHeart());
	}
	
	public void sendTask(){
		if( null != client && client.isConnected() )
			client.send(createTask());
	}

	public int getRtua() {
		return rtua;
	}

	public void setRtua(int rtua) {
		this.rtua = rtua;
	}
	
	public String getState(){
		return client.isConnected()? "ÔÚÏß" : "¶Ï¿ª";
	}
}
