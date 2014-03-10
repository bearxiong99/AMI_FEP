package cn.hexing.fk.sockserver;

import java.nio.channels.SocketChannel;

import cn.hexing.fk.common.spi.socket.ISocketServer;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.HexDump;
/**
 * 
 * @author gaoll
 *
 * @time 2013-5-2 下午02:43:05
 *
 * @info 掌机消息客户端
 */
public class AsyncHandheldSocketClient extends AsyncSocketClient{
	
	
	
	
	public byte[] currentMsg = null;
	public AsyncHandheldSocketClient(SocketChannel c,ISocketServer s){
		super(c, s);
	}
	
	public IMessage read(MessageBytes message){
		if(message ==null || (message.data.length<3 && this.currentMsg==null)){
			return null;
		}
		
		if(message.data.length>=4 && message.data[0]==0x68 && message.data[1]==message.data[2] && message.data[3]==0x68){
			//抛弃原有的错误帧
			this.currentMsg = null;
		}
		if(this.currentMsg == null){
			this.currentMsg = message.data;
		}else{
			this.currentMsg=HexDump.cat(this.currentMsg, message.data);					
		}

		if(this.currentMsg[1]!=this.currentMsg[2]) return null; //长度不一致
		
		int length=this.currentMsg[2]&0xFF;
		if(length > this.currentMsg.length){
			//说明还有后续帧，等待接收
			return null;
		}
		if(length == this.currentMsg.length-7){
			message.data = this.currentMsg;
			this.currentMsg = null;
			return message;
		}else{
			//说明长度不对
			this.currentMsg = null;
			return null;
		}
	}

	
}
