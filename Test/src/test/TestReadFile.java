package test;

import java.io.File;
import java.util.List;

import cn.hexing.fk.utils.FileUtil;


public class TestReadFile {
	public static void main(String[] args) {
		
		List<Object> s = FileUtil.readObjectFromFile(new File("./test.data"));
		System.out.println(s);
		
	}
}
