package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-27 ����1:47:48
 *
 * @info �¶ȸߵͷ�ֵ����ѹ�ߵͷ�ֵ
 */
public class Parser61 {
	
	/**
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  �����ֽڳ���
	 * @param fraction ���������ݰ�����С��λ��
	 * @return ��������
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt = null;
		
		//�˸��ֽ�У׼����
		loc+=8;
		//�ĸ��ֽڷ�ֵ (ǰ�����ֽ� �ͷ�ֵ���������ֽ�Ϊ �߷�ֵ)
		int i_high=Integer.parseInt(HexDump.toHex(data, loc, 2),16);
		loc+=2;
		int i_low=Integer.parseInt(HexDump.toHex(data, loc, 2),16);
		String s_high="";
		String s_low="";
		if(fraction>0){
			NumberFormat snf=NumberFormat.getInstance();
			snf.setMinimumFractionDigits(fraction);
			snf.setGroupingUsed(false);
			s_high=snf.format((double)i_high/ParseTool.fraction[fraction]);
			s_low = snf.format((double)i_low/ParseTool.fraction[fraction]);
		}
		rt = s_high+":"+s_low;
  		return rt;
	}
	
	public static void main(String[] args) {
		Parser61.parsevalue(HexDump.toArray("051105220533054405EB05EE"), 0, 2, 2);
		
		
		
	}
	
	
}
