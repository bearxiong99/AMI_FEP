package cn.hexing.fk.bp.model;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @author gaoll
 *
 * @time 2013-5-30 ����11:27:56
 *
 * @info �����ѹ���¶�
 */
public class TaskBYQWD implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 4034553288310775762L;
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
