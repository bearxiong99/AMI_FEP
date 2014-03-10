/**
 * JGroup system.propertity
 */
package cn.hexing.fk.fe.cluster;

import org.apache.log4j.Logger;

/**
 *
 */
public class JGroupSystemConfig {
	private static final Logger log = Logger.getLogger(JGroupSystemConfig.class);
	private static final JGroupSystemConfig instance = new JGroupSystemConfig();
	
	private JGroupSystemConfig(){
		setPreferIPv4Stack(true);
	}
	public static final JGroupSystemConfig getInstance(){ return instance; }
	
	private String bindAddr = "";
	private boolean preferIPv4Stack=true;
	
	public void init(){
		if( null!= bindAddr && bindAddr.length()>5 ){
			System.setProperty("jgroups.bind_addr", bindAddr);
			log.info("JGroupSystemConfig:: jgroups.bind_addr="+System.getProperty("jgroups.bind_addr"));
		}
		if( preferIPv4Stack )
			System.setProperty("java.net.preferIPv4Stack", "true");
		else
			System.setProperty("java.net.preferIPv4Stack", "false");
		log.info("JGroupSystemConfig:: java.net.preferIPv4Stack="+System.getProperty("java.net.preferIPv4Stack"));
	}
	
	public String getBindAddr() {
		return bindAddr;
	}
	public void setBindAddr(String bindAddr) {
		this.bindAddr = bindAddr;
	}
	public boolean isPreferIPv4Stack() {
		return preferIPv4Stack;
	}
	public void setPreferIPv4Stack(boolean preferIPv4Stack) {
		this.preferIPv4Stack = preferIPv4Stack;
	}
}
