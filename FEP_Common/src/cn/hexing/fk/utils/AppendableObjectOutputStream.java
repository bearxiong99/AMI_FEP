package cn.hexing.fk.utils;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * 
 * @author 		gaoll
 * @time 		2013��7��4��11:22:27
 * @decription  ��Ҫ���ڽ��
 * 				java.io.StreamCorruptedException: invalid type code: AC
 */
public class AppendableObjectOutputStream extends ObjectOutputStream{

	public AppendableObjectOutputStream(OutputStream out) throws IOException {
		super(out);
	}

	@Override
	protected void writeStreamHeader() throws IOException {
	}

	
	
}
