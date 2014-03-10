package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fas.model.FaalSendSmsRequest;
import cn.hexing.fas.protocol.zj.parse.Parser43;

/**
 * @filename	C28MessageCoder.java
 * TODO			请求发送短信返回帧组帧
 */
public class C28MessageEncoder extends AbstractMessageEncoder{
	private static Log log=LogFactory.getLog(C28MessageEncoder.class);
	
	public IMessage[] encode(Object obj){
		List<MessageZj> rt=null;
		try{
    		if(obj instanceof FaalSendSmsRequest){
    			FaalSendSmsRequest para=(FaalSendSmsRequest)obj;
    			String content=para.getContent();	//短信内容
    			if(content==null || content.length()==0 ){
    				//错误的参数
        			throw new MessageEncodeException("请指定短信内容");
    			}
    			//byte[] bcont=content.getBytes("UNICODE");
    			byte[] bcont=content.getBytes("GB2312");
    			exchangeBytes(bcont,1);	//?????? 可能只有高科前置机这样，要测试(高科编码为UNICODE,要将单一字符编码的两个字节交换高低位置，华隆为GBK编码，要将整个语句的编码逆序)
    			String[] phones=para.getMobiles();
    			if((phones!=null) && (phones.length>0)){
    				rt=new ArrayList<MessageZj>();    				
    				int len=14+bcont.length;
    				if(bcont.length>(content.length()*2)){
		        		len-=2;
		        	}                   
    				for(int i=0;i<phones.length;i++){
    					//帧头数据
    					MessageZjHead head=new MessageZjHead();
    			        head.c_dir=0;	//主站下发
    			        head.c_expflag=0;	//异常码
    			        head.c_func=(byte)0x28;	//功能码
    			        
    			        head.rtua_a1=(byte)0x92;//ParseTool.StringToBcd(FasConfig.getConfig().getModuleConfig("fe-module").getFrontEndConfig().getZoneCode().substring(0,2));	//get a1
    			        head.rtua_a2=0;
    			        head.rtua_b1b2=0x1E00;	//0x001E是前置机
                        
    			        head.iseq=0;	//帧内序号
    			        //head.fseq		//帧序号???????
    			        //head.msta=	//主站地址?????
    			        head.dlen=(short)len;
    			        
    			        byte[] frame=new byte[len];    			       
    			        int dlen=Parser43.constructor(frame,phones[i],0,14,0);	//phone
			        	if(dlen<=0){
			        		continue;
			        	}
			        	
			        	if(bcont.length>(content.length()*2)){
			        		System.arraycopy(bcont,2,frame,14,bcont.length-2);	//content
			        	}else{
			        		System.arraycopy(bcont,0,frame,14,bcont.length);	//content
			        	}
			        				        	
			        	MessageZj msg=new MessageZj();			        
				        msg.data=ByteBuffer.wrap(frame);
				        msg.head=head;
				        rt.add(msg);
				        if(log.isDebugEnabled()){
				        	log.debug(content+" to "+phones[i]);
				        }
    				}
    			}else{
    				//错误的参数
        			throw new MessageEncodeException("请指定短信发送对象的手机号码");
    			}
    		}else{
    			//错误的参数
    			throw new MessageEncodeException("错误的参数对象，请使用：FaalSendSmsRequest");
    		}
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		
		if(rt!=null && rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
		return null;
	}
	
	private void exchangeBytes(byte[] data){
		int i=0;
		while(i<data.length){
			//exchange i and i+1
			byte cc=data[i];
			data[i]=data[i+1];
			data[i+1]=cc;
			i+=2;
		}
	}
	
	private void exchangeBytes(byte[] data,int type){
		if(type==0){
			exchangeBytes(data);
		}else{
			int i=0;
			int j=data.length-1;
			while(i<(data.length/2)){
				//exchange i and i+1
				byte cc=data[i];
				data[i]=data[j];
				data[j]=cc;
				i++;
				j--;
			}
		}
	}	
}
