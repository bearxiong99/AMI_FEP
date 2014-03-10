package test;

import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.message.DlmsMessage;

public class TestSync {

	
	
	public static void main(String[] args) {
		Thread1 t1 = new Thread1();
		Thread2 t2 = new Thread2();
		DlmsMessage dm = new DlmsMessage();
		dm.setApdu(HexDump.toArray("C4010400090C07DC0C0A010E19124C800000"));
		t1.dm = dm;
		t2.dm = dm;
		t1.start();t2.start();
		
	}
	
	static class Thread1 extends Thread{

		public DlmsMessage dm = null;
		
		@Override
		public void run(){
			for(int i = 0 ;i<10000;i++){
				String str=dm.getRawPacketString();
				if(!str.equals("0001000100010012C4010400090C07DC0C0A010E19124C800000")){
					System.out.println("t1:"+str);
				}
			}
		}
		
		
	}
	
	static class Thread2 extends Thread{
		
		public DlmsMessage dm = null;
		
		@Override
		public void run(){
			for(int i = 0 ;i<10000;i++){
				String str=dm.getRawPacketString();	
				if(!str.equals("0001000100010012C4010400090C07DC0C0A010E19124C800000")){
					System.out.println("t2:"+str);
				}
			}
		}
	}
}

