package cn.hexing.exception;

import java.nio.ByteBuffer;

import cn.hexing.util.HexDump;

public class MessageRecognizeException extends Exception {
	private static final long serialVersionUID = 200603141603L;
	private ByteBuffer buffer;

	public MessageRecognizeException(String msg,ByteBuffer buff) {
		super(msg);
		if( null == buff )
			return;
		if (buff.position() > 0) //已经读取了一些数据
			buff.rewind();
		buffer = buff.slice();
	}
	public MessageRecognizeException(ByteBuffer buff) {
		super("消息不能识别。");
		if( null == buff )
			return;
		if (buff.position() > 0) //已经读取了一些数据
			buff.rewind();
		buffer = buff.slice();
	}

	public String getMessage() {
		String message = super.getMessage();

		if (buffer != null) {
			return message + ((message.length() > 0) ? " " : "") + "(Hexdump: "
					+ HexDump.hexDump(buffer) + ')';
		} else {
			return message;
		}
	}
}
