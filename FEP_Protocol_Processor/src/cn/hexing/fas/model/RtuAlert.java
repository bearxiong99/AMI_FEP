package cn.hexing.fas.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import cn.hexing.util.HexDump;

/**
 * 终端告警
 */
public class RtuAlert {
    /** 告警数据ID */
    private String dataSaveID;
    /** 单位代码 */
    private String corpNo;
    /** 户号 */
    private String customerNo;
    /** 终端ID */
    private String rtuId;
    /** 测量点号 */
    private String tn;
    /** 测量点局号 */
    private String stationNo;
    /** 告警编码 */
    private int alertCode;
    /** 告警编码（十六进制字符串） */
    private String alertCodeHex;
    /**表计告警编码*/
    private String innerCode;
    /**数据项*/
    private String codeItem;
    /** 告警发生时间 */
    private Date alertTime;
    /** 告警接收时间 */
    private Date receiveTime;
    /** 告警携带信息 */
    private String alertInfo="";
    /** 告警参数[RtuAlertArg] */
    private List args;
    /** 告警附加数据*/
    private String sbcs;
    /** 通讯方式*/
    private String txfs;
    /**1主动上送，2主站召测*/
    private String gjly="1";
    
    /**
     * 添加告警参数
     * @param arg 告警参数
     */
    public void addAlertArg(RtuAlertArg arg) {
        if (args == null) {
            args = new ArrayList();
        }
        args.add(arg);
    }
    
    /**
     * 返回十六进制表示的告警编码
     * @return 告警编码的十六进制表示
     */
    public String getAlertCodeHex() {
        if (alertCodeHex == null) {
            alertCodeHex = HexDump.toHex((short) alertCode);
        }
        return alertCodeHex;
    }
    
   
    public String getDataSaveID() {
		return dataSaveID;
	}

	public void setDataSaveID(String dataSaveID) {
		this.dataSaveID = dataSaveID;
	}

	/**
     * @return Returns the corpNo.
     */
    public String getCorpNo() {
        return corpNo;
    }
    /**
     * @param corpNo The corpNo to set.
     */
    public void setCorpNo(String corpNo) {
        this.corpNo = corpNo;
    }
    /**
     * @return Returns the customerNo.
     */
    public String getCustomerNo() {
        return customerNo;
    }
    /**
     * @param customerNo The customerNo to set.
     */
    public void setCustomerNo(String customerNo) {
        this.customerNo = customerNo;
    }
    /**
     * @return Returns the rtuId.
     */
    public String getRtuId() {
        return rtuId;
    }
    /**
     * @param rtuId The rtuId to set.
     */
    public void setRtuId(String rtuId) {
        this.rtuId = rtuId;
    }
    /**
     * @return Returns the tn.
     */
    public String getTn() {
        return tn;
    }
    /**
     * @param tn The tn to set.
     */
    public void setTn(String tn) {
        this.tn = tn;
    }
    /**
     * @return Returns the stationNo.
     */
    public String getStationNo() {
        return stationNo;
    }
    /**
     * @param stationNo The stationNo to set.
     */
    public void setStationNo(String stationNo) {
        this.stationNo = stationNo;
    }
    /**
     * @return Returns the alertCode.
     */
    public int getAlertCode() {
        return alertCode;
    }
    /**
     * @param alertCode The alertCode to set.
     */
    public void setAlertCode(int alertCode) {
        this.alertCode = alertCode;
    }
    /**
     * @param alertCodeHex The alertCodeHex to set.
     */
    public void setAlertCodeHex(String alertCodeHex) {
        this.alertCodeHex = alertCodeHex;
    }
    /**
     * @return Returns the alertTime.
     */
    public Date getAlertTime() {
        return alertTime;
    }
    /**
     * @param alertTime The alertTime to set.
     */
    public void setAlertTime(Date alertTime) {
        this.alertTime = alertTime;
    }
    public void setAlertTime(String alertTime) {
    	try{
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    		this.alertTime = df.parse(alertTime);
    	}
    	catch(Exception e){		
    	}
    }
    public void setAlertFullTime(String alertTime){

    	try{
    		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    		this.alertTime = df.parse(alertTime);
    	}
    	catch(Exception e){		
    	}
    }
    
    /**
     * @return Returns the receiveTime.
     */
    public Date getReceiveTime() {
        return receiveTime;
    }
    /**
     * @param receiveTime The receiveTime to set.
     */
    public void setReceiveTime(Date receiveTime) {
        this.receiveTime = receiveTime;
    }
    /**
     * @return Returns the args.
     */
    public List getArgs() {
        return args;
    }
    /**
     * @param args The args to set.
     */
    public void setArgs(List args) {
        this.args = args;
    }

	public String getSbcs() {
		return sbcs;
	}

	public void setSbcs(String sbcs) {
		this.sbcs = sbcs;
	}
    
    
	/**
	 * @return 返回 txfs。
	 */
	public String getTxfs() {
		return txfs;
	}
	/**
	 * @param txfs 要设置的 txfs。
	 */
	public void setTxfs(String txfs) {
		this.txfs = txfs;
	}

	public String getAlertInfo() {
		return alertInfo;
	}

	public void setAlertInfo(String alertInfo) {
		this.alertInfo = alertInfo;
	}

	public final String getCodeItem() {
		return codeItem;
	}

	public final void setCodeItem(String codeItem) {
		this.codeItem = codeItem;
	}

	public final String getInnerCode() {
		return innerCode;
	}

	public final void setInnerCode(String innerCode) {
		this.innerCode = innerCode;
	}

	public final String getGjly() {
		return gjly;
	}

	public final void setGjly(String gjly) {
		this.gjly = gjly;
	}

	
	
}
