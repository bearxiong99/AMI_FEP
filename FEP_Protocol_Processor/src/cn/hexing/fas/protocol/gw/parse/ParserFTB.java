package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * ��������BCD��ת����ʽ
 *
 */
public class ParserFTB {	
	/**
	 * BCD->Float
	 * @param data(���ֽڵ��õ�BCD) 	���磺
	 * @param format(��ֵ��ʽ) 		���磺
	 * @param len(�ַ���)				���磺
	 * @return String(Float)		���磺
	 */
	public static String parseValue(String data,String format,int len){
		String rt="";
		try{			
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)//��Чֵ�ж�
				return rt;
			if(!DataSwitch.isBCDString(data))//BCD����
				return rt;
			String tag="",sZS="",sXS="";
			if (format.substring(0,1).equals("C")){//�����ŵĸ�����:����������,��������Ҫ������
				tag=data.substring(0,1);           //���Ÿ߰��ֽ�
	            if ((Integer.parseInt(tag,16)&8)==8){//���λ����1Ϊ����
	            	tag=Integer.toString((Integer.parseInt(tag,16)&7));//��ȥ����λ
	            	data=tag+data.substring(1,data.length());
	            	tag="-";
	            }
	        }
	          
	        int iPos=format.indexOf('.',0);
	        int iLenBCD=data.length();
	        if (iPos !=-1) {//���ݸ�ʽ��С����
	        	 int iLenSJGS=format.length()-1;
	        	 if ((iLenBCD==iLenSJGS)&&((iLenBCD % 2)==0)) {  //���ȺϷ��ж�
	        		 sZS=data.substring(0,iPos);      //��������
		             if (iPos==0){
		            	 sZS="0";
		             }
		             sXS=data.substring(iPos,iLenBCD);//С������
		             rt=sZS+"."+sXS;
	        	 }
	        	 else {
	        		 rt="";  //��Чֵ
	        	 }
	        }
	        else {          //���ݸ�ʽΪ����
	        	int iLenSJGS=format.length();
	        	if ((iLenBCD==iLenSJGS)&&((iLenBCD % 2)==0)) {  //���ȺϷ��ж�
	        		rt=data;
	        	}
	        	else {
	        		rt="";    //��Чֵ
	        	}
	        }
	        if(tag.equals("-")&&!rt.equals("")){//Ϊ����������ֵ��Ч
	        	rt=tag+rt;
	        }
	        //rt=""+Double.parseDouble(rt);
	        String ft="%."+sXS.length()+"f";
	        rt=String.format(ft, Double.parseDouble(rt));
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * Float->BCD
	 * @param data(Float) 				���磺
	 * @param format(��ֵ��ʽ) 			���磺
	 * @param len(����ʮ�����ַ�����)		���磺
	 * @return String(���ֽڵ��õ�BCD)		���磺
	 */
	public static String constructor(String data,String format,int len){
		String rt="";
		try{
			String tag="",sZS="",sXS="";
			int iLenZS=0,iLenXS=0;
	        if (format.substring(0,1).equals("C")){//�����ŵĸ�����:����������,��������Ҫ������
	          if (data.substring(0,1).equals("-")){//����
	        	  data=data.substring(1,data.length());
	        	  tag="-";
	          }
	          else{
	        	  if (data.substring(0,1).equals("+"))//��"+"����ȥ
	        		  data=data.substring(1,data.length());
	        	  tag="+";
	          }
	        }
	        int iPos=format.indexOf('.',0);
	        if (iPos!=-1){//��ʽ��С����
	          iLenZS=(format.substring(0,iPos)).length();
	          iLenXS=(format.substring(iPos+1,format.length())).length() ;
	          iPos=data.indexOf('.',0);
	          if (iPos!=-1){//���ݴ�С����
	            sZS=DataSwitch.StrStuff("0",iLenZS,data.substring(0,iPos),"left");
	            sXS=DataSwitch.StrStuff("0",iLenXS,data.substring(iPos+1,data.length()),"right");
	          }
	          else {//û����ʽ��С����
	            sZS=DataSwitch.StrStuff("0",iLenZS,data,"left");
	            sXS=DataSwitch.StrStuff("0",iLenXS,sXS,"right");
	          }
	          rt=sZS+sXS;
	        }
	        else {//��ʽ����С����
	          iPos=data.indexOf('.',0);
	          iLenZS=format.length();
	          if (iPos!=-1){//���ݴ�С����
	            sZS=DataSwitch.StrStuff("0",iLenZS,data.substring(0,iPos),"left");
	          }
	          else {//���ݲ���С����
	            sZS=DataSwitch.StrStuff("0",iLenZS,data,"left");
	          }
	          rt=sZS;
	        }
	        if (tag.equals("-")){    //��һ���ֽ����λ��1
	        	tag=Integer.toString((Integer.parseInt(rt.substring(0,1),16)|8));
	        	rt=tag+rt.substring(1,rt.length());
	        }
	        else if(tag.equals("+")){//��һ���ֽ����λ��0
	        	tag=Integer.toString((Integer.parseInt(rt.substring(0,1),16)&7));
	        	rt=tag+rt.substring(1,rt.length());
	        }
	        rt=DataSwitch.ReverseStringByByte(rt);//���ֽڵ���
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
	
}
