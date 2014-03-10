/**
 * 监控模块客户端：文件传输处理。
 */
package cn.hexing.fk.monitor.client.biz;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;

import cn.hexing.fk.monitor.biz.HandleFile;
import cn.hexing.fk.monitor.exception.MonitorHandleException;
import cn.hexing.fk.utils.PathUtil;

/**
 *	对于配置文件，一般一次性下载完毕。对于日志文件，需要分割下载。
 */
public class ClientHandleFile {
	private static final ClientHandleFile handleFile = new ClientHandleFile();
	private static String tmpPath = ".";
	public static final ClientHandleFile getHandleFile(){
		return handleFile;
	}
	//创建临时文件存放目录。
	static {
		String root = PathUtil.getRootPath(ClientHandleFile.class);
		File f = new File(root);
		if( "bin".equals(f.getName()) || "classes".equals(f.getName())){
			f = f.getParentFile();
			root = f.getPath();
		}
		if( "plugins".equalsIgnoreCase(f.getName()) ){
			f = f.getParentFile();
			root = f.getPath();
		}
		tmpPath = root + File.separator + "tmp";
		f = new File(tmpPath);
		if( ! f.exists() )
			f.mkdir();
		tmpPath += File.separator;
		System.out.println("tmp="+tmpPath);
	}

	/**
	 * 从服务器读取文件。每次最大读取1M。
	 * @param inputBody：服务端命令应答的报文体。下行格式path+0+long + 文件数据流 
	 * path字符串，以0结尾
	 * @return：继续读取数据的body，null表示文件读取完毕。
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
				break;
			}
		}
		if( position < 0 || null == btPath )
			throw new MonitorHandleException("监控管理：获取文件异常，输入非法。");
		//下载的文件都放到临时目录：当前工作目录/tmp/
		path = path.replace('\\',File.separatorChar);
		path = path.replace('/', File.separatorChar);
		//检测，如果存在相对路径，需要预先建立相对路径。
		if( path.lastIndexOf(File.separatorChar)> 0 ){
			int index1 = path.lastIndexOf(File.separatorChar);
			String subDir = path.substring(0, index1);
			File subFile = new File(tmpPath+subDir);
			if( !subFile.exists() )
				subFile.mkdirs();
		}
		path = tmpPath + path;
		
		int offset = inputBody.position();			//数据开始读取的位置
		int dataLen = inputBody.remaining();		//数据长度
		if( 0 == dataLen )
			return null;
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
			if( dataLen < HandleFile.MAX_BLOCK )
				return null;			//文件读取完毕
			//继续请求读取后续数据
			inputBody.clear();
			inputBody.put(btPath);	inputBody.put((byte)0);
			inputBody.putLong(position);
			inputBody.flip();
			return inputBody;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
	/**
	 * 向服务器上传文件。
	 * @param inputBody：命令上行的报文体。下行格式path+0+long(offset)+数据流
	 * path字符串，以0结尾
	 * @return：null表示没有后续上传。
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
				inputBody.limit(inputBody.position());
				inputBody.rewind();
				break;
			}
		}
		if( position < 0 )
			throw new MonitorHandleException("监控管理：上传文件命令异常，输入非法。");
		//下载的文件都放到临时目录：当前工作目录/tmp/
		path = path.replace('\\',File.separatorChar);
		path = path.replace('/', File.separatorChar);
		path = tmpPath + path;
		
		try{
			RandomAccessFile raf = new RandomAccessFile(path,"r");
			if( position> raf.length() ){
				raf.close();
				throw new MonitorHandleException("监控管理：写文件命令异常，读文件起始位置>文件实际长度。");
			}
			int len = (int)(raf.length()-position);
			if( len<=0 ){
				raf.close();
				return null;	//文件上传完毕
			}
			int toRead = HandleFile.MAX_BLOCK;
			toRead = Math.min(len, toRead);
			ByteBuffer body = ByteBuffer.allocate(inputBody.remaining()+toRead);
			
			raf.seek(position);
			body.put(inputBody);
			inputBody.rewind();
			raf.read(body.array(), inputBody.limit(), toRead);
			body.position(0);
			raf.close();
			return body;
		}catch(Exception exp){
			throw new MonitorHandleException(exp);
		}
	}
	
}
