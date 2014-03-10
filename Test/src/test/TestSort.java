package test;

import java.util.Collections;
import java.util.LinkedList;

import com.hx.dlms.aa.DlmsContext;

public class TestSort {

	
	
	public static void main(String[] args) {
		LinkedList<DlmsContext> sendingList = new LinkedList<DlmsContext>();
		for(int i=0;i<10000;i++){
			DlmsContext s = new DlmsContext();
			s.lastSendTime = System.currentTimeMillis();
			sendingList.add(s);
		}
		long t=System.currentTimeMillis();
		System.out.println(System.currentTimeMillis()-t);
		long t1 = System.currentTimeMillis();
		for(int i=0;i<sendingList.size();i++){
			System.out.println(t1-sendingList.get(i).lastSendTime);
		}
	}
}
