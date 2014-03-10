package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalReadCurrentDataRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.parse.DataItemCoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;


/**
 * ����ǰ����(�����룺01H)��Ϣ������
 */
public class C01MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C01MessageEncoder.class);
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageEncoder#encode(java.lang.Object)
     */
    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalReadCurrentDataRequest){	//��ȡ��ǰ���ݵ��������
        		FaalReadCurrentDataRequest para=(FaalReadCurrentDataRequest)obj;
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			if(frp.getRtuId()==null){
				        	throw new MessageEncodeException("δָ���ٲ��ն�");
				        }
				        if(frp.getCmdId()==null){
				        	throw new MessageEncodeException("����IDȱʧ");
				        }
				        byte[] points=new byte[frp.getTn().length];
				        for(int i=0;i<points.length;i++){
				        	points[i]=Byte.parseByte(""+frp.getTn()[i]);
				        }
	    		        int[] datakeys=new int[frp.getParams().size()];
			        	int[] itemlen=new int[frp.getParams().size()];
			        	String code="";
			        	for(int i=0;i<frp.getParams().size();i++){	
			        		code=((FaalRequestParam)frp.getParams().get(i)).getName();
			        		ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(code);
			        		if (pdc!=null){
			        			if (pdc.getParentCode()!=null)	//����������,��վʹ���ڲ���ʶ
			        				datakeys[i]=ParseTool.HexToDecimal(pdc.getParentCode());
			        			else //�ն˲��������������						
			        				datakeys[i]=ParseTool.HexToDecimal(pdc.getCode());
				        		try{
					        		itemlen[i]=getDataItemConfig(code).getLength();
					        	}catch(Exception e){
					        		throw new MessageEncodeException("�ٲⲻ֧�ֵĲ���--"+((FaalRequestParam)frp.getParams().get(i)).getName());
					        	}
			        		}		        		
			        		/*datakeys[i]=ParseTool.HexToDecimal(((FaalRequestParam)frp.getParams().get(i)).getName());
				        	try{
				        		itemlen[i]=getDataItemConfig(((FaalRequestParam)frp.getParams().get(i)).getName()).getLength();
				        	}catch(Exception e){
				        		throw new MessageEncodeException("�ٲⲻ֧�ֵĲ���--"+((FaalRequestParam)frp.getParams().get(i)).getName());
				        	}*/
			        	}		        
				        //��֡
			        	int len=0;//��ʼ���峤��Ϊ0�����ݲ�ͬ�Ĺ�Լ��ȷ������
				        int loc=0;//��ȡ�������ʾ��λ�ó�ʼ����Ϊ0�����ݳ�����ȷ����ȡλ��
				        byte[] frame=new byte[len];	
			        	//�ж�֡���ͣ����lenΪ8�ֽ�TNM+2*m ���㶫��Լֻ��2*mû��8���ֽڵ�TNM
			        	if(Protocol.GG.equals(para.getProtocol())){
					       len=datakeys.length*2;	//2*���������
					        frame=new byte[len];
					        loc=0;
			        	}
			        	else if(Protocol.ZJ.equals(para.getProtocol())){
			        		    len=datakeys.length*2+8;	//8�ֽ�TNM+2*���������
			        		    loc=8;
						        frame=new byte[len];
						        for(int i=0;i<points.length;i++){	//TNM
						        	int index=0;
						        	int flag=0x01;
						        	index=(points[i] & 0xff)/8;
						        	flag=flag<<((points[i] & 0xff)%8);
						        	frame[index]=(byte)(((frame[index] & 0xff) | flag) & 0xff);
						        }
			        	}else
			        	{
			        	log.info("��Լ���Ͳ�ƥ��");
			        	}
				        int fdlen=0;	//��֡���ݳ���
				        int ntnum=0;	//���������ø���
				        List<byte[]> titems=new ArrayList<byte[]>();
				        for(int j=0;j<datakeys.length;j++){	//���ݱ�ʶ
				        	if(ParseTool.isTask(datakeys[j])){//��������
				        		titems.add(new byte[]{(byte)(datakeys[j] & 0xff),(byte)((datakeys[j] & 0xff00)>>>8)});
				        	}else{
				        		frame[loc]=(byte)(datakeys[j] & 0xff);		//DI0
					        	frame[loc+1]=(byte)((datakeys[j] & 0xff00)>>>8);	//DI1
					        	loc+=2;
					        	fdlen+=2;
					        	fdlen+=itemlen[j]*points.length;
					        	ntnum++;
				        	}		        	
				        }
				        
				        rt=new ArrayList<MessageZj>();
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
			        	if(rtu==null){
			        		log.info("�ն���Ϣδ�ڻ����б�"+frp.getRtuId());
			        		continue;
			        	}			        	
			        	int datamax=DataItemCoder.getDataMax(rtu);	//�ն�ÿ֡�������ֵ			        	
				        int msgcount=0;				        
			        	if(titems.size()>0){//�������ٲ�
			        		for(int k=0;k<titems.size();k++){
			        			//֡ͷ����
			        			MessageZjHead head=createHead(rtu);
						        head.dlen=(short)10;					        
						        byte[] frameA=new byte[10];
						        System.arraycopy(frame,0,frameA,0,8);
						        System.arraycopy((byte[])(titems.get(k)),0,frameA,8,2);
						        MessageZj msg=new MessageZj();
						        msg.setCmdId(frp.getCmdId());						        
						        msgcount++;
						        msg.data=ByteBuffer.wrap(frameA);
						        msg.head=head;
						        rt.add(msg);
			        		}
			        	}
			        	if(datamax>=(fdlen+8)||datakeys.length==1){//�����֡
			        		//֡ͷ����
			        		if(ntnum>0){//�з��������������ٲ�
			        			if(Protocol.ZJ.equals(para.getProtocol())){
						        	len=8+ntnum*2;
			        			}
			        			else if(Protocol.GG.equals(para.getProtocol())){
			        				len=ntnum*2;
			        			}
				        	MessageZjHead head=createHead(rtu);
						        head.dlen=(short)len;
						        
						        byte[] frameA=new byte[len];
						        System.arraycopy(frame,0,frameA,0,len);
						        
						        MessageZj msg=new MessageZj();
						        msg.setCmdId(frp.getCmdId());
						        msgcount++;
						        msg.data=ByteBuffer.wrap(frameA);
						        msg.head=head;
						        rt.add(msg);
					      	}
			        	}else{
			        		int dnum=0;
			        		int pos=0;
			        		int curlen=0;
			        		for(int j=0;j<ntnum;j++){			        			
			        			dnum+=1;	
			        			curlen+=(2+itemlen[j]*points.length);
			        			if(Protocol.ZJ.equals(rtu.getRtuProtocol())){
				        			if((curlen+8)>datamax||j==ntnum-1){//����+8�ֽڲ�������				
					        			//��֡
				        				MessageZjHead head=createHead(rtu);			        						        						        		
					        			head.dlen=(short)(8+dnum*2);		
					        			byte[] frameA=new byte[head.dlen];
					        			System.arraycopy(frame,0,frameA,0,8);
					        			System.arraycopy(frame,8+pos*2,frameA,8,head.dlen-8);								        
								        MessageZj msg=new MessageZj();
								        msg.setCmdId(frp.getCmdId());						        								        
								        msgcount++;
								        msg.data=ByteBuffer.wrap(frameA);
								        msg.head=head;
								        rt.add(msg);
								        pos+=dnum;
								        dnum=0;	
								        curlen=0;
					        		}
			        			}
			        			else if(Protocol.GG.equals(rtu.getRtuProtocol())){
				        			if(curlen>datamax||j==ntnum-1){//����				
					        			//��֡
				        				MessageZjHead head=createHead(rtu);			        						        						        		
					        			head.dlen=(short)(dnum*2);	
					        			byte[] frameA=new byte[head.dlen];
					        			System.arraycopy(frame,pos*2,frameA,0,head.dlen);								        
								        MessageZj msg=new MessageZj();
								        msg.setCmdId(frp.getCmdId());						        								        
								        msgcount++;
								        msg.data=ByteBuffer.wrap(frameA);
								        msg.head=head;
								        rt.add(msg);
								        pos+=dnum;
								        dnum=0;	
								        curlen=0;
					        		}	
			        			}
	        					        				        	
				        	}		        		
			        	}
		        		//ÿ���������ô˴ε��ն���֡����
			        	setMsgcount(rt,msgcount);
	        		}
		        }
		        else{
	        		throw new MessageEncodeException("δ��ֵ");
	        	}
        	}
        }catch(Exception e){
        	throw new MessageEncodeException(e);
        }
        if(rt!=null){
        	IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;           	
    }
    
    private MessageZjHead createHead(BizRtu rtu){
    	//    	֡ͷ����
    	MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//��վ�·�
        head.c_expflag=0;	//�쳣��
        head.c_func=(byte)0x01;	//������
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//������ ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//������ ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//�ն˵�ַ
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//֡�����
        //head.fseq		//֡���???????
        //head.msta=	//��վ��ַ?????
        return head;
    }
    
    private ProtocolDataItemConfig getDataItemConfig(String datakey){    	
    	return super.dataConfig.getDataItemConfig(datakey);
    }
	private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
