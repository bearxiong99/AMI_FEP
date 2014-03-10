package cn.hexing.dlms.cipher;

import java.io.IOException;

/**
 *  ����systitle   4858451100000000
 *  Byte1	MC	0x48
	Byte2	MC	0x58
	Byte3	MC	0x45
	Byte4	T1	0x01�������
				0x03�������
				0x10�������ͻ���
				0x11������ͻ���
	Byte5	T2	Byte5�ĸ߰��ֽ�bit7-bit4
				Bit7������
				Bit6���Ƿ��й�������������������ˮ��
				Bit5���Ƿ��и��ɹ���
				Bit4���Ƿ���̵���
				�ͻ��˴˰��ֽڹ̶�Ϊ0
	Byte6	SN
	Byte7	SN
	Byte8	SN
	SN	������кŵ�16�����ֽڣ���������ת��Ϊ16���Ƴ���3�����ֽ�ʱ���ص����ֽڡ�
	�������к�Ϊ12345678��˴����Ϊ0 BC 61 4E���ͻ��˵�SN�ֽڹ̶�Ϊ0

 * @author gaoll
 *
 * @time 2012-12-21 ����2:58:20
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
