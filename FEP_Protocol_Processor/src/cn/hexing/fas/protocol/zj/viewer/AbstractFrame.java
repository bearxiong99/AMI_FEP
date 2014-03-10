package cn.hexing.fas.protocol.zj.viewer;
/**
 * @filename	AbstractFrame.java
 * TODO
 * 				0x68--------------------第一帧头标志
 * 				  A1--------------------行政区码
 * 				  A2--------------------行政区码	
 * 				  B1--------------------终端地址
 * 				  B2--------------------终端地址
 * 				 MS1--------------------主站地址及命令序号
 * 				 MS2--------------------主站地址及命令序号
 * 				0x68--------------------第二帧头标志
 * 				   C--------------------功能码
 * 				   L--------------------帧长(低字节)
 * 				   L--------------------帧长(高字节)	
 * 				DATA--------------------数据N字节
 * 				  CS--------------------校验(CS之前所有字节校验和)
 * 				0x16--------------------帧尾标志 
 * 
 * 
 */
public abstract class AbstractFrame {
	protected byte A1;		/*行政区码*/
	protected byte A2;
	protected byte B1;		/*终端地址*/
	protected byte B2;
	protected int mast;		/*主站编号*/
	protected int fseq;		/*帧序号*/
	protected int ifseq;	/*帧内序号*/
	protected int C;		/*功能码*/
	protected int fexp;		/*异常标志 1:异常 0：正常*/
	protected int direction;/*传输方向 1：终端上行 0：主站下行*/
	protected int length;	/*数据长度*/
	protected byte cs;
	protected byte rcs;
	protected byte[] frame;	/*帧*/
	
	public AbstractFrame(){
		//
	}
	
	public AbstractFrame(byte[] frame){		
		if(frame!=null && frame.length>=13){
			this.frame=frame;
			fillinfo();
		}
	}
	
	public AbstractFrame(String data){
		if(data!=null){
			if(Util.validHex(data)){//合法的HEX字串
				frame=null;
				frame=new byte[(data.length()>>>1)+(data.length() & 0x1)];
				Util.HexsToBytes(frame,0,data);
				fillinfo();
			}
		}
	}
	
	private void fillinfo(){
		A1=frame[1];
		A2=frame[2];
		B1=frame[3];
		B2=frame[4];
		mast=frame[5] & 0x3f;
		fseq=((frame[6] & 0x1f)<<2)+((frame[5] & 0xC0)>>>6);
		ifseq=((frame[6] & 0xE0)>>>5);
		C=frame[8] & 0x3f;
		fexp=(frame[8] & 0x40)==0x40?1:0;
		direction=(frame[8] & 0x80)==0x80?1:0;
		length=((frame[10] & 0xff)<<8)+(frame[9] & 0xff);
		cs=frame[frame.length-2];
		rcs=Util.calculateCS(frame,0,frame.length-2);
	}
	
	protected String getBase(){
		if(frame!=null){
			StringBuffer sb=new StringBuffer();
			sb.append(direction>0?"传输方向--终端上行":"传输方向--主站下行");
			sb.append("    ");
			sb.append("终端逻辑地址--").append(Util.ByteToHex(A1)).append(Util.ByteToHex(A2)).append(Util.ByteToHex(B2)).append(Util.ByteToHex(B1));
			//sb.append("    ");
			//sb.append("终端地址--").append(Util.ByteToHex(B2)).append(Util.ByteToHex(B1));
			sb.append("    ");
			sb.append("主站地址--").append(String.valueOf(mast));
			sb.append("    ");
			sb.append("帧序号--").append(String.valueOf(fseq));
			sb.append("    ");
			sb.append("帧内序号--").append(String.valueOf(ifseq));
			sb.append("    ");
			sb.append("数据长度--").append(String.valueOf(length));
			sb.append("\n");
			sb.append("CS--").append(Util.ByteToHex(cs));
			sb.append("    DATA CS--").append(Util.ByteToHex(rcs));
			sb.append("\n");
			return sb.toString();
		}
		return null;
	}
	
	public String errCode(byte errcode){
		String err=null;
		switch(errcode & 0xFF){
			case 0x00:
				err="成功";
				break;
			case 0x01:
				err="中继命令无返回";
				break;
			case 0x02:
				err="设置内容非法";
				break;
			case 0x03:
				err="密码权限不足";
				break;
			case 0x04:
				err="无此数据项";
				break;
			case 0x05:
				err="命令时间失效";
				break;
			case 0x11:
				err="目标地址不存在";
				break;
			case 0x12:
				err="发送失败";
				break;
			case 0x13:
				err="短消息帧太长";
				break;
			default:
				break;
		}
		return err;
	}
	
	public String timeUnit(byte tunit){
		String ustr=null;
		switch(tunit & 0xFF){
			case 0x2:
				ustr="分钟";
				break;
			case 0x3:
				ustr="小时";
				break;
			case 0x4:
				ustr="日";
				break;
			case 0x5:
				ustr="月";
				break;
			default:
				ustr="未知时间单位";
				break;
		}
		return ustr;
	}
	
	public abstract String getDescription();
}
