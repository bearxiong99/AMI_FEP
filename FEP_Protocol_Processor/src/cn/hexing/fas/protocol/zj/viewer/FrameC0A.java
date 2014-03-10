package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	FrameC0A.java
 * TODO
 */
public class FrameC0A extends AbstractFrame{
	public static final String FUNC_NAME="告警确认";
	public FrameC0A(){
		//
	}
	
	public FrameC0A(byte[] frame){
		super(frame);
	}
	
	public FrameC0A(String data){
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
