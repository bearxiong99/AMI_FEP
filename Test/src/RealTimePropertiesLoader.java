


import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * 
 * @author gaoll
 *
 * @time 2013-2-26 上午11:22:20
 *
 * @info 配置文件实时读取器
 */
public class RealTimePropertiesLoader extends Thread{
	
	private List<String> proFiles =new ArrayList<String>();
	
	private long interval;
	
	public List<String> getProFiles() {
		return proFiles;
	}

	public void setProFiles(List<String> proFiles) {
		this.proFiles = proFiles;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	private static Properties properties = new Properties();
	
	public RealTimePropertiesLoader(){
	}
	
	@Override
	public void run() {
		while(true){
			//实时读取配置文件内容
			try {
				for(String file : proFiles){
				    InputStream in = RealTimePropertiesLoader.class .getClassLoader().getResourceAsStream(file);
				    properties.load(in);
				    in.close();
				}
				Enumeration<?> pnames = properties.propertyNames();
				while(pnames.hasMoreElements()){
					String propName = (String)pnames.nextElement();
					String propValue = properties.getProperty(propName);
					System.setProperty(propName, propValue);
					System.out.println("add sys propertie:("+propName+","+propValue+")");
				}
				Thread.sleep(interval);
			} catch (IOException e) {
			} catch (InterruptedException e) {
			}
		}

	}
	
	public static void main(String[] args) {
		RealTimePropertiesLoader rtpl = new RealTimePropertiesLoader();
		rtpl.setInterval(1000);
		List<String> s = new ArrayList<String>();
		s.add("config.properties");
		s.add("config1.properties");
		rtpl.setProFiles(s);
		rtpl.start();
	}
	
	
}
