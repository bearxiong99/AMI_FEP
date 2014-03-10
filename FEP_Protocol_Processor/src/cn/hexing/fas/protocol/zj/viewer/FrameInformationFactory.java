package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	FrameInformationFactory.java
 * TODO
 */
public class FrameInformationFactory {
	public static String getFrameInformation(byte[] frame){
		if(frame!=null && frame.length>=13){
			int func=frame[8] & 0x3f;
			AbstractFrame aframe=null;
			switch(func){
				case 0:
					aframe=new FrameC00(frame);
					break;
				case 1:
					aframe=new FrameC01(frame);
					break;
				case 2:
					aframe=new FrameC02(frame);
					break;
				case 4:
					aframe=new FrameC04(frame);
					break;
				case 7:
					aframe=new FrameC07(frame);
					break;
				case 8:
					aframe=new FrameC08(frame);
					break;
				case 9:
					aframe=new FrameC09(frame);
					break;
				case 10:
					aframe=new FrameC0A(frame);					
					break;
				case 0x21:
					aframe=new FrameC21(frame);	
					break;
				case 0x24:
					aframe=new FrameC24(frame);	
					break;
				default:
					break;
			}
			if(aframe!=null){
				return aframe.getDescription();
			}
		}
		return null;
	}
	
	public static String getFrameInformation(String frame){
		if(frame!=null){
			String data=frame.replaceAll(" ","");
			int index=data.indexOf("68");
			if(index>0){
				data=data.substring(index);
			}
			if(Util.validHex(data)){//ºÏ·¨µÄHEX×Ö´®			
				byte[] bframe=new byte[(data.length()>>>1)+(data.length() & 0x1)];
				Util.HexsToBytes(bframe,0,data);
				return getFrameInformation(bframe);
			}
		}
		return null;
	}
}
