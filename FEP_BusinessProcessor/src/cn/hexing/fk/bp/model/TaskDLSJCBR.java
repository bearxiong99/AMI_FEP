package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-17 下午5:08:12
 *
 * @info 电量数据月冻结
 */
public class TaskDLSJCBR implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7097157929742691414L;
	/** 数据保存ID */
	private String SJID;
    /** 数据时间 */
    private Date SJSJ;
    /** 接收时间，浙规取系统时间，国网取抄表时间 */
    private Date JSSJ;
    /** CT */
    private String CT;
    /** PT */
    private String PT;	
    /** 综合倍率 */
    private int ZHBL;	
    /** 补全标志 */
    private int BQBJ;


	private String ZXYGZ;
	private String ZXYGZ1;
	private String ZXYGZ2;
	private String ZXYGZ3;
	private String ZXYGZ4;
	private String FXYGZ ;
	private String FXYGZ1 ;
	private String FXYGZ2 ;
	private String FXYGZ3 ;
	private String FXYGZ4 ;
	private String ZXWGZ ;
	private String ZXWGZ1 ;
	private String ZXWGZ2 ;
	private String ZXWGZ3 ;
	private String ZXWGZ4 ;
	private String FXWGZ ;
	private String FXWGZ1 ;
	private String FXWGZ2 ;
	private String FXWGZ3 ;
	private String FXWGZ4 ;
	private String ZXYGZDXL ;
	private String ZXYGZDXLFSSJ;
	private String FXYGZDXL ;
	private String FXYGZDXLFSSJ;
	private String WGZXX1 ;
	private String WGZXX2 ;
	private String WGZXX3 ;
	private String WGZXX4 ;
	private String FXWGZXX1 ;
	private String FXWGZXX2 ;
	private String FXWGZXX3 ;
	private String FXWGZXX4 ;
	private String BILLING_DATE;
	public final String getSJID() {
		return SJID;
	}
	public final void setSJID(String sJID) {
		SJID = sJID;
	}
	public final Date getSJSJ() {
		return SJSJ;
	}
	public final void setSJSJ(Date sJSJ) {
		SJSJ = sJSJ;
	}
	public final Date getJSSJ() {
		return JSSJ;
	}
	public final void setJSSJ(Date jSSJ) {
		JSSJ = jSSJ;
	}
	public final String getCT() {
		return CT;
	}
	public final void setCT(String cT) {
		CT = cT;
	}
	public final String getPT() {
		return PT;
	}
	public final void setPT(String pT) {
		PT = pT;
	}
	public final int getZHBL() {
		return ZHBL;
	}
	public final void setZHBL(int zHBL) {
		ZHBL = zHBL;
	}
	public final int getBQBJ() {
		return BQBJ;
	}
	public final void setBQBJ(int bQBJ) {
		BQBJ = bQBJ;
	}
	public final String getZXYGZ() {
		return ZXYGZ;
	}
	public final void setZXYGZ(String zXYGZ) {
		ZXYGZ = zXYGZ;
	}
	public final String getZXYGZ1() {
		return ZXYGZ1;
	}
	public final void setZXYGZ1(String zXYGZ1) {
		ZXYGZ1 = zXYGZ1;
	}
	public final String getZXYGZ2() {
		return ZXYGZ2;
	}
	public final void setZXYGZ2(String zXYGZ2) {
		ZXYGZ2 = zXYGZ2;
	}
	public final String getZXYGZ3() {
		return ZXYGZ3;
	}
	public final void setZXYGZ3(String zXYGZ3) {
		ZXYGZ3 = zXYGZ3;
	}
	public final String getZXYGZ4() {
		return ZXYGZ4;
	}
	public final void setZXYGZ4(String zXYGZ4) {
		ZXYGZ4 = zXYGZ4;
	}
	public final String getFXYGZ() {
		return FXYGZ;
	}
	public final void setFXYGZ(String fXYGZ) {
		FXYGZ = fXYGZ;
	}
	public final String getFXYGZ1() {
		return FXYGZ1;
	}
	public final void setFXYGZ1(String fXYGZ1) {
		FXYGZ1 = fXYGZ1;
	}
	public final String getFXYGZ2() {
		return FXYGZ2;
	}
	public final void setFXYGZ2(String fXYGZ2) {
		FXYGZ2 = fXYGZ2;
	}
	public final String getFXYGZ3() {
		return FXYGZ3;
	}
	public final void setFXYGZ3(String fXYGZ3) {
		FXYGZ3 = fXYGZ3;
	}
	public final String getFXYGZ4() {
		return FXYGZ4;
	}
	public final void setFXYGZ4(String fXYGZ4) {
		FXYGZ4 = fXYGZ4;
	}
	public final String getZXWGZ() {
		return ZXWGZ;
	}
	public final void setZXWGZ(String zXWGZ) {
		ZXWGZ = zXWGZ;
	}
	public final String getZXWGZ1() {
		return ZXWGZ1;
	}
	public final void setZXWGZ1(String zXWGZ1) {
		ZXWGZ1 = zXWGZ1;
	}
	public final String getZXWGZ2() {
		return ZXWGZ2;
	}
	public final void setZXWGZ2(String zXWGZ2) {
		ZXWGZ2 = zXWGZ2;
	}
	public final String getZXWGZ3() {
		return ZXWGZ3;
	}
	public final void setZXWGZ3(String zXWGZ3) {
		ZXWGZ3 = zXWGZ3;
	}
	public final String getZXWGZ4() {
		return ZXWGZ4;
	}
	public final void setZXWGZ4(String zXWGZ4) {
		ZXWGZ4 = zXWGZ4;
	}
	public final String getFXWGZ() {
		return FXWGZ;
	}
	public final void setFXWGZ(String fXWGZ) {
		FXWGZ = fXWGZ;
	}
	public final String getFXWGZ1() {
		return FXWGZ1;
	}
	public final void setFXWGZ1(String fXWGZ1) {
		FXWGZ1 = fXWGZ1;
	}
	public final String getFXWGZ2() {
		return FXWGZ2;
	}
	public final void setFXWGZ2(String fXWGZ2) {
		FXWGZ2 = fXWGZ2;
	}
	public final String getFXWGZ3() {
		return FXWGZ3;
	}
	public final void setFXWGZ3(String fXWGZ3) {
		FXWGZ3 = fXWGZ3;
	}
	public final String getFXWGZ4() {
		return FXWGZ4;
	}
	public final void setFXWGZ4(String fXWGZ4) {
		FXWGZ4 = fXWGZ4;
	}
	public final String getZXYGZDXL() {
		return ZXYGZDXL;
	}
	public final void setZXYGZDXL(String zXYGZDXL) {
		ZXYGZDXL = zXYGZDXL;
	}
	public final String getZXYGZDXLFSSJ() {
		return ZXYGZDXLFSSJ;
	}
	public final void setZXYGZDXLFSSJ(String zXYGZDXLFSSJ) {
		ZXYGZDXLFSSJ = zXYGZDXLFSSJ;
	}
	public final String getFXYGZDXL() {
		return FXYGZDXL;
	}
	public final void setFXYGZDXL(String fXYGZDXL) {
		FXYGZDXL = fXYGZDXL;
	}
	public final String getFXYGZDXLFSSJ() {
		return FXYGZDXLFSSJ;
	}
	public final void setFXYGZDXLFSSJ(String fXYGZDXLFSSJ) {
		FXYGZDXLFSSJ = fXYGZDXLFSSJ;
	}
	public final String getWGZXX1() {
		return WGZXX1;
	}
	public final void setWGZXX1(String wGZXX1) {
		WGZXX1 = wGZXX1;
	}
	public final String getWGZXX2() {
		return WGZXX2;
	}
	public final void setWGZXX2(String wGZXX2) {
		WGZXX2 = wGZXX2;
	}
	public final String getWGZXX3() {
		return WGZXX3;
	}
	public final void setWGZXX3(String wGZXX3) {
		WGZXX3 = wGZXX3;
	}
	public final String getWGZXX4() {
		return WGZXX4;
	}
	public final void setWGZXX4(String wGZXX4) {
		WGZXX4 = wGZXX4;
	}
	public final String getFXWGZXX1() {
		return FXWGZXX1;
	}
	public final void setFXWGZXX1(String fXWGZXX1) {
		FXWGZXX1 = fXWGZXX1;
	}
	public final String getFXWGZXX2() {
		return FXWGZXX2;
	}
	public final void setFXWGZXX2(String fXWGZXX2) {
		FXWGZXX2 = fXWGZXX2;
	}
	public final String getFXWGZXX3() {
		return FXWGZXX3;
	}
	public final void setFXWGZXX3(String fXWGZXX3) {
		FXWGZXX3 = fXWGZXX3;
	}
	public final String getFXWGZXX4() {
		return FXWGZXX4;
	}
	public final void setFXWGZXX4(String fXWGZXX4) {
		FXWGZXX4 = fXWGZXX4;
	}
	public final String getBILLING_DATE() {
		return BILLING_DATE;
	}
	public final void setBILLING_DATE(String bILLING_DATE) {
		BILLING_DATE = bILLING_DATE;
	}
	
}
