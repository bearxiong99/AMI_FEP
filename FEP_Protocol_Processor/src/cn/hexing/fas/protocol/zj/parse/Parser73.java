package cn.hexing.fas.protocol.zj.parse;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2013-8-29 上午10:05:08
 *
 * @info 直接以字符串返回帧内容
 */
public class Parser73 {
	private static final Logger log = Logger.getLogger(Parser73.class);

	/**
	 * 解析
	 * @param data 数据帧
	 * @param loc  解析开始位置
	 * @param len  解析长度
	 * @param fraction 解析后小数位数
	 * @return 数据值
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		byte[] dest = new byte[len];
		System.arraycopy(data, loc, dest, 0, len);
		return HexDump.toHex(dest);
	}
}
