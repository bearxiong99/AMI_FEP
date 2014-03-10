package cn.hexing.fas.protocol.zj.ggcodec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.FaalWriteParamsRequest;
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
 * 写对象参数(功能码：04H)消息编码器
 * @author Administrator
 * @time 2012年12月3日
 *
 */
public class C08MessageEncoder extends AbstractMessageEncoder{
	private static Log log=LogFactory.getLog(C08MessageEncoder.class);
	
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalWriteParamsRequest){
				FaalWriteParamsRequest para=(FaalWriteParamsRequest)obj;					
				//组帧
//		        int point=Integer.parseInt(para.getTn());	//测量点号			        	        
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){			        				        	
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));			        	
			        	byte[] fdata=null;			        	
			        	if(rtu==null){
			        		log.info("终端信息未在缓存列表："+frp.getRtuId());
			        		continue;
			        	}
			        	int[] itemlen=new int[frp.getParams().size()];
				        int[] keysinpara=new int[frp.getParams().size()];
				        String[] valsinpara=new String[frp.getParams().size()];
				        
				        byte[] rowdata=new byte[2048];
				        byte[] rowdataHL=new byte[2048];	//厂家特殊处理,如APN
				        byte[] rowdataHLi=new byte[2048];	//厂家特殊处理,如APN
				        for(int p=0;p<frp.getTn().length;p++){
			        		int loc=0;	
				        		int point=frp.getTn()[p];
				        		point=0;  			 //暂时对测量点0即集中器本身进行操作
						        rowdata[0]=(byte)point;
						        rowdata[1]=(byte)point;
						        rowdata[2]=0x11;	//取最高权限				        
						        rowdataHL[0]=(byte)point;
						        rowdataHL[1]=(byte)point;
						        rowdataHL[2]=0x11;	//取最高权限				        
						        rowdataHLi[0]=(byte)point;
						        rowdataHLi[1]=(byte)point;
						        rowdataHLi[2]=0x11;	//取最高权限				        
						        loc=6;  		
					        int index=0;
					        for(int i=0;i<frp.getParams().size();i++){
				        		FaalRequestParam fp=(FaalRequestParam)frp.getParams().get(i);
					        	ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(fp.getName());
					        	if(pdc!=null){	//支持的参数
				        			rowdata[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdata[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			rowdataHL[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdataHL[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			rowdataHLi[loc]=(byte)(pdc.getDataKey() & 0xff);
				        			rowdataHLi[loc+1]=(byte)((pdc.getDataKey() & 0xff00)>>>8);
				        			
				        			loc+=2;
				        			int dlen=DataItemCoder.coder(rowdata,loc,fp,pdc);
						        	if(dlen<=0){
						        		//错误的参数
						        		throw new MessageEncodeException(fp.getName(),("错误的参数:"+fp.getName()+"---"+fp.getValue()));
						        	}
						        	
						        	if((pdc.getDataKey() & 0xffff)==0x8015){//apn特殊处理
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);	//华立
						        		int zi=16;	//非0个数
						        		int si=loc+15;
						        		for(int k=0;k<16;k++){
						        			if((rowdata[si] & 0xff)==0x0){
						        				rowdataHL[loc+k]=0;
						        				rowdataHLi[si]=(byte)0xAA;
						        				si--;
						        				zi--;
						        			}else{				        				
						        				break;
						        			}
						        		}
						        		if(zi>0){
						        			System.arraycopy(rowdata,loc,rowdataHL,loc+16-zi,zi);
						        		}
						        	}else if((pdc.getDataKey() & 0xffff)==0x8902){//表地址
						        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);	
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
						        		int zi=6;	//非AA个数
						        		int si=loc+5;
						        		for(int k=0;k<6;k++){
						        			if((rowdata[si] & 0xff)==0xAA){
						        				rowdataHLi[si]=0;
						        				si--;
						        				zi--;
						        			}else{				        				
						        				break;
						        			}
						        		}
						        	}else{
						        		System.arraycopy(rowdata,loc,rowdataHL,loc,dlen);
						        		System.arraycopy(rowdata,loc,rowdataHLi,loc,dlen);
						        	}
						        	
						        	itemlen[index]=dlen;
						        	keysinpara[index]=pdc.getDataKey();
						        	valsinpara[index]=fp.getValue();
						        	index++;
				        			loc+=dlen;
					        	}else{
					        		throw new MessageEncodeException(fp.getName(),"配置无法获取，数据项："+fp.getName());
					        	}
					        }
					        
					        //厂家编码变更华隆11->0087,千能18->0112,八达33->0094,威胜27->0117,华立13->0061,恒业31->0098
				        	if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("0087") || rtu.getManufacturer().equalsIgnoreCase("0112") || rtu.getManufacturer().equalsIgnoreCase("0094")|| rtu.getManufacturer().equalsIgnoreCase("0117"))){	//是HL
				        		fdata=rowdataHL;
				        	}else if(rtu.getManufacturer()!=null && (rtu.getManufacturer().equalsIgnoreCase("0061") || rtu.getManufacturer().equalsIgnoreCase("0098"))){
				        		fdata=rowdataHLi;
				        	}else{
				        		fdata=rowdata;
				        	}			        	
				        	int datamax=DataItemCoder.getDataMax(rtu);	//终端每帧数据最大值
					        int msgcount=0;				        
			        		int dnum=0;
			        		int pos=0;
			        		int curlen=0;
			        		for(int j=0;j<itemlen.length;j++){
			        			if((curlen+5+(2+itemlen[j]))>datamax){//数据+1测量点+1权限类型+3密码          广东规约测量点2个字节
			        				MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());							        
							        if(msg!=null){							        	
								        msgcount++;
							        	rt.add(msg);
							        }
			        				pos+=curlen;
							        dnum=1;
							        curlen=2+itemlen[j];							   							        
			        			}else{
			        				dnum+=1;
			        				curlen+=(2+itemlen[j]);			        				
			        				if(keysinpara[j]>0x8100 && keysinpara[j]<=0x81FE){//(是任务相关参数设置就分帧，适应一些终端只支持一个任务设置每帧的情形)
			        					MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());							        
								        if(msg!=null){							        	
									        msgcount++;
								        	rt.add(msg);
								        }
								        dnum=0;
								        pos+=curlen;
								        curlen=0;
			        				}
			        			}
			        		}
			        		if(dnum>0){
			        			MessageZj msg=createMessageZj(fdata,rtu,pos,curlen,frp.getCmdId());
			        			if(msg!=null){
							        msgcount++;
			        				rt.add(msg);
			        			}
			        		}
				        	//每个报文设置此次单终端组帧总数
				        	setMsgcount(rt,msgcount);	
			        	}
				        
				        
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
	
	private MessageZjHead createHead(BizRtu rtu){
    	//    	帧头数据
		MessageZjHead head=new MessageZjHead();
        head.c_dir=0;	//主站下发
        head.c_expflag=0;	//异常码
        head.c_func=(byte)0x08;	//功能码
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//地市吗 ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//区县码 ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//终端地址
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//帧内序号
        //head.fseq		//帧序号???????
        //head.msta=	//主站地址?????
        return head;
    }
	
	private MessageZj createMessageZj(byte[] rowdata,BizRtu rtu,int pos,int dlen,Object cmdid){//组帧
		MessageZjHead head=createHead(rtu);
		String pwd=rtu.getHiAuthPassword();
    	byte[] frameA=new byte[head.dlen];
		if(pwd==null){
			throw new MessageEncodeException("rtu password missing");
		}
			 head.dlen=(short)(dlen+6);
		     frameA=new byte[head.dlen];
			 System.arraycopy(rowdata,0,frameA,0,6);
			 System.arraycopy(rowdata,6+pos,frameA,6,dlen);
		     ParseTool.HexsToBytesAA(frameA,3,pwd,3,(byte)0xAA);	
		     MessageZj msg=new MessageZj();
		     msg.setCmdId((Long)cmdid);
		     msg.data=ByteBuffer.wrap(frameA);
		     msg.head=head;
		  return msg;
        }
	
	private void setMsgcount(List msgs,int msgcount){
		for(Iterator iter=msgs.iterator();iter.hasNext();){
			MessageZj msg=(MessageZj)iter.next();
			if (msg.getMsgCount()==0)
				msg.setMsgCount(msgcount);
		}
	}
}
