package cn.hexing.fk.monitor.message;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.exception.SocketClientCloseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.monitor.MonitorCommand;

/**
 * 管理监控模块的消息对象。
 * 协议格式：
 * 9字节报文标志[JBMONITOR]+2字节命令码+4字节整数（报文内容长度） + 报文内容。
 */
public class MonitorMessage implements IMessage {
	//静态属性
	private static final Logger log = Logger.getLogger(MonitorMessage.class);
	private static final MessageType msgType = MessageType.MSG_MONITOR;
	private static final byte[] FLAG = "JBMONITOR".getBytes();
	private static final ByteBuffer voidBody = ByteBuffer.allocate(0);
	//消息对象属性
	private long ioTime = System.currentTimeMillis();	//对监控管理模块，ioTime没有作用。 ***
	private String peerAddr;	//对方的IP:PORT地址。对于监控管理来说，没有实际意义。		  ***
	private String serverAddress;
	private String txfs = "tcp";	//对监控管理没有意义。								  ***
	private IChannel source;
	private int priority = IMessage.PRIORITY_LOW;	//low priority					  ***
	private short command = MonitorCommand.CMD_INVALID;
	private int bodyLen = 0;						//报文体长度
	private ByteBuffer body = voidBody;					//报文体
	//内部辅助属性
	private int state = IMessage.STATE_INVALID;		//辅助消息对象的完整读取或者发送

	public long getIoTime() {
		return ioTime;
	}

	public MessageType getMessageType() {
		return msgType;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public int getPriority() {
		return priority;
	}

	public byte[] getRawPacket() {
		return body.array();
	}

	public String getRawPacketString() {
		return "un implemented";
	}

	public IChannel getSource() {
		return source;
	}

	public String getTxfs() {
		return txfs;
	}

	public boolean read(ByteBuffer readBuffer) {
		if( state == IMessage.STATE_INVALID && readBuffer.remaining()<15 ){
			if( log.isDebugEnabled() )
				log.debug("长度不足读取监控报文头，等会儿继续读取。readBuffer.remaining="+readBuffer.remaining());
			return false;		//长度明显不足，等会儿继续读取
		}
		if( state == IMessage.STATE_INVALID ){	//消息头没有读取
			for(int i=0; i<FLAG.length; i++ ){
				if( readBuffer.get() != FLAG[i] )
					throw new SocketClientCloseException("报文标志不匹配。必须关闭通讯连接");
			}
			command = readBuffer.getShort();
			bodyLen = readBuffer.getInt();		//默认网络字节顺序.
			body = ByteBuffer.allocate(bodyLen);
			state = IMessage.STATE_READ_DATA;
			if( readBuffer.remaining()>= body.remaining() ){
				readBuffer.get(body.array());
				state = IMessage.STATE_READ_DONE;
				return true;
			}
			body.put(readBuffer);
			return false;
		}
		else if( state == IMessage.STATE_READ_DATA ){
			if( readBuffer.remaining()>= body.remaining() ){
				readBuffer.get(body.array(),body.position(),body.remaining() );
				body.position(0);		//完整读取后，position置0，表示数据可读状态
				state = IMessage.STATE_READ_DONE;
				return true;
			}
			body.put(readBuffer);
			return false;
		}
		else{
			log.error("状态非法。不是读取信息状态。关闭通信连接。请检查程序BUG。");
			throw new SocketClientCloseException("状态非法。不是读取信息状态。关闭通信连接。请检查程序BUG。");
		}
	}

	public void setIoTime(long time) {
		ioTime = time;
	}

	public void setPeerAddr(String peer) {
		peerAddr = peer;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setSource(IChannel src) {
		source = src;
	}

	public void setTxfs(String fs) {
		txfs = fs;
	}

	public boolean write(ByteBuffer writeBuffer) {
		if( IMessage.STATE_SEND_DONE == state )
			return true;
		if( IMessage.STATE_INVALID == state ){	//先发送报文头
			if( writeBuffer.remaining()<15 ){
				log.debug("缓冲区长度不足，不能发送监控报文头。writeBuffer.len="+writeBuffer.remaining());
				return false;
			}
			writeBuffer.put(FLAG);
			writeBuffer.putShort(command);
			writeBuffer.putInt(body.remaining());
			state = IMessage.STATE_SEND_DATA;
			return sendDataSection(writeBuffer);
		}
		else if( IMessage.STATE_SEND_DATA == state )
			return sendDataSection(writeBuffer);
		else{
			log.error("状态非法。不是发送监控消息状态。关闭通信连接。请检查程序BUG。");
			throw new SocketClientCloseException("状态非法。不是发送监控消息状态。关闭通信连接。请检查程序BUG。");
		}
	}

	private boolean sendDataSection(ByteBuffer writeBuffer){
		if( writeBuffer.remaining()> body.remaining() ){
			writeBuffer.put(body);
			body.rewind();
			state = IMessage.STATE_SEND_DONE;
			ioTime = System.currentTimeMillis();
			return true;
		}
		//读缓冲区不足以发送全部的body数据。
		int limit = body.limit();
		body.limit(body.position()+writeBuffer.remaining());
		writeBuffer.put(body);
		body.limit(limit);
		return false;
	}

	public short getCommand() {
		return command;
	}

	public void setCommand(short command) {
		this.command = command;
	}

	public ByteBuffer getBody() {
		return body;
	}

	public void setBody(ByteBuffer body) {
		this.body = body;
	}
	
	public void resetMessageState(){
		state = IMessage.STATE_INVALID;
	}
	
	public Long getCmdId() {
		return new Long(0);
	}

	public String getStatus() {
		return "";
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public boolean isHeartbeat() {
		return false;
	}

	public int getRtua() {
		return 0;
	}
	
	public String getLogicalAddress(){
		return null;
	}
	
	public void setLogicalAddress(String logicAddr){
		
	}

	public int length() {
		return 0;
	}

	public void setStatus(String status) {
	}

	public boolean isTask() {
		return false;
	}

	public void setTask(boolean isTask) {
	}

}
