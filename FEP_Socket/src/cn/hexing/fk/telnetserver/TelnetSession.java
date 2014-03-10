package cn.hexing.fk.telnetserver;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.telnetserver.process.TelnetCommandDispatcher;

public class TelnetSession {
	private static final Logger log = Logger.getLogger(TelnetSession.class);
	private IChannel channel = null;
	private ByteBuffer buffer = ByteBuffer.allocate(1024*10);
	
	public TelnetSession(IChannel client){
		channel = client;
		TelnetCommandDispatcher.getInstance().dispatch(this, null);
	}
	
	public long getLastIoTime(){
		return null != channel ? channel.getLastIoTime() : 0;
	}
	
	public boolean isTimeout(){
		return System.currentTimeMillis() - getLastIoTime() > 1000* 60 * 30 ;
	}
	
	public void onReceive(byte[] in){
		boolean lineReady = false;
		try{
			for(int i=0; i<in.length; ){
				if( in[i] != TelnetOption.IAC ){
					if( in[i] == '\r' || in[i] == '\n' ){
						lineReady = true;
						break;	// we get a line input.
					}
					buffer.put(in[i++]);
				}
				else{
					//get option IAC + verb[sub] + opt + [sub.modifier + subEnd]  
					int len = 3;
					if( in[i+1] == TelnetOption.SUB ){
						while( in[i+len] != TelnetOption.SUBEND )
							len++;
					}
					TelnetOption to = new TelnetOption(in,i,len);
					log.info("receive option: "+to);
					i += 3;
				}
			}
		}catch(Throwable e){
			log.error("TelnetSession receive data exp.",e);
			buffer.clear();
			return;
		}
		if( lineReady ){
			buffer.flip();
			String cmdLine = new String(buffer.array(),0,buffer.remaining());
			buffer.clear();
			log.info("command line= " + cmdLine);
			TelnetCommandDispatcher.getInstance().dispatch(this, cmdLine);
		}
	}
	
	public void send(byte[] out){
		MessageBytes msg = new MessageBytes();
		msg.setData(out);
		channel.send(msg);
	}
	
	public void send(String out){
		if( out.length()>0 )
			send(out.getBytes());
		send("\r\n#".getBytes());
	}
	
	public IChannel getClient(){
		return channel;
	}
}
