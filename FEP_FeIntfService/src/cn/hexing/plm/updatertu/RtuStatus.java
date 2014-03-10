package cn.hexing.plm.updatertu;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.util.HexDump;

public class RtuStatus {
	private static final TraceLog tracer = TraceLog.getTracer(RtuStatus.class);
	private String batchId = null;		//主站升级的批次号
	private int rtua;
	private ByteBuffer buffer;
	private int iPacket = 0;	//第几段
	private int total = 0;		//总段数
	private long lastTime = System.currentTimeMillis();
	private int resendCount = 0;
	private Map<Integer,Integer> sendCountMap=new HashMap<Integer,Integer>();
	
	private int getPacketLength(){
		return UpdateRtuModule.getInstance().getPacketLength();
	}
	private String getPassword(){
		return UpdateRtuModule.getInstance().getPassword();
	}
	public RtuStatus(String bNo,int rtu, ByteBuffer buf){
		batchId = bNo;
		this.rtua = rtu;
		buffer = buf;
		total = buf.remaining() / getPacketLength();
		if( buf.remaining() % getPacketLength() != 0 )
			total++;
	}
	
	public String getBatchId(){
		return batchId;
	}
	
	public int getRtua(){
		return rtua;
	}
	
	public int getCurPacket(){
		return iPacket;
	}
	
	public void setCurPacket(int iPacket) {
		this.iPacket = iPacket;
	}
	public synchronized int getCurPacketSendCount(int iPacket) {
		Integer count=sendCountMap.get(iPacket);
		if(count==null)
			return 0;
		else{
			return count;
		}
	}
	public int getTotalPacket(){
		return total;
	}
	
	public boolean isLastPacket(){
		return (iPacket+1) == total;
	}
	
	public long getLastTime(){
		return lastTime;
	}
	
	public void setLastTime(long time){
		lastTime = time;
	}
	
	public synchronized void move(int curPacket){		//如果收到上次下行的确认帧，那么前进1步
		if(curPacket==iPacket&&curPacket<total-1){
			iPacket=curPacket+1;
			resendCount = 0;
		}
	}
	
	public synchronized MessageGw nextPacket(){	
		if(iPacket==0){
			sendCountMap.clear();
		}		
		Integer count=sendCountMap.get(iPacket+1);
		if(count==null)
			sendCountMap.put(iPacket+1, new Integer(1));
		else{
			count++;
			sendCountMap.put(iPacket+1, count);
		}

		int fl = getPacketLength();
		if( (iPacket+1) == total ){	//最后一段
			if( buffer.remaining() % getPacketLength() !=0 )
				fl = buffer.remaining() % getPacketLength();
		}
		ByteBuffer data;
		//为中试所测试临时增加16个字节密码
		if(!getPassword().equals(""))
			//4字节数据单元标识 ＋ 数据单元
			data = ByteBuffer.allocate(4+11+fl+16);
		else 
			data = ByteBuffer.allocate(4+11+fl);
		byte czero = 0, cone = 1;
		data.put(czero); data.put(czero);
		data.put(cone);
		data.put(czero);
		data.put(cone);		//文件标识
		if( (iPacket+1) == total )	//结束帧
			data.put(cone);
		else
			data.put(czero);
		data.put(czero);		//文件指令
		//总段数
		byte c;
		c = (byte)(total & 0x00FF) ;
		data.put(c);
		c = (byte)( (total>>8) & 0x00FF );
		data.put(c);
		//第i段标识
		c = (byte)(iPacket & 0x00FF) ;
		data.put(c);
		c = (byte)( (iPacket>>8) & 0x00FF );
		data.put(c);
		c = (byte)( (iPacket>>16) & 0x00FF );
		data.put(c);
		c = (byte)( (iPacket>>24) & 0x00FF );
		data.put(c);
		//第i段数据长度Lf
		c = (byte)(fl & 0x00FF) ;
		data.put(c);
		c = (byte)( (fl>>8) & 0x00FF );
		data.put(c);
		//文件数据
		int offset = iPacket * getPacketLength();
		try{			
			for(int i=0; i<fl; i++ ){
				data.put(buffer.get(offset+i));
			}
		}catch(Exception ex){
			tracer.trace("nextPacket error:iPacket="+iPacket+",total="+total+",offset="+offset+",iPacket="+iPacket+",data.len="+data.position());
		}
		
		//为中试所测试临时增加16个字节密码
		if(!getPassword().equals(""))
			data.put(HexDump.toByteBuffer(getPassword()));
		data.flip();
		MessageGw msg = new MessageGw();
		msg.data = data;
		msg.head.rtua = rtua;
		msg.setAFN(MessageConst.GW_FUNC_FILE);
		msg.setSEQ((byte)(iPacket+1));
		msg.needConfirm(true);
		return msg;
	}

	public int getResendCount() {
		return resendCount;
	}

	public void incResendCount() {
		this.resendCount++;
	}
	
	
}
