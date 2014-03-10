package cn.hexing.dlms.cipher;

import java.io.IOException;

/**
 *  下行systitle   4858451100000000
 *  Byte1	MC	0x48
	Byte2	MC	0x58
	Byte3	MC	0x45
	Byte4	T1	0x01—单相表
				0x03—三相表
				0x10—公共客户端
				0x11—管理客户端
	Byte5	T2	Byte5的高半字节bit7-bit4
				Bit7—保留
				Bit6—是否有管理其他能量表如气表、水表
				Bit5—是否有负荷管理
				Bit4—是否带继电器
				客户端此半字节固定为0
	Byte6	SN
	Byte7	SN
	Byte8	SN
	SN	表的序列号的16进制字节，当表序列转换为16进制超过3个半字节时，截掉高字节。
	如表的序列号为12345678则此处表达为0 BC 61 4E。客户端的SN字节固定为0

 * @author gaoll
 *
 * @time 2012-12-21 下午2:58:20
 *
 * @info
 */
public class Application {
	public static void main(String[] args) throws IOException {
		String frame = "CF15300000000A8EBD94F04183FF2421B6B0854AAE4C95";
		String sysTitle = "4858450305573043";
		System.out.println(DecipherAssistant.getInstance().decipher(frame, sysTitle));
	}
}
