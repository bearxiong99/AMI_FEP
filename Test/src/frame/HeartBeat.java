package frame;

import cn.hexing.util.HexDump;

public class HeartBeat {
	public static void main(String[] args) {
		String heart = "DD1000000000303B30333930383530303138";
		byte[] buf = HexDump.toArray(heart);
		int begin = 2, end = buf.length-1;
		while( buf[begin] == 0  )
			begin++;
		while( buf[end] == 0 )
			end--;
		
		for(int i = begin ; i < end;i++){
			if(buf[i]<48 || buf[i]>57){
				buf[i] = 48;
			}
		}
		
		String meterId = new String(buf,begin,end-begin+1);
		
		System.out.println(meterId);
	}
}
