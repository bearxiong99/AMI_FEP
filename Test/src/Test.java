import cn.hexing.fk.utils.HexDump;




/**
 * Java�̣߳��̳߳�
 * 
 * @author ��С��
 */
public class Test {
	public static void main(String[] args) {
		System.out.println("\0");
		System.out.println(HexDump.toHex(36));
		
		new MyThread().start();
	}
}

class MyThread extends Thread {
	@Override
	public void run() {
		for(StackTraceElement str : this.getStackTrace()){
			System.out.println(str);
		}
		System.out.println(this.getStackTrace());
		System.out.println(Thread.currentThread().getName() + "����ִ�С�����");
	}
}
