package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN10Request;
import cn.hexing.fas.model.FaalHX645RelayRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.model.UpdateCollectorKeyRequest;
import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.meter.HX645MeterParser;
import cn.hexing.fas.protocol.meter.IMeterParser;
import cn.hexing.fas.protocol.meter.MeterParserFactory;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

import com.hx.dlms.cipher.AESGcm128;

/**
 * 中继转发(功能码：10H)下行消息编码器
 * 
 */
public class C10MessageEncoder  extends AbstractMessageEncoder {
	private static Log log=LogFactory.getLog(C10MessageEncoder.class);

	public IMessage[] encode(Object obj) {		
		List<MessageGw> rt=new ArrayList<MessageGw>();
		try{
			if(obj instanceof FaalRequest){
				FaalGWAFN10Request request=(FaalGWAFN10Request)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sdata="",tp="",param="",portstr="",pw="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				if(request.getPort()<0){
					request.setPort(1);
				}
				param=DataItemCoder.constructor(""+request.getPort(),"HTB1");//通讯端口号			
				param=param+DataItemCoder.constructor(""+request.getKzz(),"BS1");//通信控制字
				param=param+DataItemCoder.constructor(""+request.getMsgTimeout(),"HTB1");//报文超时时间
				param=param+DataItemCoder.constructor(""+request.getByteTimeout(),"HTB1");//字节超时时间
				String sDADT="";	
				for (FaalRequestRtuParam rp:rtuParams){
					sdata="";
					int[] tn=rp.getTn();
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
					if(rtu==null){
						throw new MessageEncodeException("终端信息未在缓存列表："+rp.getRtuId());
					}
					if (rtu.getHiAuthPassword()!=null&&rtu.getHiAuthPassword().length()==32)
						pw=DataSwitch.ReverseStringByByte(rtu.getHiAuthPassword());//终端密码
					else{
						log.warn("Terminal "+rp.getRtuId()+" hiAuthPassword is null,use default password.");
						pw="00000000000000000000000000000000";
					}
					String[] codes=new String[rp.getParams().size()];
					String[] value=new String[rp.getParams().size()];
					for (int i=0;i<rp.getParams().size();i++){
						FaalRequestParam pm=(FaalRequestParam)rp.getParams().get(i);
						codes[i]=pm.getName();
						value[i]=pm.getValue();
					}
					for (int i=0;i<tn.length;i++){						
						List<String> meterFrames=new ArrayList<String>();
						if (request.getTransmitType()!=null&&request.getTransmitType().equals("F9")){	//转发主站直接对电表的抄读数据命令
							sDADT=DataItemCoder.getCodeFrom1To1(0,"10F009");
							meterFrames=createF9Data(codes,request.getFixProto());
							param=param.substring(0,2)+"FF"+DataSwitch.ReverseStringByByte(DataSwitch.StrStuff("0", 12, request.getFixAddre(), "left"));
						}
						if(request.getTransmitType()!=null&&request.getTransmitType().equals("F10")){	//转发主站直接对电表的抄读数据命令
							sDADT=DataItemCoder.getCodeFrom1To1(0,"10F010");
							param=param.substring(0,2)+"00"+DataSwitch.ReverseStringByByte(DataSwitch.StrStuff("0", 12, request.getFixAddre(), "left"));
						}
						else{									//通明转发下行的数据单元标识
							sDADT=DataItemCoder.getCodeFrom1To1(0,"10F001");
							if(request instanceof UpdateCollectorKeyRequest){
								
								String newKeyMsg=createCollectorNewKeyMsg((UpdateCollectorKeyRequest) request,tn[i]);
								
								meterFrames.add(newKeyMsg);
							}else if(request instanceof FaalHX645RelayRequest){//海兴645中继转发
								HX645MeterParser parser = new HX645MeterParser();
								String msg = DataItemCoder.constructor(""+tn[i],"HTB2")+HexDump.toHex(parser.constructor((FaalHX645RelayRequest)request));
								meterFrames.add(msg);
							}else{
								meterFrames=createMeterFrame(""+tn[i],rtu,codes,value,request.getFixProto(),request.getFixAddre(),request.getBroadcastAddress(),request.getBroadcast(),request.getEndata());	
							}
						}	
						if(request.getTransmitType()!=null&&request.getTransmitType().equals("F10")){
							sdata=sDADT+param+value[0];
							MessageGwHead head=new MessageGwHead();
							//设置报文头相关信息
							head.rtua=rtu.getRtua();
							
							MessageGw msg=new MessageGw();
							msg.head=head;
							msg.setAFN((byte)request.getType());
							msg.data=HexDump.toByteBuffer(sdata+pw);
							if (!tp.equals(""))//下行带时间标签则设置Aux
								msg.setAux(HexDump.toByteBuffer(tp), true);
							msg.setCmdId(rp.getCmdId());
							msg.setMsgCount(meterFrames.size()*tn.length);
							rt.add(msg);
						}
						for(int j=0;j<meterFrames.size();j++){
							if (request.getTransmitType()!=null&&request.getTransmitType().equals("F9")){//转发主站直接对电表的抄读数据命令
								sdata=sDADT+param+meterFrames.get(j);
							}
							else										//通明转发
								sdata=sDADT+param+DataItemCoder.constructor(""+meterFrames.get(j).length()/2,"HTB2")+meterFrames.get(j);
							MessageGwHead head=new MessageGwHead();
							//设置报文头相关信息
							head.rtua=rtu.getRtua();
							
							MessageGw msg=new MessageGw();
							msg.head=head;
							msg.setAFN((byte)request.getType());
							msg.data=HexDump.toByteBuffer(sdata+pw);
							if (!tp.equals(""))//下行带时间标签则设置Aux
								msg.setAux(HexDump.toByteBuffer(tp), true);
							msg.setCmdId(rp.getCmdId());
							msg.setMsgCount(meterFrames.size()*tn.length);
							rt.add(msg);
						}						
					}					
				}				
			}
		}catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null&&rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			
			rt.toArray(msgs);
			return msgs;
        }
		else
			return null;  
	}
	private List<String> createF9Data(String[] codes,String fixProto){
		List<String> data=new ArrayList<String>();
		IMeterParser mparser=null;
		if(fixProto==null){			
			throw new MessageEncodeException("表规约非法:"+fixProto);
		}
		mparser=MeterParserFactory.getMeterParser(fixProto);
		if(mparser==null)
			throw new MessageEncodeException("不支持的表规约");
		String[] dks=null;
		String type="";//数据标识类型
		if(fixProto.equals(Protocol.BBMeter07)){//07版全国电表规约数据项标识取特殊字段
			dks=mparser.getMeter2Code(codes);	
			type="01";
		}
		else if(fixProto.equals(Protocol.BBMeter97)){
			dks=mparser.convertDataKey(mparser.getMeter1Code(codes));
			type="00";
		}
		else
			throw new MessageEncodeException("不支持的表规约");
	
		for(int k=0;k<dks.length;k++){
			if(dks[k]==null || dks[k].length()<=0){
				break;
			}
			if(fixProto.equals(Protocol.BBMeter97))//补足字节数
				data.add(type+DataSwitch.ReverseStringByByte(dks[k])+"0000");	
			else
				data.add(type+DataSwitch.ReverseStringByByte(dks[k]));	
		}
		return data;
	}
	private List<String> createMeterFrame(String tn,BizRtu rtu,String[] codes,String[] value,String fixProto,String fixAddre,String broadcastAddress,boolean broadcast,String endata){
		List<String> meterFrame=new ArrayList<String>();
		IMeterParser mparser=null;
		if(fixProto==null){			
			throw new MessageEncodeException("表规约非法:"+fixProto);
		}
		if(fixAddre==null&&broadcastAddress==null){			
			fixAddre=getBroadcastAddress(fixProto);
		}
		mparser=MeterParserFactory.getMeterParser(fixProto);
		if(mparser==null)
			throw new MessageEncodeException("不支持的表规约");
		else{
			//华立的特殊地址
			if(fixProto.equals(Protocol.ZJMeter)){
				if(fixAddre.length()>2){
					String xxa=fixAddre.substring(fixAddre.length()-2);			        				
					if(xxa.equalsIgnoreCase("AA")){
						//错误
						fixAddre=fixAddre.substring(0,2);
					}else{
						fixAddre=xxa;
					}
				}
			}
		}
		DataItem dipara=new DataItem();
		dipara.addProperty("point",fixAddre);
		String[] dks=null;
		if(fixProto.equals(Protocol.BBMeter07)){//07版全国电表规约数据项标识取特殊字段
			dks=mparser.getMeter2Code(codes);
			if (dks!=null&&dks.length>0&&dks[0].equals("070000FF"))//身份认证
				dipara.addProperty("identify",endata);
			else if(dks!=null&&dks.length>0&&(dks[0].equals("0700000100")))//电表控制命令
				dipara.addProperty("userControl",endata);
			else if(dks!=null&&dks.length>0&&(dks[0].equals("070204FF")
											||dks[0].equals("070201FF")
											||dks[0].equals("070202FF")
											||dks[0].equals("070203FF")))//主控、远程控制、参数、身份认证密钥更新
				dipara.addProperty("keyUpdate",endata);
			else if(dks!=null&&dks.length>0&&dks[0].equals("04000103")){
				dipara.addProperty("time", endata);
			}
		}
		else if(fixProto.equals(Protocol.BBMeter97)){
			dks=mparser.convertDataKey(mparser.getMeter1Code(codes));
		}
		else
			dks=mparser.getMeter1Code(codes);

		int msgcount=0;			
		for(int k=0;k<dks.length;k++){
			if(dks[k]==null || dks[k].length()<=0){
				break;
			}
			if(value[k]!=null){
				dipara.addProperty("write", value[k]);
			}else{
				dipara.addProperty("write", null);
			}
			byte[] cmd=mparser.constructor(new String[]{dks[k]},dipara);
			//上行异常不带数据项，这里讲数据项放到rtu里面
			rtu.addParamToMap(9999, dks[k]);
			if(cmd==null){
				StringBuffer se=new StringBuffer();
				for(int j=0;j<codes.length;j++){
					se.append(codes[j]);
					se.append(" ");
				}
				throw new MessageEncodeException("不支持召测的表规约数据："+se.toString()+"  RTU:"+ParseTool.IntToHex4(rtu.getRtua()));
			}
			else{
				String frame=HexDump.hexDumpCompact(cmd, 0, cmd.length);
				if(!fixProto.equals("20"))//非浙规电表规约，加4个字节FE的前缀
					frame="FEFEFEFE"+frame;
				meterFrame.add(frame);
			}
			msgcount++;
		}
		return meterFrame;
	}
	
	/**
	 * 生成采集器密钥消息
	 * @param request
	 * @param tn
	 * @return
	 * @throws InvalidCipherTextException
	 */
	private String createCollectorNewKeyMsg(UpdateCollectorKeyRequest request,int tn) throws InvalidCipherTextException{
		List<FaalRequestRtuParam> params = request.getRtuParams();
		if(params == null || params.size()!=1){
			throw new RuntimeException("params must not null.");
		}
		
		//更新采集器密钥
		String masterKey = "00000000000000000000000000000000";
		String iv = "000000000000000000000000";
		String plain = "000000000000000000"+HexDump.toHex((byte)( request).getKeyVersion())+DataSwitch.StrStuff("0",12,request.getUpdateKey(),"left");
		byte[] enc=AESGcm128.encrypt(HexDump.toArray(masterKey), HexDump.toArray(iv), HexDump.toArray(plain), new byte[0]);
		RFC3394WrapEngine rfc = new RFC3394WrapEngine(new AESEngine());
		rfc.init(true, new KeyParameter(HexDump.toArray(masterKey)));
		String newKey = HexDump.toHex(rfc.wrap(enc, 0, 16));
		
		String msg = "11C0"+	//ID
				     "00000000"+ //密码
					 HexDump.toHex((byte)request.getKeyVersion())+//keyVersion
					 newKey;//密钥
		byte[] allFrame = new byte[44];
		allFrame[0]=0x68;
		ParseTool.HexsToBytesAA(allFrame,1,request.getCollectorNo(),6,(byte)0xFF);
		allFrame[7]=0x68;
		allFrame[8]=0x00;
		allFrame[9]=0x04;
		byte[] frame=HexDump.toArray(msg);
		allFrame[10]=(byte)frame.length;
		for(int i = 0 ; i<frame.length;i++){
			allFrame[(i+1)+10] = (byte)(frame[i]+0x33);
		}
		allFrame[42]=ParseTool.calculateCS(allFrame, 0, 42);
		allFrame[43]=0x16;
		return DataItemCoder.constructor(""+tn,"HTB2")+HexDump.toHex(allFrame);
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
}
