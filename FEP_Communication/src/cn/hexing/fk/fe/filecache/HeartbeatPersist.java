/**
 * �ն�������Ϣ���浽�����ļ���
 * ÿ��һ���ļ���ÿ���ն�ÿ�����288����������λ���棬��36�ֽ�)��
 * 30���նˣ��ļ�Ϊ12M��С��ѭ�����1���£���ռ�����Ϊ 360M��
 * ��Ÿ�ʽ��
 * ÿ���նˣ�rtua(4�ֽ�)+ 36 �ֽ�
 * ÿ���ļ����ƣ�heartbeat-i.data	i��ʾ�·ݵ�����.ÿ���賿����
 * 
 * �㷨��
 * 1���ն˱���Ķ�λ������ComRtu��heartSavePositionֱ�Ӷ�λ
 * 2����ʼ��ʱ����Ҫ�Ӵ��̼����ļ������¶�λ��Ϣ��
 * 	 ���heartSavePosition �� -1�����ʾ���նˣ���Ҫ�����µ�λ�á�
 * 3��36�ֽ�˳��: byte1 byte2 ... ÿ���ֽ����λ��ʾ�ȷ����������¼�(little ending)��
 * 4���������ĵ�ʱ�䣬���Ƶ�5����ʱ�䣬Ȼ���ڶ�λ���ֽں�λ�������ļ���
 */
package cn.hexing.fk.fe.filecache;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.HexDump;

/**
 *
 */
public class HeartbeatPersist {
	private static final Logger log = Logger.getLogger(HeartbeatPersist.class);
	private static HeartbeatPersist instance = null;
	private static final int STOPPED = 0;
	private static final int RUNNING = 1;
	private static final int STOPPING = 2;
	private static final int ONE_RTU_CACHE_LEN = 40; 
	private static String rootPath;
	private Map<Integer,HeartbeatInfo> mapRtuHeartbeats = new TreeMap<Integer,HeartbeatInfo>();
	
	static {
		//����Ƿ����dataĿ¼
		try{
			File file = new File("data");
			file.mkdirs();
			rootPath = file.getAbsolutePath() + File.separatorChar ;
			instance = new HeartbeatPersist();
		}catch(Exception exp){
			log.error(exp.getLocalizedMessage(),exp);
		}
	}
	//������
	private int batchSize = 1000;
	//�ڴ�ӳ��
	private String filePath = null;
	private final LinkedList<HeartbeatInfo> tobeSave = new LinkedList<HeartbeatInfo>();
	
	//ϵͳֹͣʱ����Ҫִ��������
	private int _state = 0;
	
	private HeartbeatPersist(){
		new HeartbeatHandleThread();
	}
	
	public static final HeartbeatPersist getInstance(){
		return instance;
	}

	public void handleHeartbeat(int rtua){
		handleHeartbeat(rtua,System.currentTimeMillis());
	}
	
	public void handleHeartbeat(int rtua, long time){
/*		HeartbeatInfo hi = this.mapRtuHeartbeats.get(rtua);
		if( null == hi ){
			hi = new HeartbeatInfo();
			hi.rtua = rtua;
			synchronized(instance){
				this.mapRtuHeartbeats.put(rtua, hi);
			}
		}
		hi.receiveHeartbeat(time);
		synchronized(instance){
			tobeSave.add(hi);
		}
		if( tobeSave.size()>= batchSize )
			this.processHeartInfoList();
*/	}
	
	private String todayFilePath(){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(System.currentTimeMillis());
		int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH);
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
		return filePath;
	}
	/**
	 * must be called after Rtu loaded from db or cache.
	 */
	public void initOnStartup(){
		log.info("Heart Beat Initialization initOnStartup");//log.info("������ʼ��initOnStartup");
		filePath = this.todayFilePath();
		File f = new File(filePath);
		if( !f.exists() || f.length()==0 )
		{
			log.warn("Heart Beat {"+filePath+"} file unexist ,End initialization");//log.warn("����"+filePath+"�ļ�������,��ʼ������");
			return;
		}
		synchronized(instance){
			RandomAccessFile raf = null;
			try{
				raf = new RandomAccessFile(f,"r");
				int count = (int) (raf.length() / ONE_RTU_CACHE_LEN);
				for( int i=0; i< count; i++ ){
					HeartbeatInfo hi = new HeartbeatInfo();
					hi.load(raf);
					this.mapRtuHeartbeats.put(hi.rtua, hi);
				}
			}catch(Exception e){
				log.error("heartbeat file exception:"+e.getLocalizedMessage(),e);
			}
			finally{
				if( null != raf ){
					try{
						raf.close();
						raf = null;
					}catch(Exception e){}
				}
			}
		}
		log.info("������ʼ��initOnStartup �������");
	}
	
	public void dispose(){
		if( _state != RUNNING )
			return;
		_state = STOPPING ;
		synchronized(tobeSave){
			tobeSave.notifyAll();
		}
		int cnt = 10;
		while( cnt-->0 && _state != STOPPED ){
			try{
				Thread.sleep(100);
			}catch(Exception e){}
		}
	}
	
	/**
	 * ÿ��00:00:10����һ�Ρ�
	 */
	public void initPerDay(){
		Calendar cl = Calendar.getInstance();
		cl.setTimeInMillis(System.currentTimeMillis());
		int dayOfMonth = cl.get(Calendar.DAY_OF_MONTH);
		
		log.info(dayOfMonth+"�ų�ʼ�������ļ�");
		
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
		synchronized(instance){
			RandomAccessFile raf = null;
			try{
				raf = new RandomAccessFile(filePath,"rw");
				log.info("����ÿ���ʼ��rtu���д�С��"+ this.mapRtuHeartbeats.size());
				Collection<HeartbeatInfo> all = this.mapRtuHeartbeats.values();
				raf.setLength(0);
				for(HeartbeatInfo hi: all ){
					hi.clear();
					hi.save(raf);
				}
			}catch(Exception e){
				log.error("heartbeat file exception:"+e.getLocalizedMessage(),e);
			}
			finally{
				if( null != raf ){
					try{
						raf.close();
						raf = null;
					}catch(Exception e){}
				}
			}
		}
	}
	
	public String queryHeartbeatInfo(int rtua){
		HeartbeatInfo hi = this.mapRtuHeartbeats.get(rtua);
		
		if( null == hi )
			return "no rtu";
		return hi.desc();
	}
	
	public String queryHeartbeatInfo(int rtua, int dayOfMonth){
		filePath = rootPath + "heartbeat-"+dayOfMonth+".data";
		File f = new File(filePath);
		if( !f.exists() || f.length() == 0 ){
			return "file not exist:"+filePath;
		}
		String result = "rtu not found in that day";
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(f,"r");
			int count = (int) (raf.length() / ONE_RTU_CACHE_LEN);
			HeartbeatInfo hi = new HeartbeatInfo();
			for( int i=0; i< count; i++ ){
				hi.pos = -1;
				hi.load(raf);
				if( hi.rtua == rtua )
					return hi.desc();
			}
		}catch(Exception e){
			result = "heartbeat file exception:"+e.getLocalizedMessage();
			log.error(result,e);
		}
		finally{
			if( null != raf ){
				try{
					raf.close();
					raf = null;
				}catch(Exception e){}
			}
		}
		return result;
	}
	
	/**
	 * dump the heart-beat info into text file
	 */
	public void dump(){
		PrintStream out = null;		
		try{
			String path = rootPath + "heart-beat.txt";
			out = new PrintStream(new FileOutputStream(path,false));
			Iterator<HeartbeatInfo> iter = this.mapRtuHeartbeats.values().iterator();
			while( iter.hasNext() ){
				out.println(iter.next());
			}
		}catch(Throwable e){
			log.error("save heart-beat exp:",e);
		}
		finally{
			if( null != out ){
				out.close();
			}
		}
	}
	
	private void processHeartInfoList(){
		if( tobeSave.size() == 0 )
			return;
		RandomAccessFile raf = null;		
		try{
			if( null == filePath )
				filePath = this.todayFilePath();
			raf = new RandomAccessFile(filePath,"rw");
			synchronized(instance){
				Iterator<HeartbeatInfo> iter = this.tobeSave.iterator();
				while( iter.hasNext() ){
					iter.next().save(raf);
				}
			}
		}catch(Throwable e){
			log.error("save heart-beat exp:",e);
		}
		finally{
			synchronized(instance){
				tobeSave.clear();
			}
			if( null != raf ){
				try{
					raf.close();
				}catch(Throwable e){}
			}
		}
	}
	
	class HeartbeatInfo{
		public int rtua = 0;
		public byte[] bits = new byte[36];
		public int pos = -1;
		public boolean dirty = true;
		
		public HeartbeatInfo(){
			clear();
		}
		
		public HeartbeatInfo(int rtua){
			this.rtua = rtua;
			clear();
		}
		
		public void clear(){
			dirty = true;
			pos = -1;
			for(int i=0; i<bits.length; i++ )
				bits[i] = 0;
		}
		
		public void receiveHeartbeat(long time){
			Calendar cl = Calendar.getInstance();
			cl.setTimeInMillis(time);
			int offset = (cl.get(Calendar.HOUR_OF_DAY)*60 + cl.get(Calendar.MINUTE))/5;
			int quotient = offset / 8;
			int delta = 7- (offset % 8);
			byte b = 1;
			if( delta>0 )
				b = (byte)(b << delta);
			bits[quotient] |= b;
			dirty = true;
		}
		
		public void save(RandomAccessFile raf) throws IOException{
			if( ! dirty )
				return;
			dirty = false;
			if( pos<0 ){
				//this RTU not in cache file
				pos = (int) raf.length();
				long len = pos + ONE_RTU_CACHE_LEN;
				raf.setLength(len);
			}
			raf.seek(pos);
			raf.writeInt(rtua);
			raf.write(bits);
		}
		
		public void load(RandomAccessFile raf) throws IOException{
			dirty = false;
			if( pos<0 )
				pos = (int)raf.getFilePointer();
			else
				raf.seek(pos);
			rtua = raf.readInt();
			raf.readFully(bits);
		}
		
		public String desc(){
			StringBuilder sb = new StringBuilder();
			try{
				//����ʱ����
				int minutes = 0;
				for(int i=0; i<bits.length; i++ ){
					byte b = bits[i];
					for( int j=7; j>=0; j-- ){
						if( j>0 )
							b = (byte)( b >>> j );
						if( (b & 0x01) != 0 ){
							minutes = (i*8 + (7-j)) * 5;
							sb.append(minutes/60).append(":").append(minutes % 60).append("; ");
						}
					}
				}
			}catch(Exception e){
				log.error(e.getLocalizedMessage(),e);
			}
			return sb.toString();
		}
		
		public String toString(){
			return HexDump.toHex(rtua) + " " + desc();
		}
	}
	
	class HeartbeatHandleThread extends Thread {
		public HeartbeatHandleThread(){
			super("HeartbeatHandle");
			this.setDaemon(true);
			start();
		}
		
		@Override
		public void run() {
			_state = RUNNING;
			long startTime = System.currentTimeMillis();
			while(true){
				try{
					synchronized(tobeSave){
						tobeSave.wait(1000*60);
						if( tobeSave.size() == 0 )
							continue;
					}

					long t1 = System.currentTimeMillis();
					processHeartInfoList();
					if( startTime !=0 && t1 - startTime > 1000* 180 ){
						dump();
						startTime = 0;
					}
					if( log.isDebugEnabled() ){
						long t2 = System.currentTimeMillis();
						log.debug("save heartbeat takes "+(t2-t1)+" milliseconds");
					}
					if( _state == STOPPING )
						break;
				}catch(Exception e){
					log.warn(e.getLocalizedMessage(),e);
				}
			}
			_state = STOPPED;
		}
	}

	public final void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}
}
