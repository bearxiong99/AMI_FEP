/**
 * 处理文件列表。
 */
package cn.hexing.fk.monitor.biz;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * 
 */
public class HandleListFile {
	private static final HandleListFile listFile = new HandleListFile();
	public static final HandleListFile getListFile(){
		return listFile;
	}
	
	public String list(String path){
		StringBuffer sb = new StringBuffer(1024);
		File file = new File(path);
		if( !file.exists() || !file.isDirectory() )
			return "";
		File[] files = file.listFiles();
		for(int i=0; i<files.length; i++ ){
			if( files[i].isDirectory() )
				continue;
			sb.append(path).append(File.separator).append(files[i].getName());
			sb.append(",").append(files[i].length());
			sb.append(";");	//文件名称分割符号
		}
		return sb.toString();
	}

	/**
	 * 按照后缀进行文件列表，例如 .xml,.properties
	 * @param path
	 * @param postFix
	 * @return
	 */
	public String list(String path,String postFix){
		StringTokenizer st = new StringTokenizer(postFix,"*,.");
		ArrayList<String> suffixs = new ArrayList<String>(5);
		while( st.hasMoreTokens() ){
			String name = st.nextToken();
			if( name.length()==0 )
				continue;
			suffixs.add(name);
		}
		FilenameFilter filter = new ListFileFilter(suffixs);
		StringBuffer sb = new StringBuffer(1024);
		File file = new File(path);
		if( !file.exists() || !file.isDirectory() )
			return "";
		File[] files = file.listFiles(filter);
		for(int i=0; i<files.length; i++ ){
			if( files[i].isDirectory() )
				continue;
			sb.append(path).append(File.separator).append(files[i].getName());
			sb.append(";");	//文件名称分割符号
		}
		return sb.toString();
	}
	
	class ListFileFilter implements FilenameFilter{
		ArrayList<String> filters;
		public ListFileFilter(ArrayList<String> filters){
			this.filters = filters;
		}

		public boolean accept(File dir, String name) {
			for(String suffix: filters){
				if( name.endsWith(suffix))
					return true;
			}
			return false;
		}
		
	}
}
