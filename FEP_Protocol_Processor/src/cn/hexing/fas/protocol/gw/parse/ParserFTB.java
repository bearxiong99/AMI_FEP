package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 浮点数与BCD码转换格式
 *
 */
public class ParserFTB {	
	/**
	 * BCD->Float
	 * @param data(按字节倒置的BCD) 	例如：
	 * @param format(数值格式) 		例如：
	 * @param len(字符数)				例如：
	 * @return String(Float)		例如：
	 */
	public static String parseValue(String data,String format,int len){
		String rt="";
		try{			
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)//无效值判断
				return rt;
			if(!DataSwitch.isBCDString(data))//BCD码检测
				return rt;
			String tag="",sZS="",sXS="";
			if (format.substring(0,1).equals("C")){//带符号的浮点数:负数带负号,正数不需要带正号
				tag=data.substring(0,1);           //符号高半字节
	            if ((Integer.parseInt(tag,16)&8)==8){//最高位等于1为负数
	            	tag=Integer.toString((Integer.parseInt(tag,16)&7));//消去符号位
	            	data=tag+data.substring(1,data.length());
	            	tag="-";
	            }
	        }
	          
	        int iPos=format.indexOf('.',0);
	        int iLenBCD=data.length();
	        if (iPos !=-1) {//数据格式带小数点
	        	 int iLenSJGS=format.length()-1;
	        	 if ((iLenBCD==iLenSJGS)&&((iLenBCD % 2)==0)) {  //长度合法判断
	        		 sZS=data.substring(0,iPos);      //整数部分
		             if (iPos==0){
		            	 sZS="0";
		             }
		             sXS=data.substring(iPos,iLenBCD);//小数部分
		             rt=sZS+"."+sXS;
	        	 }
	        	 else {
	        		 rt="";  //无效值
	        	 }
	        }
	        else {          //数据格式为整数
	        	int iLenSJGS=format.length();
	        	if ((iLenBCD==iLenSJGS)&&((iLenBCD % 2)==0)) {  //长度合法判断
	        		rt=data;
	        	}
	        	else {
	        		rt="";    //无效值
	        	}
	        }
	        if(tag.equals("-")&&!rt.equals("")){//为负数且数据值有效
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
	 * @param data(Float) 				例如：
	 * @param format(数值格式) 			例如：
	 * @param len(返回十进制字符长度)		例如：
	 * @return String(按字节倒置的BCD)		例如：
	 */
	public static String constructor(String data,String format,int len){
		String rt="";
		try{
			String tag="",sZS="",sXS="";
			int iLenZS=0,iLenXS=0;
	        if (format.substring(0,1).equals("C")){//带符号的浮点数:负数带负号,整数不需要带正号
	          if (data.substring(0,1).equals("-")){//负数
	        	  data=data.substring(1,data.length());
	        	  tag="-";
	          }
	          else{
	        	  if (data.substring(0,1).equals("+"))//带"+"则消去
	        		  data=data.substring(1,data.length());
	        	  tag="+";
	          }
	        }
	        int iPos=format.indexOf('.',0);
	        if (iPos!=-1){//格式带小数点
	          iLenZS=(format.substring(0,iPos)).length();
	          iLenXS=(format.substring(iPos+1,format.length())).length() ;
	          iPos=data.indexOf('.',0);
	          if (iPos!=-1){//数据带小数点
	            sZS=DataSwitch.StrStuff("0",iLenZS,data.substring(0,iPos),"left");
	            sXS=DataSwitch.StrStuff("0",iLenXS,data.substring(iPos+1,data.length()),"right");
	          }
	          else {//没按格式带小数点
	            sZS=DataSwitch.StrStuff("0",iLenZS,data,"left");
	            sXS=DataSwitch.StrStuff("0",iLenXS,sXS,"right");
	          }
	          rt=sZS+sXS;
	        }
	        else {//格式不带小数点
	          iPos=data.indexOf('.',0);
	          iLenZS=format.length();
	          if (iPos!=-1){//数据带小数点
	            sZS=DataSwitch.StrStuff("0",iLenZS,data.substring(0,iPos),"left");
	          }
	          else {//数据不带小数点
	            sZS=DataSwitch.StrStuff("0",iLenZS,data,"left");
	          }
	          rt=sZS;
	        }
	        if (tag.equals("-")){    //第一个字节最高位置1
	        	tag=Integer.toString((Integer.parseInt(rt.substring(0,1),16)|8));
	        	rt=tag+rt.substring(1,rt.length());
	        }
	        else if(tag.equals("+")){//第一个字节最高位置0
	        	tag=Integer.toString((Integer.parseInt(rt.substring(0,1),16)&7));
	        	rt=tag+rt.substring(1,rt.length());
	        }
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
	
}
