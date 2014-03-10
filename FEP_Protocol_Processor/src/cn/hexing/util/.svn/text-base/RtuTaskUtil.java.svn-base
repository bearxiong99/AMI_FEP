package cn.hexing.util;

import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fk.model.TaskTemplate;


/**
 *@filename	RtuTaskUtil.java
 *TODO		终端任务工具
 */
public class RtuTaskUtil {
	private static final Log log=LogFactory.getLog(RtuTaskUtil.class);
	public static final String TIME_UNIT_MINUTE="02";
	public static final String TIME_UNIT_HOUR="03";
	public static final String TIME_UNIT_DAY="04";
	public static final String TIME_UNIT_MONTH="05";
	
	public static final long SECONDES_IN_MINUTE=60;
	public static final long SECONDES_IN_HOUR=3600;
	public static final long SECONDES_IN_DAY=86400;
	
	public static final int[] dayInMonth=new int[]{31,28,31,30,31,30,31,31,30,31,30,31};
	
	/**
	 * 最近的运行时刻---指上报时刻
	 * @param task		任务配置
	 * @param time		指定截止时刻
	 * @return			最近的运行时刻，不晚于time
	 */
	public static long lastRuntime(TaskTemplate task,long time){
		long rt=time;
		try{
			if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				rt=lastRuntimeM(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_HOUR)){
				rt=lastRuntimeH(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else if(task.getUploadIntervalUnit().equalsIgnoreCase(TIME_UNIT_DAY)){
				rt=lastRuntimeD(task.getUploadStartTimeUnit(),task.getUploadStartTime(),task.getUploadIntervalUnit(),task.getUploadInterval(),time);
			}else{
				//
			}			
		}catch(Exception e){
			log.error("取最近运行时刻",e);
		}
		return rt;
	}
	
	/**
	 * 最近的采样时刻
	 * @param task		任务配置
	 * @param time		指定截止时刻
	 * @return			最近的采样时刻，不晚于time
	 */
	public static long lastSampTime(TaskTemplate task,long time){
		long rt=time;
		try{
			if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				rt=lastRuntimeM(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_HOUR)){
				rt=lastRuntimeH(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else if(task.getSampleIntervalUnit().equalsIgnoreCase(TIME_UNIT_DAY)){
				rt=lastRuntimeD(task.getSampleStartTimeUnit(),task.getSampleStartTime(),task.getSampleIntervalUnit(),task.getSampleInterval(),time);
			}else{
				//
			}	
		}catch(Exception e){
			log.error("取最近采样时刻",e);
		}
		return rt; 
	}
	
	/**
	 * 一次自动上报包含的采样点数
	 * @param task
	 * @return			返回值是0时，有可能是包含点数不定，如月内天数
	 */
	public static int rCapacity(TaskTemplate task){
		int rt=0;
		try{
			if(task!=null){
				if(task.getSampleInterval()>0 && task.getUploadInterval()>0){
					long sinterval=getUnitSecondes(task.getSampleIntervalUnit())*((long)task.getSampleInterval());
					long rinterval=getUnitSecondes(task.getUploadIntervalUnit())*((long)task.getUploadInterval());
					if(sinterval==0){	//采样单位是月
						if(rinterval==0){
							rt=task.getUploadInterval()/task.getSampleInterval();
						}
					}else{
						long frq=task.getFrequence();
						if(frq<=0){
							frq=1;
						}
						rt=(int)(rinterval/(sinterval*frq));
					}
				}
			}
		}catch(Exception e){
			log.error("取上报包含数据点数",e);
		}
		return rt;
	}
	
	/**
	 * 获取本次上报数据开始的时间
	 * @param task		任务配置
	 * @param time		上报时刻
	 * @return
	 */
	public static long getStartTimeOfTask(TaskTemplate task,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//修整
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			if(task.getUploadStartTimeUnit().equalsIgnoreCase(TIME_UNIT_MINUTE)){
				ctime.add(Calendar.MINUTE, -task.getUploadStartTime());
			}else{
				if(task.getUploadStartTimeUnit().equalsIgnoreCase(task.getUploadIntervalUnit())){
					int delt=(int)getUnitSecondes(task.getUploadStartTimeUnit())*task.getUploadStartTime();
					ctime.add(Calendar.SECOND, -delt);
				}
			}
			int spu=(int)getUnitSecondes(task.getUploadIntervalUnit())*task.getUploadInterval();
			ctime.add(Calendar.SECOND, spu);
			rt=ctime.getTimeInMillis();
		}catch(Exception e){
			log.error("",e);
		}
		return rt;
	}
	
	public static long getUnitSecondes(String unit){
		long rt=0;
		if(unit!=null){
			try{
				int u=Integer.parseInt(unit);
				switch(u){
					case 2:
						rt=SECONDES_IN_MINUTE;
						break;
					case 3:
						rt=SECONDES_IN_HOUR;
						break;
					case 4:
						rt=SECONDES_IN_DAY;
						break;
				}
			}catch(Exception e){
				log.error("单位时间包含秒数",e);
			}
		}
		return rt;
	}
	
	/**
	 * 最近运行时间----间隔类型是分
	 * @param bUU		运行基准单位
	 * @param bNN		运行基准
	 * @param iUU		运行间隔单位
	 * @param iNN		运行间隔
	 * @param time		截止时刻
	 * @return			最近运行时刻
	 */
	private static long lastRuntimeM(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//修整
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//截止时刻距零点的分钟数
			int minutes=ctime.get(Calendar.HOUR_OF_DAY)*60+ctime.get(Calendar.MINUTE);
			if(minutes<bNN){//指定时刻当天未运行一次
				ctime.set(Calendar.MINUTE, bNN);
				ctime.add(Calendar.MINUTE, -iNN);
				rt=ctime.getTimeInMillis();
			}else{
				minutes-=bNN;
				long mtime=minutes % iNN;
				if(mtime>0){
					ctime.add(Calendar.MINUTE, -(int)mtime);
				}else{
					//截止时刻就是运行时刻
					
				}
				rt=ctime.getTimeInMillis();
			}
		}catch(Exception e){
			log.error("获取最近运行时刻-分",e);
		}
		return rt;
	}
	
	/**
	 * 最近运行时间----间隔类型是时
	 * @param bUU
	 * @param bNN
	 * @param iUU
	 * @param iNN
	 * @param time
	 * @return
	 */
	private static long lastRuntimeH(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//修整
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//截止时刻距零点的距离，单位秒
			long dtime=((long)ctime.get(Calendar.HOUR_OF_DAY))*SECONDES_IN_HOUR+((long)ctime.get(Calendar.MINUTE))*SECONDES_IN_MINUTE;
			//每天的首次允许时刻距零点距离，单位秒
			long ftime=getUnitSecondes(bUU)*(long)bNN;
			//运行间隔，单位秒
			long interval=getUnitSecondes(iUU)*(long)iNN;
			if(ftime>dtime){//本日未运行过
				if(bUU.equalsIgnoreCase(TIME_UNIT_MINUTE)){
					ctime.set(Calendar.MINUTE, bNN);
					ctime.set(Calendar.HOUR_OF_DAY, 0);
					ctime.add(Calendar.SECOND, (int)interval);	//上一运行时刻
					rt=ctime.getTimeInMillis();
				}else if(bUU.equalsIgnoreCase(TIME_UNIT_HOUR)){
					ctime.set(Calendar.MINUTE, 0);
					ctime.set(Calendar.HOUR_OF_DAY, bNN);
					ctime.add(Calendar.SECOND, (int)interval);	//上一运行时刻
					rt=ctime.getTimeInMillis();
				}else{
					//错误的时间配置
					log.warn("错误的时间配置 bUU--"+bUU+" iUU--"+iUU);
				}				
			}else{
				long delt=(dtime-ftime)%interval;
				ctime.add(Calendar.SECOND, (int)delt);
				rt=ctime.getTimeInMillis();
			}
		}catch(Exception e){
			log.error("获取最近运行时刻-时",e);
		}
		return rt;
	}
	
	/**
	 * 最近运行时间----间隔类型是天
	 * @param bUU
	 * @param bNN
	 * @param iUU
	 * @param iNN
	 * @param time
	 * @return
	 */
	private static long lastRuntimeD(String bUU,int bNN,String iUU,int iNN,long time){
		long rt=time;
		try{
			Calendar ctime=Calendar.getInstance();
			ctime.setTimeInMillis(time);
			//修整
			ctime.set(Calendar.MINUTE, 0);
			ctime.set(Calendar.SECOND, 0);
			ctime.set(Calendar.MILLISECOND, 0);
			//起始时刻
			if(bUU.equalsIgnoreCase(TIME_UNIT_DAY)){
				//默认起始时刻为当月bNN日，零时零分
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				if(bNN>cday){
					//当月未运行过
					ctime.set(Calendar.DAY_OF_MONTH, bNN);
					ctime.add(Calendar.DAY_OF_MONTH, -iNN);
					rt=ctime.getTimeInMillis();
				}else{
					cday-=(cday-bNN)%iNN;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					rt=ctime.getTimeInMillis();
				}
			}else if(bUU.equalsIgnoreCase(TIME_UNIT_HOUR)){
				//默认起始时刻为当月1日，bNN时零分
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				int delt=(cday-1)%iNN;
				if(delt<=0){
					//截止日期是运行日
					if(ctime.get(Calendar.HOUR_OF_DAY)<bNN){
						//当日未到运行时刻
						ctime.set(Calendar.HOUR_OF_DAY, bNN);
						ctime.add(Calendar.DAY_OF_MONTH, -iNN);
						rt=ctime.getTimeInMillis();
					}else{
						//已过运行时刻
						ctime.set(Calendar.HOUR_OF_DAY, bNN);
						rt=ctime.getTimeInMillis();
					}
				}else{
					cday-=delt;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					ctime.set(Calendar.HOUR_OF_DAY, bNN);
					rt=ctime.getTimeInMillis();
				}
			}else if(bUU.equalsIgnoreCase(TIME_UNIT_MINUTE)){
				//默认起始时刻为当月1日，零时bNN分
				int cday=ctime.get(Calendar.DAY_OF_MONTH);
				int delt=(cday-1)%iNN;
				if(delt<=0){
					//截止日期是运行日
					if(ctime.get(Calendar.HOUR_OF_DAY)==0 && ctime.get(Calendar.MINUTE)<bNN){
						//当日未到运行时刻
						ctime.set(Calendar.MINUTE, bNN);
						ctime.add(Calendar.DAY_OF_MONTH, -iNN);
						rt=ctime.getTimeInMillis();
					}else{
						//已过运行时刻
						ctime.set(Calendar.HOUR_OF_DAY, 0);
						ctime.set(Calendar.MINUTE, bNN);
						rt=ctime.getTimeInMillis();
					}
				}else{
					cday-=delt;
					ctime.set(Calendar.DAY_OF_MONTH, cday);
					ctime.set(Calendar.HOUR_OF_DAY, 0);
					ctime.set(Calendar.MINUTE, bNN);
					rt=ctime.getTimeInMillis();
				}
			}else{
				//错误的时间配置
				log.warn("错误的时间配置 bUU--"+bUU+" iUU--"+iUU);
			}			
		}catch(Exception e){
			log.error("获取最近运行时刻-天",e);
		}
		return rt;
	}
}
