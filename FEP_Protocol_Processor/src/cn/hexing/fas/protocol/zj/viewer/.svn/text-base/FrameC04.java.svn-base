package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	FrameC04.java
 * TODO
 */
public class FrameC04 extends AbstractFrame{
	public static final String FUNC_NAME="终端编程日志";
	public FrameC04(){
		//
	}
	
	public FrameC04(byte[] frame){
		super(frame);
	}
	
	public FrameC04(String data){
		super(data);
	}
	
	public String getDescription() {
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(super.getBase());
			sb.append("命令类型--").append(FUNC_NAME);
			sb.append("\n");
			sb.append("数据--").append(Util.BytesToHex(frame,11,length));			
			return sb.toString();
		}
		return null;
	}
}
