package cn.hexing.tool;

import java.io.File;

public class GetClassPath {

	public static void main(String[] args) {
		File f = new File("./lib");
		String classPath = "Class-Path: .";
		for(String s:f.list()){
			classPath+=" lib/"+s;
		}
		System.out.println(classPath);
	}
}
