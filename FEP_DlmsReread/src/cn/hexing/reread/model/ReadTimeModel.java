package cn.hexing.reread.model;
/**
 * 需要召测时间的终端、表计
 * @ClassName:ReadTimeModel
 * @Description:TODO
 * @author kexl
 * @date 2012-11-15 下午02:45:40
 *
 */
public class ReadTimeModel {
	private String dwdm; //单位代码
	
	private String zdjh; //终端局号
	
	private String zdljdz; //终端逻辑地址
	
	private String txdz; //通讯地址
	
	private String yhlx; //用户类型
	
	private String zdgylx; //终端规约类型
	
	private String bjjh; // 表计局号
	
	private String cldh; //测量点号
	
	private String iszj; //是否中继

	private String txgy; //表计规约类型
	
	public ReadTimeModel() {
		super();
	}


	

	public ReadTimeModel(String dwdm, String zdjh, String zdljdz, String txdz,
			String yhlx, String zdgylx, String bjjh, String cldh, String iszj,
			String txgy) {
		super();
		this.dwdm = dwdm;
		this.zdjh = zdjh;
		this.zdljdz = zdljdz;
		this.txdz = txdz;
		this.yhlx = yhlx;
		this.zdgylx = zdgylx;
		this.bjjh = bjjh;
		this.cldh = cldh;
		this.iszj = iszj;
		this.txgy = txgy;
	}

	public String getDwdm() {
		return dwdm;
	}


	public void setDwdm(String dwdm) {
		this.dwdm = dwdm;
	}


	public String getZdjh() {
		return zdjh;
	}


	public void setZdjh(String zdjh) {
		this.zdjh = zdjh;
	}


	public String getZdljdz() {
		return zdljdz;
	}


	public void setZdljdz(String zdljdz) {
		this.zdljdz = zdljdz;
	}


	public String getTxdz() {
		return txdz;
	}


	public void setTxdz(String txdz) {
		this.txdz = txdz;
	}


	public String getYhlx() {
		return yhlx;
	}


	public void setYhlx(String yhlx) {
		this.yhlx = yhlx;
	}


	public String getZdgylx() {
		return zdgylx;
	}


	public void setZdgylx(String zdgylx) {
		this.zdgylx = zdgylx;
	}


	public String getBjjh() {
		return bjjh;
	}


	public void setBjjh(String bjjh) {
		this.bjjh = bjjh;
	}


	public String getCldh() {
		return cldh;
	}


	public void setCldh(String cldh) {
		this.cldh = cldh;
	}


	public String getIszj() {
		return iszj;
	}


	public void setIszj(String iszj) {
		this.iszj = iszj;
	}


	public String getTxgy() {
		return txgy;
	}


	public void setTxgy(String txgy) {
		this.txgy = txgy;
	}
	
	public String toString(){
		return "ReadTimeModel[dwdm:" + dwdm
		+",zdjh:" + zdjh
		+",zdljdz:" + zdljdz
		+",txdz:" + txdz
		+",yhlx:" + yhlx
		+",zdgylx:" + zdgylx
		+",bjjh:" + bjjh
		+",cldh:" + cldh
		+",iszj:" + iszj
		+",txgy:" + txgy
		+"]";
	}
}
