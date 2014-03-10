package cn.hexing.fas.protocol.zj.ggcodec;

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
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageEncoder;
import cn.hexing.fas.protocol.zj.parse.DataItemCoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;

/**
 * 	读当前数据消息编码器 
 *  控制码（C=01H）
 * @author luolb
 *
 */
public class C01MessageEncoder extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C01MessageEncoder.class);
    /* (non-Javadoc)
     * @see cn.hexing.fas.protocol.codec.MessageEncoder#encode(java.lang.Object)
     */
    public IMessage[] encode(Object obj) {        
        List<MessageZj> rt=null;
    	try{	    	
        	if(obj instanceof FaalReadCurrentDataRequest){	//读取当前数据的请求对象
        		FaalReadCurrentDataRequest para=(FaalReadCurrentDataRequest)obj;
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){
	        			if(frp.getRtuId()==null){
				        	throw new MessageEncodeException("未指定召测终端");
				        }
				        if(frp.getCmdId()==null){
				        	throw new MessageEncodeException("命令ID缺失");
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
			        			if (pdc.getParentCode()!=null)	//测量点数据,主站使用内部标识
			        				datakeys[i]=ParseTool.HexToDecimal(pdc.getParentCode());
			        			else //终端参数、测量点参数						
			        				datakeys[i]=ParseTool.HexToDecimal(pdc.getCode());
				        		try{
					        		itemlen[i]=getDataItemConfig(code).getLength();
					        	}catch(Exception e){
					        		throw new MessageEncodeException("召测不支持的参数--"+((FaalRequestParam)frp.getParams().get(i)).getName());
					        	}
			        		}		        		
			        		/*datakeys[i]=ParseTool.HexToDecimal(((FaalRequestParam)frp.getParams().get(i)).getName());
				        	try{
				        		itemlen[i]=getDataItemConfig(((FaalRequestParam)frp.getParams().get(i)).getName()).getLength();
				        	}catch(Exception e){
				        		throw new MessageEncodeException("召测不支持的参数--"+((FaalRequestParam)frp.getParams().get(i)).getName());
				        	}*/
			        	}		        
				        //组帧
				        int len=datakeys.length*2+8;	//8字节TNM+2*数据项个数
				        
				        byte[] frame=new byte[len];
				        for(int i=0;i<points.length;i++){	//TNM
				        	int index=0;
				        	int flag=0x01;
				        	index=(points[i] & 0xff)/8;
				        	flag=flag<<((points[i] & 0xff)%8);
				        	frame[index]=(byte)(((frame[index] & 0xff) | flag) & 0xff);
				        }
				        int loc=8;
				        int fdlen=0;	//回帧数据长度
				        int ntnum=0;	//非任务配置个数
				        List<byte[]> titems=new ArrayList<byte[]>();
				        for(int j=0;j<datakeys.length;j++){	//数据标识
				        	if(ParseTool.isTask(datakeys[j])){//任务配置
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
			        		log.info("终端信息未在缓存列表："+frp.getRtuId());
			        		continue;
			        	}			        	
			        	int datamax=DataItemCoder.getDataMax(rtu);	//终端每帧数据最大值			        	
				        int msgcount=0;				        
			        	if(titems.size()>0){//有任务召测
			        		for(int k=0;k<titems.size();k++){
			        			//帧头数据
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
			        	if(datamax>=(fdlen+8)||datakeys.length==1){//无须分帧
			        		//帧头数据
			        		if(ntnum>0){//有非任务配置数据召测
					        	len=8+ntnum*2;
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
			        			if((curlen+8)>datamax||j==ntnum-1){//数据+8字节测量点组				
				        			//组帧
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
			        	}
		        		//每个报文设置此次单终端组帧总数
			        	setMsgcount(rt,msgcount);
	        		}
		        }
		        else{
	        		throw new MessageEncodeException("未传值");
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
    	//    	帧头数据
    	MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//主站下发
        head.c_expflag=0;	//异常码
        head.c_func=(byte)0x01;	//功能码
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//地市吗 ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//区县码 ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//终端地址
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//帧内序号
        //head.fseq		//帧序号???????
        //head.msta=	//主站地址?????
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
