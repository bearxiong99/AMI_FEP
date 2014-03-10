package cn.hexing.fas.model.pos;

/**
 *  
 * @author gaoll
 *
 * @time 2012-11-14 上午10:50:15
 *
 * @info pos机命令请求对象
 */
public class PosCommandRequest {
	private byte fun_c ;
	
	private String param;
	
	private String seq ;
	
	private short subFun_c=-1;
	
	private long clientVersion=0x01070101;
	
	private int sf_flag=0;//分帧标识
	
	private int c_seq=1;//帧序号
	
	private int zip_flag=0;//压缩标识
	
	
	
	public final String getParam() {
		return param;
	}

	public final void setParam(String param) {
		this.param = param;
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

	public void setClientVersion(short clientVersion) {
		this.clientVersion = clientVersion;
	}


	public int getFrameSeq() {
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

	public void setClientVersion(long clientVersion) {
		this.clientVersion = clientVersion;
	}

	public int getSfFlag() {
		return sf_flag;
	}

	public void setSfFlag(int sf_flag) {
		this.sf_flag = sf_flag;
	}
}
