package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;
/**
 * 
 * @author gaoll
 *
 * @time 2013-1-10 ����4:50:19
 *
 * @info Ԥ���������ն���
 */
public class TaskYFFSJ implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 504157303437629317L;
	/** ���ݱ���ID */
	private String SJID;
    /** ����ʱ�� */
    private Date SJSJ;
    /** ����ʱ�䣬���ȡϵͳʱ�䣬����ȡ����ʱ�� */
    private Date JSSJ;
    /** CT */
    private String CT;
    /** PT */
    private String PT;	
    /** �ۺϱ��� */
    private int ZHBL;	
    /** ��ȫ��־ */
    private int BQBJ;
    
    private String YE;
    
    private String ZJDQZT;
    
    private String ZJDQCZYY;
    
    private String ZJDQMS;
    
    private String DBMS;
    
    private String JSDYEZT;
    
    private String LJYQL;
    
    private String ZXYGZ;

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

	public final String getYE() {
		return YE;
	}

	public final void setYE(String yE) {
		YE = yE;
	}

	public final String getZJDQZT() {
		return ZJDQZT;
	}

	public final void setZJDQZT(String zJDQZT) {
		ZJDQZT = zJDQZT;
	}

	public final String getZJDQCZYY() {
		return ZJDQCZYY;
	}

	public final void setZJDQCZYY(String zJDQCZYY) {
		ZJDQCZYY = zJDQCZYY;
	}

	public final String getZJDQMS() {
		return ZJDQMS;
	}

	public final void setZJDQMS(String zJDQMS) {
		ZJDQMS = zJDQMS;
	}

	public final String getDBMS() {
		return DBMS;
	}

	public final void setDBMS(String dBMS) {
		DBMS = dBMS;
	}

	public final String getJSDYEZT() {
		return JSDYEZT;
	}

	public final void setJSDYEZT(String jSDYEZT) {
		JSDYEZT = jSDYEZT;
	}

	public final String getLJYQL() {
		return LJYQL;
	}

	public final void setLJYQL(String lJYQL) {
		LJYQL = lJYQL;
	}

	public final String getZXYGZ() {
		return ZXYGZ;
	}

	public final void setZXYGZ(String zXYGZ) {
		ZXYGZ = zXYGZ;
	}
}
