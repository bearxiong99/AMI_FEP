/**
 * ©�㲹��������Ϣ�����ļ����档
 */
package cn.hexing.fk.bp.filecache;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.sql.Date;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalRereadTaskResponse;
/**
 *
 */
public class RereadTaskCache {
	private static final Logger log = Logger.getLogger(RereadTaskCache.class);
	/** ���� */
    private static RereadTaskCache instance;
	//��������
	private static String path;			//�ն�©�����ݻ����ļ�·����
	private static int maxCount = 2;	//���3���ļ�����ѭ������
	private static int maxSizeM = 100;	//ÿ���ļ����100M��
	//�����ļ����ơ�������������+".1"��".2"
	private static final String fileName = "rtu-rereadtask.dat";
	private static final Object lock = new Object();
	private long minInterval = 60*1000;		//����������60�롣�������
	private long lastCacheRead = System.currentTimeMillis() - minInterval;
	private int maxSize = 30000;			//ÿ�ζ�ȡ�������ֵ		
	static {		
		String workDir = System.getProperty("user.dir");
		path = workDir + File.separator + "data";
		File f = new File(path);
		if( !f.exists() )
			f.mkdir();	
		instance = new RereadTaskCache();
		log.info("RereadTaskCache file path:"+path);
	}
	private RereadTaskCache(){
	}
	public static final RereadTaskCache getInstance(){
		return instance;
	}

	public void save2File(Collection<FaalRereadTaskResponse> rereadtasks){
		long time1 = System.currentTimeMillis();
		synchronized(lock){
			try{
				_save(rereadtasks);
			}catch(Exception e){
				log.error("©�㲹����Ϣд���ļ��쳣:"+e.getLocalizedMessage(),e);
			}
		}
		if( log.isInfoEnabled()){
			long spend = System.currentTimeMillis() - time1;
			if( spend == 0 )
				spend = 1;
			long speed = (rereadtasks.size()*1000)/spend;
			log.info("д�뻺��©�㲹����Ϣ����="+rereadtasks.size()+",����ʱ��(ms)="+spend+",�ٶ�(/s)="+speed);
		}
	}
	
	private static void _save(Collection<FaalRereadTaskResponse> rereadtasks) throws IOException{
		String nextPath = getNextFilePath();
		PrintWriter printer = new PrintWriter(new BufferedWriter(new FileWriter(nextPath,true),1024*1024));
		try{					
			for( FaalRereadTaskResponse rereadtask: rereadtasks ){
				printer.print(rereadtask.getLogicAddress()); printer.print('|');
				printer.print(rereadtask.getDeptCode()); printer.print('|');
				printer.print(rereadtask.getTaskNum()); printer.print('|');
				printer.print(rereadtask.getTaskTemplateID()); printer.print('|');
				printer.print(rereadtask.getSJSJ().getTime()); printer.print('|');
				printer.print(rereadtask.getRereadTag()); printer.println();
			}
			printer.flush();
		}finally{
			printer.close();
			printer=null;
		}
		
	}
	/**
	 * ������һ����Ч�Ļ����ļ�·�����ơ�
	 * @return
	 */
	private static String getNextFilePath(){
		try{
			String npath = path + File.separatorChar + fileName;
			int stdNameLen = fileName.length();
			File f = new File(npath);
			if( !f.exists() )
				return npath;
			if( f.length()>= (maxSizeM<<20 ) ){
				//��ǰ�ļ��Ѿ����ˣ���Ҫ�������ơ�
				f = new File(path);
				File[] allFiles = new File[maxCount+1];
				int maxIndex = -1;
				File[] files = f.listFiles();
				String pfix="";
				for(int i=0; i<files.length; i++){
					if( ! files[i].isFile() )
						continue;
					String fn =  files[i].getName();
					if( !fn.startsWith(fileName) )
						continue;
					if (fn.length()>fileName.length())
						pfix = fn.substring(stdNameLen);
					if (pfix.length()>0){//��N���ļ�
						int appendInt = Integer.parseInt(pfix);
						if( appendInt>= allFiles.length )
							continue;
						allFiles[appendInt] = files[i];
					}	
					else//��1���ļ�
						allFiles[0] = files[i];
					maxIndex = i;
				}
				for( int i=maxIndex; i>=0; i-- ){
					if( i >= maxCount ){
						allFiles[i].delete();
						continue;
					}
					npath = path + File.separatorChar + fileName+(i+1);
					allFiles[i].renameTo(new File(npath));
				}
			}
		}catch(Exception exp){
			log.error("get next file of rereadtask path error:"+exp.getLocalizedMessage());
		}
		return path + File.separatorChar + fileName;
	}
	
	public List<FaalRereadTaskResponse> loadFromFile(){
		List<FaalRereadTaskResponse> list = new LinkedList<FaalRereadTaskResponse>();
		long now = System.currentTimeMillis();
		if( now-this.lastCacheRead < this.minInterval )
			return list;
		this.lastCacheRead = System.currentTimeMillis();
		
		synchronized(lock){
			RandomAccessFile raf = null;
			String filename = _findReadCacheFileName();
			if( null == filename )
				return list;
			if( log.isDebugEnabled() )
				log.debug("begin read cache file(��ʼ���ػ����ļ�):"+filename);
			try{
				raf = new RandomAccessFile(filename,"rwd");
				String serial;
				int count =0;
				int maxCount = this.maxSize;
				
				while( null != (serial=raf.readLine()) ){
					String[] items = serial.split("\\|");
					if( items.length<5 ){
						log.warn("��������󣺶�ȡ��Ч���ݣ�"+serial);
						continue;
					}
					
					int i=0;
					FaalRereadTaskResponse rereadTask=new FaalRereadTaskResponse();
					rereadTask.setLogicAddress(items[i++]);
					rereadTask.setDeptCode(items[i++]);
					rereadTask.setTaskNum(items[i++]);
					rereadTask.setTaskTemplateID(items[i++]);
					rereadTask.setSJSJ(new Date(Long.parseLong(items[i++])));
					rereadTask.setRereadTag(Integer.parseInt(items[i++]));
					
					list.add(rereadTask);					
					count++;
					if( count>= maxCount )
						break;
				}
				if( count>0 && log.isInfoEnabled() )
					log.info("���δӻ����ļ�װ����Ϣ�����file="+filename+",count="+count);
				
				//�������������̫�࣬����Ҫ��ʣ����Ϣ�Ƶ��ļ�ͷ��
				long readPos = raf.getFilePointer();
				long writePos = 0;
				int n = 0;
				long remaining = raf.length() - readPos;
	
				byte buffer[] = new byte[512*1024];
				while( remaining>0 ){
					raf.seek(readPos);
					n = raf.read(buffer);
					if( n<=0 )
						break;
					raf.seek(writePos);
					raf.write(buffer,0,n);
					readPos += n;
					writePos += n;
					remaining -= n;
				}
				raf.setLength(writePos);
				raf.close();
				raf = null;
				return list;
			}catch(Exception exp){
				StringBuffer sb = new StringBuffer();
				sb.append("�ӻ���װ��©����Ϣ����Ϣ�����쳣,filename=").append(filename);
				sb.append(",ԭ��").append(exp.getLocalizedMessage());
				log.error(sb.toString(),exp);
				if( null != raf ){
					try{
						raf.close();
						raf = null;
					}
					catch(Exception e){}
				}
			}
		}
		return list;
	}
	/**
	 * Ϊ�˶����棬��ȡһ�������ļ����ơ��ļ����Ƹ�ʽ�� rtu-rereadtask.dat+i
	 * @return filename
	 */
	public String _findReadCacheFileName(){
		String fname0 = "rtu-rereadtask.dat";
		File f = new File(path);
		File [] list = f.listFiles();
		if( null == list ){
			log.warn(f.getPath()+":�б����null==list");
			return null;
		}
		
		File file;
		for(int j=0;j<list.length; j++){
			file = list[j];
			if( !file.isFile() || file.length()<=0 ) continue;
			
			String s = file.getName();
			if( s.indexOf(fname0) == 0 )
				return file.getPath();
		}
		if( log.isDebugEnabled() )
			log.debug(f.getPath()+":Ŀ¼���޻����ļ���");
		return null;
	}
}
