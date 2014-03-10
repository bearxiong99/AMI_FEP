package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	FrameC02.java
 * TODO
 */
public class FrameC02 extends AbstractFrame{
	public static final String FUNC_NAME="终端任务数据";
	public FrameC02(){
		//
	}
	
	public FrameC02(byte[] frame){
		super(frame);
	}
	
	public FrameC02(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("命令类型--").append(FUNC_NAME);
			sb.append("\n");			
			if(direction>0){	//终端应答
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){				
				buffer.append("异常应答--").append(errCode(frame[11]));				
			}else{				
				buffer.append("任务号--")	.append(frame[11] & 0xFF).append("    ");
				buffer.append("起始任务数据采集时间---");
				buffer.append("20").append(Util.ByteToHex(frame[12])).append("-").append(Util.ByteToHex(frame[13]))
						.append("-").append(Util.ByteToHex(frame[14])).append(" ").append(Util.ByteToHex(frame[15]))
						.append(":").append(Util.ByteToHex(frame[16])).append(":00\n");
				buffer.append("本次上送数据点数--").append(frame[17] & 0xFF).append("    ");
				buffer.append("数据点采集间隔时间--").append(frame[19] & 0xFF).append(timeUnit(frame[18])).append("\n");
				buffer.append("任务数据--").append(Util.BytesToHex(frame,20,length-9));
			}
		}catch(Exception e){
			//
		}
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("任务号--")	.append(frame[11] & 0xFF).append("    ");
			buffer.append("起始任务数据采集时间---");
			buffer.append("20").append(Util.ByteToHex(frame[12])).append("-").append(Util.ByteToHex(frame[13]))
					.append("-").append(Util.ByteToHex(frame[14])).append(" ").append(Util.ByteToHex(frame[15]))
					.append(":").append(Util.ByteToHex(frame[16])).append(":00\n");
			buffer.append("本次召测数据点数--").append(frame[17] & 0xFF).append("    ");
			buffer.append("数据点间隔倍率--").append(frame[18] & 0xFF).append("\n");
		}catch(Exception e){
			//
		}
	}
}
