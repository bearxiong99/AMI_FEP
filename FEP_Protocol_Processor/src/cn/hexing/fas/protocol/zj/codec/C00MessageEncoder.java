package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalReadForwardDataRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.meter.IMeterParser;
import cn.hexing.fas.protocol.meter.MeterParserFactory;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;


public class C00MessageEncoder  extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C00MessageEncoder.class);
	
	public IMessage[] encode(Object obj) {		
		List<MessageZj> rt=new ArrayList<MessageZj>();		
		try{
			if(obj instanceof FaalReadForwardDataRequest){
				FaalReadForwardDataRequest para=(FaalReadForwardDataRequest)obj;
				
				//组帧	        
		        int waittime=para.getTimeout();	//超时时间?????
		        byte character=(byte)0x0;	//截取用特征字 0x00表示不考虑特征字截取
		        int cutindex=0;		//截取开始位置,0表示第一个字节
		        int cutlen=0;	//截取长度，0表示不截取，全部接收        

		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams())	{
	        			if(frp.getParams()==null)
	        				continue;
			        	String[] datakey=new String[frp.getParams().size()];	
			        	for(int i=0;i<frp.getParams().size();i++){
			        		datakey[i]=((FaalRequestParam)frp.getParams().get(i)).getName();
			        	}
			        	createRtuFrame(rt, para, waittime, character, cutindex, cutlen, frp.getTn(), datakey, frp.getRtuId(), frp.getCmdId());
	        		}				        			        				      			       
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

	private void createRtuFrame(List<MessageZj> rt, FaalReadForwardDataRequest para, int waittime, byte character, int cutindex, int cutlen, int[] tns, String[] datakey, String rtuId, Long cmdId) {
		try{
			MessageZj msg=null;
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rtuId));
			if(rtu==null){
				throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
			}
			
			IMeterParser mparser=null;
			String maddr=null;
			String portstr=null;

			int msgcount=0;
			for(int tn:tns){
				mparser=MeterParserFactory.getMeterParser(para.getFixProto());
				if(mparser==null){
					throw new MessageEncodeException("不支持的表规约");
				}
				if(para.getFixAddre()==null){//表地址为空取广播地址	
					maddr=para.getBroadcastAddress();
					if (maddr==null)
						maddr=getBroadcastAddress(para.getFixProto());
				}else{			
					maddr=para.getFixAddre();
				}
				if(para.getFixPort()==null){//表计端口号为空,则默认01
					portstr="01";
				}else{
					portstr=para.getFixPort();
				}				
								
				if(maddr==null){
					throw new MessageEncodeException("测量点地址缺失！");
				}else{
					//华立的特殊地址
					if(para.getFixProto().equals(Protocol.ZJMeter)){
						if(maddr.length()>2){
							String xxa=maddr.substring(maddr.length()-2);			        				
							if(xxa.equalsIgnoreCase("AA")){
								//错误
								maddr=maddr.substring(0,2);
							}else{
								maddr=xxa;
							}
						}
					}
				}
						
				DataItem dipara=new DataItem();
				dipara.addProperty("point",maddr);
				String[] dks=null;
				if(para.getFixProto().equals(Protocol.BBMeter97)){
					dks=mparser.convertDataKey(mparser.getMeter1Code(datakey));
				}
				else if(para.getFixProto().equals(Protocol.BBMeter07)){
					dks=mparser.getMeter2Code(datakey);
				}
				else
					dks=mparser.getMeter1Code(datakey);
				
				for(int k=0;k<dks.length;k++){
					if(dks[k]==null || dks[k].length()<=0){
						break;
					}
					byte[] cmd=mparser.constructor(new String[]{dks[k]},dipara);
					if(cmd==null){
						StringBuffer se=new StringBuffer();
						for(int j=0;j<datakey.length;j++){
							se.append(datakey[j]);
							se.append(" ");
						}
						throw new MessageEncodeException("不支持召测的表规约数据："+se.toString()+"  RTU:"+ParseTool.IntToHex4(rtu.getRtua()));
					}
					int len=cmd.length+7;
					
					//帧头数据
					MessageZjHead head=new MessageZjHead();
				    head.c_dir=0;	//主站下发
				    head.c_expflag=0;	//异常码
				    head.c_func=(byte)0x00;	//功能码
				    head.rtua=rtu.getRtua();
				    head.iseq=0;	//帧内序号
				    head.dlen=(short)len;
				    
				    int port=1;	//默认值
				    if(portstr!=null){
				    	port=Integer.parseInt(portstr);
				    }
				    byte[] frame=new byte[len];
				    frame[0]=(byte)port;
				    frame[1]=(byte)waittime;
				    frame[2]=character;
				    frame[3]=(byte)(cutindex & 0xff);
				    frame[4]=(byte)((cutindex & 0xff00)>>>8);
				    frame[5]=(byte)(cutlen & 0xff);
				    frame[6]=(byte)((cutlen & 0xff00)>>>8);
				    System.arraycopy(cmd,0,frame,7,cmd.length);
				    
				    msg=new MessageZj();
				    msg.data=ByteBuffer.wrap(frame);
				    
				    msg.setCmdId(cmdId);			        
				    msg.head=head;
				    rt.add(msg);
				    msgcount++;
				}
			}			
        	setMsgcount(rt,msgcount);
		}catch(Exception e){
			//
			try{
				MessageZj msg=new MessageZj();
				HostCommand hc=new HostCommand();
				hc.setId(cmdId);
				msg.setCmdId(cmdId);
				msg.setStatus(HostCommand.STATUS_PARA_INVALID);
				rt.add(msg);
			}catch(Exception ex){
				//
			}
		}
	}	
	
	private String getBroadcastAddress(String protocol){
		String maddr=null;
		if(protocol.equals(Protocol.BBMeter97)||protocol.equals(Protocol.BBMeter07)){
			maddr="999999999999";
		}
		else if(protocol.equals(Protocol.ZJMeter)){
			maddr="FF";
		}
		return maddr;
	}
    private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
