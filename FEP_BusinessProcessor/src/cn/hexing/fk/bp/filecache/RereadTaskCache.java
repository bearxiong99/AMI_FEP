/**
 * 漏点补招任务信息本地文件保存。
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
	/** 单例 */
    private static RereadTaskCache instance;
	//辅助属性
	private static String path;			//终端漏点数据缓存文件路径。
	private static int maxCount = 2;	//最多3个文件进行循环保存
	private static int maxSizeM = 100;	//每个文件最大100M。
	//缓存文件名称。如果多个，后面+".1"、".2"
	private static final String fileName = "rtu-rereadtask.dat";
	private static final Object lock = new Object();
	private long minInterval = 60*1000;		//最快读缓存间隔60秒。以免堵塞
	private long lastCacheRead = System.currentTimeMillis() - minInterval;
	private int maxSize = 30000;			//每次读取数量最大值		
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
				log.error("漏点补招信息写入文件异常:"+e.getLocalizedMessage(),e);
			}
		}
		if( log.isInfoEnabled()){
			long spend = System.currentTimeMillis() - time1;
			if( spend == 0 )
				spend = 1;
			long speed = (rereadtasks.size()*1000)/spend;
			log.info("写入缓存漏点补招信息数量="+rereadtasks.size()+",花费时间(ms)="+spend+",速度(/s)="+speed);
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
	 * 返回下一个有效的缓存文件路径名称。
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
				//当前文件已经满了，需要更改名称。
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
					if (pfix.length()>0){//第N个文件
						int appendInt = Integer.parseInt(pfix);
						if( appendInt>= allFiles.length )
							continue;
						allFiles[appendInt] = files[i];
					}	
					else//第1个文件
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
				log.debug("begin read cache file(开始加载缓存文件):"+filename);
			try{
				raf = new RandomAccessFile(filename,"rwd");
				String serial;
				int count =0;
				int maxCount = this.maxSize;
				
				while( null != (serial=raf.readLine()) ){
					String[] items = serial.split("\\|");
					if( items.length<5 ){
						log.warn("读缓存错误：读取无效内容："+serial);
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
					log.info("本次从缓存文件装载消息情况：file="+filename+",count="+count);
				
				//如果缓冲区数据太多，则需要把剩余消息移到文件头。
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
				sb.append("从缓存装载漏点信息到消息队列异常,filename=").append(filename);
				sb.append(",原因：").append(exp.getLocalizedMessage());
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
	 * 为了读缓存，获取一个缓存文件名称。文件名称格式： rtu-rereadtask.dat+i
	 * @return filename
	 */
	public String _findReadCacheFileName(){
		String fname0 = "rtu-rereadtask.dat";
		File f = new File(path);
		File [] list = f.listFiles();
		if( null == list ){
			log.warn(f.getPath()+":列表错误。null==list");
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
			log.debug(f.getPath()+":目录下无缓存文件。");
		return null;
	}
}
