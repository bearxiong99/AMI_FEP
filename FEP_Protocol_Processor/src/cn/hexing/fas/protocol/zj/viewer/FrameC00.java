package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	FrameC00.java
 * TODO
 */
public class FrameC00 extends AbstractFrame{
	public static final String FUNC_NAME="中继";
	public FrameC00(){
		//
	}
	
	public FrameC00(byte[] frame){
		super(frame);
	}
	
	public FrameC00(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("命令类型--").append(FUNC_NAME);
			sb.append("\n");
			//sb.append("数据--").append(Util.BytesToHex(frame,11,length));			
			if(direction>0){	//终端应答
				descRtuReply(sb);
			}else{	
				descMastCmd(sb);
			}
			return sb.toString();
		}
		return null;
	}
	
	private void descMastCmd(StringBuffer buffer){
		try{
			buffer.append("端口号--").append(Util.BytesToHex(frame,11,1));
			buffer.append("    ");
			buffer.append("超时时间--").append(frame[12] & 0xFF);
			buffer.append("    ");
			buffer.append("特征字节--").append(Util.BytesToHex(frame,13,1));
			buffer.append("    ");
			int loc=(frame[14] & 0xFF) +((frame[15] & 0xFF)<<8);
			buffer.append("截取开始--").append(loc);
			buffer.append("    ");
			loc=(frame[16] & 0xFF) +((frame[17] & 0xFF)<<8);
			buffer.append("截取长度--").append(loc);
			buffer.append("    ");
			buffer.append("中继命令--").append(Util.BytesToHex(frame,18,length-7));
		}catch(Exception e){
			//
		}
	}
	
	private void descRtuReply(StringBuffer buffer){
		try{
			if(fexp>0){
				buffer.append("中继失败--").append(errCode(frame[11]));
			}else{
				buffer.append("端口号--").append(Util.BytesToHex(frame,11,1));			
				buffer.append("\n");			
				buffer.append("中继返回--").append(Util.BytesToHex(frame,12,length-1));
			}			
		}catch(Exception e){
			//
		}
	}
}
