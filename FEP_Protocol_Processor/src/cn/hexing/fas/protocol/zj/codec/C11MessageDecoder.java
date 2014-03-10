package cn.hexing.fas.protocol.zj.codec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.FaalGGKZM11Request;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.model.RtuData;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.ErrorCode;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;

/**
 * ���������ն�������(�����룺11H)��Ӧ��Ϣ������
 * 
 */
public class C11MessageDecoder extends AbstractMessageDecoder {	
	private static Log log=LogFactory.getLog(C11MessageDecoder.class);

	public Object decode(IMessage message) {
    	List<RtuData> tasks = new ArrayList<RtuData>();
    	try{
    		if(ParseTool.getOrientation(message)==DataMappingZJ.ORIENTATION_TO_APP){	//���ն�Ӧ��
        		//Ӧ������
        		int rtype=(ParseTool.getErrCode(message));  
        		HostCommand hc=new HostCommand();
            	List<HostCommandResult> value=new ArrayList<HostCommandResult>();
        		if(rtype==DataMappingZJ.ERROR_CODE_OK){	//�����ն�Ӧ��
        			hc.setStatus(HostCommand.STATUS_SUCCESS);
        			//ȡӦ������
        			String data=ParseTool.getDataString(message);
        			BizRtu rtu=RtuManage.getInstance().getBizRtuInCache(message.getRtua());
        			
        			if (rtu!=null&&data!=null&&data.length()>=18){
        				//��ȡ�����
        				String meterNo=DataSwitch.ReverseStringByByte(data.substring(0, 12));
        				//ͨ��meterNo���Ҳ������
        				MeasuredPoint mp=rtu.getMeasuredPointByTnAddr(meterNo);
        				byte[] datas=ParseTool.getData(message);
						int index=6;	//ȡ�����ͨ�ŵ�ַ
	        			while(index<datas.length){
	        				if(2<(datas.length-index)){	//����Ҫ��3�ֽ����ݣ�2�ֽ����ݱ�ʾ+����1�ֽ����ݣ�
	        					int datakey=((datas[index+1] & 0xff)<<8)+(datas[index] & 0xff); //���ݱ�ʾ��		        					
	        					ProtocolDataItemConfig dic=getDataItemConfig(datakey);
	        					if(dic!=null){
	        						int loc=index+2;
	        						int itemlen=0;
        							itemlen=parseBlockData(datas,loc,dic,mp.getTn(),new Long(0),value);
        							loc+=itemlen;
	        						index=loc;
	        					}else{
	        						//��֧�ֵ�����		        							
	        						log.info("��֧�ֵ�����:"+ParseTool.IntToHex(datakey));	
	        						break;	//�߿Ƶ��������ݱȽ����⣬��ʱ����˴���
	        					}
	        					
	        					
	        				}else{
	        					//����֡����
	        					throw new MessageDecodeException("֡����̫��");	
	        				}
	        			}	
              			MessageZj zjMsg=(MessageZj) message;
	        			byte fseq = zjMsg.head.fseq;
	        			FaalGGKZM11Request req=(FaalGGKZM11Request)rtu.getParamFromMap(fseq);
	        			if(req!=null){
	        				Date time = req.getDataTime();
		        			String stime="";
		        			if("day".equalsIgnoreCase(req.getOperator()) ||"month".equalsIgnoreCase(req.getOperator()) ){
		        				if("day".equalsIgnoreCase(req.getOperator()) ){
			        				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM-dd");
				        			stime =sdf.format(time);
				        			stime+=" 00:00:00";	
		        				}
		        				else {
			        				SimpleDateFormat sdf=new SimpleDateFormat("yyyy-MM");
				        			stime =sdf.format(time);
				        			stime+="-01 00:00:00";	
		        				}
			    				HostCommandResult hcr1=new HostCommandResult();
			    				hcr1.setCode("0400122000");
			    				hcr1.setValue(stime);
			    				hcr1.setCommandId(new Long(0));
			    				hcr1.setTn(mp.getTn());
			    				value.add(hcr1);
		        			}
	        			}
	        			hc.setResults(value);
	        			rtu.removeParamFromMap(fseq);
        			}
	        		
        		}else{
    				//�쳣Ӧ��֡
        			byte[] data=ParseTool.getData(message);
        			if(data!=null && data.length>0){
        				if(data.length==1){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[0]));
        				}else if(data.length==9){
        					hc.setStatus(ErrorCode.toHostCommandStatus(data[8]));
        				}else{
        					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        				}
        			}else{
        				hc.setStatus(HostCommand.STATUS_RTU_FAILED);
        			}
    			}
        	return hc;
    		}
        }catch(Exception e){
        	throw new MessageDecodeException(e);
        }     
        return tasks;
    }      
    /**
     * ����������
     * @param data		����֡
     * @param loc		������ʼλ��
     * @param pdc		����������
     * @param points	�ٲ�Ĳ���������
     * @param pnum		�ٲ�Ĳ��������
     * @param result	�������
     */
    @SuppressWarnings("rawtypes")
	private int parseBlockData(byte[] data,int loc,ProtocolDataItemConfig pdc,String point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{    		
    		List children=pdc.getChildItems();
    		int index=loc;
    		if((children!=null) && (children.size()>0)){	//���ݿ��ٲ�    			
    			for(int i=0;i<children.size();i++){
    				ProtocolDataItemConfig cpdc=(ProtocolDataItemConfig)children.get(i);
    				int dlen=parseBlockData(data,index,cpdc,point,cmdid,result);
    				index+=dlen;
    				rt+=dlen;
    			}    			
    		}else{
    			int dlen=parseItem(data,loc,pdc,point,cmdid,result);
    			if("EE0A".equals(pdc.getParentCode())){
    				//��ȡ�̵���״̬���滹�������ֽ����ݣ���ʱ������ֱ���Ƴ���
    				dlen+=2;
    			}
    			rt+=dlen;
    		}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    
    private int parseItem(byte[] data,int loc,ProtocolDataItemConfig pdc,String point,Long cmdid,List<HostCommandResult> result){
    	int rt=0;
    	try{
//    		int datakey=pdc.getDataKey();
    		int itemlen=0;   		
			itemlen=pdc.getLength();			
			if(itemlen<=(data.length-loc)){	//���㹻����				
				Object di=DataItemParser.parsevalue(data,loc,itemlen,pdc.getFraction(),pdc.getParserno());
				HostCommandResult hcr=new HostCommandResult();
				hcr.setCode(pdc.getCode());
				if(di!=null){
					hcr.setValue(di.toString());
				}
				hcr.setCommandId(cmdid);
				hcr.setTn(point);
				result.add(hcr);
				rt=itemlen;
			}else{
				//��������
				if((data.length-loc)==0){
					//û�и����ֽڽ������������ն��п����ݲ�ȫ���������ݶ�ʧ
					
				}else{
					throw new MessageDecodeException(
							"�������ݳ��ȣ������"+pdc.getCode()+" �������ݳ��ȣ�"+itemlen+" �������ȣ�"+(data.length-loc));
				}				      							
			}
    	}catch(Exception e){
    		throw new MessageDecodeException(e);
    	}
    	return rt;
    }
    private ProtocolDataItemConfig getDataItemConfig(int datakey){    	
    	return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
    }
}
