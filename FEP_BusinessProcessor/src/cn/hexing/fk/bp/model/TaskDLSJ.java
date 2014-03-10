package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 数据库映射表:上报电量类
 */

public class TaskDLSJ implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/** 数据保存ID */
	private String SJID;
    /** 数据时间 */
    private Date SJSJ ;
    /** 接收时间，浙规取系统时间，国网取抄表时间 */
    private Date JSSJ ;
    /** CT */
    private String CT;
    /** PT */
    private String PT;	
    /** 综合倍率 */
    private int ZHBL;	
    /** 补全标志 */
    private int BQBJ;

	private String FXYGZ2;
    private String FXYGZ3;
    private String FXYGZ4;
    private String ZXWGZ;
    private String ZXWGZ1;
    private String ZXWGZ2;
    private String ZXWGZ3;
    private String ZXWGZ4;
    private String FXWGZ;
    private String FXWGZ1;
    private String FXWGZ2;
    private String FXWGZ3;
    private String FXWGZ4;
    private String ZXYGZDXL;
    private String ZXYGZDXLFSSJ;
    private String ZXYGZDXL1;
    private String ZXYGZDXL1FSSJ;
    private String ZXYGZDXL2;
    private String ZXYGZDXL2FSSJ;
    private String ZXYGZDXL3;
    private String ZXYGZDXL3FSSJ;
    private String ZXYGZDXL4;
    private String ZXYGZDXL4FSSJ;
    private String FXYGZDXL;
    private String FXYGZDXLFSSJ;
    private String ZXWGZDXL;
    private String ZXWGZDXLFSSJ;
    private String ZXWGZDXL1;
    private String ZXWGZDXL1FSSJ;
    private String ZXWGZDXL2;
    private String ZXWGZDXL2FSSJ;
    private String ZXWGZDXL3;
    private String ZXWGZDXL3FSSJ;
    private String ZXWGZDXL4;
    private String ZXWGZDXL4FSSJ;
    private String FXWGZDXL;
    private String FXWGZDXLFSSJ;
    private String WGZXX1;
    private String WGZXX2;
    private String WGZXX3;
    private String WGZXX4;
    private String FXWGZXX1;
    private String FXWGZXX2;
    private String FXWGZXX3;
    private String FXWGZXX4;
    private String ZXYGZXL;
    private String ZXYGZXLFSSJ;
    private String ZXYGZXL1;
    private String ZXYGZXL1FSSJ;
    private String ZXYGZXL2;
    private String ZXYGZXL2FSSJ;
    private String ZXYGZXL3;
    private String ZXYGZXL3FSSJ;
    private String ZXYGZXL4;
    private String ZXYGZXL4FSSJ;
    private String ZXYGSZZXL;
    private String ZXYGSZZXLFSSJ;
    private String ZXYGSZZXL1;
    private String ZXYGSZZXL1FSSJ;
    private String ZXYGSZZXL2;
    private String ZXYGSZZXL2FSSJ;
    private String ZXYGSZZXL3;
    private String ZXYGSZZXL3FSSJ;
    private String ZXYGSZZXL4;
    private String ZXYGSZZXL4FSSJ;
    private String SYXLJGSJ;
    private String XLZQS;
    private String ZXYGLJZDXL;
    private String ZXYGLJZDXL1;
    private String ZXYGLJZDXL2;
    private String ZXYGLJZDXL3;
    private String ZXYGLJZDXL4;
    private String ZJYCXLFWSJ;
    private String XLFWCS;
    private String ZJHGS;
    private String ZXYGZ;
    private String ZXYGZ1;
    private String ZXYGZ2;
    private String ZXYGZ3;
    private String ZXYGZ4;
    private String FXYGZ;
    private String FXYGZ1;
    private String WLYGZ;
    private String WLYG1;
    private String WLYG2;
    private String WLYG3;
    private String WLYG4;
    private String JDYGZ;
    private String JDYG1;
    private String JDYG2;
    private String JDYG3;
    private String JDYG4;
    
	public int getBQBJ() {
		return BQBJ;
	}
	public void setBQBJ(int bqbj) {
		BQBJ = bqbj;
	}
	public String getCT() {
		return CT;
	}
	public void setCT(String ct) {
		CT = ct;
	}
	public String getSJID() {
		return SJID;
	}
	public void setSJID(String sjid) {
		SJID = sjid;
	}
	public Date getSJSJ() {
		return SJSJ;
	}
	public void setSJSJ(Date sjsj) {
		SJSJ = sjsj;
	}
	public String getPT() {
		return PT;
	}
	public void setPT(String pt) {
		PT = pt;
	}
	public int getZHBL() {
		return ZHBL;
	}
	public void setZHBL(int zhbl) {
		ZHBL = zhbl;
	}
	public String getZXYGZ() {
		return ZXYGZ;
	}
	public void setZXYGZ(String zxygz) {
		ZXYGZ = zxygz;
	}
	public String getZXYGZ1() {
		return ZXYGZ1;
	}
	public void setZXYGZ1(String zxygz1) {
		ZXYGZ1 = zxygz1;
	}
	public String getZXYGZ2() {
		return ZXYGZ2;
	}
	public void setZXYGZ2(String zxygz2) {
		ZXYGZ2 = zxygz2;
	}
	public String getZXYGZ3() {
		return ZXYGZ3;
	}
	public void setZXYGZ3(String zxygz3) {
		ZXYGZ3 = zxygz3;
	}
	public String getZXYGZ4() {
		return ZXYGZ4;
	}
	public void setZXYGZ4(String zxygz4) {
		ZXYGZ4 = zxygz4;
	}
	public String getWGZXX1() {
		return WGZXX1;
	}
	public void setWGZXX1(String wgzxx1) {
		WGZXX1 = wgzxx1;
	}
	public String getWGZXX4() {
		return WGZXX4;
	}
	public void setWGZXX4(String wgzxx4) {
		WGZXX4 = wgzxx4;
	}
	public Date getJSSJ() {
		return JSSJ;
	}
	public void setJSSJ(String jssj) {
		try{
			if (jssj.trim().length()==16){
				SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
				this.JSSJ = df.parse(jssj);
			}
		}
		catch(Exception e){		
		}
	}
	public void setJSSJ(Date jssj) {
		JSSJ = jssj;
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
	public final String getZXWGZ4() {
		return ZXWGZ4;
	}
	public final void setZXWGZ4(String zXWGZ4) {
		ZXWGZ4 = zXWGZ4;
	}
	public final String getFXYGZ() {
		return FXYGZ;
	}
	public final void setFXYGZ(String fXYGZ) {
		FXYGZ = fXYGZ;
	}
	public final String getZXWGZ() {
		return ZXWGZ;
	}
	public final void setZXWGZ(String zXWGZ) {
		ZXWGZ = zXWGZ;
	}
	public final String getFXWGZ() {
		return FXWGZ;
	}
	public final void setFXWGZ(String fXWGZ) {
		FXWGZ = fXWGZ;
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
	public String getZXYGZDXL1() {
		return ZXYGZDXL1;
	}
	public void setZXYGZDXL1(String zXYGZDXL1) {
		ZXYGZDXL1 = zXYGZDXL1;
	}
	public String getZXYGZDXL1FSSJ() {
		return ZXYGZDXL1FSSJ;
	}
	public void setZXYGZDXL1FSSJ(String zXYGZDXL1FSSJ) {
		ZXYGZDXL1FSSJ = zXYGZDXL1FSSJ;
	}
	public String getZXYGZDXL2() {
		return ZXYGZDXL2;
	}
	public void setZXYGZDXL2(String zXYGZDXL2) {
		ZXYGZDXL2 = zXYGZDXL2;
	}
	public String getZXYGZDXL2FSSJ() {
		return ZXYGZDXL2FSSJ;
	}
	public void setZXYGZDXL2FSSJ(String zXYGZDXL2FSSJ) {
		ZXYGZDXL2FSSJ = zXYGZDXL2FSSJ;
	}
	public String getZXYGZDXL3() {
		return ZXYGZDXL3;
	}
	public void setZXYGZDXL3(String zXYGZDXL3) {
		ZXYGZDXL3 = zXYGZDXL3;
	}
	public String getZXYGZDXL3FSSJ() {
		return ZXYGZDXL3FSSJ;
	}
	public void setZXYGZDXL3FSSJ(String zXYGZDXL3FSSJ) {
		ZXYGZDXL3FSSJ = zXYGZDXL3FSSJ;
	}
	public String getZXYGZDXL4() {
		return ZXYGZDXL4;
	}
	public void setZXYGZDXL4(String zXYGZDXL4) {
		ZXYGZDXL4 = zXYGZDXL4;
	}
	public String getZXYGZDXL4FSSJ() {
		return ZXYGZDXL4FSSJ;
	}
	public void setZXYGZDXL4FSSJ(String zXYGZDXL4FSSJ) {
		ZXYGZDXL4FSSJ = zXYGZDXL4FSSJ;
	}
	public String getZXWGZDXL() {
		return ZXWGZDXL;
	}
	public void setZXWGZDXL(String zXWGZDXL) {
		ZXWGZDXL = zXWGZDXL;
	}
	public String getZXWGZDXLFSSJ() {
		return ZXWGZDXLFSSJ;
	}
	public void setZXWGZDXLFSSJ(String zXWGZDXLFSSJ) {
		ZXWGZDXLFSSJ = zXWGZDXLFSSJ;
	}
	public String getZXWGZDXL1() {
		return ZXWGZDXL1;
	}
	public void setZXWGZDXL1(String zXWGZDXL1) {
		ZXWGZDXL1 = zXWGZDXL1;
	}
	public String getZXWGZDXL1FSSJ() {
		return ZXWGZDXL1FSSJ;
	}
	public void setZXWGZDXL1FSSJ(String zXWGZDXL1FSSJ) {
		ZXWGZDXL1FSSJ = zXWGZDXL1FSSJ;
	}
	public String getZXWGZDXL2() {
		return ZXWGZDXL2;
	}
	public void setZXWGZDXL2(String zXWGZDXL2) {
		ZXWGZDXL2 = zXWGZDXL2;
	}
	public String getZXWGZDXL2FSSJ() {
		return ZXWGZDXL2FSSJ;
	}
	public void setZXWGZDXL2FSSJ(String zXWGZDXL2FSSJ) {
		ZXWGZDXL2FSSJ = zXWGZDXL2FSSJ;
	}
	public String getZXWGZDXL3() {
		return ZXWGZDXL3;
	}
	public void setZXWGZDXL3(String zXWGZDXL3) {
		ZXWGZDXL3 = zXWGZDXL3;
	}
	public String getZXWGZDXL3FSSJ() {
		return ZXWGZDXL3FSSJ;
	}
	public void setZXWGZDXL3FSSJ(String zXWGZDXL3FSSJ) {
		ZXWGZDXL3FSSJ = zXWGZDXL3FSSJ;
	}
	public String getZXWGZDXL4() {
		return ZXWGZDXL4;
	}
	public void setZXWGZDXL4(String zXWGZDXL4) {
		ZXWGZDXL4 = zXWGZDXL4;
	}
	public String getZXWGZDXL4FSSJ() {
		return ZXWGZDXL4FSSJ;
	}
	public void setZXWGZDXL4FSSJ(String zXWGZDXL4FSSJ) {
		ZXWGZDXL4FSSJ = zXWGZDXL4FSSJ;
	}
	public String getFXWGZDXL() {
		return FXWGZDXL;
	}
	public void setFXWGZDXL(String fXWGZDXL) {
		FXWGZDXL = fXWGZDXL;
	}
	public String getFXWGZDXLFSSJ() {
		return FXWGZDXLFSSJ;
	}
	public void setFXWGZDXLFSSJ(String fXWGZDXLFSSJ) {
		FXWGZDXLFSSJ = fXWGZDXLFSSJ;
	}
	public String getZXYGZXL() {
		return ZXYGZXL;
	}
	public void setZXYGZXL(String zXYGZXL) {
		ZXYGZXL = zXYGZXL;
	}
	public String getZXYGZXLFSSJ() {
		return ZXYGZXLFSSJ;
	}
	public void setZXYGZXLFSSJ(String zXYGZXLFSSJ) {
		ZXYGZXLFSSJ = zXYGZXLFSSJ;
	}
	public String getZXYGZXL1() {
		return ZXYGZXL1;
	}
	public void setZXYGZXL1(String zXYGZXL1) {
		ZXYGZXL1 = zXYGZXL1;
	}
	public String getZXYGZXL1FSSJ() {
		return ZXYGZXL1FSSJ;
	}
	public void setZXYGZXL1FSSJ(String zXYGZXL1FSSJ) {
		ZXYGZXL1FSSJ = zXYGZXL1FSSJ;
	}
	public String getZXYGZXL2() {
		return ZXYGZXL2;
	}
	public void setZXYGZXL2(String zXYGZXL2) {
		ZXYGZXL2 = zXYGZXL2;
	}
	public String getZXYGZXL2FSSJ() {
		return ZXYGZXL2FSSJ;
	}
	public void setZXYGZXL2FSSJ(String zXYGZXL2FSSJ) {
		ZXYGZXL2FSSJ = zXYGZXL2FSSJ;
	}
	public String getZXYGZXL3() {
		return ZXYGZXL3;
	}
	public void setZXYGZXL3(String zXYGZXL3) {
		ZXYGZXL3 = zXYGZXL3;
	}
	public String getZXYGZXL3FSSJ() {
		return ZXYGZXL3FSSJ;
	}
	public void setZXYGZXL3FSSJ(String zXYGZXL3FSSJ) {
		ZXYGZXL3FSSJ = zXYGZXL3FSSJ;
	}
	public String getZXYGZXL4() {
		return ZXYGZXL4;
	}
	public void setZXYGZXL4(String zXYGZXL4) {
		ZXYGZXL4 = zXYGZXL4;
	}
	public String getZXYGZXL4FSSJ() {
		return ZXYGZXL4FSSJ;
	}
	public void setZXYGZXL4FSSJ(String zXYGZXL4FSSJ) {
		ZXYGZXL4FSSJ = zXYGZXL4FSSJ;
	}
	public String getZXYGSZZXL() {
		return ZXYGSZZXL;
	}
	public void setZXYGSZZXL(String zXYGSZZXL) {
		ZXYGSZZXL = zXYGSZZXL;
	}
	public String getZXYGSZZXLFSSJ() {
		return ZXYGSZZXLFSSJ;
	}
	public void setZXYGSZZXLFSSJ(String zXYGSZZXLFSSJ) {
		ZXYGSZZXLFSSJ = zXYGSZZXLFSSJ;
	}
	public String getZXYGSZZXL1() {
		return ZXYGSZZXL1;
	}
	public void setZXYGSZZXL1(String zXYGSZZXL1) {
		ZXYGSZZXL1 = zXYGSZZXL1;
	}
	public String getZXYGSZZXL1FSSJ() {
		return ZXYGSZZXL1FSSJ;
	}
	public void setZXYGSZZXL1FSSJ(String zXYGSZZXL1FSSJ) {
		ZXYGSZZXL1FSSJ = zXYGSZZXL1FSSJ;
	}
	public String getZXYGSZZXL2() {
		return ZXYGSZZXL2;
	}
	public void setZXYGSZZXL2(String zXYGSZZXL2) {
		ZXYGSZZXL2 = zXYGSZZXL2;
	}
	public String getZXYGSZZXL2FSSJ() {
		return ZXYGSZZXL2FSSJ;
	}
	public void setZXYGSZZXL2FSSJ(String zXYGSZZXL2FSSJ) {
		ZXYGSZZXL2FSSJ = zXYGSZZXL2FSSJ;
	}
	public String getZXYGSZZXL3() {
		return ZXYGSZZXL3;
	}
	public void setZXYGSZZXL3(String zXYGSZZXL3) {
		ZXYGSZZXL3 = zXYGSZZXL3;
	}
	public String getZXYGSZZXL3FSSJ() {
		return ZXYGSZZXL3FSSJ;
	}
	public void setZXYGSZZXL3FSSJ(String zXYGSZZXL3FSSJ) {
		ZXYGSZZXL3FSSJ = zXYGSZZXL3FSSJ;
	}
	public String getZXYGSZZXL4() {
		return ZXYGSZZXL4;
	}
	public void setZXYGSZZXL4(String zXYGSZZXL4) {
		ZXYGSZZXL4 = zXYGSZZXL4;
	}
	public String getZXYGSZZXL4FSSJ() {
		return ZXYGSZZXL4FSSJ;
	}
	public void setZXYGSZZXL4FSSJ(String zXYGSZZXL4FSSJ) {
		ZXYGSZZXL4FSSJ = zXYGSZZXL4FSSJ;
	}
	public String getSYXLJGSJ() {
		return SYXLJGSJ;
	}
	public void setSYXLJGSJ(String sYXLJGSJ) {
		SYXLJGSJ = sYXLJGSJ;
	}
	public String getXLZQS() {
		return XLZQS;
	}
	public void setXLZQS(String xLZQS) {
		XLZQS = xLZQS;
	}
	public String getZXYGLJZDXL() {
		return ZXYGLJZDXL;
	}
	public void setZXYGLJZDXL(String zXYGLJZDXL) {
		ZXYGLJZDXL = zXYGLJZDXL;
	}
	public String getZXYGLJZDXL1() {
		return ZXYGLJZDXL1;
	}
	public void setZXYGLJZDXL1(String zXYGLJZDXL1) {
		ZXYGLJZDXL1 = zXYGLJZDXL1;
	}
	public String getZXYGLJZDXL2() {
		return ZXYGLJZDXL2;
	}
	public void setZXYGLJZDXL2(String zXYGLJZDXL2) {
		ZXYGLJZDXL2 = zXYGLJZDXL2;
	}
	public String getZXYGLJZDXL3() {
		return ZXYGLJZDXL3;
	}
	public void setZXYGLJZDXL3(String zXYGLJZDXL3) {
		ZXYGLJZDXL3 = zXYGLJZDXL3;
	}
	public String getZXYGLJZDXL4() {
		return ZXYGLJZDXL4;
	}
	public void setZXYGLJZDXL4(String zXYGLJZDXL4) {
		ZXYGLJZDXL4 = zXYGLJZDXL4;
	}
	public String getZJYCXLFWSJ() {
		return ZJYCXLFWSJ;
	}
	public void setZJYCXLFWSJ(String zJYCXLFWSJ) {
		ZJYCXLFWSJ = zJYCXLFWSJ;
	}
	public String getXLFWCS() {
		return XLFWCS;
	}
	public void setXLFWCS(String xLFWCS) {
		XLFWCS = xLFWCS;
	}
	public String getZJHGS() {
		return ZJHGS;
	}
	public void setZJHGS(String zJHGS) {
		ZJHGS = zJHGS;
	}
	public String getWLYGZ() {
		return WLYGZ;
	}
	public void setWLYGZ(String wLYGZ) {
		WLYGZ = wLYGZ;
	}
	public String getWLYG1() {
		return WLYG1;
	}
	public void setWLYG1(String wLYG1) {
		WLYG1 = wLYG1;
	}
	public String getWLYG2() {
		return WLYG2;
	}
	public void setWLYG2(String wLYG2) {
		WLYG2 = wLYG2;
	}
	public String getWLYG3() {
		return WLYG3;
	}
	public void setWLYG3(String wLYG3) {
		WLYG3 = wLYG3;
	}
	public String getWLYG4() {
		return WLYG4;
	}
	public void setWLYG4(String wLYG4) {
		WLYG4 = wLYG4;
	}
	public String getJDYG1() {
		return JDYG1;
	}
	public void setJDYG1(String jDYG1) {
		JDYG1 = jDYG1;
	}
	public String getJDYG2() {
		return JDYG2;
	}
	public void setJDYG2(String jDYG2) {
		JDYG2 = jDYG2;
	}
	public String getJDYG3() {
		return JDYG3;
	}
	public void setJDYG3(String jDYG3) {
		JDYG3 = jDYG3;
	}
	public String getJDYG4() {
		return JDYG4;
	}
	public void setJDYG4(String jDYG4) {
		JDYG4 = jDYG4;
	}
	public String getJDYGZ() {
		return JDYGZ;
	}
	public void setJDYGZ(String jDYGZ) {
		JDYGZ = jDYGZ;
	}
	
	
}
