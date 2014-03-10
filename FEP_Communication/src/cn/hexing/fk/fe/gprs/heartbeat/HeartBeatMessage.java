package cn.hexing.fk.fe.gprs.heartbeat;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import cn.hexing.db.heartbeat.HeartBeat;
import cn.hexing.db.heartbeat.HeartBeatArray;
import cn.hexing.db.heartbeat.HeartBeatDao;
import cn.hexing.db.heartbeat.HeartBeatLog;

public class HeartBeatMessage {

	private int batchSize = 1000;//spring 配置 批量信息

	private int weekNum = 2;//spring 配置 保存几周数据
	
	private HeartBeatDao heartBeatDao;//spring 配置

	private static HeartBeatArray[] workList = new HeartBeatArray[35];

	private static List<HeartBeatArray> poolList = new Vector<HeartBeatArray>(20);

	private List<HeartBeatArray> batchSaveList = new Vector<HeartBeatArray>();

	private String poolListLock = "";
	private Worker worker;
	private boolean working;
	private boolean isNeedInit = false;
	private static int nInitWeekNo = 0;
	
	private boolean stop;

	/**
	 * 
	 * @param weekNO
	 */
	public void  initHeart(int weekNO) {
		nInitWeekNo = weekNO;
		long curTime = System.currentTimeMillis();
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(curTime);
		System.out.println("initial1："+System.currentTimeMillis());	
		try {
			List<HeartBeatLog> list = heartBeatDao.getLogResult(weekNO);
			if(list.size()>0){
				for(HeartBeatLog hbl : list){
					int id = hbl.getId();
					boolean isSuccess = hbl.getIssuccess();
					if(isSuccess == false) { //更新上次初始化失败的信息
						boolean b = heartBeatDao.doInit(nInitWeekNo,weekNum);
						if(b) {
							isNeedInit = false;
						}
						else {
							isNeedInit = true;
						}
						heartBeatDao.updateLogResult(b,curTime,id,nInitWeekNo);
					}else {
						return;
					}		
				}
			}else {
				boolean b = heartBeatDao.doInit(nInitWeekNo,weekNum);
				if(b) {
					isNeedInit = false;
				}
				else {
					isNeedInit = true;
				}
				heartBeatDao.insertLogResult(nInitWeekNo,curTime);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("initial2："+System.currentTimeMillis());
	}

	/*
	 * 保存心跳
	 * 0 ----- 315 ----- 630 ----- 945 ----- 1260 ----- 1440 
     * 0:00    5:15      10:30    15:45	     21:00      24:00
	 * 
	 * */
	public void putBeat(String rtua, long heartBeatTime,String deptCode) {
//		 System.out.println("get in putBeat\n");
		HeartBeat heartBeat = new HeartBeat();
		HeartBeatArray batchSaveArray = null;
//		通过算法得到心跳信息
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(heartBeatTime);

		int hour = c.get(Calendar.HOUR_OF_DAY);
		int minutes = c.get(Calendar.MINUTE);
		int day = c.get(Calendar.DAY_OF_WEEK);
		// int dayOfYear = c.get(Calendar.DAY_OF_YEAR);
		//c.get(Calendar.SECOND);
		int columNum = (day - 1) * 5 + (hour * 60 + minutes) / (63 * 5);

		long binaryTemp = ((hour * 60 + minutes) % (63 * 5));
		long binaryLocation = binaryTemp / 5;// 整除5，这个5是5分钟的5
		
		int weekOfYear = c.get(Calendar.WEEK_OF_YEAR);
		int weekFlag = weekOfYear % weekNum;
		heartBeat.setRtua(rtua);
		heartBeat.setWeekOfYear(weekOfYear);
		heartBeat.setValueOrigin(String.valueOf(heartBeatTime));
		heartBeat.setDeptCode(deptCode);
		long hearbeatvalue = (long)1;

		hearbeatvalue = hearbeatvalue << (62 - binaryLocation);
		
		heartBeat.setColumnIndex(columNum + 1);
		heartBeat.setWeekTag(weekFlag);
		heartBeat.setValue(hearbeatvalue);
//	
		synchronized (workList) {
			HeartBeatArray heartBeats = workList[columNum];
			if (heartBeats == null) {
				heartBeats = getArrayFromPool(columNum + 1);
				workList[columNum] = heartBeats;
				//System.out.println("get a array from pool");
			}// end if
			else if(heartBeats.isFull()){
				batchSaveArray = heartBeats;
				heartBeats = getArrayFromPool(columNum + 1);
				workList[columNum] = heartBeats;
				//System.out.println("hello.");
			}
			heartBeats.addHeartBeat(heartBeat);
		}// end sync
//		批次保存
		if (batchSaveArray != null)
			addBatchSaveArray(batchSaveArray);
	}

	private HeartBeatArray getArrayFromPool(int columnIndex) {
		synchronized(poolListLock){
			if(poolList.size() > 0){
				HeartBeatArray array = poolList.remove(0);
				array.setColumnIndex(columnIndex);
				return array;
			}//endif
		}//end sync	
		return new HeartBeatArray(columnIndex, batchSize);
	}

	private void freeMap(HeartBeatArray array) {
		if (poolList.size() < 20) {
			array.initArray();
			poolList.add(array);
		}//
	}

	private void addBatchSaveArray(HeartBeatArray heartBeats) {
		batchSaveList.add(heartBeats);
		if (worker == null) {
			stop = false;
			worker = new Worker();
			worker.start();
		}

		if (!working) {
			synchronized(worker){
				worker.notify();
			}
		}//end if
	}

	public void pleaseStop() {
		// .....
		stop = true;
		if (worker.isAlive()) {
			synchronized(worker){
				worker.notify();
			}
		}
	}

	private void doBatchSave(HeartBeatArray heartBeats)
			throws SQLException {
		int columnIndex = heartBeats.getColumnIndex();

		List<HeartBeat> orHeartBeats = new ArrayList<HeartBeat>();
			for (int i = 0; i < heartBeats.getSize(); i++) {
				orHeartBeats.add(heartBeats.getHeartBeat(i));
			}// end for

		List<HeartBeat> insertHeartBeats = new ArrayList<HeartBeat>();
		if (orHeartBeats.size() > 0) {
			int[] executeds = heartBeatDao.batchUpdate(orHeartBeats, columnIndex);
			int i = 0;
			for (HeartBeat heartBeat : orHeartBeats) {
				if (executeds[i++] == 0) {
					insertHeartBeats.add(heartBeat);
				}// end if
			}//
		}// end if

		if (insertHeartBeats.size() > 0) {
			// ....................
			//System.out.println("update failed and get in insert\n");
			Map<String, HeartBeat> map = new HashMap<String, HeartBeat>();
			for (HeartBeat temp : insertHeartBeats)
				map.put(temp.getKey(), temp);

			List<HeartBeat> tempList = new ArrayList<HeartBeat>();
			tempList.addAll(map.values());
			heartBeatDao.batchInsert(tempList, columnIndex);
			if (map.size() < batchSize)
				heartBeatDao.batchUpdate(insertHeartBeats, columnIndex);
		}// end if
	}

	// public static put
	public int getWeekNo(long time)
	{
		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(time);
		//System.out.println("initial1："+System.currentTimeMillis());
		return c.get(Calendar.WEEK_OF_YEAR);
	}
	class Worker extends Thread {
		public Worker() {
			super("Work Threads");
		}

		public void run() {
			while (!stop) {
				HeartBeatArray array = null;
				working = true;
				if (!batchSaveList.isEmpty()) {
					//System.out.println("save\n");
					array = batchSaveList.remove(0);
					try {
						doBatchSave(array);
					} catch (SQLException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					freeMap(array);
					
					if(batchSaveList.size() > 0)
						continue;
				}// end if
				
				if (isNeedInit){
					initHeart(nInitWeekNo);
				}

				long waitStart = System.currentTimeMillis();
				synchronized (this) {
					try {
						working = false;
						System.out.println("3:"+System.currentTimeMillis());
						this.wait(120*1000);
					} catch (InterruptedException e) {
					 // TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				long waited = System.currentTimeMillis() - waitStart;
				if(waited > 119*1000){
					synchronized(workList){
						for(int i=0;i<workList.length;i++){
							HeartBeatArray a = workList[i];
							if(a.getSize() > 0){
								workList[i] = null;
								addBatchSaveArray(a);	
								//System.out.println("hello.");
							}//end if
						}//end for
					}//end synch
				}//end if
			}// end while

			worker = null;
			stop = true;
		}
	}
	public void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	public void setHeartBeatDao(HeartBeatDao heartBeatDao) {
		this.heartBeatDao = heartBeatDao;
	}

	public void setWeekNum(int weekNum) {
		this.weekNum = weekNum;
	}

	
	
}
