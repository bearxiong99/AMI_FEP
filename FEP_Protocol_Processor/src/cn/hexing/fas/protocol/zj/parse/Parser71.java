package cn.hexing.fas.protocol.zj.parse;

import java.net.Inet4Address;
import java.net.InetAddress;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.utils.HexDump;
/**
 * 
 * @author gaoll
 *
 * @time 2013-8-23 上午10:09:42
 *
 * @info 用于FTP上传  ip#port#日志编号 广规
 */
public class Parser71 {
	
//	private static final Logger log = Logger.getLogger(Parser71.class);

	/**
	 * 组帧
	 * @param frame 存放数据的帧
	 * @param value 数据值(前后不能有多余空字符)
	 * @param loc   存放开始位置
	 * @param len   组帧长度
	 * @param fraction 数据中包含的小数位数
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			
			String[] values = value.split("#");
			String ip = values[0];
			String no = values[2];
			InetAddress netAddress = Inet4Address.getByName(ip);
			byte[] b_ip=netAddress.getAddress();
			
			ip = DataSwitch.ReverseStringByByte(HexDump.toHex(b_ip));
			b_ip = HexDump.toArray(ip);
			System.arraycopy(b_ip, 0, frame, loc, 4);
			int i_port = Integer.parseInt(values[1]);
			loc+=4;
    		frame[loc]=(byte)(i_port & 0xff);
    		frame[loc+1]=(byte)((i_port & 0xff00)>>8);
			loc+=2;
			frame[loc]=(byte) (Integer.parseInt(no) & 0xFF);
			len=7;
		}catch(Exception e){
			throw new MessageEncodeException("错误的 MM-DD hh:mm 组帧参数:"+value);
		}
		return len;
	}
}
