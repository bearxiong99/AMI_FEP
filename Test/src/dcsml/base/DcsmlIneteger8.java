package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlIneteger8 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlIneteger8(){
		super();
		fixedLength(2);
	}
	
	public DcsmlIneteger8(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlIneteger8 du = new DcsmlIneteger8(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
