package cn.hexing.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.zj.parse.ParseTool;


/**
 * 浙江表规约帧
 *
 */
public class ZjMeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(ZjMeterFrame.class);
	public static final int CHARACTER_HEAD_FLAG=0x68;
	public static final int CHARACTER_TAIL_FLAG=0x0D;
	public static final int MINIMUM_FRAME_LENGTH=7;
	public static final int FLAG_REPLY_ERROR=0xF0;
	public static final int FLAG_REPLY_OK=0xFA;
	public static final int FLAG_BLOCK_DATA=0xED;
	public static final int FLAG_NO_DATA=0xBA;
	public static final int FLAG_ADDRESS_POSITION=0x4;
	
	private int datalen;	/*数据长度*/
	private int pos;		/*数据区开始位置*/
	private String meteraddr;/*表地址*/
	
	public ZjMeterFrame(){
		super();
		pos=FLAG_ADDRESS_POSITION;
	}
	
	public ZjMeterFrame(byte[] data,int loc,int len){
		super();
		parse(data,loc,len);
		pos=FLAG_ADDRESS_POSITION;
	}
	
	/**
	 * 识别浙江表规约帧
	 */
	public void parse(byte[] data,int loc,int len) {
		int head=loc;
		int rbound=0;
		
		super.clear();
		try{
			if(data!=null){	//数据非空
				if(data.length>(loc+len)){
					rbound=loc+len;
				}else{
					rbound=data.length;
				}
				if((rbound-loc)>=MINIMUM_FRAME_LENGTH){	//数据足够多
					while(head<=(rbound-MINIMUM_FRAME_LENGTH)){	//have chance to find frame from rest data
						if(CHARACTER_HEAD_FLAG==(data[head] & 0xff)){
							if((CHARACTER_HEAD_FLAG==(data[head+3])) 
									&& (data[head+1]==data[head+2])){	//second head flag and frame len
								int flen=(data[head+1] & 0xff);	//假定帧长
								if((head+flen+4+2)<=rbound){		//长度符合要求
									if(CHARACTER_TAIL_FLAG==(data[head+5+flen] & 0xff)){	//tail char
										//cheak cs
										if(calculateCS(data,head+4,flen)==data[head+4+flen]){
											start=0;
											this.len=flen+6;
											this.data=new byte[this.len];
											datalen=flen;											
											System.arraycopy(data,head,this.data,start,this.len);
											meteraddr=ParseTool.ByteToHex( this.data[pos]);	//表地址
											pos=FLAG_ADDRESS_POSITION;
											break;
										}
									}
								}
							}
						}
						head++;	//search from next byte
					}
				}
			}
		}catch(Exception e){
			log.error("浙江表规约解析",e);
		}		
	}
	
	private byte calculateCS(byte[] data,int start,int len){
		int cs=0;
		for(int i=start;i<start+len;i++){
			cs+=(data[i] & 0xff);
			cs&=0xff;
		}
		return (byte)(cs & 0xff);
	}

	/**
	 * @return Returns the datalen.
	 */
	public int getDatalen() {
		return datalen;
	}

	/**
	 * @return Returns the meteraddr.
	 */
	public String getMeteraddr() {
		return meteraddr;
	}

	/**
	 * @param meteraddr The meteraddr to set.
	 */
	public void setMeteraddr(String meteraddr) {
		this.meteraddr = meteraddr;
	}

	/**
	 * @return Returns the pos.
	 */
	public int getPos() {
		return pos;
	}

	/**
	 * @param pos The pos to set.
	 */
	public void setPos(int pos) {
		this.pos = pos;
	}
	
	
}
