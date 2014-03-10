package cn.hexing.fk.model;


import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import cn.hexing.fk.message.gw.MessageGw;

/**
 * ҵ�������ն˵����ṹ
 */
public class BizRtu {   
    /** �ն˾ֺ�ID */
    private String rtuId;
    /** ��λ���� */
    private String deptCode;
    /** �ն˹�Լ���� */
    private String rtuProtocol;
    /** �ն���;��01ר�䣬02���䣬03��ѹ */
    private String rtuType;
    /** �ն����ͣ�01ר���ն� 02�����ն� 03������ 04���߲ɼ��� */
    private String rtuClass;
    /** �ն��߼���ַ */
    private int rtua;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** ���̣���ţ� */
    private String manufacturer;
    /** ��Ȩ������ */
    private String hiAuthPassword;
    /** ��Ȩ������ */
    private String loAuthPassword;  
    /**�Գ���Կ�汾*/
    private int symmetricKeyVersion;
    /**�ǶԳ���Կ�汾*/
    private int asymmetricKeyVersion;
    /**����������Կ*/
    private String pubKey;
    /**ͨ�ŷ�ʽ*/
    private String linkMode;
    
    /** �������б� */
    private Map<String,MeasuredPoint> measuredPoints=new HashMap<String,MeasuredPoint>();
    /** �ն������б� */
    private Map<Integer,RtuTask> tasksMap=new HashMap<Integer,RtuTask>();

    /** ����ʱ���б� */
    private Map<Integer,Object> paramMap=new HashMap<Integer,Object>();
    
    /** �����ļ������б� */
    private Map<Integer,MessageGw> fileMessageMap=new HashMap<Integer,MessageGw>();
    /** �����ļ�֡�����б�*/
    private Map<Integer,Integer> messageCountMap=new HashMap<Integer,Integer>();
    /** �����ļ�֡��ǰ���б�*/
    private Map<Integer,Integer> currentMessageCountMap=new HashMap<Integer,Integer>();
    /** �����ļ������б�*/
    private Map<Integer,Object>requestMap=new HashMap<Integer,Object>();
    
    //�ݴ�������Ϣ
    private Map<String,Object> upgradeParams = new HashMap<String,Object>();
    
    private Date lastRefreshTime;
    
    //��֡��ʱ��Ҫ�õ�
    private int firstFrameSeq; 
    
    public String getRtuType() {
		return rtuType;
	}

	public void setRtuType(String rtuType) {
		this.rtuType = rtuType;
	}
	public String getAnyTaskNum(){
    	String taskNum="";
		for(RtuTask task:tasksMap.values()){
    		taskNum=""+task.getRtuTaskNum();
    		break;
    	}				
		return taskNum;
	}
	/**
     * ���ݲ������ȡ�ò�����
     * @param tn �������
     * @return �����㡣��������ڣ��򷵻� null
     */
    public MeasuredPoint getMeasuredPoint(String tn) {
    	return (MeasuredPoint) measuredPoints.get(tn);
    }
    /**
     * ���ݱ��ַȡ�ò�����
     * @param tnAddr �������ַ
     * @return �����㡣��������ڣ��򷵻� null
     */
    public MeasuredPoint getMeasuredPointByTnAddr(String tnAddr) {
    	try{
    		if (measuredPoints!=null){
        		Iterator<Map.Entry<String,MeasuredPoint>> it=measuredPoints.entrySet().iterator();
        		while(it.hasNext()){
        			Map.Entry<String,MeasuredPoint> entry=it.next();
        			MeasuredPoint mp=(MeasuredPoint)entry.getValue();
        			String addr=mp.getTnAddr();
        			String stationNo = mp.getStationNo();
        			addr=strStuff("0",12,addr,"left");
        			stationNo=strStuff("0",12,stationNo,"left");
        			tnAddr=strStuff("0",12,tnAddr,"left");
        			if(addr.equals(tnAddr) || stationNo.equals(tnAddr))
        				return mp;
        		}
            	return (MeasuredPoint) measuredPoints.get(tnAddr);
        	}
        	else
        		return null;
    	}catch(Exception ex){
    		return null;
    	}
    	
    }
    /**
     * ��Ӳ�����
     * @param mp ������
     */
    public void addMeasuredPoint(MeasuredPoint mp) {
        measuredPoints.put(mp.getTn(),mp);                
    }
    /**
     * ����ն�����
     * @param rt �ն�����
     */
    public void addRtuTask(RtuTask rt) {
    	tasksMap.put(new Integer(rt.getRtuTaskNum()), rt);            
    }     
    
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[id=").append(rtuId)
            .append(", logicAddress=").append(logicAddress)
            .append(", protocol=").append(rtuProtocol)
            .append(", manufacturer=").append(manufacturer).append(", ... ]");
        return sb.toString();
    }
    public RtuTask getRtuTask(String taskNum) {   
    	if (tasksMap == null || taskNum == null) {
            return null;
        }
    	return (RtuTask)tasksMap.get(new Integer(taskNum)); 
    }
    /**
     * ��Ӳ���
     * @param ifseq ֡���
     * @param ob   ����
     */
    public void addParamToMap(int ifseq, Object ob) {
    	paramMap.put(ifseq, ob);            
    }     
    
    public Object removeParamFromMap(int ifseq){
    	return paramMap.remove(ifseq);
    }

    /**
     * ��ò���
     * @param ifseq ֡���
     * @return
     */
    public Object getParamFromMap(int ifseq) {   
    	return (Object)paramMap.get(ifseq); 
    }
    /**
     * ��Ӳ���
     * @param key 
     * @param ob   ����
     */
    public void addRequestToMap(int key, Object ob) {
    	requestMap.put(key, ob);            
    }     
    
    public Object removeRequestFromMap(int key){
    	return requestMap.remove(key);
    }

    /**
     * ��ò���
     * @param key 
     * @return
     */
    public Object getRequestFromMap(int key) {   
    	return (Object)requestMap.get(key); 
    }

    /**
     * ��Ӳ���
     * @param   cout ��Ϣ���
     * @param message   ��Ϣ
     */
    public void addParamToFileMap(int cout, MessageGw message) {
    	fileMessageMap.put(cout, message);            
    }     
    
    public Object removeParamFromFileMap(int cout){
    	return fileMessageMap.remove(cout);
    }
    public void removeAllFromFileMap(){
    	 fileMessageMap.clear();
    }
    
    /**
     * ��ò���
     * @param cout ��Ϣ���
     * @return
     */
    public Object getParamFromFileMap(int cout) {   
    	return (MessageGw)fileMessageMap.get(cout); 
    }
    /**
     * ��Ӳ���
     * @param   key
     * @param messagecount   ��Ϣ����
     */
    public void addParamToMessageCountMap(int key, int count) {
    	messageCountMap.put(key, count);            
    }     
    
    public Object removeParamFromMessageCountMap(int key){
    	return messageCountMap.remove(key);
    }

    /**
     * ��ò���
     * @param key 
     * @return
     */
    public int getParamFromMessageCountMap(int key) {   
    	return messageCountMap.get(key); 
    }
    
    /**
     * ��Ӳ���
     * @param   key
     * @param messagecount   ��Ϣ����
     */
    public void addParamToCurrentMessageCountMap(int key, int count) {
    	currentMessageCountMap.put(key, count);            
    }     
    
    public Object removeParamFromCurrentMessageCountMap(int key){
    	return currentMessageCountMap.remove(key);
    }

    /**
     * ��ò���
     * @param key 
     * @return
     */
    public int getParamFromCurrentMessageCountMap(int key) {   
    	return currentMessageCountMap.get(key); 
    }
    /**
     * ���������ȡ���ն����񼰱���������Ϣ
     * @param taskNum �����
     * @return �ն��������û�ж�Ӧ�������򷵻� null
     */
    public TaskTemplate getTaskTemplate(String taskNum) {
        if (tasksMap == null || taskNum == null) {
            return null;
        }
        RtuTask rt=(RtuTask)tasksMap.get(new Integer(taskNum)); 
        if(rt !=null){
        	return RtuManage.getInstance().getTaskPlateInCache(rt.getTaskTemplateID());
        	/*MeasuredPoint mp=getMeasuredPoint(rt.getTn());
        	if (mp!=null){
        		tp.setDeptCode(deptCode);        		
        		tp.setTn(mp.getTn());
            	tp.setDataSaveID(mp.getDataSaveID());
            	tp.setCt(mp.getCt());
            	tp.setPt(mp.getPt());
        	}  */      
        	//return tp;
        }                
        return null;
    }
    /**
     * ���ݽ����õ����������б���ն�ģ����������ȫƥ��õ������
     * @param dataCodes �������б�
     * @return ��Ӧ���ն�����ţ����򷵻� null
     */
    @SuppressWarnings("unchecked")
	public String getTaskNum(List<String> dataCodes,String rtuType){
    	String taskNum=null,codeStr="";
    	for (int i=0;i<dataCodes.size();i++){
    		if (codeStr.indexOf(dataCodes.get(i))<0)//�����ظ�������
    			codeStr=codeStr+dataCodes.get(i)+",";
    	}
    	Iterator<?> it=tasksMap.entrySet().iterator();
		while(it.hasNext()){
			int icount=0;
			Map.Entry<Integer,RtuTask> entry=(Map.Entry<Integer,RtuTask>)it.next();
			RtuTask rt=(RtuTask)entry.getValue();
			TaskTemplate ttp=RtuManage.getInstance().getTaskPlateInCache(rt.getTaskTemplateID());
			//if (!rtuType.equals(ttp.getTaskType()))//�ն���;����������Ҫһ�²�ƥ��
				//continue;
			List<String> codes=ttp.getDataCodes();
			//ģ��������
			//����������������
			//���ģ�������������,��������������������
//			if (dataCodes.size()>=codes.size()){
				for (int i=0;i<codes.size();i++){
					//����ģ��������������ڽ�������������б����ҵ�����ȷ���Ƿ������
					if(codeStr.contains(codes.get(i)) && !"0400122000".equals(codes.get(i))){
						//�����������������ݶ���0400122000,���ܶ����񶼲�������ֶΣ�ֻ�г�����������ģ����������ֶΣ����Գ���ʱ�䲻���ж�֮��
						icount++;
						break;
					}
//					if (codeStr.indexOf(codes.get(i))<0)
//						break;
//					else 
//						icount++;
				}
				if(icount>0){
					if (taskNum == null) {
						taskNum = "" + entry.getKey();
					} else {
						taskNum += "," + entry.getKey();
					}
				}
//				if (codes.size()==icount){
//					if(taskNum==null){
//						taskNum=""+entry.getKey();
//					}else{
//						taskNum+=","+entry.getKey();						
//					}
//				}
//			}
		}
    	return taskNum;
    	
    }
    
    /**
     * @return Returns the id.
     */
    public String getRtuId() {
        return rtuId;
    }
    /**
     * @param id The id to set.
     */
    public void setRtuId(String rtuId) {
        this.rtuId = rtuId;
    }
      
    /**
	 * @return ���� rtuProtocol��
	 */
	public String getRtuProtocol() {
		return rtuProtocol;
	}

	/**
	 * @param rtuProtocol Ҫ���õ� rtuProtocol��
	 */
	public void setRtuProtocol(String rtuProtocol) {
		this.rtuProtocol = rtuProtocol;
	}

	/**
     * @return Returns the rtua.
     */
    public int getRtua() {
        return rtua;
    }
    /**
     * @param rtua The rtua to set.
     */
    public void setRtua(int rtua) {
        this.rtua = rtua;
    }
    /**
     * @return Returns the logicAddress.
     */
    public String getLogicAddress() {
        return logicAddress;
    }
    /**
     * @param logicAddress The logicAddress to set.
     */
    public void setLogicAddress(String logicAddress) {
        this.logicAddress = logicAddress;
    }
    
    
    /**
     * @return Returns the manufacturer.
     */
    public String getManufacturer() {
        return manufacturer;
    }
    /**
     * @param manufacturer The manufacturer to set.
     */
    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }
    
    /**
     * @return Returns the hiAuthPassword.
     */
    public String getHiAuthPassword() {
        return hiAuthPassword;
    }
    /**
     * @param hiAuthPassword The hiAuthPassword to set.
     */
    public void setHiAuthPassword(String hiAuthPassword) {
        this.hiAuthPassword = hiAuthPassword;
    }
    /**
     * @return Returns the loAuthPassword.
     */
    public String getLoAuthPassword() {
        return loAuthPassword;
    }
    /**
     * @param loAuthPassword The loAuthPassword to set.
     */
    public void setLoAuthPassword(String loAuthPassword) {
        this.loAuthPassword = loAuthPassword;
    }
        
	public String getDeptCode() {
		return deptCode;
	}
	public void setDeptCode(String deptCode) {
		this.deptCode = deptCode;
	}

	public String getRtuClass() {
		return rtuClass;
	}

	public void setRtuClass(String rtuClass) {
		this.rtuClass = rtuClass;
	}
	
	//�ַ�����ƥ�亯��:�����ַ���Ҫ�󳤶ȣ������ַ��������㷽��
	public String strStuff(String str,int iLen,String sInput,String sSign){
		String sOutput="";
		try{
		    int iLenStr=sInput.length();
		    if (iLen>iLenStr){//�����ַ���Ҫ����
		    for (int i = 0; i < (iLen-iLenStr); i++){
			    if (sSign.equals("left")){//����
			      sInput=str+sInput;
			    }
			    else {//�Ҳ���
			      sInput=sInput+str;
			    }
			}
		}
		else if (iLen<iLenStr){//�����ַ�������Ҫ��ȥ
		  if (sSign.equals("left")){//��ȥ��
		    sInput = sInput.substring(iLenStr-iLen,iLenStr);
		  }
		  else {//��ȥ�Ҳ�
			    sInput = sInput.substring(0,iLen);
			  }
			}
			sOutput=sInput;
		}
		catch(Exception e){
		}
		return sOutput;
	}

	public final int getSymmetricKeyVersion() {
		return symmetricKeyVersion;
	}

	public final void setSymmetricKeyVersion(int symmetricKeyVersion) {
		this.symmetricKeyVersion = symmetricKeyVersion;
	}

	public final int getAsymmetricKeyVersion() {
		return asymmetricKeyVersion;
	}

	public final void setAsymmetricKeyVersion(int asymmetricKeyVersion) {
		this.asymmetricKeyVersion = asymmetricKeyVersion;
	}

	public final String getPubKey() {
		return pubKey;
	}

	public final void setPubKey(String pubKey) {
		this.pubKey = pubKey;
	}

	public final Map<String, MeasuredPoint> getMeasuredPoints() {
		return measuredPoints;
	}

	public final void setMeasuredPoints(Map<String, MeasuredPoint> measuredPoints) {
		this.measuredPoints = measuredPoints;
	}

	public final Map<Integer, RtuTask> getTasksMap() {
		return tasksMap;
	}

	public final void setTasksMap(Map<Integer, RtuTask> tasksMap) {
		this.tasksMap = tasksMap;
	}

	public final void setRequestMap(Map<Integer, Object> paramMap) {
		this.requestMap = paramMap;
	}

	public final Map<Integer, Object> getRequestMap() {
		return requestMap;
	}

	public final void setParamMap(Map<Integer, Object> paramMap) {
		this.paramMap = paramMap;
	}

	public final Map<Integer, Object> getParamMap() {
		return paramMap;
	}
	public final Map<Integer, MessageGw> getFilemessageMap() {
		return fileMessageMap;
	}

	public final void setFilemessageMap(Map<Integer, MessageGw> paramMap) {
		this.fileMessageMap = paramMap;
	}

	public final Date getLastRefreshTime() {
		return lastRefreshTime;
	}

	public final void setLastRefreshTime(Date lastRefreshTime) {
		this.lastRefreshTime = lastRefreshTime;
	}
	public final Map<Integer, Integer> getMessageCountMap() {
		return messageCountMap;
	}

	public final void setMessageCountMap(Map<Integer, Integer> paramMap) {
		this.messageCountMap = paramMap;
	}

	public final Map<Integer, Integer> getCurrentMessageCountMap() {
		return currentMessageCountMap;
	}

	public final void setCurrentMessageCountMap(Map<Integer, Integer> paramMap) {
		this.currentMessageCountMap = paramMap;
	}
	public int getFirstFrameSeq() {
		return firstFrameSeq;
	}

	public void setFirstFrameSeq(int firstFrameSeq) {
		this.firstFrameSeq = firstFrameSeq;
	}

	public String getLinkMode() {
		return linkMode;
	}

	public void setLinkMode(String linkMode) {
		this.linkMode = linkMode;
	}
	
	public boolean isCanRefresh(){
		Date refreshDate = this.getLastRefreshTime();
		boolean canRefresh = false;
		if(refreshDate==null){
			canRefresh = true;
		}else{
			Calendar calendar=Calendar.getInstance();
			calendar.setTime(refreshDate);
			try {
				calendar.add(Calendar.MINUTE, Integer.parseInt(System.getProperty("bp.task.refreshInterval")));
			} catch (NumberFormatException e) {
				calendar.add(Calendar.MINUTE, 1);
			}
			Calendar nowCalendar = Calendar.getInstance();
			nowCalendar.setTime(new Date());
			if(calendar.before(nowCalendar)){
				canRefresh=true;
			}
		}
		return canRefresh;
	}

	public Map<String, Object> getUpgradeParams() {
		return upgradeParams;
	}

	public void setUpgradeParams(Map<String, Object> upgradeParams) {
		this.upgradeParams = upgradeParams;
	}

	public void copy(BizRtu tmpRtu) {
		if(tmpRtu == null)
			return;
		this.upgradeParams = tmpRtu.upgradeParams;
		this.requestMap =tmpRtu.requestMap;
		this.fileMessageMap = tmpRtu.fileMessageMap;
		this.messageCountMap =tmpRtu.messageCountMap;
		this.currentMessageCountMap = tmpRtu.currentMessageCountMap;
		this.fileMessageMap = tmpRtu.fileMessageMap;
	}

	

  			
}
