package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import org.bouncycastle.crypto.engines.AESEngine;
import org.bouncycastle.crypto.engines.RFC3394WrapEngine;
import org.bouncycastle.crypto.params.KeyParameter;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWupdateKeyRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.EsamUtil;
import cn.hexing.util.HexDump;

import com.hx.dlms.cipher.AESGcm128;

/**
 * 更新非对称密钥
 * @author Administrator
 *
 */
public class C06MessageEncoder extends AbstractMessageEncoder{

	/**更新主站公钥*/
	private static final String UPDATE_MASTER_PUBKEY = "06F020";
	
	/**更新集中器公钥*/
	private static final String UPDATE_TERMINAL_PUBKEY = "06F021";
	
	/**更新采集器密钥*/
	private static final String UPDATE_COLLECTOR_KEY="06F022";
	@Override
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();		
		try{
			if(obj instanceof FaalGWupdateKeyRequest){
				FaalGWupdateKeyRequest request=(FaalGWupdateKeyRequest)obj;

				String sDADT="",sValue="",sdata="",tp="",pw="";
				//终端参数列表
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				int paramsSize = rtuParams.size();
				for (int n = 0 ; n < paramsSize;n++){
					FaalRequestRtuParam rp = rtuParams.get(n);
					sdata="";
					BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
					if(rtu==null){
						throw new MessageEncodeException("终端信息未在缓存列表："+rp.getRtuId());
					}
					int fseq = request.getFseq()[n];
					
					//测量点参数列表
					List<FaalRequestParam> params=rp.getParams();
					int[] tn=rp.getTn();
					for (int i=0;i<tn.length;i++){
						for (FaalRequestParam pm:params){
							tn[i] = 0;
							sDADT=DataItemCoder.getCodeFrom1To1(tn[i],pm.getName());//数据单元标识
							if(UPDATE_MASTER_PUBKEY.equals(pm.getName())){  //主站下发更新主站公钥密令，带48字节的主站公钥
								
								//调用加密机，产生48字节公钥
								if(request.getFlag()==0){
									sValue = EsamUtil.getInstance().createSPK(rtu, fseq);
									//对生成的公钥进行加密
									sValue=EsamUtil.getInstance().encript(rtu, sValue, fseq, 0,"");
								}else if(request.getFlag()==1){
									//16字节随机数
									sValue = pm.getValue();
									sValue=EsamUtil.getInstance().encript(rtu, sValue, fseq,2,"");
								}
								
								//flag#fseq#value;
								sValue=HexDump.toHex((byte)request.getFlag())+HexDump.toHex(fseq)+sValue;
								
							}else if(UPDATE_TERMINAL_PUBKEY.equals(pm.getName())){//更新集中器本身公钥
								//是否首次FLGA+seq+0xAA,0xAA
								//将数据使用集中器主控密钥加密,数据区使用 0xaa,0xaa加密
								sValue = HexDump.toHex(new byte[]{(byte) 0xaa,(byte) 0xaa});
								
								String signature="";
								boolean isFirstTime=rtu.getAsymmetricKeyVersion()==0;
								if(!isFirstTime){//如果不是首次
									//使用0xAA,0xAA进行签名
									signature=EsamUtil.getInstance().priKeySign(1, sValue);
								}
								
								//使用加密主密钥进行加密
								sValue=EsamUtil.getInstance().encript(rtu, sValue, fseq,0,"");
								
								sValue = HexDump.toHex((byte)(isFirstTime?0:1))+HexDump.toHex(fseq)+sValue+signature;
							}else if(UPDATE_COLLECTOR_KEY.equals(pm.getName())){//更新采集器密钥
								String masterKey = "00000000000000000000000000000000";
								String iv = "000000000000000000000000";
								String plain = "000000000000000000"+HexDump.toHex((byte)( request).getKeyVersion())+DataSwitch.StrStuff("0",12,""+((FaalGWupdateKeyRequest) request).getCollectorNo(),"left");
								byte[] enc=AESGcm128.encrypt(HexDump.toArray(masterKey), HexDump.toArray(iv), HexDump.toArray(plain), new byte[0]);
								RFC3394WrapEngine rfc = new RFC3394WrapEngine(new AESEngine());
								rfc.init(true, new KeyParameter(HexDump.toArray(masterKey)));
								String newKey = HexDump.toHex(rfc.wrap(enc, 0, 16));
								
								
								sValue = HexDump.toHex((byte)request.getKeyVersion())+//keyVersion
										 newKey;//密钥
								
							}
							
							sdata=sdata+sDADT+sValue;
						} 
					}
				
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
					msg.setMsgCount(1);
					rt.add(msg);
				}				
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null&&rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;  
	}
	
}
