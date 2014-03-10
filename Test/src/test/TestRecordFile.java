package test;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.fk.utils.FileUtil;

public class TestRecordFile {
	private static String destDir="login";
	public static void main(String[] args) {
		for(int i=20000;i>0;i--){
			recordLogin("10"+"0000000000".substring((""+(i)).length())+(i));
		}
	}
	private static void recordLogin(String logicalAddr) {
		File dir = FileUtil.mkdirs(destDir);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = logicalAddr+"-"+sdf.format(new Date());
		boolean isFind = false;
		if(dir.exists() && dir.isDirectory()){
			for(File file : dir.listFiles()){
				if(file.getName().startsWith(logicalAddr)){
					file.renameTo(new File(destDir+File.separator+fileName));
					isFind = true;
					break;
				}
			}
			if(!isFind){
				FileUtil.openFile(destDir, fileName);
			}
		}
	}
}
