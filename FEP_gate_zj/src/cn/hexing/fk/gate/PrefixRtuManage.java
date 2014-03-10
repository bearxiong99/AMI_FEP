/**
 * Regarding to GaoKe RTU, old version need prefix characters to active RTU.
 * This feature is supported by rtu list file which defined at application.properties
 * Any file name begin with 'rtu-prefix*.txt' will be loaded automatically.
 */
package cn.hexing.fk.gate;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import cn.hexing.fk.utils.HexDump;

/**
 *
 */
public class PrefixRtuManage {
	private static final Logger log = Logger.getLogger(PrefixRtuManage.class);
	//attributes configurable by Spring
	private String prefixRtuFiles = null;		//define prefix file location, separated by ';' or ','
	private String defPrefixString = null;
	private String filePattern = "rtu\\-prefix.*\\.txt";
	
	//attributes used locally by bean itself.
	private Date lastRead ;			//last read time from all files
	private static PrefixRtuManage instance = new PrefixRtuManage();
	private Map<Integer,byte[]> pRtus = new HashMap<Integer,byte[]>();
	private byte[] defPrefix = null;
	private FilenameFilter fileFilter = null;
	private Pattern pattern = null;
	private File curDir = null;
	
	private PrefixRtuManage(){
		fileFilter = new FilenameFilter(){
			public boolean accept(File dir, String name) {
				return pattern.matcher(name).matches();
			}
		};
		initialize();
	}
	
	public static final PrefixRtuManage getInstance(){
		return instance;
	}
	
	public void initialize(){
		pattern = Pattern.compile(this.filePattern);
		String curPath = System.getProperty("user.dir");
		log.info("RTU-prefix配置文件必须在该目录下:"+curPath);
		curDir = new File(curPath);
		if( null == defPrefixString ){
			setDefPrefixString("FEFEFEFEFEFEFEFEFEFEFEFEFEFEFE");
		}
		checkPrefixFile();
	}
	
	public byte[] getRtuPrefix(int rtua){
		synchronized( pRtus ){
			return pRtus.get(rtua);
		}
	}

	public void setPrefixRtuFiles(String prefixRtuFiles) {
		this.prefixRtuFiles = prefixRtuFiles;
	}

	public void setDefPrefixString(String defPrefixString) {
		this.defPrefixString = defPrefixString;
		int len = defPrefixString.length()/2;
		defPrefix = new byte[len];
		for(int i=0; i<len ; i++ ){
			byte cHigh = c2byte(defPrefixString.charAt(i*2));
			byte cLow = c2byte(defPrefixString.charAt(i*2+1));
			defPrefix[i] =  (byte)( ((cHigh & 0x0F) << 4) | (cLow & 0x0F) );
		}
		log.info("前导字符串:"+HexDump.hexDumpCompact(defPrefix,0,len));
	}
	
	private byte c2byte(char c){
		if( c>= '0' && c <='9' )
			return (byte)(c - '0');
		else if( c>='A' && c<= 'F' )
			return (byte)(c - 'A' + 10);
		else if( c>= 'a' && c<= 'f' )
			return (byte)( c-'a' + 10 );
		else
			return 0;
	}
	
	/**
	 * Java job to check the rtu-prefile*.txt file. Spring configuration job.
	 */
	public void checkPrefixFile(){
		if( null != prefixRtuFiles && prefixRtuFiles.length()>0 ){
			String [] files = prefixRtuFiles.split(",;");
			if( null != files ){
				for( String fname: files ){
					loadFromFile(fname);
				}
			}
		}
		File[] files = curDir.listFiles(fileFilter);
		for(File file: files ){
			loadFromFile(file);
		}
		lastRead = new Date(System.currentTimeMillis());
	}
	
	private void loadFromFile(String fname){
		loadFromFile(new File(fname));
	}
	
	private void loadFromFile(File file){
		if( ! file.isFile() || !file.canRead() ){
			log.warn("file:"+file.getPath()+", is not file or can not be read !");
			return;
		}
		Date lastModified = new Date(file.lastModified());
		//如果配置文件不是比上次检查时间更新，那么退出加载。
		if( null != lastRead && (! lastModified.after(lastRead)) )
			return;
		log.info("开始加载带前导的RTU："+file.getPath());
		try{
			_doLoad(file);
		}catch(Exception e){
			log.error("load rtu-prefix file exception:"+file.getName()+",reason="+e.getLocalizedMessage(),e);
		}
	}
	
	private void _doLoad(File f) throws FileNotFoundException,IOException {
		long fLen = f.length();
		int bufLen = (int)fLen;
		if( bufLen<0 ){
			log.error("rtu-prefix too long. filelen="+fLen+",convert to int bufLen="+bufLen);
			return;
		}
		FileInputStream fis = new FileInputStream(f);
		byte[] buf = new byte[bufLen];
		int offset = 0, bytesRead = 1;
		while( bytesRead > 0 ){
			bytesRead = fis.read(buf, offset, buf.length-offset);
			if( bytesRead>0 )
				offset += bytesRead ;
		}
		fis.close();
		log.debug("file length="+fLen+", bytesRead="+offset );
		String strBuffer = new String(buf);
		Pattern pat = Pattern.compile(",|;|\\s");
		String[] rtuStrArray = pat.split(strBuffer);
		synchronized(pRtus){
			for( String sRtu: rtuStrArray){
				sRtu = sRtu.trim();
				if( sRtu.length()<8 )
					continue;
				try{
					long lrtua = Long.parseLong(sRtu, 16);
					int rtua = (int)lrtua;
					pRtus.put(rtua, this.defPrefix);
					if( log.isDebugEnabled() ){
						log.debug("RTUA="+HexDump.toHex(rtua)+",prefix="+HexDump.hexDumpCompact(defPrefix,0,defPrefix.length));
					}
				}catch(Exception e){
					
				}
			}
		}
	}

	public void setFilePattern(String filePattern) {
		this.filePattern = filePattern;
	}
}
