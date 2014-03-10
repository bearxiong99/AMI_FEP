/**
 * 下载文件
 */
package cn.hexing.fk.monitor.biz;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import cn.hexing.fk.monitor.exception.MonitorHandleException;

/**
 *	对于配置文件，一般一次性下载完毕。对于日志文件，需要分割下载。
 */
public class HandleFile {
	public static final int MAX_BLOCK = 1024*1024;
	private static final HandleFile handleFile = new HandleFile();
	public static final HandleFile getHandleFile(){
		return handleFile;
	}

	/**
	 * 读取文件。每次最大读取1M。
	 * @param inputBody：命令下行的报文体。下行格式path+0+long  
	 * path字符串，以0结尾
	 * @return：
	 */
	public ByteBuffer getFile(ByteBuffer inputBody){
		String path="";
		byte[] btPath = null;
		long position = -1;
		int index = 0;
		for(index=0; index<inputBody.limit(); index++ ){
			if( 0 == inputBody.get(index) ){
				btPath = new byte[index];
				inputBody.get(btPath);
				path = new String(btPath);
				inputBody.get();		//skip 0
				position = inputBody.getLong();
				inputBody.rewind();
				break;
			}
		}
		if( -1 == position || null == btPath )
			throw new MonitorHandleException("监控管理：获取文件异常，输入非法。");
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"r");
			int len = (int)(raf.length()-position);
			if( len<=0 ){
				raf.close();
				return inputBody;
			}
			int toRead = MAX_BLOCK;
			toRead = Math.min(len, toRead);
			ByteBuffer body = ByteBuffer.allocate(inputBody.remaining()+toRead);
			raf.seek(position);
			body.put(inputBody);
			inputBody.rewind();
			raf.read(body.array(), inputBody.remaining(), toRead);
			body.position(0);
			raf.close();
			return body;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
	/**
	 * 写文件。
	 * @param inputBody：命令下行的报文体。下行格式path+0+long(offset)+数据流
	 * path字符串，以0结尾
	 * @return：
	 */
	public ByteBuffer putFile(ByteBuffer inputBody){
		String path="";
		byte[] btPath = null;
		long position = -1;
		int index = 0;
		for(index=0; index<inputBody.limit(); index++ ){
			if( 0 == inputBody.get(index) ){
				btPath = new byte[index];
				inputBody.get(btPath);
				path = new String(btPath);
				inputBody.get();		//skip 0
				position = inputBody.getLong();
				break;
			}
		}
		int offset = inputBody.position();
		int dataLen = inputBody.remaining();
		if( -1 == position || null == btPath)
			throw new MonitorHandleException("监控管理：写文件命令异常，输入非法。");
		
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"rwd");
			if( position> raf.length() ){
				raf.close();
				throw new MonitorHandleException("监控管理：写文件命令异常，写文件起始位置>文件实际长度。");
			}
			
			raf.setLength(position);
			raf.seek(position);
			raf.write(inputBody.array(), offset, dataLen);
			raf.close();
			position += dataLen;
			
			inputBody.clear();
			inputBody.put(btPath);	inputBody.put((byte)0);
			inputBody.putLong(position);
			inputBody.flip();
			return inputBody;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
}
