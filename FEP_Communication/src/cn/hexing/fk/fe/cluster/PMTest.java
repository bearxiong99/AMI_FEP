/**
 * 集群同步性能测试。
 * spring配置后才生效
 */
package cn.hexing.fk.fe.cluster;

import cn.hexing.fk.utils.HexDump;

import org.apache.log4j.Logger;

/**
 *
 */
public class PMTest extends Thread{
	private static final Logger log = Logger.getLogger(PMTest.class);
	private static final PMTest instance = new PMTest();
	private int batchSize = 10000;
	private int interval = 0;
	private int rtuaBase = 0x10100001;
	
	private PMTest(){ super("cluster-pm-test"); setDaemon(true); }
	public static final PMTest getInstance(){ return instance; }
	
	public void init(){
		start();
	}
	
	@Override
	public void run() {
		long time = System.currentTimeMillis();
		while(true){
			try{
				if( interval <=0 ){
					Thread.sleep( 1 * 1000);
					continue;
				}
				Thread.sleep(interval * 1000);
				for(int i=0; i<batchSize; i++ ){
					RtuWorkStateItem item = new RtuWorkStateItem();
					item.setFunc(RtuWorkStateItem.FUNC_HEART);
					item.setIoTime(time);
					item.setLen(13);
					item.setRtua(HexDump.toHex(rtuaBase+i));
					BatchSynchronizer.getInstance().addWorkState(item);
				}
				time += interval * 1000;
			}catch(Throwable e){
				log.error(e);
			}
		}
	}
	
	public int getBatchSize() {
		return batchSize;
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
	public int getInterval() {
		return interval;
	}
	public void setInterval(int interval) {
		this.interval = interval;
	}
	public void setRtuaBase(int rtuaBase) {
		this.rtuaBase = rtuaBase;
	}
}
