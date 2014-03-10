package cn.hexing.fk.telnetserver;

public class TelnetOption {
	public static final byte IAC = (byte)255;		//标志符,代表是一个TELNET 指令 
	public static final byte DONT = (byte)254;		//表示一方要求另一方停止使用，或者确认你不再希望另一方使用指定的选项
	public static final byte DO = (byte)253;		//表示一方要求另一方使用，或者确认你希望另一方使用指定的选项
	public static final byte WONT = (byte)252;		//表示拒绝使用或者继续使用指定的选项
	public static final byte WILL = (byte)251;		//表示希望开始使用或者确认所使用的是指定的选项
	public static final byte SUB = (byte)250;		//表示后面所跟的是对需要的选项的子谈判
	public static final byte SUBEND = (byte)240;	//子谈判参数的结束
	//==============
	public static final byte NOP = (byte)241;		//无操作
	public static final byte DATA_MARK = (byte)242;	//Synch的数据流部分。这应该总和TCP紧急标志一起发送
	public static final byte BREAK = (byte)243;		//NVT 字符 BRK
	public static final byte INTERRUPT = (byte)244;
	public static final byte ABORT_OUTPUT = (byte)245;	//
	public static final byte R_U_WHERE = (byte)246;	//Are You There 
	public static final byte ERASE = (byte)247;		//Erase character 
	public static final byte GO_AHEAD = (byte)249;
	public static final byte OPT_FLAG_ECHO = 1;		//回显
	public static final byte OPT_FLAG_UECHO = 3;	//抑制继续进行
	public static final byte OPT_FLAG_STATE = 5;	//
	public static final byte OPT_FLAG_TIMER = 6;
	
	private byte[] options = null;
	
	public TelnetOption(byte[] buf, int pos, int len){
		options = new byte[len];
		for(int i=0; i<len; i++)
			options[i] = buf[pos+i];
	}
	
	private int ubyte(byte b){
		return 0x00ff & b;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("verb=").append(ubyte(options[1]));
		sb.append(", option=").append(ubyte(options[2]));
		return sb.toString();
	}
}
