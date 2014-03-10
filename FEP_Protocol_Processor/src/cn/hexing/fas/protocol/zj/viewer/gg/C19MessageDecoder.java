package cn.hexing.fas.protocol.zj.viewer.gg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.RtuAlert;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;

/**
 * �¼��澯���ݣ�C=19H������
 * @author Administrator
 * @time 2012��12��11��10
 * 
 */
public class C19MessageDecoder  extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C19MessageDecoder.class);

    public Object decode(IMessage message) {
    	List<RtuAlert> rt=new ArrayList<RtuAlert>();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
        		//Ӧ������
        		int rtype=(ParseTool.getErrCode(message));  
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//�����ն�Ӧ��
        			//ȡӦ������
        			String data=ParseTool.getDataString(message);
        			// ȡ�澯������ALRN��1�ֽ�16����
        			int alrnNumber=Integer.parseInt(data.substring(0, 2));
        			data=data.substring(2);	//��ȥ�澯����1���ֽ�
        			log.info("C19MessageDecoder �¼��澯����="+alrnNumber);
        			if(alrnNumber==0){
        				log.info("û���¼���Ҫ��������");
        			}
        			//������ӱ�����Ŀ�Զ�ע��֡���������⣬����
//        			if(data.length()%68!=0){
//        				log.info("֡���Ȳ����Ϲ淶");
//        				return null;
//        			}
        			for(int i =0;i<alrnNumber;i++){
        				//��ȡ�����
        				String meterNo= DataSwitch.ReverseStringByByte(data.substring(0, 12));
        				//ͨ��meterNo���Ҳ������
            			data=data.substring(12);//��ȥ���ͨ�ŵ�ַ6���ֽ�
            			String stime="20"+data.substring(0, 12);
            			SimpleDateFormat sdf=new SimpleDateFormat("yyyyMMddHHmmss");
            			Date date=sdf.parse(stime);
            			data=data.substring(12);//��ȥʱ��6���ֽ�
            			String alertCode=DataSwitch.ReverseStringByByte(data.substring(0, 4));
            			RtuAlert ra=new RtuAlert();	 
            			ra.setAlertCode(Integer.parseInt(alertCode,16));
            			ra.setAlertCodeHex("04"+alertCode);//�¼�����ǰ�����04 �Ա�ʾ�ǹ���¼�
            			ra.setAlertTime(date);
            			//01A9 ���ע�� 01AB ɾ�� 01AC��λ����
            			if("01A9".equals(alertCode)){
            				String value="";
            				String meterType=data.substring(4, 6);
            				String UserFocus=data.substring(6, 8);
            				String PLC=data.substring(8, 10);
            				String credit=data.substring(10, 12);
            				String MAC=data.substring(12, 16);
            				//�������+ ������+ �Ƿ��ص��û�+ PLC��λ +���ͨ�ŵ�ַ + �û����ö� + MAC��ַ
            				value=meterType+"#"+"FF"+"#"+UserFocus+"#"+PLC+"#"+meterNo+"#"+credit+"#"+MAC;
            				ra.setSbcs(value);
            				ra.setTn("00");
            			}
            			else if("01AB".equals(alertCode)){
            				String value="";
            				String location=data.substring(4, 6);
            				String deleteMeterNo=DataSwitch.ReverseStringByByte(data.substring(6, 18));
            				value=location+"#"+deleteMeterNo;
            				ra.setSbcs(value);
            				ra.setTn("00");
            			}
            			else if("01AC".equals(alertCode)){
            				String value="";
            				String location=data.substring(4, 6);
            				String oldMeterNo=DataSwitch.ReverseStringByByte(data.substring(6, 18));
            				String newMeterNo=DataSwitch.ReverseStringByByte(data.substring(18, 30));
            				value=location+"#"+oldMeterNo+"#"+newMeterNo;
            				ra.setSbcs(value);
            				ra.setTn("00");
            			}
            			ra.setReceiveTime(new Date(((MessageZj) message).getIoTime()));
        				if("true".equals(System.getProperty("isG3MeterBox"))){
        					try {
								data=data.substring(48);
							} catch (Exception e) {
	        					data=data.substring(44);//��ȥ2���ֽڵı����20�ֽڵ�����
							}//G3������Ŀ֡�����⡣        					
        				}else{
        					data=data.substring(44);//��ȥ2���ֽڵı����20�ֽڵ�����
        				}
            			rt.add(ra);
        			}
        		}else{
        			//�쳣Ӧ��
        		}
        	}
    		else{
    			//�¼��澯�������ϱ�����
    		}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }     
        return rt;
    }      

}
