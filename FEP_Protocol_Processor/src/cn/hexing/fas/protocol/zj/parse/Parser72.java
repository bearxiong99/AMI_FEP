package cn.hexing.fas.protocol.zj.parse;

import java.text.NumberFormat;

import org.apache.log4j.Logger;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.utils.StringUtil;

/**
 * ������λ��BCD����ֵ��������֡(��λ��ǰ����λ�ں�)
 *
 */
public class Parser72 {
	private static final Logger log = Logger.getLogger(Parser72.class);

	/**
	 * ����
	 * @param data ����֡
	 * @param loc  ������ʼλ��
	 * @param len  ��������
	 * @param fraction ������С��λ��
	 * @return ����ֵ
	 */
	public static Object parsevalue(byte[] data,int loc,int len,int fraction){
		Object rt=null;
		try{
			boolean ok=true;
			/*if((data[loc] & 0xff)==0xff){
				ok=ParseTool.isValid(data,loc,len);
			}*/
			ok=ParseTool.isValidBCD(data,loc,len);
			if(ok){
				boolean bn=((data[loc] & 0xF0)==0xF0);
				int val=ParseTool.nBcdToDecimalC(data,bn?loc+1:loc,bn?len-1:len);
				if(val>=0){
					if(bn){
						val=-val;
					}
					if(fraction>0){
						NumberFormat snf=NumberFormat.getInstance();
						snf.setMinimumFractionDigits(fraction);
						snf.setMinimumIntegerDigits(1);
						snf.setGroupingUsed(false);
						rt=snf.format((double)val/ParseTool.fraction[fraction]);
					}else{
						rt=new Integer(val);
					}
				}
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
	public static void main(String[] args) {
		parsevalue(new byte[]{(byte) 0xF0,(byte) 0x81,0x68},0,3,2);
	}
	
	/**
	 * ��֡
	 * @param frame ������ݵ�֡
	 * @param value ����ֵ
	 * @param loc   ��ſ�ʼλ��
	 * @param len   ��֡����
	 * @param fraction �����а�����С��λ��
	 * @return
	 */
	public static int constructor(byte[] frame,String value,int loc,int len,int fraction){
		try{
			NumberFormat nf=NumberFormat.getInstance();
			nf.setMaximumFractionDigits(4);			
			
			double val=nf.parse(value).doubleValue();
			if(fraction>0){
				val*=ParseTool.fraction[fraction];
			}
			boolean bn=(val<0);
			if(bn){
				val=-val;
			}
			ParseTool.IntToBcd(frame,(int)Math.round(val),loc,len);
			if(bn){
				frame[loc+len-1]=(byte)((frame[loc+len-1] & 0xf) | 0x10);
			}
		}catch(Exception e){
			throw new MessageEncodeException("�����BCD����֡����:"+value);
		}
		return len;
	}
}
