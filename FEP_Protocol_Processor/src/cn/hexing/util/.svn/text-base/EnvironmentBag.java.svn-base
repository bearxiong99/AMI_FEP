package cn.hexing.util;

import java.util.HashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 *@filename	EnvironmentBag.java
 *TODO		环境工具
 */
public class EnvironmentBag {
	private static final Log log=LogFactory.getLog(EnvironmentBag.class);
	private static EnvironmentBag _instance;
	private HashMap fronts;
	private long time;
	
	private EnvironmentBag(){
		fronts= new HashMap();
		time=System.currentTimeMillis();
	}
	
	public static EnvironmentBag getInstance(){
		if(_instance==null){
			synchronized(EnvironmentBag.class){
				_instance=new EnvironmentBag();
			}
		}
		return _instance;
	}
	
	public void onFrontConnected(String ip,Object dthread) {
		synchronized(fronts){
			if(fronts.containsKey(ip)){
				fronts.remove(ip);
			}						
			fronts.put(ip,dthread);			
		}		
	}
	
	public void onFrontClose(String ip) {
		synchronized(fronts){
			fronts.remove(ip);			
		}		
	}
	
	/**
	 * 前置机是否在线
	 * @param ip
	 * @return
	 */
	public boolean isAliveFront(String ip){
		boolean rt=false;
		try{
			if(fronts!=null){
				rt=fronts.containsKey(ip);
			}
		}catch(Exception e){
			//
		}
		return rt;
	}
	
	
}
