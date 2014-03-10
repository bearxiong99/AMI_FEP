/**
 * TelnetCommandDispatcher handles TELNET commands received by TelnetSession which supported by 
 * TelnetServerEventHandler class.
 */
package cn.hexing.fk.telnetserver.process;

import java.beans.PropertyDescriptor;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.ConvertUtils;
import org.apache.commons.beanutils.Converter;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.log4j.Logger;
import org.springframework.util.Assert;

import cn.hexing.fk.FasSystem;
import cn.hexing.fk.sockserver.AsyncSocketClient;
import cn.hexing.fk.telnetserver.TelnetCommandHandler;
import cn.hexing.fk.telnetserver.TelnetServer;
import cn.hexing.fk.telnetserver.TelnetSession;
import cn.hexing.fk.utils.ApplicationContextUtil;

/**
 *
 */
public class TelnetCommandDispatcher {
	private static final Logger log = Logger.getLogger(TelnetCommandDispatcher.class);
	private static final TelnetCommandDispatcher instance = new TelnetCommandDispatcher();
	private TelnetServer server = null;
	private Map<String,TelnetCommandHandler> mapHandler = new HashMap<String,TelnetCommandHandler>();
	
	//none configurable attributes. Used by this class locally.
	private TelnetCommandHandler defaultHandler = null;
	
	private TelnetCommandDispatcher(){}
	public static final TelnetCommandDispatcher getInstance(){ return instance; }
	
	public void dispatch(TelnetSession session,String cmdLine){
		try{
			Assert.notNull(server, "telnet server must be set.");
			if( null == cmdLine ){
				//session第一次登录时候，调用本方法。cmdLine = null。用于发送欢迎词。
				defaultHandler.execute(session, null);
				return;
			}
			cmdLine.trim();
			if( cmdLine.length()==0 )
				return;
			String[] args = cmdLine.split(" ");
			String verb = args[0];
			TelnetCommandHandler handler = mapHandler.get(verb);
			if( null == handler )
				handler = defaultHandler;
			handler.execute(session, args);
		}catch(Throwable e){
			log.error("handle telnet command exp. command="+cmdLine,e);
		}
	}
	
	public void setServer( TelnetServer telnetServer ){
		server = telnetServer;
		for(TelnetCommandHandler exec: mapHandler.values()){
			exec.setServer(server);
		}
		defaultHandler = createDefaultHandler();
		defaultHandler.setServer(server);
	}
	
	public void setHandlers(List<TelnetCommandHandler> list){
		for(TelnetCommandHandler exec: list){
			mapHandler.put(exec.getCommand(), exec);
			if( null != server )
				exec.setServer(server);
		}
	}
	
	private TelnetCommandHandler createDefaultHandler(){
		if( null == defaultHandler ){
			defaultHandler = new TelnetCommandHandler(){

				public void execute(TelnetSession session, String[] args) throws Exception{
					if( null == args ){
						printWelcome(session,false);
						return;
					}
					if( "dump".equalsIgnoreCase(args[0])){
						Options options = new Options( );
						options.addOption("?", "help", false, "Print 'dump' usage information");
						options.addOption("a", "all", false, "Dump all application profile information");
						options.addOption("f", "file", false, "Dump all application profile information");
						options.addOption("t", "thread", false, "Dump application threads information");
						options.addOption("s", "server", false, "Dump application socket servers information");
						options.addOption("h", "hook", false, "Dump application event hooks information");
						CommandLineParser parser = new BasicParser( );
						CommandLine cmd = parser.parse( options, args );
						FasSystem sys = FasSystem.getFasSystem();
						if( cmd.hasOption('t')){
							ThreadGroup tg = Thread.currentThread().getThreadGroup();
							while( tg.getParent() != null )
								tg = tg.getParent();
							int cnt = tg.activeCount();
							log.info("top thread-group:"+tg.getName()+",cnt="+cnt);
							cnt += cnt;
							Thread[] list = new Thread[cnt];
							
							PrintStream out = null;
							try{
								out = new PrintStream(new FileOutputStream("dumpThreads.txt",false));
								int limit = tg.enumerate(list);
								for(int i=0; i<limit; i++){
									StackTraceElement[] stacks = list[i].getStackTrace();
									out.println(list[i]);
									for( StackTraceElement se: stacks){
										out.print("    at ");
										out.println(se);
									}
									out.println();
								}
							}catch(Throwable exp){
								log.error(exp.getLocalizedMessage(),exp);
								session.send(">> Dump failed:"+exp.getLocalizedMessage());
							}
							finally{
								if( null != out )
									out.close();
							}
							session.send(">> Dump successfully.");
						}
						else if( cmd.hasOption('s')){
							session.send(sys.getModuleProfile());
						}
						else if( cmd.hasOption('h')){
							session.send(sys.getEventHookProfile());
						}
						else if( cmd.hasOption('a') || cmd.hasOption('f')){
							//保存到文件
							PrintStream out = null;
							try{
								out = new PrintStream(new FileOutputStream("dumpProfile.txt",false));
								out.println(sys.getProfile());
								out.flush();
							}catch(Throwable exp){
								log.error(exp.getLocalizedMessage(),exp);
								session.send(">> Dump failed:"+exp.getLocalizedMessage());
								return;
							}
							finally{
								if( null != out )
									out.close();
							}
							session.send(">> Dump successfully.");
						}
						else {
							//print help.
							final String USAGE = "Dump system resource into file(dump*.txt).";
							final String HEADER = "Options:";
							final String FOOTER = "Any question,please ask administrator.";

							HelpFormatter helpFormatter = new HelpFormatter( );
							ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
							PrintWriter out = new PrintWriter(bo);
							helpFormatter.printHelp(out, 80, USAGE,HEADER,options,4,8,FOOTER );
							out.flush();
							session.send(bo.toByteArray());
							session.send("");
							out.close();
						}
					}
					else if( "exit".equalsIgnoreCase(args[0])){
						AsyncSocketClient c = (AsyncSocketClient)session.getClient();
						server.forceCloseClient(c);
					}
					else if( "stopServer".equalsIgnoreCase(args[0]) ||
							"shutdown".equalsIgnoreCase(args[0])){
						Options options = new Options( );
						options.addOption("force", "force", false, "shutdown the server now");
						CommandLineParser parser = new BasicParser( );
						CommandLine cmd = parser.parse( options, args );
						if( cmd.hasOption("force")){
							session.send(">> Server is stopping...".getBytes());
							FasSystem sys = FasSystem.getFasSystem();
							sys.stopSystem();
						}
						else{
							session.send(">> Invalid option");
						}
					}
					else if( "set".equals(args[0])){
						Options options = new Options( );
						options.addOption("b", "bean", true, "bean's spring ID");
						options.addOption("p", "propertity", true, "bean's propertity");
						options.addOption("v", "value", true, "bean propertity's value");
						CommandLineParser parser = new BasicParser( );
						CommandLine cmd = parser.parse( options, args );
						if( !cmd.hasOption("b") || !cmd.hasOption("p") || !cmd.hasOption("v") ){
							session.send(">> Useage: set -b 'beanID' -p 'propertity' -v 'value'");
							return;
						}
						String beanId = cmd.getOptionValue('b');
						String beanProp = cmd.getOptionValue('p');
						String strPropValue = cmd.getOptionValue('v');
						Object newValue = strPropValue;
						String oldVal = "";
						try{
							Object bean = ApplicationContextUtil.getBean(beanId);
							oldVal = PropertyUtils.getProperty(bean, beanProp).toString();
							PropertyDescriptor propDesc = PropertyUtils.getPropertyDescriptor(bean, beanProp);
							if( null != propDesc ){
								Converter converter = ConvertUtils.lookup(propDesc.getPropertyType());
								newValue = converter.convert(propDesc.getPropertyType(), newValue);
							}
							PropertyUtils.setProperty(bean, beanProp, newValue);
						}catch(Throwable e){
							session.send("execute exception:"+e.getLocalizedMessage());
							log.error(e.getLocalizedMessage(),e);
						}
						session.send("set value successfully. oldValue="+oldVal+"\r\n");
					}
					else //if( "help".equalsIgnoreCase(args[0])){
					{
						printWelcome(session,true);
					}
				}

				public String getCommand() {
					return "defultHandler";
				}
				public void setCommand(String cmd) {
				}
				public void setServer(TelnetServer server) {
				}
				
				private void printWelcome(TelnetSession session, boolean unknownCmd ){
					final StringBuilder USAGE = new StringBuilder();
					if( unknownCmd )
						USAGE.append("Unknown command.\r\n");
					USAGE.append("Welcome to this console. Basic supported commands list below:\r\n");
					USAGE.append("  exit    ----Exit this session.\r\n");
					USAGE.append("  help    ----Get help information.\r\n");
					USAGE.append("  dump -thread [-server] [-hook] \r\n");
					HelpFormatter helpFormatter = new HelpFormatter( );
					ByteArrayOutputStream bo = new ByteArrayOutputStream(1024);
					PrintWriter out = new PrintWriter(bo);
					helpFormatter.printHelp(out,80,USAGE.toString(),"",new Options(),4,4,"" );
					out.flush();
					session.send(bo.toByteArray());
					session.send("");
					out.close();
				}
			};
		}
		return defaultHandler;
	}
}
