package cn.hexing.fas.model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * �ն���������
 */
public class RtuData {
	private static final Logger log = Logger.getLogger(RtuData.class);
	/** ��λ���� �������񱣴�*/
    //private String deptCode;
	/** ���ݱ���ID */
    //private String dataSaveID;
    /** �ն������ */
    private String taskNum;
    /** �ն������������� */
    //private String taskProperty;
    /** �ն��߼���ַ��HEX�� */
    private String logicAddress;
    /** ������� */
    private String tn;
    /** ����ʱ�� */
    private Date time;
    
    /** �����б� */
    private List<RtuDataItem> dataList=new ArrayList<RtuDataItem>();


    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append("[logicAddress=").append(getLogicAddress())
        	.append(", taskNum=").append(getTaskNum())
            .append(", time=").append(getTime()).append("]");
        return sb.toString();
    }
    /**
     * ��Ӹ澯����
     * @param arg �澯����
     */
    public void addDataList(RtuDataItem rtuDataItem) {
    	dataList.add(rtuDataItem);
    }

	public String getLogicAddress() {
		return logicAddress;
	}
	public void setLogicAddress(String logicAddress) {
		this.logicAddress = logicAddress;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public Date getTime() {
		return time;
	}
	public void setTime(Date time) {
		this.time = time;
	}
	public void setTime(String dt) {
		try{
			SimpleDateFormat df = null ;
			if (dt.trim().length()==16){
				df= new SimpleDateFormat("yyyy-MM-dd HH:mm");
			}
			else if (dt.trim().length()==10){
				df = new SimpleDateFormat("yyyy-MM-dd");
			}else if (dt.trim().length()==7){
				df = new SimpleDateFormat("yyyy-MM");
			}else if(dt.trim().length()==19){
				df=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
			this.time = df.parse(dt);
		}
		catch(Exception e){		
			log.error("setTime time="+dt+" error:"+e);
		}
	}
	public String getTimeStr() {
		String sDate="";
		try{
			SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			sDate=df.format(time);
		}catch(Exception ex){			
		}
		return sDate;
	}
	public List<RtuDataItem> getDataList() {
		return dataList;
	}
	public String getTn() {
		return tn;
	}
	public void setTn(String tn) {
		this.tn = tn;
	}
	//ȡ���⴦���������ʱ�䣬��ǰ+1��
	public Date getNextday() {
		Calendar cl = Calendar.getInstance();
		cl.setTime(time);
		cl.add(Calendar.DATE, +1);
		return cl.getTime();
	}
	//�����һ���£�Ϊ��ͳһ������DLMS�¶���
	public Date getNextMonth(){
		Calendar cl = Calendar.getInstance();
		cl.setTime(time);
		cl.add(Calendar.MONTH, +1);
		return cl.getTime();
	}
	//�������ʱ��Ϊ����������ʱ��  �ȱ��ʵ�ʶ������ݵ�ʱ����һ�죬��ǰ-1��
	public Date getDateBefore(){
		Calendar calendar = Calendar.getInstance(); //�õ�����
		calendar.setTime(time);//�ѵ�ǰʱ�丳������
		calendar.add(Calendar.DAY_OF_MONTH, -1);  //����Ϊǰһ��
		return  calendar.getTime();   //�õ�ǰһ���ʱ��
	}
}
