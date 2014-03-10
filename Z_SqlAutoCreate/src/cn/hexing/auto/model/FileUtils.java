package cn.hexing.auto.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class FileUtils {

	
	public static boolean writeTo(File file,String value){
		
		try {
			FileOutputStream fos = new FileOutputStream(file);
			fos.write(value.getBytes());
		} catch (FileNotFoundException e) {
			return false;
		} catch (IOException e) {
			return false;
		}
		return true;
	}
	
}
