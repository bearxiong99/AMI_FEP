package dcsml.base;

import java.io.IOException;

import cn.hexing.fk.utils.HexDump;

public class DcsmlInteger16 extends DcsmlUnsigned{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5699835440185567804L;

	
	public DcsmlInteger16(){
		super();
		fixedLength(3);
	}
	
	public DcsmlInteger16(int initValue){
		this();
		this.setValue(initValue);
	}
	
	
	public static void main(String[] args) throws IOException {
		DcsmlInteger16 du = new DcsmlInteger16(18);
		System.out.println(HexDump.toHex(du.encode()));
		
	}
	
	
}
