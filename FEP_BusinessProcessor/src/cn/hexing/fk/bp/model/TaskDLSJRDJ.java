package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-17 下午5:08:12
 *
 * @info 电量数据日冻结
 */
public class TaskDLSJRDJ implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = -5719429424376884727L;
	/** 数据保存ID */
	private String SJID;
    /** 数据时间 */
    private Date SJSJ;
    /** 接收时间，浙规取系统时间，国网取抄表时间 */
    private Date JSSJ;
    /** CT */
    private int CT;
    /** PT */
    private int PT;	
    /** 综合倍率 */
    private int ZHBL;	
    /** 补全标志 */
    private int BQBJ;
    /** 9010 */
    private String ZXYGZ;
    /** 9011 */
    private String ZXYGZ1;
    /** 9012 */
    private String ZXYGZ2;
    /** 9013 */
    private String ZXYGZ3;	
    /** 9014 */
    private String ZXYGZ4;
    /**正向无功总*/
    private String ZXWGZ;
    /**正向无功总1*/
    private String ZXWGZ1;
    /**正向无功总2*/
    private String ZXWGZ2;
    /**正向无功总3*/
    private String ZXWGZ3;
    /**正向无功总4*/
    private String ZXWGZ4;
    /**反向有功总*/
    private String FXYGZ;
    /**反向无功总*/
    private String FXWGZ;
    
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
	public final int getCT() {
		return CT;
	}
	public final void setCT(int cT) {
		CT = cT;
	}
	public final int getPT() {
		return PT;
	}
	public final void setPT(int pT) {
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
	public final String getFXYGZ() {
		return FXYGZ;
	}
	public final void setFXYGZ(String fXYGZ) {
		FXYGZ = fXYGZ;
	}
	public final String getFXWGZ() {
		return FXWGZ;
	}
	public final void setFXWGZ(String fXWGZ) {
		FXWGZ = fXWGZ;
	}
}
