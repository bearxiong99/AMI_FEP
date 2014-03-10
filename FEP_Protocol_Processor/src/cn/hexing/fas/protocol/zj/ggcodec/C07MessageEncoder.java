package cn.hexing.fas.protocol.zj.ggcodec;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalRealTimeWriteParamsRequest;
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
 * @info 时数据写编码(07H)
 * @author Administrator
 * @time 2012年12月3日
 */
public class C07MessageEncoder extends AbstractMessageEncoder{
	private final Log log=LogFactory.getLog(C07MessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageZj> rt=null;		
		try{
			if(obj instanceof FaalRealTimeWriteParamsRequest){
				FaalRealTimeWriteParamsRequest para=(FaalRealTimeWriteParamsRequest)obj;
		        byte rights=0x11;	//权限等级 0x00低级权限 0x11 高级权限		        		        		        
		        byte[] rowdata=new byte[2048];
		        byte[] rowdataHL=new byte[2048];	//厂家特殊处理,如APN
		        byte[] rowdataHLi=new byte[2048];	//厂家特殊处理,如APN		        		        
		        Calendar time=para.getCmdTime();	//设置时的时间
		        if(time==null){
		        	time=Calendar.getInstance();
		        }
		        int wt=para.getTimeout();			//超时时间		        		        	        
		        rt=new ArrayList<MessageZj>();
		        if(para.getRtuParams()!=null){
	        		for(FaalRequestRtuParam frp:para.getRtuParams()){			        	
			        	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(frp.getRtuId()));
			        	byte[] fdata=null;			        	
			        	if(rtu==null){
			        		log.info("终端信息缺失，终端ID--"+rtu.getRtuId());
			        		continue;
			        	}
			        	for(int p=0;p<frp.getTn().length;p++){
			        		int point=frp.getTn()[p];
			        		int loc=0;
					        rowdata[0]=(byte)point;
					        rowdata[1]=rights;
					        rowdata[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdata[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdata[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdata[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdata[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdata[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        rowdataHL[0]=(byte)point;
					        rowdataHL[1]=rights;
					        rowdataHL[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdataHL[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdataHL[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdataHL[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdataHL[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdataHL[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        rowdataHLi[0]=(byte)point;
					        rowdataHLi[1]=rights;
					        rowdataHLi[5]=ParseTool.IntToBcd(time.get(Calendar.YEAR)%100);	//year
					        rowdataHLi[6]=ParseTool.IntToBcd(time.get(Calendar.MONTH)+1);	//month
					        rowdataHLi[7]=ParseTool.IntToBcd(time.get(Calendar.DAY_OF_MONTH));	//day
					        rowdataHLi[8]=ParseTool.IntToBcd(time.get(Calendar.HOUR_OF_DAY));	//hour
					        rowdataHLi[9]=ParseTool.IntToBcd(time.get(Calendar.MINUTE));	//minute
					        rowdataHLi[10]=ParseTool.IntToBcd(wt);	//minute
					        
					        loc=11;
					        
					        int index=0;	
					        int[] itemlen=new int[frp.getParams().size()];
					        int[] keysinpara=new int[frp.getParams().size()];
					        String[] valsinpara=new String[frp.getParams().size()];
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
						        		throw new MessageEncodeException(fp.getName(),"错误的参数:"+fp.getName()+"---"+fp.getValue());
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
				        	}
				        	else{
				        		fdata=rowdata;
				        	}
				        	String pwd=rtu.getHiAuthPassword();
				        	if(pwd==null){
				        		log.info("终端密码缺失，终端ID--"+rtu.getRtuId());
				        		continue;
				        	}		        	
				        	int datamax=DataItemCoder.getDataMax(rtu);	//终端每帧数据最大值
					        int msgcount=0;			        
			        		int dnum=0;
			        		int pos=0;
			        		int curlen=0;		        		
			        		for(int j=0;j<itemlen.length;j++){
			        			if((curlen+11+(2+itemlen[j]))>datamax){//数据+1测量点+1权限类型+3密码+5命令时间+1有效时长
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
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
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
        head.c_func=(byte)0x07;	//功能码
        //head.rtua_a1=(byte)((zonecode & 0xff00)>>>8);	//地市吗 ??????
        //head.rtua_a2=(byte)(zonecode & 0xff);	//区县码 ??????
        //head.rtua_b1b2=(short)rtu.getRtua();	//终端地址
        head.rtua=rtu.getRtua();
        
        head.iseq=0;	//帧内序号
        //head.fseq		//帧序号???????
        //head.msta=	//主站地址?????
        return head;
    }
	
	private MessageZj createMessageZj(byte[] rowdata,BizRtu rtu,int pos,int dlen,Object cmdid){
		//组帧
		MessageZjHead head=createHead(rtu);
    	head.dlen=(short)(dlen+11);
    	
    	byte[] frameA=new byte[head.dlen];
		System.arraycopy(rowdata,0,frameA,0,11);
		System.arraycopy(rowdata,11+pos,frameA,11,dlen);
		
		String pwd=rtu.getHiAuthPassword();
		ParseTool.HexsToBytesAA(frameA,2,pwd,3,(byte)0xAA);
		
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
