package cn.hexing.fas.protocol.meter;

/**
 * 表帧抽象类
 *
 */
public abstract class AbstractMeterFrame {
	protected int start;		/*帧开始位置*/
	protected int len;		/*帧长度*/
	protected byte[] data;	/*帧数据,包含干扰数据*/
	
	public AbstractMeterFrame(){
		this(0,-1,new byte[0]);
	}
	
	public AbstractMeterFrame(int start,int len,byte[] data){
		this.data=data;
		this.len=len;
		this.start=start;
	}
	
	/**
	 * 表规约帧识别器-----从二进制字节数组中识别规约帧
	 * @param data
	 */
	public abstract void parse(byte[] data,int loc,int len);
	
	/**
	 * @return Returns the data.
	 */
	public byte[] getData() {
		return data;
	}

	/**
	 * @return Returns the len.
	 */
	public int getLen() {
		return len;
	}

	/**
	 * @return Returns the start.
	 */
	public int getStart() {
		return start;
	}
	
	public void clear(){
		data=null;
		start=0;
		len=0;
	}
}
