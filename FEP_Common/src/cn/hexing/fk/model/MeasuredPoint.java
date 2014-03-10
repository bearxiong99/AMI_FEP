package cn.hexing.fk.model;

/**
 * ������
 */
public class MeasuredPoint {
    /** CT/PT ��ȱʡֵ */
    private static final String DEFAULT_CT_PT = "1/1";
    
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ���� */
    private String customerNo;
    /** ������� */
    private String tn;
    /** ������ֺ� */
    private String stationNo;
    /** CT */
    private String ct = DEFAULT_CT_PT;
    /** PT */
    private String pt = DEFAULT_CT_PT;
    /** ���ݱ���ID */
    private String dataSaveID;
    /** �������ַ������Ϊ�� */
    private String tnAddr;
    /**�����ݿ��Ȩ������ȡ��*/
    private String hiPassword;

    /**
     * ���ַ�����ʽ���� CT ֵ
     * @param ctStr CT ���ַ�����ʽ
     */
    public void setCtStr(String ctStr) {
        if (ctStr == null) {
            ct = DEFAULT_CT_PT;
        }
        else {
            try {
                ct = ctStr;
            }
            catch (Exception ex) {
                ct = DEFAULT_CT_PT;
            }
        }
    }
    
    /**
     * ���ַ�����ʽ���� PT ֵ
     * @param ptStr PT ���ַ�����ʽ
     */
    public void setPtStr(String ptStr) {
        if (ptStr == null) {
            pt = DEFAULT_CT_PT;
        }
        else {
            try {
                pt = ptStr;
            }
            catch (Exception ex) {
                pt = DEFAULT_CT_PT;
            }
        }
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[rtuId=").append(rtuId)
            .append(", tn=").append(tn)
            .append(", stationNo=").append(stationNo).append("]");
        return sb.toString();
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
	 * @return ���� customerNo��
	 */
	public String getCustomerNo() {
		return customerNo;
	}

	/**
	 * @param customerNo Ҫ���õ� customerNo��
	 */
	public void setCustomerNo(String customerNo) {
		this.customerNo = customerNo;
	}

	/**
	 * @return ���� dataSaveID��
	 */
	public String getDataSaveID() {
		return dataSaveID;
	}

	/**
	 * @param dataSaveID Ҫ���õ� dataSaveID��
	 */
	public void setDataSaveID(String dataSaveID) {
		this.dataSaveID = dataSaveID;
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
     * @return Returns the ct.
     */
    public String getCt() {
        return ct;
    }
    /**
     * @param ct The ct to set.
     */
    public void setCt(String ct) {
        this.ct = ct;
    }
    /**
     * @return Returns the pt.
     */
    public String getPt() {
        return pt;
    }
    /**
     * @param pt The pt to set.
     */
    public void setPt(String pt) {
        this.pt = pt;
    }

	public String getTnAddr() {
		return tnAddr;
	}

	public void setTnAddr(String tnAddr) {
		this.tnAddr = tnAddr;
	}

	public String getHiPassword() {
		return hiPassword;
	}

	public void setHiPassword(String hiPassword) {
		this.hiPassword = hiPassword;
	}
   
}
