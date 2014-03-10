import cn.hexing.util.HexDump;

/**
 * 	 * 初始化，客户端通过action操作image transfer对象的方法1来告之表计将要传输的升级包的image_identifier以及升级包的字节数，其中image_identifier包含升级文件头中的升级文件标识+文件类型+对散列校验码加密的加密类型+校验类型+	版本控制+散列校验码（当对散列校验码加密的加密类型=00时）或散列校验码密文（当对散列校验码加密的加密类型不等于00时）
	 * 1)	Byte1-byte2升级文件标识：2字节，固定HX的ASCII码；
	   2)	Byte3文件类型：1字节
			a)	00H：清除传输文件，恢复到升级前状态。
			b)	01H：终端升级文件（适用于终端、集中器、计量柜）
			c)	02H：远程（上行）通讯模块升级文件。
			d)	03H：本地通讯模块升级文件。
			e)	04H：采集器升级的采集器地址文件。
			f)	05H：采集器升级的采集器程序文件。
			g)	06H：采集器通信模块升级的地址文件。
			h)	07H：采集器通信模块升级的程序文件。
			i)	08H：表计升级文件
			j)	FFH：代表主站下发任意文件程序（其中文件的第一帧中包含文件的相关信息，目前采用该格式升级集中器程序和表计的程序）
	   3)	Byte4对散列校验码加密的加密类型：1字节； 定义（
			00：不加密；
	     	01：AES－GCM-128加密，。其中IV固定为12字节00，当表计通信的身份验证机制为NONE时，密钥固定为16字节00；当表计通信的身份验证机制为LLS时，密钥为8字节00+LLS密码，如LLS密码为12345678时，此处的密钥为00 00 00 00 00 00 00 0031 32 33 34 35 36 37 38；当身份验证机制为HLS时，又分为两种情况，通信加密或不加密，通信加密时，此处的密钥为通信密钥EK，通信不加密时，此处的密钥为身份验证的密钥HLS Secret。密钥是通信密钥或者全0；）
	   4)	Byte5校验类型：1字节； 定义（0：无校验；1：MD5；2：CRC16；）
	   5)	Byte6-byte25版本控制：20字节，不足后补00；一般用于文件名来指定适用表计的类型和版本。
	   6)	Byte26-byte41散列校验码：16字节，不足后补00；
 * @author gaoll
 *
 * @time 2013-2-17 上午9:30:16
 *
 * @info
 */
public class FileHead {
	public static void main(String[] args) {
		byte[] fileHead = new byte[41];
		byte[] hx = "HX".getBytes();//固定两个字节
		System.arraycopy(hx, 0,fileHead , 0, 2);
		fileHead[2]=0x08;//表计升级
		fileHead[3]=0;//不加密
		fileHead[4]=0;//无校验
		byte[] version = new byte[20];
		byte[] srcVersion = "1".getBytes();
		System.arraycopy(srcVersion, 0, version, 0, srcVersion.length);
		System.arraycopy(version, 0,fileHead , 5, 20);
		byte[] verfiy = new byte[16];
		System.arraycopy(verfiy, 0,fileHead , 25, 16);
		System.out.println(HexDump.toHex(fileHead));
	}
}
