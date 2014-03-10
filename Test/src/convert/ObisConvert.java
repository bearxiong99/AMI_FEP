package convert;

import java.util.ArrayList;

import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Oid;

public class ObisConvert {
	public static void main(String[] args) {
		String obis = convert("1-0:32.15.0.255");
		System.out.println(obis);
	}
	
	public static String convert(String obis){
		int[] intOids = ASN1Oid.parse(obis);
		if( null == intOids || intOids.length != 6 )
			throw new RuntimeException("Invalid OBIS:"+obis);
		byte[] ret = new byte[6];
		for(int i=0; i<ret.length; i++ )
			ret[i] = (byte) intOids[i];
		return HexDump.toHex(ret);
	}
	
	public static byte[] convetObis(String obis){
		int[] intOids = ASN1Oid.parse(obis);
		if( null == intOids || intOids.length != 6 )
			throw new RuntimeException("Invalid OBIS:"+obis);
		byte[] ret = new byte[6];
		for(int i=0; i<ret.length; i++ )
			ret[i] = (byte) intOids[i];
		return ret;
	}
	
	public static int[] parse(String strOID){
		ArrayList<Integer> arrList = new ArrayList<Integer>();
		int len = strOID.length();
		int val = 0;
		boolean computing = false;
		char c;
		for(int i=0; i<len; i++ ){
			c = strOID.charAt(i);
			if( c>='0' && c <='9' ){
				if( ! computing )
					computing = true;
				val = val*10 + (c-'0');
			}
			else{
				if( computing ){
					arrList.add(val);
					computing = false;
					val = 0;
				}
				continue;
			}
		}
		if( computing )
			arrList.add(val);
		int[] result = new int[arrList.size()];
		for(int i=0; i<arrList.size(); i++)
			result[i] = arrList.get(i);
		return result;
	}

}
