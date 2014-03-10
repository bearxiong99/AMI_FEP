package cn.hexing.fas.model.pos;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-14 上午10:35:46
 *
 * @info pos机命令返回数据
 */
public class PosCommandResult {
	private String value;
	
	private byte  fun_c;
	
	private String seq;
	
	private short subFun_c;
	
	private long clientVersion=0x01070101;
	
	private int sf_flag=0;//分帧标识
	
	private int c_seq=1;//帧序号
	
	private int zip_flag=0;//压缩标	

	public final String getValue() {
		return value;
	}

	public final void setValue(String value) {
		this.value = value;
	}

	public final String getSeq() {
		return seq;
	}

	public final void setSeq(String seq) {
		this.seq = seq;
	}

	public final byte getFun_c() {
		return fun_c;
	}

	public final void setFun_c(byte fun_c) {
		this.fun_c = fun_c;
	}

	public short getSubFun_c() {
		return subFun_c;
	}

	public void setSubFun_c(short subFun_c) {
		this.subFun_c = subFun_c;
	}

	public long getClientVersion() {
		return clientVersion;
	}

	public void setClientVersion(long clientVersion) {
		this.clientVersion = clientVersion;
	}

	public int getSfFlag() {
		return sf_flag;
	}

	public void setSfFlag(int sf_flag) {
		this.sf_flag = sf_flag;
	}

	public int getC_seq() {
		return c_seq;
	}

	public void setFrameSeq(int c_seq) {
		this.c_seq = c_seq;
	}

	public int getZipFlag() {
		return zip_flag;
	}

	public void setZipFlag(int zip_flag) {
		this.zip_flag = zip_flag;
	}
}
