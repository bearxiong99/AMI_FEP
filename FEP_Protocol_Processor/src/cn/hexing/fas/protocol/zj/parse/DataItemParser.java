package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.StringUtil;

/**
 * 字节数据解析为可读数据项数据
 *
 */
public class DataItemParser {
	
	
	private static final Logger log = Logger.getLogger(DataItemParser.class);

	
	public static final int COMM_TYPE_SMS=0x01;
	public static final int COMM_TYPE_GPRS=0x02;
	public static final int COMM_TYPE_DTMF=0x03;
	public static final int COMM_TYPE_ETHERNET=0x04;
	public static final int COMM_TYPE_INFRA=0x05;
	public static final int COMM_TYPE_RS232=0x06;
	public static final int COMM_TYPE_CSD=0x07;
	public static final int COMM_TYPE_RADIO=0x08;
	public static final int COMM_TYPE_INVALID=0xFF;
	
	public static final int TASK_TYPE_NORMAL=0x01;
	public static final int TASK_TYPE_RELAY=0x02;
	public static final int TASK_TYPE_EXCEPTION=0x04;
	
	/**
	 * 按数据类型解析数据
	 * @param data 待解析数据
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析数据应包含的小数位数
	 * @param parserno 解析数据的类型
	 * @return
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction,int parserno){
		Object rt=null;
		try{
			if(data!=null){
				switch(parserno){
					case 1:
						rt=Parser01.parsevalue(data,loc,len,fraction);
						break;
					case 2:
						rt=Parser02.parsevalue(data,loc,len,fraction);
						break;
					case 3:
						rt=Parser03.parsevalue(data,loc,len,fraction);
						break;
					case 4:
						rt=Parser04.parsevalue(data,loc,len,fraction);
						break;
					case 5:
						rt=Parser05.parsevalue(data,loc,len,fraction);
						break;
					case 6:
						rt=Parser06.parsevalue(data,loc,len,fraction);
						break;
					case 7:
						rt=Parser07.parsevalue(data,loc,len,fraction);
						break;
					case 8:
						rt=Parser08.parsevalue(data,loc,len,fraction);
						break;
					case 9:
						rt=Parser09.parsevalue(data,loc,len,fraction);
						break;
					case 10:
						rt=Parser10.parsevalue(data,loc,len,fraction);
						break;
					case 11:
						rt=Parser11.parsevalue(data,loc,len,fraction);
						break;
					case 12:
						rt=Parser12.parsevalue(data,loc,len,fraction);
						break;
					case 13:
						rt=Parser13.parsevalue(data,loc,len,fraction);
						break;
					case 14:
						rt=Parser14.parsevalue(data,loc,len,fraction);
						break;
					case 15:
						rt=Parser15.parsevalue(data,loc,len,fraction);
						break;
					case 16:
						rt=Parser16.parsevalue(data,loc,len,fraction);
						break;
					case 17:
						rt=Parser17.parsevalue(data,loc,len,fraction);
						break;
					case 18:
						rt=Parser18.parsevalue(data,loc,len,fraction);
						break;
					case 19:
						rt=Parser19.parsevalue(data,loc,len,fraction);
						break;
					case 20:
						rt=Parser20.parsevalue(data,loc,len,fraction);
						break;
					case 21:
						rt=Parser21.parsevalue(data,loc,len,fraction);
						break;
					case 22:
						rt=Parser22.parsevalue(data,loc,len,fraction);
						break;
					case 23:
						rt=Parser23.parsevalue(data,loc,len,fraction);
						break;
					case 24:
						rt=Parser24.parsevalue(data,loc,len,fraction);
						break;
					case 25:
						rt=Parser25.parsevalue(data,loc,len,fraction);
						break;
					case 26:
						rt=Parser26.parsevalue(data,loc,len,fraction);
						break;
					case 27:
						rt=Parser27.parsevalue(data,loc,len,fraction);
						break;
					case 28:
						rt=Parser28.parsevalue(data,loc,len,fraction);
						break;
					case 29:
						rt=Parser29.parsevalue(data,loc,len,fraction);
						break;
					case 30:
						rt=Parser30.parsevalue(data,loc,len,fraction);
						break;
					case 31:
						rt=Parser31.parsevalue(data,loc,len,fraction);
						break;
					case 32:
						rt=Parser32.parsevalue(data,loc,len,fraction);
						break;
					case 33:
						rt=Parser33.parsevalue(data,loc,len,fraction);
						break;
					case 34:
						rt=Parser34.parsevalue(data,loc,len,fraction);
						break;
					case 35:
						rt=Parser35.parsevalue(data,loc,len,fraction);
						break;
					case 36:
						rt=Parser36.parsevalue(data,loc,len,fraction);
						break;
					case 37:
						rt=Parser37.parsevalue(data,loc,len,fraction);
						break;
					case 38:
						rt=Parser38.parsevalue(data,loc,len,fraction);
						break;
					case 39:
						rt=Parser39.parsevalue(data,loc,len,fraction);
						break;
					case 40:
						rt=Parser40.parsevalue(data,loc,len,fraction);
						break;
					case 41:
						rt=Parser41.parsevalue(data,loc,len,fraction);
						break;
					case 42:
						rt=Parser42.parsevalue(data,loc,len,fraction);
						break;
					case 43:
						rt=Parser43.parsevalue(data,loc,len,fraction);
						break;
					case 44:
						rt=Parser44.parsevalue(data,loc,len,fraction);
						break;
					case 45:
						rt=Parser45.parsevalue(data,loc,len,fraction);
						break;
					case 46:
						rt=Parser46.parsevalue(data,loc,len,fraction);
						break;
					case 47:
						rt=Parser47.parsevalue(data,loc,len,fraction);
						break;
					case 48:
						rt=Parser48.parsevalue(data,loc,len,fraction);
						break;
					case 49:
						rt=Parser49.parsevalue(data,loc,len,fraction);
						break;
					case 50:
						rt=Parser50.parsevalue(data,loc,len,fraction);
						break;
					case 51:
						rt=Parser51.parsevalue(data,loc,len,fraction);
						break;
					case 52:
						rt=Parser52.parsevalue(data,loc,len,fraction);
						break;
					case 53:
						rt=Parser53.parsevalue(data,loc,len,fraction);
						break;
					case 54:
						rt=Parser54.parsevalue(data,loc,len,fraction);
						break;
					case 55:
						rt=Parser55.parsevalue(data,loc,len,fraction);
						break;
					case 56:
						rt=Parser56.parsevalue(data,loc,len,fraction);
						break;
					case 57:
						rt=Parser57.parsevalue(data,loc,len,fraction);
						break;
					case 58:
						rt=Parser58.parsevalue(data,loc,len,fraction);
						break;
					case 59:
						rt=Parser59.parsevalue(data,loc,len,fraction);
						break;
					case 60:
						rt=Parser60.parsevalue(data);
						break;
					case 61:
						rt=Parser61.parsevalue(data, loc, len, fraction);
						break;
					case 62:
						rt=Parser62.parsevalue(data, loc);
						break;
					case 63:
						rt=Parser63.parsevalue(data,loc);
						break;
					case 64:
						rt=Parser64.parsevalue(data, loc);
						break;
					case 65:
						rt=Parser65.parsevalue(data, loc);
						break;
					case 66:
						rt=Parser66.parsevalue(data, loc);
						break;
					case 67:
						rt=Parser67.parsevalue(data, loc);
						break;
					case 68:
						rt=Parser68.parsevalue(data, loc,len);
						break;
					case 69:
						rt=Parser69.parsevalue(data,loc,len,fraction);
						break;
					case 70:
						rt=Parser70.parsevalue(data,loc,len,fraction);
						break;
					case 73:
						rt=Parser73.parsevalue(data,loc,len,fraction);
						break;
					case 72:
						rt=Parser72.parsevalue(data,loc,len,fraction);
						break;
					default:						
						break;
				}
			}else{
				//解析数据空
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 按数据ID解析数据
	 * @param data 	待解析数据
	 * @param index	解析开始位置
	 * @param datakey	数据ID
	 * @return
	 */
	public static Object parsevalue(byte[] data,int index,int datakey){
		Object di=null;		
		try{
			switch(datakey & 0xff00){
				case 0x8000:
					di=parseVC8000(data,index,0,datakey);
					break;
				case 0x8100:
					di=parseVC81XX(data,index,0,datakey);
					break;
				case 0x8200:
					di=parseVC82XX(data,index,0,datakey);
					break;
				case 0x8300:
					di=parseVC83XX(data,index,0,datakey);
					break;
				case 0x8400:
					di=parseVC84XX(data,index,0,datakey);
					break;
				case 0x8500:
					di=parseVC85XX(data,index,0,datakey);
					break;
				case 0x8600:
					di=parseVC86XX(data,index,0,datakey);
					break;
				case 0x8700:
					di=parseVC87XX(data,index,0,datakey);
					break;
				case 0x8800:
					di=parseVC88XX(data,index,0,datakey);
					break;
				case 0x8900:
					di=parseVC89XX(data,index,0,datakey);
					break;
				case 0x8E00:
					di=parseVC8EXX(data,index,0,datakey);
					break;
				case 0x9000:
				case 0x9100:				
					di=parseVC9XXX(data,index,4,datakey);
					break;
				case 0xA000:
				case 0xA400:
					di=parseVCAXXX(data,index,3,datakey);
					break;
				case 0xB000:
					di=parseVCB0XX(data,index,4,datakey);
					break;
				case 0xB200:
					di=parseVCB2XX(data,index,2,datakey);
					break;
				case 0xB300:
					di=parseVCB3XX(data,index,3,datakey);
					break;
				case 0xB400:
					di=parseVCB0XX(data,index,4,datakey);
					break;
				case 0xB600:
					di=parseVCB6XX(data,index,4,datakey);
					break;
				//..........
				default:
					break;
			}
		}catch(Exception e){
			//
		}
		return di;
	}
	
	/**
	 * 解析数据项——0X8010（主站通讯地址）
	 * @param data		字节数据
	 * @param index		数据开始字节索引
	 * @return
	 */
	public static Object parseC8010(byte[] data,int index,int len){
		String des="";		
		int type=(data[index+8] & 0xff);
		switch(type){
			case COMM_TYPE_SMS:	//SMS				
				des=ParseTool.toPhoneCode(data,index,8,0xAA);				
				break;
			case COMM_TYPE_GPRS:				
				int port=ParseTool.nBcdToDecimal(data,index,2);	//net port				
				String ip=(data[index+5] & 0xff)+"."+(data[index+4] & 0xff)+"."+(data[index+3] & 0xff)+"."+(data[index+2] & 0xff);				
				des=ip+":"+port;
				break;
			case COMM_TYPE_DTMF:				
				des=ParseTool.toPhoneCode(data,index,8,0xAA);
				break;
			case COMM_TYPE_ETHERNET:				
				port=ParseTool.nBcdToDecimal(data,index,2);	//net port				
				ip=(data[index+5] & 0xff)+"."+(data[index+4] & 0xff)+"."+(data[index+3] & 0xff)+"."+(data[index+2] & 0xff);
				des=ip+":"+port;
				break;
			case COMM_TYPE_INFRA:				
				break;
			case COMM_TYPE_RS232:				
				break;
			case COMM_TYPE_CSD:				
				des=ParseTool.toPhoneCode(data,index,8,0xAA);
				break;
			case COMM_TYPE_RADIO:				
				break;
			default:
				break;
		}		
		return des;
	}
	
	/**
	 * 解析数据项——0X8013 (短信中心号码)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8013(byte[] data,int index,int len){
		String rt="";
		try{
			rt=ParseTool.toPhoneCode(data,index,len,0xAA);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8014 (默认网关或代理服务器地址IP+port)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8014(byte[] data,int index,int len){
		String rt="";
		try{
			int port=ParseTool.nBcdToDecimal(data,index,2);	//net port				
			String ip=(data[index+5] & 0xff)+"."+(data[index+4] & 0xff)+"."+(data[index+3] & 0xff)+"."+(data[index+2] & 0xff);				
			rt=ip+":"+port;
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8015 (APN)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8015(byte[] data,int index,int len){
		String rt="";
		try{
			rt=new String(data,index,len);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8016 (地市区县码)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseC8016(byte[] data,int index,int len){
		//DataItem rt=new DataItem();
		//rt.addProperty("datakey",new Integer(0x8016));
		int code=((data[index] & 0xff)<<8)+(data[index+1] & 0xff);
		//rt.addProperty("value",ParseTool.IntToHex(code));
		return String.valueOf(code);
	}
	
	/**
	 * 解析数据项——0X8017 (终端地址) -----这个是反顺序的终端地址 
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseC8017(byte[] data,int index,int len){
		//DataItem rt=new DataItem();
		//rt.addProperty("datakey",new Integer(0x8017));
		int code=((data[index] & 0xff)<<8)+(data[index+1] & 0xff);
		//rt.addProperty("value",ParseTool.IntToHex(code));
		return String.valueOf(code);
	}
	
	/**
	 * 解析数据项——0X8020 (PIN码及保存位置)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8020(byte[] data,int index,int len){
		String rt="";
		try{			
			rt=(data[index] & 0xf)+","+ParseTool.BytesToHexC(data,index+1,2);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8021 0X8022 0X8023(权限)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8021(byte[] data,int index,int len){
		String rt="";
		try{
			rt=ParseTool.BytesToHexL(data,index,len);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8030(终端时间)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8030(byte[] data,int index,int len){
		String rt="";
		try{
			Calendar time=ParseTool.getTimeW(data,index);
			SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");			
			rt=sf.format(time.getTime());
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8031(对时规则)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8031(byte[] data,int index,int len){
		String rt="";
		try{
//			int num=(data[index+3] & 0xff);
//			switch(num){
//				case 0x0:
//					rt+="不对时";
//					break;
//				case 0x10:
//					rt+="终端对表计";
//					break;
//				case 0x11:
//					rt+="表计对终端";
//					break;
//				default:
//					rt+="不对时";
//					break;
//			}
			rt+=ParseTool.ByteToHex(data[index+3]);
			rt+=(","+(data[index+2] & 0xff));
			rt+=(","+ParseTool.ByteToHex(data[index+1])+":"+ParseTool.ByteToHex(data[index]));
			//rt=ParseTool.BytesToHexL(data,index,4);
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8032(终端接线方式)
	 * @param data
	 * @param index
	 * @return
	 */
	public static Object parseVC8032(byte[] data,int index,int len){		
		return new Integer(data[index] & 0xf);
	}
	
	
	/**
	 * 解析数据项——0X8xxx (终端参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC8000(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey & 0xfff0){
			case 0x8010:
				rt=parseVC801X(data,index,len,datakey);
				break;
			case 0x8020:
				rt=parseVC802X(data,index,len,datakey);
				break;
			case 0x8030:
				rt=parseVC803X(data,index,len,datakey);
				break;
			case 0x8040:
				rt=parseVC804X(data,index,len,datakey);
				break;
			case 0x8050:
				rt=parseVC805X(data,index,len,datakey);
				break;
			case 0x8060:
				rt=parseVC806X(data,index,len,datakey);
				break;			
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X801x (终端参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC801X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){
			case 0x8010:
			case 0x8011:
			case 0x8012:
				rt=parseC8010(data,index,len);			
				break;
			case 0x8013:
				rt=parseVC8013(data,index,len);
				break;
			case 0x8014:
				rt=parseVC8014(data,index,len);
				break;
			case 0x8015:
				rt=parseVC8015(data,index,len);
				break;
			case 0x8016:
				rt=parseC8016(data,index,len);
				break;
			case 0x8017:
				rt=parseC8017(data,index,len);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X802x (终端参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC802X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8020:
				rt=parseVC8020(data,index,3);			
				break;
			case 0x8021:
			case 0x8022:
				rt=parseVC8021(data,index,3);
				break;
			case 0x8023:	
				rt=parseVC8021(data,index,1);
				break;			
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X803x (终端参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC803X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8030:
				rt=parseVC8030(data,index,6);			
				break;
			case 0x8031:
				rt=parseVC8031(data,index,4);
			case 0x8032:
				rt=parseVC8032(data,index,1);
				break;
			case 0x8033:	
				rt=ParseTool.BytesToHexL(data,index,16);
				break;
			case 0x8034:
			case 0x8035:
			case 0x8036:
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x8037:
				rt=ParseTool.ByteToHex(data[index]);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8048 0x8049(功控累加标识)
	 * @param data
	 * @param index
	 * @return NN(标识)+测量点号1+……+测量点号8
	 */
	public static Object parseVC8048(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();
		sb.append(ParseTool.ByteToHex(data[index+8]));	//标识
		for(int i=0;i<8;i++){
			sb.append(",");
			sb.append(String.valueOf(data[index+7-i] & 0xff));
		}
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X8048 0x8049(临时限电参数)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC804C(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		sb.append(String.valueOf(data[index+3] & 0xf));	//标识
		sb.append(",");
		sb.append(nf.format((double)(data[index+2] & 0xff)/((double)100.0)));
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+1]));
		sb.append(":");
		sb.append(ParseTool.ByteToHex(data[index]));
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X804x (终端参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC804X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8040:
			case 0x8041:
			case 0x8042:
			case 0x8043:
			case 0x8044:
				rt=ParseTool.ByteToHex(data[index]);			
				break;
			case 0x8045:
			case 0x8046:				
				rt=ParseTool.ByteToHex(data[index+2])+":"+ParseTool.ByteToHex(data[index+1])+":"+ParseTool.ByteToHex(data[index]);			
				break;			
			case 0x8047:
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x8048:
			case 0x8049:
				rt=parseVC8048(data,index,9);
				break;
			case 0x804A:
			case 0x804B:
				rt=new Double(ParseTool.ByteToPercent(data[index]));
				break;
			case 0x804C:
				rt=parseVC804C(data,index,4);
				break;
			case 0x804D:
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x804E:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,4))/((double)100.0));
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8051... 0x8058(功控定值系列一设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8051(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);		
		nf.setGroupingUsed(false);
		sb.append(ParseTool.ByteToHex(data[index+6]));	//hh:mm
		sb.append(":");
		sb.append(ParseTool.ByteToHex(data[index+5]));
		sb.append(",");
		sb.append(String.valueOf(data[index+4] & 0xf));	//标识
		sb.append(",");
		sb.append(nf.format(((double)ParseTool.nBcdToDecimal(data,index,4))/((double)100.0)));
		
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X8059(功控定值系列一执行设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8059(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);		
		nf.setGroupingUsed(false);
		sb.append(ParseTool.ByteToHex(data[index+8]));	//MS
		sb.append("-");
		sb.append(ParseTool.ByteToHex(data[index+7])); //DS
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+6]));	//ME
		sb.append("-");
		sb.append(ParseTool.ByteToHex(data[index+5])); //DE
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+4]));	//TI
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+3]));	//N3
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+2]));	//N2
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+1]));	//N1
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+0]));	//N0		
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X805x (功控方案一设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC805X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8050:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x8051:
			case 0x8052:
			case 0x8053:
			case 0x8054:
			case 0x8055:
			case 0x8056:
			case 0x8057:
			case 0x8058:
				rt=parseVC8051(data,index,7);
				break;
			case 0x8059:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	
	/**
	 * 解析数据项——0X806x (电量控)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC806X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8060:
			case 0x8061:
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,4));	//电量限额
			case 0x8062:
				rt=String.valueOf(ParseTool.nBcdToDecimalS(data,index+1,4))+","+(data[index] & 0xff);
				break;
			case 0x8063:
				rt=new Integer(ParseTool.nBcdToDecimalS(data,index,5));
				break;
			case 0x8064:
				rt=ParseTool.ByteToHex(data[index+1])+":"+ParseTool.ByteToHex(data[index]);
				break;
			case 0x8065:
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,4));	//月度电量告警值
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X807x (功控方案二设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC807X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8070:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x8071:
			case 0x8072:
			case 0x8073:
			case 0x8074:
			case 0x8075:
			case 0x8076:
			case 0x8077:
			case 0x8078:
				rt=parseVC8051(data,index,7);
				break;
			case 0x8079:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X808x (功控方案三设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC808X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8080:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x8081:
			case 0x8082:
			case 0x8083:
			case 0x8084:
			case 0x8085:
			case 0x8086:
			case 0x8087:
			case 0x8088:
				rt=parseVC8051(data,index,7);
				break;
			case 0x8089:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X809x (功控方案四设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC809X(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x8090:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x8091:
			case 0x8092:
			case 0x8093:
			case 0x8094:
			case 0x8095:
			case 0x8096:
			case 0x8097:
			case 0x8098:
				rt=parseVC8051(data,index,7);
				break;
			case 0x8099:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X80Ax (功控方案五设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC80AX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x80A0:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x80A1:
			case 0x80A2:
			case 0x80A3:
			case 0x80A4:
			case 0x80A5:
			case 0x80A6:
			case 0x80A7:
			case 0x80A8:
				rt=parseVC8051(data,index,7);
				break;
			case 0x80A9:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X80Bx (功控方案6设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC80BX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){			
			case 0x80B0:
				rt=new Integer(data[index] & 0xf);	//功控时段数不超过8
			case 0x80B1:
			case 0x80B2:
			case 0x80B3:
			case 0x80B4:
			case 0x80B5:
			case 0x80B6:
			case 0x80B7:
			case 0x80B8:
				rt=parseVC8051(data,index,7);
				break;
			case 0x80B9:
				rt=parseVC8059(data,index,9);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X81XX(普通任务设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8101(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();		
		sb.append(ParseTool.ByteToHex(data[index]));	//TT
		sb.append(",");
		sb.append(String.valueOf(data[index+2] & 0xff)); //TS--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+1]));	//TS--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+4] & 0xff)); //TI--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+3]));	//TI--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+6] & 0xff)); //RS--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+5]));	//RS--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+8] & 0xff)); //RI--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+7]));	//RI--UU
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+9]))); //RDI
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+10]))); //TN
		sb.append(",");
		sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,index+11,2)));	//SP
		sb.append(",");
		sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,index+13,2)));	//RT
		sb.append(",");
		int din=ParseTool.BCDToDecimal(data[index+15]);
		sb.append(String.valueOf(din)); //DIN		
		if(din<=32){	//DI 不超过32
			int loc=index+16;
			for(int i=0;i<din;i++){
				sb.append(",");
				sb.append(ParseTool.BytesToHexC(data,loc,2));
				loc+=2;
			}
		}
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X81XX(中继任务设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8102(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();		
		sb.append(ParseTool.ByteToHex(data[index]));	//TT
		sb.append(",");
		sb.append(String.valueOf(data[index+2] & 0xff)); //TS--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+1]));	//TS--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+4] & 0xff)); //TI--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+3]));	//TI--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+6] & 0xff)); //RS--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+5]));	//RS--UU
		sb.append(",");
		sb.append(String.valueOf(data[index+8] & 0xff)); //RI--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+7]));	//RI--UU
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+9]))); //RDI
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+10]))); //PN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+11]));	//PS
		sb.append(",");
		sb.append(String.valueOf(ParseTool.nBcdToDecimal(data,index+12,2)));	//SP
		sb.append(",");
		sb.append(String.valueOf(data[index+14] & 0xff)); //WT
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+15]));	//CC
		sb.append(",");
		sb.append(String.valueOf(ParseTool.nByteToInt(data,index+16,2)));	//GF
		sb.append(",");
		sb.append(String.valueOf(ParseTool.nByteToInt(data,index+18,2)));	//GL
		sb.append(",");
		int cl=ParseTool.BCDToDecimal(data[index+20]);
		sb.append(String.valueOf(cl)); //CL	
		sb.append(",");
		if(cl<=32){	//CI 不超过32
			sb.append(ParseTool.BytesToHexL(data,index+21,cl));
		}
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X81XX(异常任务设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8104(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();		
		sb.append(ParseTool.ByteToHex(data[index]));	//TT
		sb.append(",");
		sb.append(ParseTool.BytesToHexC(data,index+1,2)); //ALR
		sb.append(",");		
		sb.append(String.valueOf(data[index+4] & 0xff)); //TI--NN
		sb.append(",");
		sb.append(ParseTool.ByteToHex(data[index+3]));	//TI--UU
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+5]))); //TN
		sb.append(",");
		int din=ParseTool.BCDToDecimal(data[index+6]);
		sb.append(String.valueOf(din)); //DIN		
		if(din<=32){	//DI 不超过32
			int loc=index+7;
			for(int i=0;i<din;i++){
				sb.append(",");
				sb.append(ParseTool.ByteToHex(data[loc+2]));
				sb.append(" ");
				sb.append(ParseTool.BytesToHexC(data,loc,2));				
				loc+=3;
			}
			sb.append(",");
			sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc])));
		}		
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X81xx (任务设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC81XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		if(datakey==0x8100){
			rt=new Integer(data[index] & 0xff);	//任务数
		}else if((datakey>0x8100) && (datakey<0x81fe)){
			//任务设置
			int type=(data[index] & 0xff);
			switch(type){
				case 0x01:
					parseVC8101(data,index,16);
					break;
				case 0x02:
					parseVC8102(data,index,21);
					break;
				case 0x04:
					parseVC8104(data,index,7);
					break;
				default:
					break;
			}
		}else if(datakey==0x81fe){	//任务标记
			rt=ParseTool.BytesToHexC(data,index,32);
		}		
		return rt;
	}
	
	/**
	 * 解析数据项——0X82xx (开关、信号设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC82XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		rt=new Integer(data[index] & 0xff);
		return rt;
	}
	
	/**
	 * 解析数据项——0X83xx (模拟量设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC83XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		if(datakey==0x8300){
			rt=new Integer(data[index] & 0xff);	//模拟量数
		}else if((datakey>0x8300) && (datakey<0x83fe)){
			//设置
			rt=String.valueOf(ParseTool.BCDToDecimal(data[index+1]))+","+String.valueOf((data[index] & 0xff)>>4)+","+String.valueOf((data[index] & 0xf));
		}else if(datakey==0x83fe){	//标记
			rt=ParseTool.BytesToHexC(data,index,32);
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X84xx (脉冲量设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC84XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		if(datakey==0x8400){
			rt=new Integer(data[index] & 0xff);	//模拟量数
		}else if((datakey>0x8400) && (datakey<0x84fe)){
			//设置
			rt=String.valueOf(ParseTool.BCDToDecimal(data[index+4]))+","+ParseTool.ByteToHex(data[index+3])+","+String.valueOf(ParseTool.nBcdToDecimal(data,index,3));
		}else if(datakey==0x84fe){	//标记
			rt=ParseTool.BytesToHexC(data,index,32);
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8501(运算量设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8501(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();		
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+18])));	//MM
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BytesToHexC(data,index+16,2)));	//DI1DI0		
		int loc=index+14;
		for(int i=0;i<8;i++){
			sb.append(",");
			sb.append(ParseTool.ByteToHex(data[loc+1]));	//cc
			sb.append(",");
			sb.append(String.valueOf(ParseTool.BCDToDecimal(data[loc]))); //NN
			loc-=2;
		}
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X85xx (运算量设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC85XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		if(datakey==0x8500){
			rt=new Integer(data[index] & 0xff);	//运算量数
		}else if((datakey>0x8500) && (datakey<0x85fe)){
			//设置
			rt=parseVC8501(data,index,19);
		}else if(datakey==0x85fe){	//标记
			rt=ParseTool.BytesToHexC(data,index,32);
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8601(差动规则设置)
	 * @param data
	 * @param index
	 * @return 
	 */
	public static Object parseVC8601(byte[] data,int index,int len){		
		StringBuffer sb=new StringBuffer();		
		NumberFormat nf=NumberFormat.getInstance();
		nf.setMaximumFractionDigits(2);
		nf.setGroupingUsed(false);
		sb.append(String.valueOf(ParseTool.BytesToHexC(data,index+9,2)));	//DI1DI0
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+8])));	//NN1
		sb.append(",");
		sb.append(String.valueOf(ParseTool.BCDToDecimal(data[index+7])));	//NN2
		sb.append(",");
		sb.append(nf.format(((double)ParseTool.nBcdToDecimal(data,index+4,3))/((double)100.0)));	//MMMM.MM
		sb.append(",");
		sb.append(nf.format(((double)ParseTool.nBcdToDecimal(data,index+2,2))/((double)100.0)));	//RR.RR
		sb.append(",");
		sb.append(nf.format(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)100.0)));	//SS.SS
		return sb.toString();
	}
	
	/**
	 * 解析数据项——0X86xx (差动规则设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC86XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		if(datakey==0x8600){
			rt=new Integer(data[index] & 0xff);	//运算量数
		}else if((datakey>0x8600) && (datakey<0x86fe)){
			//设置			
			rt=parseVC8601(data,index,19);
		}else if(datakey==0x86fe){	//标记
			rt=ParseTool.BytesToHexC(data,index,32);
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X87xx (端口设置)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC87XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey & 0xff0f){
			case 0x8700:
				rt=new Integer((data[index] & 0xff) * 300);
				break;
			case 0x8701:
				rt=new Integer(ParseTool.BCDToDecimal(data[index]));
				break;
			case 0x8702:
				rt=new Integer(ParseTool.BCDToDecimal(data[index]));
				break;
			case 0x8703:
				rt=new Integer(ParseTool.BCDToDecimal(data[index]));
				break;				
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X88xx ()
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC88XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){
			case 0x8800:	//终端状态字
				rt=ParseTool.BytesToHexC(data,index,2);
				break;
			case 0x8801:	//终端供电电压
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)10.0));
				break;
			case 0x8802:
			case 0x8803:
			case 0x8804:
			case 0x8805:
			case 0x8806:
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x8807:
				rt=ParseTool.BytesToHexC(data,index,2);
				break;
			case 0x8808:
				rt=ParseTool.BytesToHexC(data,index,1);
				break;
			case 0x8809:
				rt=ParseTool.BytesToHexC(data,index,8);
				break;
			case 0x880A:
				rt=ParseTool.BytesToHexC(data,index,2);
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X89xx (测量点参数)
	 * @param data
	 * @param index
	 * @param len
	 * @param datakey
	 * @return
	 */
	public static Object parseVC89XX(byte[] data,int index,int len,int datakey){
		Object rt=null;
		switch(datakey){
			case 0x8900:	//终端状态字
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x8901:	//终端供电电压
				rt=ParseTool.ByteToHex(data[index]);
				break;
			case 0x8902:
				rt=ParseTool.BytesToHexC(data,index,6);
				break;
			case 0x8903:
				rt=ParseTool.ByteToHex(data[index]);
				break;
			case 0x8904:
				rt=new Integer(ParseTool.BCDToDecimal(data[index]));
				break;
			case 0x8905:
				rt=new Integer(ParseTool.BCDToDecimal(data[index])*300);
				break;
			case 0x8806:
				rt=new Integer(data[index] & 0xff);
				break;
			case 0x8910:
				rt=ParseTool.ByteToHex(data[index]);
				break;				
			case 0x8911:
			case 0x8912:
			case 0x8913:
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,2));
				break;
			case 0x8914:
			case 0x8915:
			case 0x8916:
			case 0x8921:
			case 0x8922:
			case 0x8923:
			case 0x8924:
			case 0x8925:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)100.0));
				break;
			case 0x8926:
			case 0x8927:
			case 0x8928:
			case 0x8929:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,1))/((double)100.0));
				break;
			default:
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X9xxx (电能数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVC9XXX(byte[] data,int index,int len,int datakey){
		Double rt=null;
		int v=ParseTool.nBcdToDecimal(data,index,len);
		rt=new Double(((double)v)/((double)100.0));
		return rt;
	}
	
	/**
	 * 解析数据项——0XAxxx (需量数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCAXXX(byte[] data,int index,int len,int datakey){
		Double rt=null;
		int v=ParseTool.nBcdToDecimal(data,index,len);
		rt=new Double(((double)v)/((double)10000.0));
		return rt;
	}
	
	/**
	 * 解析数据项——0XB0xx 0XB4xx(需量发生时间)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCB0XX(byte[] data,int index,int len,int datakey){		
		return ParseTool.getTimeM(data,index);
	}
	
	/**
	 * 解析数据项——0XB2xx (编程记录块)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCB2XX(byte[] data,int index,int len,int datakey){		
		Object rt=null;
		switch(datakey){
			case 0xB210:
			case 0xB211:
				rt=parseVCB0XX(data,index,len,datakey);
				break;
			case 0xB212:
			case 0xB213:
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,len));
				break;
			default:
				
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0XB3xx (断相数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCB3XX(byte[] data,int index,int len,int datakey){		
		Object rt=null;
		switch(datakey & 0xfff0){
			case 0xB310:			
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,2));
				break;
			case 0xB320:			
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,3));
				break;
			default:
				
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0XB6xx (断相数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCB6XX(byte[] data,int index,int len,int datakey){		
		Object rt=null;
		switch(datakey & 0xfff0){
			case 0xB610:			
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,2));
				break;
			case 0xB620:			
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)100.0));
				break;
			case 0xB630:			
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,3))/((double)10000.0));
				break;
			case 0xB640:			
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)100.0));
				break;
			case 0xB650:			
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)1000.0));
				break;
			default:
				
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0XCXxx (表计数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVCCXXX(byte[] data,int index,int len,int datakey){		
		Object rt=null;		
		switch(datakey){
			case 0xC010:			
				rt="20"+ParseTool.ByteToHex(data[index+3])+"-"+ParseTool.ByteToHex(data[index+2])
					+"-"+ParseTool.ByteToHex(data[index+1])+","+ParseTool.ByteToHex(data[index]);				
				break;
			case 0xC011:			
				rt=ParseTool.ByteToHex(data[index+2])
					+":"+ParseTool.ByteToHex(data[index+1])+":"+ParseTool.ByteToHex(data[index]);
				break;
			case 0xC020:			
				rt=new Integer(data[index] & 0xf);
				break;
			case 0xC030:
			case 0xC031:
				rt=new Integer(ParseTool.nBcdToDecimal(data,index,3));
				break;
			case 0xC119:
			case 0xC11A:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,4))/((double)100.0));
				break;
			case 0xC331:
			case 0xC332:
			case 0xC333:
			case 0xC334:
			case 0xC335:
			case 0xC336:
			case 0xC337:
			case 0xC338:
				rt=ParseTool.ByteToHex(data[index+2])
					+":"+ParseTool.ByteToHex(data[index+1])+","+ParseTool.ByteToHex(data[index]);
				break;
			default:
				
				break;
		}
		return rt;
	}
	
	/**
	 * 解析数据项——0X8Exx (断相数据)
	 * @param data
	 * @param index
	 * @param len
	 * @return
	 */
	public static Object parseVC8EXX(byte[] data,int index,int len,int datakey){		
		Object rt=null;
		switch(datakey & 0xfff0){
			case 0x8E10:
			case 0x8E20:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,4))/((double)100.0));
				break;
			case 0x8E30:
			case 0x8E40:
				rt=new Double(((double)ParseTool.nBcdToDecimalS(data,index,4))/((double)10.0));
				break;
			case 0x8E60:			
				if(datakey<0x8E62){
					rt=new Double(((double)ParseTool.nBcdToDecimalS(data,index,4))/((double)10.0));
				}else{
					rt=new Double(((double)ParseTool.nBcdToDecimalS(data,index,3))/((double)100.0));
				}
				break;
			case 0x8E70:
				rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)100.0));
				break;
			case 0x8E80:
				if(datakey<0x8E86){
					rt=new Integer(ParseTool.BCDToDecimal(data[index]));
				}else{
					SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd hh:mm");
					Calendar time=ParseTool.getTime(data,index+2);
					int val=ParseTool.nBcdToDecimal(data,index,2);
					rt=sf.format(time.getTime())+","+val;
				}
				break;
			case 0x8E90:
				if(datakey==0x8E90){
					rt=new Double(((double)ParseTool.nBcdToDecimal(data,index,2))/((double)10.0));
				}else if(datakey<0x8E93){
					SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
					NumberFormat nf=NumberFormat.getInstance();
					nf.setMaximumFractionDigits(2);
					nf.setGroupingUsed(false);
					Calendar time=ParseTool.getTime(data,index+3);
					int val=ParseTool.nBcdToDecimalS(data,index,3);
					rt=sf.format(time.getTime())+","+nf.format(((double)val)/((double)100.0));
				}else{
					SimpleDateFormat sf=new SimpleDateFormat("yyyy-MM-dd HH:mm");
					NumberFormat nf=NumberFormat.getInstance();
					nf.setMaximumFractionDigits(2);
					nf.setGroupingUsed(false);
					Calendar time=ParseTool.getTime(data,index+2);
					int val=ParseTool.nBcdToDecimal(data,index,2);
					rt=sf.format(time.getTime())+","+nf.format(((double)val)/((double)100.0));
				}
				break;
			default:
				
				break;
		}
		return rt;
	}
	
	
	
	/**
	 * 解析错误码
	 * @param err
	 * @return
	 */
	public static Object parseVError(byte err){
		String rt="";
		switch(err & 0xff){
			case 0x0:
				rt="正确";
				break;
			case 0x01:
				rt="中继命令没有返回";
				break;
			case 0x02:
				rt="设置内容非法";
				break;
			case 0x03:
				rt="密码权限不足";
				break;
			case 0x04:
				rt="无此数据项";
				break;
			case 0x05:
				rt="命令时间失效";
				break;
			case 0x11:
				rt="目标地址不存在";
				break;
			case 0x12:
				rt="发送失败";
				break;
			case 0x13:
				rt="短信息帧太长";
				break;
		    default:
		    	break;
		}
		return rt;
	}
}
