package cn.hexing.fk.telnetserver;

import java.util.List;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.sockserver.TcpSocketServer;
import cn.hexing.fk.sockserver.io.SimpleIoHandler;
import cn.hexing.fk.telnetserver.process.TelnetCommandDispatcher;

public class TelnetServer extends TcpSocketServer {
	private static TelnetServer instance = null;
	private TelnetServerEventHandler eventHandler = new TelnetServerEventHandler();
	private TelnetCommandDispatcher dispatcher = null;
	
	public static final TelnetServer getInstance() {
		if( null == instance )
			instance = new TelnetServer();
		return instance;
	}
	private TelnetServer(){
		this(2323);
	}
	
	private TelnetServer(int port){
		super.port = port;
		super.name = "telnetServer";
		super.bufLength = 1024;
		super.messageCreator = new MessageBytesCreator();
		super.ioHandler = new SimpleIoHandler();
		eventHandler.setSource(this);
		dispatcher = TelnetCommandDispatcher.getInstance();
		dispatcher.setServer(this);
		FasSystem.getFasSystem().addModule(this);
	}

	@Override
	public void stop() {
		super.stop();
		eventHandler.stop();
	}

	@Override
	public boolean start() {
		eventHandler.start();
		return super.start();
	}
	
	public void setHandlers(List<TelnetCommandHandler> list){
		dispatcher.setHandlers(list);
	}
}
