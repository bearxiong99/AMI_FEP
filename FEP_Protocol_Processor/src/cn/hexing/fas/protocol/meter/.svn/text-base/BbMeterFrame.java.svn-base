package cn.hexing.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.zj.parse.ParseTool;

/**
 * @filename	BbMeterFrame.java
 * TODO
 */
public class BbMeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(BbMeterFrame.class);
	public static final int CHARACTER_HEAD_FLAG=0x68;
	public static final int CHARACTER_TAIL_FLAG=0x16;
	public static final int MINIMUM_FRAME_LENGTH=12;
	public static final int FLAG_ADDRESS_POSITION=0x1;
	public static final int FLAG_DATA_POSITION=0x0A;
	public static final int FLAG_CTRL_POSITION=0x8;
	public static final int FLAG_BLOCK_DATA=0xAA;
	
	private int datalen;		/*数据长度*/
	private int pos;			/*数据区开始位置*/
	private String meteraddr;	/*表地址*/
	private int ctrl;			/*控制码*/
	
	public BbMeterFrame(){
		super();
		datalen=-1;
		pos=FLAG_DATA_POSITION;
	}
	
	public BbMeterFrame(byte[] data,int loc,int len){
		super();
		parse(data,loc,len);
		pos=FLAG_DATA_POSITION;
	}
	
	public void parse(byte[] data, int loc, int len) {
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
							if(CHARACTER_HEAD_FLAG==(data[head+7] & 0xff)){	//second head flag
								int flen=(data[head+9] & 0xff);	//假定帧长
								if((head+flen+FLAG_DATA_POSITION+1)<=rbound){		//长度符合要求
									if(CHARACTER_TAIL_FLAG==(data[head+FLAG_DATA_POSITION+flen+1] & 0xff)){	//tail char
										//cheak cs
										if(ParseTool.calculateCS(data,head,flen+FLAG_DATA_POSITION)==data[head+FLAG_DATA_POSITION+flen]){
											start=0;
											this.len=flen+12;
											this.data=new byte[this.len];
											datalen=flen;											
											System.arraycopy(data,head,this.data,start,this.len);
											meteraddr=ParseTool.BytesToHexC( this.data,FLAG_ADDRESS_POSITION,6,(byte)0xAA);	//表地址
											pos=FLAG_DATA_POSITION;
											ctrl=this.data[FLAG_CTRL_POSITION];
											//处理数据域 -0x33
											adjustData(this.data,pos,datalen,0x33);
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
			log.error("部颁帧识别",e);
		}
	}
	
	private void adjustData(byte[] data,int start,int len,int adjust){
		if(data!=null && data.length>=(start+len)){
			for(int i=start;i<start+len;i++){
				data[i]-=adjust;
			}
		}
	}
	
	/**
	 * @return Returns the datalen.
	 */
	public int getDatalen() {
		return datalen;
	}

	/**
	 * @param datalen The datalen to set.
	 */
	public void setDatalen(int datalen) {
		this.datalen = datalen;
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

	/**
	 * @return Returns the ctrl.
	 */
	public int getCtrl() {
		return ctrl;
	}
	
	
}
