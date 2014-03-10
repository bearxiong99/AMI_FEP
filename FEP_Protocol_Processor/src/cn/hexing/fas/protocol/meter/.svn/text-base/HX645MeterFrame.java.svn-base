package cn.hexing.fas.protocol.meter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.utils.HexDump;

/**
 * @filename	BbMeterFrame.java
 * TODO
 */
public class HX645MeterFrame extends AbstractMeterFrame{
	private final Log log=LogFactory.getLog(HX645MeterFrame.class);
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
	private byte[] dataArea;
	private byte[] id = new byte[2];
	private String value;
	private boolean setFlag;
	
	public HX645MeterFrame(){
		super();
		datalen=-1;
		pos=FLAG_DATA_POSITION;
	}
	
	public HX645MeterFrame(byte[] data,int loc,int len){
		super();
		parse(data,loc,len);
		pos=FLAG_DATA_POSITION;
	}
	
	public static void main(String[] args) {
		HX645MeterFrame mf = new HX645MeterFrame();
		byte[] s = HexDump.toArray("689600960068888619170902106D000001000316006804870413000068008109531D894C3C345443468E161216");
		mf.parse(s, 0, s.length);
		System.out.println(mf);
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
								int flen=(data[head+10] & 0xff);	//假定帧长
								if((head+flen+FLAG_DATA_POSITION+1)<=rbound){		//长度符合要求
									if(CHARACTER_TAIL_FLAG==(data[head+FLAG_DATA_POSITION+flen+2] & 0xff)){	//tail char
										start=0;
										this.len=flen+12;
										this.data=new byte[this.len];
										datalen=flen;	
										this.dataArea=new byte[datalen];
										System.arraycopy(data,head,this.data,start,this.len);
										System.arraycopy(data, head+11, dataArea, 0, datalen);
										meteraddr=ParseTool.BytesToHexC( this.data,FLAG_ADDRESS_POSITION,6,(byte)0xFF);	//表地址
										pos=FLAG_DATA_POSITION+1;
										ctrl=this.data[FLAG_CTRL_POSITION+1]&0xFF;
										if((ctrl & 0x40) != 0){
											//返回异常
											value = "01";
											break;
										}
										setFlag = (ctrl&04) !=0?true:false;
										//处理数据域 -0x33
										adjustData(this.dataArea,0,datalen,0x33);
										adjustData(this.data,pos,datalen,0x33);
										id[0] = dataArea[1];id[1] = dataArea[0];
										HX645MeterParser hx645 = new HX645MeterParser();
										Object[] result = hx645.parser(this.dataArea, 0, this.dataArea.length,setFlag);
										if(result!=null && result instanceof String[]){
											value = (String) result[0];
										}
										if(setFlag){
											String[] returnValue = value.split("#");
											String strId = HexDump.toHex(id);
											String preFix = "";
											//返回设置状态，充值结果是 0,或者1 ,其他的设置结果是 00或01
											if(!("EE20".equals(strId) || "EE21".equals(strId))){
												preFix = "0";
											}
											if("EE04".equals(strId) || "EE03".equals(strId)) 
												value ="00";
											else{
												if(returnValue[0].equals("") || returnValue[0].indexOf("#")==-1){
													value = preFix+value;
												}else{
													value =preFix+value.substring(0,value.indexOf("#"));
												}												
											}
											
										}
										break;
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

	public final byte[] getDataArea() {
		return dataArea;
	}

	public final void setDataArea(byte[] dataArea) {
		this.dataArea = dataArea;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}


	public boolean isSetFlag() {
		return setFlag;
	}

	public void setSetFlag(boolean setFlag) {
		this.setFlag = setFlag;
	}
	
	
}
