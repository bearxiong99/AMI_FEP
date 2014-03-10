/**
 * ´´½¨embedded WebService Server
 */
package cn.hexing.ws;

import org.apache.log4j.Logger;
import org.mortbay.jetty.Connector;
import org.mortbay.jetty.Handler;
import org.mortbay.jetty.handler.DefaultHandler;
import org.mortbay.jetty.handler.HandlerCollection;
import org.mortbay.jetty.nio.SelectChannelConnector;
import org.mortbay.jetty.webapp.WebAppContext;

import cn.hexing.fk.common.spi.IModule;

/**
 *
 */
public class WebServiceServer implements IModule {
	private static final Logger log = Logger.getLogger(WebServiceServer.class);
	private int wsPort = 9002;
	
	org.mortbay.jetty.Server server = null;
	
	public String getModuleType() {
		return "webService";
	}

	public String getName() {
		return "webService";
	}

	public String getTxfs() {
		return "ws";
	}

	public boolean isActive() {
		return null != server && server.isRunning() ;
	}

	public boolean start() {
		server = new org.mortbay.jetty.Server();
        SelectChannelConnector connector = new SelectChannelConnector();
        connector.setPort(wsPort);
        server.setConnectors(new Connector[] {connector});

        WebAppContext webappcontext = new WebAppContext();
        webappcontext.setContextPath("/");

        webappcontext.setWar("webapp");

        HandlerCollection handlers = new HandlerCollection();
        handlers.setHandlers(new Handler[] {webappcontext, new DefaultHandler()});

        server.setHandler(handlers);
        
        boolean ret = false;
        try{
        	server.start();
        	ret = true;
        }catch(Exception e){
        	log.warn("sever start exception:"+e.getLocalizedMessage(),e);
        }
        log.info("WebService Server started£¬port=" + wsPort);
		return ret;
	}

	public void stop() {
		if( ! isActive() )
			return;
		try{
			server.stop();
		}catch(Exception e){
			log.error(e.getLocalizedMessage(),e);
		}
	}

	public long getLastReceiveTime() {
		return 0;
	}

	public long getLastSendTime() {
		return 0;
	}

	public int getMsgRecvPerMinute() {
		return 0;
	}

	public int getMsgSendPerMinute() {
		return 0;
	}

	public long getTotalRecvMessages() {
		return 0;
	}

	public long getTotalSendMessages() {
		return 0;
	}

	public String profile() {
		return "";
	}

	public final int getWsPort() {
		return wsPort;
	}

	public final void setWsPort(int wsPort) {
		this.wsPort = wsPort;
	}

}
