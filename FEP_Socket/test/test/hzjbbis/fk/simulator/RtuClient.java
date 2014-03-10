package test.hzjbbis.fk.simulator;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.clientmod.ClientModule;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.utils.HexDump;

public class RtuClient {
	private static final Logger log = Logger.getLogger(RtuClient.class);

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String path[] = new String[] { 
				"classpath*:applicationContext-common.xml",
				"classpath*:applicationContext-socket.xml",
				"classpath*:applicationContext-socket-client.xml" };
		ApplicationContext context = new ClassPathXmlApplicationContext(path);
		String msg = "689101000080046882310004051024150001020F353829018889170074056200742538009916110041363600047406005217030052271500991611000D16";
		ClientModule client = (ClientModule)context.getBean("test.rtu.client");
		client.start();
		ByteBuffer buf = HexDump.toByteBuffer(msg);
		MessageZj msgzj = new MessageZj();
		try{
			Thread.sleep(200);
			msgzj.read(buf);
			for(int i=0; i<1000000; i++ ){
				client.sendMessage(msgzj);
//				Thread.sleep(1);
				if( i % 1000 == 0 )
					log.info("i="+i);
			}
			log.info("complete");
			client.stop();
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}

}
