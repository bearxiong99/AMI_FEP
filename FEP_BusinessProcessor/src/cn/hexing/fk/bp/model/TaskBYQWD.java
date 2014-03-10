package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-30 上午11:27:56
 *
 * @info 任务变压器温度
 */
public class TaskBYQWD implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4034553288310775762L;
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
    
    private String WD;
    
    private String YL;

	public String getSJID() {
		return SJID;
	}

	public void setSJID(String sJID) {
		SJID = sJID;
	}

	public Date getSJSJ() {
		return SJSJ;
	}

	public void setSJSJ(Date sJSJ) {
		SJSJ = sJSJ;
	}

	public Date getJSSJ() {
		return JSSJ;
	}

	public void setJSSJ(Date jSSJ) {
		JSSJ = jSSJ;
	}

	public String getCT() {
		return CT;
	}

	public void setCT(String cT) {
		CT = cT;
	}

	public String getPT() {
		return PT;
	}

	public void setPT(String pT) {
		PT = pT;
	}

	public int getZHBL() {
		return ZHBL;
	}

	public void setZHBL(int zHBL) {
		ZHBL = zHBL;
	}

	public int getBQBJ() {
		return BQBJ;
	}

	public void setBQBJ(int bQBJ) {
		BQBJ = bQBJ;
	}

	public String getWD() {
		return WD;
	}

	public void setWD(String wD) {
		WD = wD;
	}

	public String getYL() {
		return YL;
	}

	public void setYL(String yL) {
		YL = yL;
	}
}
