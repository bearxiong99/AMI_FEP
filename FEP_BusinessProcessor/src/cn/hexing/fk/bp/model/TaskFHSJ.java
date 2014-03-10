package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 数据库映射表:负荷数据类
 */

public class TaskFHSJ implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 5647847203900351266L;
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
    /** B611 */
    private String AXDYXWJ         ;
    private String BXDYXWJ         ;
    private String CXDYXWJ         ;
    private String AXDLXWJ         ;
    private String BXDLXWJ         ;
    private String CXDLXWJ         ;
    private String NBWD            ;
    private String AXSZGL          ;
    private String BXSZGL          ;
    private String CXSZGL          ;
    private String DCDY            ;
    private String SYXLZQXL        ;
    private String DQXLZQSYSJ      ;
    private String YPJGLYS         ;
    private String YPJGLYS1        ;
    private String YPJGLYS2        ;
    private String YPJGLYS3        ;
    private String YPJGLYS4        ;
    private String DQYZDXL         ;
    private String DQYZDXL1        ;
    private String DQYZDXL2        ;
    private String DQYZDXL3        ;
    private String DQYZDXL4        ;
    private String AXDY            ;
    private String BXDY            ;
    private String CXDY            ;
    private String AXDL            ;
    private String BXDL            ;
    private String CXDL            ;
    private String SZGL            ;
    private String GLYS            ;
    private String SSYG            ;
    private String SSWG            ;
    private String FXYGGL          ;
    private String FXWGGL          ;
    private String ZXYGZ           ;
    private String ZXWGZ           ;
    private String FXYGZ           ;
    private String FXWGZ           ;
    private String WGZ1            ;
    private String WGZ2            ;
    private String WGZ3            ;
    private String WGZ4            ;
    private String AXGLYS          ;
    private String BXGLYS          ;
    private String CXGLYS          ;
    private String AXSSYG          ;
    private String BXSSYG          ;
    private String CXSSYG          ;
    private String AXSSWG          ;
    private String BXSSWG          ;
    private String CXSSWG          ;
    private String AXZXYGZ         ;
    private String BXZXYGZ         ;
    private String CXZXYGZ         ;
    private String AXZXWGZ         ;
    private String BXZXWGZ         ;
    private String CXZXWGZ         ;
    private String DWPL            ;

    
	public String getAXDL() {
		return AXDL;
	}
	public void setAXDL(String axdl) {
		AXDL = axdl;
	}
	public String getAXDY() {
		return AXDY;
	}
	public void setAXDY(String axdy) {
		AXDY = axdy;
	}
	public int getBQBJ() {
		return BQBJ;
	}
	public void setBQBJ(int bqbj) {
		BQBJ = bqbj;
	}
	public String getBXDL() {
		return BXDL;
	}
	public void setBXDL(String bxdl) {
		BXDL = bxdl;
	}
	public String getBXDY() {
		return BXDY;
	}
	public void setBXDY(String bxdy) {
		BXDY = bxdy;
	}
	public String getCT() {
		return CT;
	}
	public void setCT(String ct) {
		CT = ct;
	}
	public String getCXDL() {
		return CXDL;
	}
	public void setCXDL(String cxdl) {
		CXDL = cxdl;
	}
	public String getCXDY() {
		return CXDY;
	}
	public void setCXDY(String cxdy) {
		CXDY = cxdy;
	}
	
	public String getGLYS() {
		return GLYS;
	}
	public void setGLYS(String glys) {
		GLYS = glys;
	}
	
	public String getPT() {
		return PT;
	}
	public void setPT(String pt) {
		PT = pt;
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
	
	public String getSZGL() {
		return SZGL;
	}
	public void setSZGL(String szgl) {
		SZGL = szgl;
	}
	
	
	public Date getJSSJ() {
		return JSSJ;
	}
	public void setJSSJ(Date jssj) {
		JSSJ = jssj;
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
	public int getZHBL() {
		return ZHBL;
	}
	public void setZHBL(int zhbl) {
		ZHBL = zhbl;
	}
	public String getSSYG() {
		return SSYG;
	}
	public void setSSYG(String ssyg) {
		SSYG = ssyg;
	}
	public String getSSWG() {
		return SSWG;
	}
	public void setSSWG(String sswg) {
		SSWG = sswg;
	}
	public String getZXYGZ() {
		return ZXYGZ;
	}
	public void setZXYGZ(String zxygz) {
		ZXYGZ = zxygz;
	}
	public String getWGZ1() {
		return WGZ1;
	}
	public void setWGZ1(String wgz1) {
		WGZ1 = wgz1;
	}
	public String getWGZ4() {
		return WGZ4;
	}
	public void setWGZ4(String wgz4) {
		WGZ4 = wgz4;
	}
	public final String getZXWGZ() {
		return ZXWGZ;
	}
	public final void setZXWGZ(String zXWGZ) {
		ZXWGZ = zXWGZ;
	}
	public final String getAXGLYS() {
		return AXGLYS;
	}
	public final void setAXGLYS(String aXGLYS) {
		AXGLYS = aXGLYS;
	}
	public final String getBXGLYS() {
		return BXGLYS;
	}
	public final void setBXGLYS(String bXGLYS) {
		BXGLYS = bXGLYS;
	}
	public final String getCXGLYS() {
		return CXGLYS;
	}
	public final void setCXGLYS(String cXGLYS) {
		CXGLYS = cXGLYS;
	}
	public final String getAXSSYG() {
		return AXSSYG;
	}
	public final void setAXSSYG(String aXSSYG) {
		AXSSYG = aXSSYG;
	}
	public final String getBXSSYG() {
		return BXSSYG;
	}
	public final void setBXSSYG(String bXSSYG) {
		BXSSYG = bXSSYG;
	}
	public final String getCXSSYG() {
		return CXSSYG;
	}
	public final void setCXSSYG(String cXSSYG) {
		CXSSYG = cXSSYG;
	}
	public final String getAXSSWG() {
		return AXSSWG;
	}
	public final void setAXSSWG(String aXSSWG) {
		AXSSWG = aXSSWG;
	}
	public final String getBXSSWG() {
		return BXSSWG;
	}
	public final void setBXSSWG(String bXSSWG) {
		BXSSWG = bXSSWG;
	}
	public final String getCXSSWG() {
		return CXSSWG;
	}
	public final void setCXSSWG(String cXSSWG) {
		CXSSWG = cXSSWG;
	}
	public final String getWGZ2() {
		return WGZ2;
	}
	public final void setWGZ2(String wGZ2) {
		WGZ2 = wGZ2;
	}
	public final String getWGZ3() {
		return WGZ3;
	}
	public final void setWGZ3(String wGZ3) {
		WGZ3 = wGZ3;
	}
	public String getAXZXYGZ() {
		return AXZXYGZ;
	}
	public void setAXZXYGZ(String aXZXYGZ) {
		AXZXYGZ = aXZXYGZ;
	}
	public String getBXZXYGZ() {
		return BXZXYGZ;
	}
	public void setBXZXYGZ(String bXZXYGZ) {
		BXZXYGZ = bXZXYGZ;
	}
	public String getCXZXYGZ() {
		return CXZXYGZ;
	}
	public void setCXZXYGZ(String cXZXYGZ) {
		CXZXYGZ = cXZXYGZ;
	}
	public String getAXZXWGZ() {
		return AXZXWGZ;
	}
	public void setAXZXWGZ(String aXZXWGZ) {
		AXZXWGZ = aXZXWGZ;
	}
	public String getBXZXWGZ() {
		return BXZXWGZ;
	}
	public void setBXZXWGZ(String bXZXWGZ) {
		BXZXWGZ = bXZXWGZ;
	}
	public String getCXZXWGZ() {
		return CXZXWGZ;
	}
	public void setCXZXWGZ(String cXZXWGZ) {
		CXZXWGZ = cXZXWGZ;
	}
	public String getFXYGGL() {
		return FXYGGL;
	}
	public void setFXYGGL(String fXYGGL) {
		FXYGGL = fXYGGL;
	}
	public String getFXWGGL() {
		return FXWGGL;
	}
	public void setFXWGGL(String fXWGGL) {
		FXWGGL = fXWGGL;
	}
	public String getFXYGZ() {
		return FXYGZ;
	}
	public void setFXYGZ(String fXYGZ) {
		FXYGZ = fXYGZ;
	}
	public String getFXWGZ() {
		return FXWGZ;
	}
	public void setFXWGZ(String fXWGZ) {
		FXWGZ = fXWGZ;
	}
	public String getDWPL() {
		return DWPL;
	}
	public void setDWPL(String dWPL) {
		DWPL = dWPL;
	}
	public String getAXDYXWJ() {
		return AXDYXWJ;
	}
	public void setAXDYXWJ(String aXDYXWJ) {
		AXDYXWJ = aXDYXWJ;
	}
	public String getBXDYXWJ() {
		return BXDYXWJ;
	}
	public void setBXDYXWJ(String bXDYXWJ) {
		BXDYXWJ = bXDYXWJ;
	}
	public String getCXDYXWJ() {
		return CXDYXWJ;
	}
	public void setCXDYXWJ(String cXDYXWJ) {
		CXDYXWJ = cXDYXWJ;
	}
	public String getAXDLXWJ() {
		return AXDLXWJ;
	}
	public void setAXDLXWJ(String aXDLXWJ) {
		AXDLXWJ = aXDLXWJ;
	}
	public String getBXDLXWJ() {
		return BXDLXWJ;
	}
	public void setBXDLXWJ(String bXDLXWJ) {
		BXDLXWJ = bXDLXWJ;
	}
	public String getCXDLXWJ() {
		return CXDLXWJ;
	}
	public void setCXDLXWJ(String cXDLXWJ) {
		CXDLXWJ = cXDLXWJ;
	}
	public String getNBWD() {
		return NBWD;
	}
	public void setNBWD(String nBWD) {
		NBWD = nBWD;
	}
	public String getAXSZGL() {
		return AXSZGL;
	}
	public void setAXSZGL(String aXSZGL) {
		AXSZGL = aXSZGL;
	}
	public String getBXSZGL() {
		return BXSZGL;
	}
	public void setBXSZGL(String bXSZGL) {
		BXSZGL = bXSZGL;
	}
	public String getCXSZGL() {
		return CXSZGL;
	}
	public void setCXSZGL(String cXSZGL) {
		CXSZGL = cXSZGL;
	}
	public String getDCDY() {
		return DCDY;
	}
	public void setDCDY(String dCDY) {
		DCDY = dCDY;
	}
	public String getSYXLZQXL() {
		return SYXLZQXL;
	}
	public void setSYXLZQXL(String sYXLZQXL) {
		SYXLZQXL = sYXLZQXL;
	}
	public String getDQXLZQSYSJ() {
		return DQXLZQSYSJ;
	}
	public void setDQXLZQSYSJ(String dQXLZQSYSJ) {
		DQXLZQSYSJ = dQXLZQSYSJ;
	}
	public String getYPJGLYS() {
		return YPJGLYS;
	}
	public void setYPJGLYS(String yPJGLYS) {
		YPJGLYS = yPJGLYS;
	}
	public String getYPJGLYS1() {
		return YPJGLYS1;
	}
	public void setYPJGLYS1(String yPJGLYS1) {
		YPJGLYS1 = yPJGLYS1;
	}
	public String getYPJGLYS2() {
		return YPJGLYS2;
	}
	public void setYPJGLYS2(String yPJGLYS2) {
		YPJGLYS2 = yPJGLYS2;
	}
	public String getYPJGLYS3() {
		return YPJGLYS3;
	}
	public void setYPJGLYS3(String yPJGLYS3) {
		YPJGLYS3 = yPJGLYS3;
	}
	public String getYPJGLYS4() {
		return YPJGLYS4;
	}
	public void setYPJGLYS4(String yPJGLYS4) {
		YPJGLYS4 = yPJGLYS4;
	}
	public String getDQYZDXL() {
		return DQYZDXL;
	}
	public void setDQYZDXL(String dQYZDXL) {
		DQYZDXL = dQYZDXL;
	}
	public String getDQYZDXL1() {
		return DQYZDXL1;
	}
	public void setDQYZDXL1(String dQYZDXL1) {
		DQYZDXL1 = dQYZDXL1;
	}
	public String getDQYZDXL2() {
		return DQYZDXL2;
	}
	public void setDQYZDXL2(String dQYZDXL2) {
		DQYZDXL2 = dQYZDXL2;
	}
	public String getDQYZDXL3() {
		return DQYZDXL3;
	}
	public void setDQYZDXL3(String dQYZDXL3) {
		DQYZDXL3 = dQYZDXL3;
	}
	public String getDQYZDXL4() {
		return DQYZDXL4;
	}
	public void setDQYZDXL4(String dQYZDXL4) {
		DQYZDXL4 = dQYZDXL4;
	}
	
	
	
}
