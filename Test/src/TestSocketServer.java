import cn.hexing.fk.FasSystem;
import cn.hexing.fk.sockserver.SyncTcpServer;


public class TestSocketServer {

	public static void main(String[] args) {
		SyncTcpServer stc = new SyncTcpServer();
		stc.setPort(1111);
		stc.start();
	}
}
