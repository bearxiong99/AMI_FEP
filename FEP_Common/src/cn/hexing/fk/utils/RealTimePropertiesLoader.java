package cn.hexing.fk.utils;


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
 * @time 2013-2-26 ����11:22:20
 *
 * @info �����ļ�ʵʱ��ȡ��
 */
public class RealTimePropertiesLoader{
	
	private List<String> proFiles =new ArrayList<String>();
	
	
	public List<String> getProFiles() {
		return proFiles;
	}

	public void setProFiles(List<String> proFiles) {
		this.proFiles = proFiles;
	}

	private static Properties properties = new Properties();
	
	public void reloadProperties() {
		//ʵʱ��ȡ�����ļ�����
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
			}
		} catch (IOException e) {
		}
	}
}
