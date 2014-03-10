package cn.hexing.fas.protocol.gw.parse;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.exception.MessageEncodeException;

/**
 * 带幂部和正负号的十进制与十六进制转换格式
 *
 */
public class ParserA2 {	
	/**
	 * HEX->BCD
	 * @param data(按字节倒置的HEX) 	例如：
	 * @param len(字符数)				例如：
	 * @return String(带正负号BCD)	例如：
	 */
	public static String parseValue(String data,int len){
		String rt="";
		try{			
			data=DataSwitch.ReverseStringByByte(data.substring(0,len));	
			if (data.indexOf("EE")>=0||data.indexOf("FF")>=0)
				return rt;
			if(!DataSwitch.isBCDString(data.substring(1,data.length())))//BCD码检测
				return rt;
			String tag=data.substring(0,1);             //幂部、符号高半字节
	        int iMB=Integer.parseInt(tag,16)&14;//高三位为幕部
	        if ((Integer.parseInt(tag,16)&1)==1)//最低位等于1为负数
	        	tag="-";
	        else
	        	tag=""; //正数不带正号
	       
	        float iBCD=Integer.parseInt(data.substring(1,data.length())); //取整数部分
	        switch(iMB){
            	case 0:{
            		iBCD=iBCD*10000;
            		break;
            	}
            	case 2:{
            		iBCD=iBCD*1000;
            		break;
            	}
            	case 4:{
            		iBCD=iBCD*100;
            		break;
            	}
            	case 6:{
            		iBCD=iBCD*10;
            		break;
            	}
            	case 8:{
            		iBCD=iBCD*1;
            		break;
            	}
            	case 10:{
            		iBCD=iBCD/10;
            		break;
            	}
            	case 12:{
            		iBCD=iBCD/100;
            		break;
            	}
            	case 14:{
            		iBCD=iBCD/1000;
            		break;
            	}
	        }
	        rt=tag+""+iBCD;
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return rt;
	}
	
	/**
	 * BCD->HEX
	 * @param data(带正负号BCD) 			例如：
	 * @param len(返回十六进制字符长度)		例如：
	 * @return String(按字节倒置的HEX)		例如：
	 */
	public static String constructor(String data,int len){
		String rt="";
		try{			
			Double d = new Double(data);
		    String sFH = "";
		    String sZSSjz = "";
		    String sMB = "";
		    if (d.doubleValue() >= 0.0) { //判断是正数、还是负数
		      sFH = "+";
		      if (d.doubleValue() >= 9990000.0) {
		        return "0999"; //最大值
		      }
		    }
		    else if (d.doubleValue() < 0.0) {
		      sFH = "-";
		      if (d.doubleValue() > -0.0001) {
		        return "8001"; //最小值
		      }
		      try {
		    	  data = d.toString().substring(1);
		      }
		      catch (Exception ex7) {
		        return "0000";//处理异常情况，返回0000
		      }
		      d = new Double(data);
		    }
		    try {
		      char[] cFloat = d.toString().toCharArray();
		      for (int i = 0; i < cFloat.length; i++) {
		        if ( ( (cFloat[i] >= '0') && (cFloat[i] <= '9')) || (cFloat[i] == '.')) { //判断数字中是否存在非法字符
		          continue;
		        }
		        else {
		          return "0000";
		        }
		      }
		    }
		    catch (Exception ex6) {
		      return "0000";//处理异常情况，返回0000
		    }
		    int iDotPos = data.indexOf(".");
		    if (iDotPos == -1) { //处理没有小数点的值
		      try {
		        if (data.length() >= 7) {
		          sZSSjz = data.substring(0, 3); //数字有效个数最多为3个
		          sMB = "10000"; //幂指数最大的情况
		        }
		      }
		      catch (Exception ex4) {
		        return "0000";//处理异常情况，返回0000
		      }
		      if ( (data.length() < 7) && (data.length() > 3)) {
		        try {
		          sZSSjz = data.substring(0, 3); //取前面三位数字
		        }
		        catch (Exception ex5) {
		          return "0000";//处理异常情况，返回0000
		        }
		        switch (data.length() - 3) { //根据剩余长度计算幂指数
		          case 1: {
		            sMB = "10";
		            break;
		          }
		          case 2: {
		            sMB = "100";
		            break;
		          }
		          case 3: {
		            sMB = "1000";
		            break;
		          }
		        }
		      }
		      else {
		        while (data.length() < 3) {
		        	data ="0"+ data ;
		        }
		        sZSSjz = data; //不足3个字符补0
		        sMB = "1";
		      }
		    }
		    try {
		      if ( (iDotPos == 1) && (data.substring(0, 1).equals("0"))) { //处理如0.123 或0.12345
		        sMB = "0.001"; //幂指数是固定的
		        if ( (data.length() - iDotPos) < 4) { //有效数字的个数处理，这种情况需要在数字后面补充0
		          sZSSjz = data.substring(iDotPos + 1, data.length());
		          while (sZSSjz.length() < 3) {
		            sZSSjz = sZSSjz + "0";
		          } //处理成120或100或010
		        }
		        else { //否则就取前面3位数字即可
		          sZSSjz = data.substring(iDotPos + 1, iDotPos + 4);
		        }
		      }
		    }
		    catch (Exception e) {
		    	throw new MessageEncodeException(e);
		    }
		    try {
		      if ( (iDotPos >= 0) && (iDotPos <= 2) &&
		          (!data.substring(0, 1).equals("0"))) { //处理如1.2345或12.345
		        if (data.length() < 4) { //如1.2
		        	data = data + "0"; //补上0，保证有三位数字可以取到
		        }
		        data = data.substring(0, 4); //得到如1.23或 12.3或1.20
		        sZSSjz = data.substring(0, iDotPos) +
		        data.substring(iDotPos + 1, 4); //去除小数点后的三位有效数字
		        if ( (data.length() - iDotPos) == 2) { //计算幂指数
		          sMB = "0.1";
		        }
		        else {
		          sMB = "0.01";
		        }
		      }
		    }
		    catch (Exception e) {
		    	throw new MessageEncodeException(e);
		    }
		    if ( (iDotPos >= 3) && (iDotPos < 7)) { //处理如12.3、123.4等情况
		      try {
		    	  data = data.substring(0, 3); //取前面三位的有效数字
		      }
		      catch (Exception e) {
		    	  throw new MessageEncodeException(e);
		      }
		      switch (iDotPos + 1 - data.length()) { //计算幂指数
		        case 1: {
		          sMB = "1";
		          break;
		        }
		        case 2: {
		          sMB = "10";
		          break;
		        }
		        case 3: {
		          sMB = "100";
		          break;
		        }
		        case 4: {
		          sMB = "1000";
		          break;
		        }
		      }
		      sZSSjz = data;
		    }
		    if (iDotPos >= 7) { //小数点在很后面的情况，幂指数也是固定，数字也取前三位有效
		      try {
		    	  data = data.substring(0, 3);
		      }
		      catch (Exception ex) {
		        return "0000";//处理异常情况，返回0000
		      }
		      sMB = "10000";
		      sZSSjz = data;
		    }
		    if (sFH.equals("+")) { //正号和幂部，根据数字符号和幂指数生成头一个字节的值
		      if (sMB.equals("10000")) {
		        sMB = "0";
		      }
		      else if (sMB.equals("1000")) {
		        sMB = "2";
		      }
		      else if (sMB.equals("100")) {
		        sMB = "4";
		      }
		      else if (sMB.equals("10")) {
		        sMB = "6";
		      }
		      else if (sMB.equals("1")) {
		        sMB = "8";
		      }
		      else if (sMB.equals("0.1")) {
		        sMB = "A";
		      }
		      else if (sMB.equals("0.01")) {
		        sMB = "C";
		      }
		      else if (sMB.equals("0.001")) {
		        sMB = "E";
		      }
		    }
		    else { //负号和幂部
		      if (sMB.equals("10000")) {
		        sMB = "1";
		      }
		      else if (sMB.equals("1000")) {
		        sMB = "3";
		      }
		      else if (sMB.equals("100")) {
		        sMB = "5";
		      }
		      else if (sMB.equals("10")) {
		        sMB = "7";
		      }
		      else if (sMB.equals("1")) {
		        sMB = "9";
		      }
		      else if (sMB.equals("0.1")) {
		        sMB = "B";
		      }
		      else if (sMB.equals("0.01")) {
		        sMB = "D";
		      }
		      else if (sMB.equals("0.001")) {
		        sMB = "F";
		      }
		    }
		    rt= sMB + sZSSjz; //和取到的三位有效数字组合后就返回结果
	        rt=DataSwitch.ReverseStringByByte(rt);//按字节倒置
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		return rt;
	}
}
