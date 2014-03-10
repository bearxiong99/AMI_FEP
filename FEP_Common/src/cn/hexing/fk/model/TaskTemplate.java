package cn.hexing.fk.model;

import java.util.ArrayList;
import java.util.List;

/**
 * �ն�����
 */
public class TaskTemplate {   
    /** ����ģ��ID */
    private String taskTemplateID;
    /** ��������: 01ר�䣬02���䣬03��ѹ,���ն���;�ֶα���һ��*/
    private String taskType;	
    /** ������ʼ��׼ʱ�� */
    private int sampleStartTime;
    /**��������  �ն��ᡢ�¶���.......*/
    private String taskProperty;
    /** ������ʼ��׼ʱ�䵥λ */
    private String sampleStartTimeUnit;
    /** �������ʱ�� */
    private int sampleInterval;
    /** �������ʱ�䵥λ */
    private String sampleIntervalUnit;
    /** ���ͻ�׼ʱ�� */
    private int uploadStartTime;
    /** ���ͻ�׼ʱ�䵥λ */
    private String uploadStartTimeUnit;
    /** ���ͼ��ʱ�� */
    private int uploadInterval;
    /** ���ͼ��ʱ�䵥λ */
    private String uploadIntervalUnit;
    /** �ϱ�����Ƶ�� */
    private int frequence;
    /**�������*/
    private int savepts;
    /**ִ�д���*/
    private int donums;			
    /** ����¼�����������ַ������ */
    private String dataCodesStr;
    /** ����¼����������б�[String] */
    private List<String> dataCodes=new ArrayList<String>();   
	/** �������(�ǳ�ʼ������) */
    private String tn;
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        
        return sb.toString();
    }
    
    /**
     * ������������
     * @param code �ϱ����������
     */
    public void addDataCode(String code) {  
        dataCodes.add(code);
    }
    
    /**
     * ȡ������¼�����������ַ������
     * @return
     */
    public String getDataCodesAsString() {
        if (dataCodesStr == null && dataCodes != null) {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < dataCodes.size(); i++) {
                if (sb.length() > 0) {
                    sb.append(",");
                }
                sb.append((String) dataCodes.get(i));
            }
        }
        return dataCodesStr;
    }
    
    
    /**
     * @return Returns the taskType.
     */
    public String getTaskType() {
        return taskType;
    }
    /**
     * @param taskType The taskType to set.
     */
    public void setTaskType(String taskType) {
        this.taskType = taskType;
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
     * @return Returns the sampleStartTime.
     */
    public int getSampleStartTime() {
        return sampleStartTime;
    }
    /**
     * @param sampleStartTime The sampleStartTime to set.
     */
    public void setSampleStartTime(int sampleStartTime) {
        this.sampleStartTime = sampleStartTime;
    }
    /**
     * @return Returns the sampleStartTimeUnit.
     */
    public String getSampleStartTimeUnit() {
        return sampleStartTimeUnit;
    }
    /**
     * @param sampleStartTimeUnit The sampleStartTimeUnit to set.
     */
    public void setSampleStartTimeUnit(String sampleStartTimeUnit) {
        this.sampleStartTimeUnit = sampleStartTimeUnit;
    }
    /**
     * @return Returns the sampleInterval.
     */
    public int getSampleInterval() {
        return sampleInterval;
    }
    /**
     * @param sampleInterval The sampleInterval to set.
     */
    public void setSampleInterval(int sampleInterval) {
        this.sampleInterval = sampleInterval;
    }
    /**
     * @return Returns the sampleIntervalUnit.
     */
    public String getSampleIntervalUnit() {
        return sampleIntervalUnit;
    }
    /**
     * @param sampleIntervalUnit The sampleIntervalUnit to set.
     */
    public void setSampleIntervalUnit(String sampleIntervalUnit) {
        this.sampleIntervalUnit = sampleIntervalUnit;
    }
    /**
     * @return Returns the uploadStartTime.
     */
    public int getUploadStartTime() {
        return uploadStartTime;
    }
    /**
     * @param uploadStartTime The uploadStartTime to set.
     */
    public void setUploadStartTime(int uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }
    /**
     * @return Returns the uploadStartTimeUnit.
     */
    public String getUploadStartTimeUnit() {
        return uploadStartTimeUnit;
    }
    /**
     * @param uploadStartTimeUnit The uploadStartTimeUnit to set.
     */
    public void setUploadStartTimeUnit(String uploadStartTimeUnit) {
        this.uploadStartTimeUnit = uploadStartTimeUnit;
    }
    /**
     * @return Returns the uploadInterval.
     */
    public int getUploadInterval() {
        return uploadInterval;
    }
    /**
     * @param uploadInterval The uploadInterval to set.
     */
    public void setUploadInterval(int uploadInterval) {
        this.uploadInterval = uploadInterval;
    }
    /**
     * @return Returns the uploadIntervalUnit.
     */
    public String getUploadIntervalUnit() {
        return uploadIntervalUnit;
    }
    /**
     * @param uploadIntervalUnit The uploadIntervalUnit to set.
     */
    public void setUploadIntervalUnit(String uploadIntervalUnit) {
        this.uploadIntervalUnit = uploadIntervalUnit;
    }
    /**
     * @return Returns the frequence.
     */
    public int getFrequence() {
        return frequence;
    }
    /**
     * @param frequence The frequence to set.
     */
    public void setFrequence(int frequence) {
        this.frequence = frequence;
    }
    
    /**
     * @return Returns the dataCodes.
     */
    public List<String> getDataCodes() {
        return dataCodes;
    }
    /**
     * @param dataCodes The dataCodes to set.
     */
    public void setDataCodes(List<String> dataCodes) {
        this.dataCodes = dataCodes;
        this.dataCodesStr = null;
    }
   

	

	public int getDonums() {
		return donums;
	}

	public int getSavepts() {
		return savepts;
	}

	
	public void setDonums(int donums) {
		this.donums = donums;
	}

	public void setSavepts(int savepts) {
		this.savepts = savepts;
	}
	/**
	 * @return ���� dataCodesStr��
	 */
	public String getDataCodesStr() {
		return dataCodesStr;
	}

	/**
	 * @param dataCodesStr Ҫ���õ� dataCodesStr��
	 */
	public void setDataCodesStr(String dataCodesStr) {
		this.dataCodesStr = dataCodesStr;
	}

	public String getTaskTemplateID() {
		return taskTemplateID;
	}

	public void setTaskTemplateID(String taskTemplateID) {
		this.taskTemplateID = taskTemplateID;
	}

	public final String getTaskProperty() {
		return taskProperty;
	}

	public final void setTaskProperty(String taskProperty) {
		this.taskProperty = taskProperty;
	}	

}
