/**
 * 
 */
package cn.hexing.fk.monitor;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.apache.log4j.Logger;

import cn.hexing.fk.FasSystem;

/**
 * monite the CPU usage of windows and linux.
 */
public class OsSystemMonitor {
	private static final Logger log = Logger.getLogger(OsSystemMonitor.class);

	//Linux CPU calculate
	private long lastUsed = 0;
	private long lastTotal = 0;
	
	//CPU采样相关属性
	private int sampleCount = 60*10;
	private final Object lock = new Object();
	private long	maxMemory = 0;
	private boolean autoMonitor = true;
	private Vector<MonitorDataItem> queue = new Vector<MonitorDataItem>();
	
	//内部属性
	private boolean isWindows = false;
	
	private Timer timer = null;

	private static final OsSystemMonitor cpuMonitor = new OsSystemMonitor();

	private OsSystemMonitor(){
		maxMemory = Runtime.getRuntime().maxMemory()>>>20;
		String osName = System.getProperty("os.name");
		if (osName.indexOf("Windows") > -1) {
			isWindows = true;
		}
	}
	
	public static final OsSystemMonitor getInstance(){
		return cpuMonitor;
	}
	
	public MonitorDataItem[] getAllItems(){
		return queue.toArray(new MonitorDataItem[queue.size()]);
	}
	
	public MonitorDataItem getCurrentData(){
		MonitorDataItem item = new MonitorDataItem();
		item.cpuUsage = getCPUUsage();
		item.totalMemory = Runtime.getRuntime().totalMemory()>>>20;
		item.freeMemory = Runtime.getRuntime().freeMemory() >>> 20;
		item.maxMemory = Runtime.getRuntime().maxMemory() >>> 20;
		item.freeDisk = getFreeDisk();
		return item;
	}
	
	public void initialize(){
		if( autoMonitor ){
			timer = new Timer(true);
			timer.scheduleAtFixedRate(new TimerTask(){
				@Override
				public void run() {
					synchronized(lock){
						MonitorDataItem item = null;
						if( queue.size() < sampleCount ){
							item = new MonitorDataItem();
						}
						else{
							item = queue.remove(0);
						}
						queue.add(item);
						item.cpuUsage = getCPUUsage();
						item.totalMemory = Runtime.getRuntime().totalMemory()>>>20;
						item.freeMemory = Runtime.getRuntime().freeMemory() >>> 20;
						item.maxMemory = Runtime.getRuntime().maxMemory() >>> 20;
						item.freeDisk = getFreeDisk();
						updateOsProfile(item);
						if( log.isDebugEnabled() )
							log.debug(item);
					}
				}
			}, 1000, 60*1000);
		}
	}
	
	private void updateOsProfile(MonitorDataItem item){
		StringBuffer sb = new StringBuffer(1024);
		sb.append("<os-profile>\r\n");
		sb.append("      <cpu>").append(item.cpuUsage).append("</cpu>\r\n");
		sb.append("      <totalMemory>").append(item.totalMemory).append("</totalMemory>\r\n");
		sb.append("      <maxMemory>").append(item.maxMemory).append("</maxMemory>\r\n");
		sb.append("      <freeMemory>").append(item.freeMemory).append("</freeMemory>\r\n");
		sb.append("      <freeDisk>").append(item.freeDisk).append("</freeDisk>\r\n");
		sb.append("    </os-profile>\r\n");
		FasSystem.getFasSystem().setOsProfile(sb.toString());
	}
	
	private String[] execute(String[] commands) {
		String[] strs = null;
		File scriptFile = null;
		try {
			List<String> cmdList = new ArrayList<String>();
			if ( isWindows ) {
				scriptFile = new File("monitor.vbs");
				cmdList.add("CMD.EXE");
				cmdList.add("/C");
				cmdList.add("CSCRIPT.EXE");
				cmdList.add("//NoLogo");
			} else {
				scriptFile = new File("monitor.sh");
				cmdList.add("/bin/bash");
			}
			if( !scriptFile.exists() || scriptFile.length() == 0 ){
				PrintWriter writer = new PrintWriter(scriptFile);
				for (int i = 0; i < commands.length; i++) {
					writer.println(commands[i]);
				}
				writer.flush();
				writer.close();
			}
			String fileName = scriptFile.getCanonicalPath();
			cmdList.add(fileName);

			ProcessBuilder pb = new ProcessBuilder(cmdList);
			Process p = pb.start();
			p.waitFor();

			String line = null;
			BufferedReader stdout = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			List<String> stdoutList = new ArrayList<String>();
			while ((line = stdout.readLine()) != null) {
				stdoutList.add(line);
			}
			if( log.isDebugEnabled() ){
				log.debug("CPUMonitor stdout:"+stdoutList);
			}

			BufferedReader stderr = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			List<String> stderrList = new ArrayList<String>();
			while ((line = stderr.readLine()) != null) {
				stderrList.add(line);
			}
			if( stderrList.size()>0 ){
				log.warn("CPUMonitor stderr="+stderrList);
			}
			strs = stdoutList.toArray(new String[0]);
		} catch (Exception e) {
			e.printStackTrace();
		}
/*		finally {
			if (scriptFile != null)
				scriptFile.delete();
		}
*/		return strs;
	}

	private double parseResult(String[] strs) {
		double value = 0;
		if ( isWindows ) {
			String strValue = strs[0];
			try{
				value = Double.parseDouble(strValue);
			}catch(Exception e){
				log.debug("parse double error.");
			}
		} else {
			String strValue = strs[0];
			String[] values = strValue.split(" ");
			Vector<String> vv = new Vector<String>(10);
			for( String v: values ){
				if( v.length() != 0 )
					vv.add(v);
			}
			values = vv.toArray(new String[vv.size()]);
			if (values.length == 2) {
				long used = Long.parseLong(values[0]);
				long total = Long.parseLong(values[1]);

				if (lastUsed > 0 && lastTotal > 0) {
					long deltaUsed = used - lastUsed;
					long deltaTotal = total - lastTotal;
					if (deltaTotal > 0) {
						value = ( (long)( ((deltaUsed * 100) / deltaTotal) * 10) ) / 10;
					}
				}
				lastUsed = used;
				lastTotal = total;
			}
			else if(values.length >= 8 ){
				int index = 1;
				long _user = Long.parseLong(values[index++]);
				long _nice = Long.parseLong(values[index++]);
				long _system = Long.parseLong(values[index++]);
				long _idle = Long.parseLong(values[index++]);
				long _iowait = Long.parseLong(values[index++]);
				long _irq = Long.parseLong(values[index++]);
				long _softirq = Long.parseLong(values[index++]);
				long used = _user + _nice + _system + _iowait + _irq + _softirq ;
				long total = used + _idle ;
				if (lastUsed > 0 && lastTotal > 0) {
					long deltaUsed = used - lastUsed;
					long deltaTotal = total - lastTotal;
					if (deltaTotal > 0) {
						value = ( (long)( ((deltaUsed * 100) / deltaTotal) * 10) ) / 10;
					}
				}
				lastUsed = used;
				lastTotal = total;
			}
		}
		return value;
	}

	private double getCPUUsage() {
		String[] scriptCmds = null;
		if ( isWindows ) {
			scriptCmds = new String[] {
					"strComputer = \".\"",
					"Set objWMIService = GetObject(\"winmgmts:\" _",
					" & \"{impersonationLevel=impersonate}!\\\\\" & strComputer & \"\\root\\cimv2\")",
					"Set colItems = objWMIService.ExecQuery(\"Select * from Win32_Processor \",,48)",
					"load = 0", "n = 0", "For Each objItem in colItems",
					" load = load + objItem.LoadPercentage", " n = n + 1",
					"Next", "Wscript.Echo (load/n)" };
		} else {
			scriptCmds = new String[] {
					"cat /proc/stat | head -n 1"
/*					"user=`cat /proc/stat | head -n 1 | awk '{print $2}'`",
					"nice=`cat /proc/stat | head -n 1 | awk '{print $3}'`",
					"system=`cat /proc/stat | head -n 1 | awk '{print $4}'`",
					"idle=`cat /proc/stat | head -n 1 | awk '{print $5}'`",
					"iowait=`cat /proc/stat | head -n 1 | awk '{print $6}'`",
					"irq=`cat /proc/stat | head -n 1 | awk '{print $7}'`",
					"softirq=`cat /proc/stat | head -n 1 | awk '{print $8}'`",
					"let used=$user+$nice+$system+$iowait+$irq+$softirq",
					"let total=$used+$idle", "echo \"$used $total\"" 
*/					};
		}
		return parseResult(execute(scriptCmds));
	}
	
	//取当前目录下可用磁盘空间。单位M。
	private long getFreeDisk(){
		if( isWindows ){
			try{
				List<String> cmdList = new ArrayList<String>();
				cmdList.add("CMD.EXE");
				cmdList.add("/C");
				cmdList.add("dir");
				cmdList.add(System.getProperty("user.dir"));
				
				ProcessBuilder pb = new ProcessBuilder(cmdList);
				Process p = pb.start();
				//Process p = Runtime.getRuntime().exec(cmds);
				BufferedReader stdout = new BufferedReader(new InputStreamReader(p.getInputStream()));
				String lastLine = null,str = null;
				while( null != (str=stdout.readLine()) ){
					lastLine = str;
				}
				if( null != lastLine ){
					//格式：              12 个目录  4,314,185,728 可用字节
					lastLine = lastLine.trim();
					int index = lastLine.lastIndexOf(",");
					if( index<=0 )
						return 0;
					int pos0 = 0;
					while( --index >= 0 ){
						char c = lastLine.charAt(index);
						if( Character.isDigit(c) || c == ',' )
							continue;
						pos0 = index+1;
						break;
					}
					StringBuffer sb = new StringBuffer();
					while( pos0 <lastLine.length() ){
						char c = lastLine.charAt(pos0++);
						if( Character.isDigit(c))
							sb.append(c);
						else if( c == ',' )
							continue;
						else
							break;
					}
					long freeBytes = Long.parseLong(sb.toString());
					return freeBytes >>> 20;
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		else{
			return 0;
		}
		return 0;
	}


	public final void setSampleCount(int sampleCount) {
		this.sampleCount = sampleCount;
	}

	public final long getMaxMemory() {
		return maxMemory;
	}

	public final void setAutoMonitor(boolean autoMonitor) {
		this.autoMonitor = autoMonitor;
	}

}
