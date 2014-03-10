package cn.hexing.fk.bp.dlms.cipher;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import org.apache.axis.utils.StringUtils;
import org.apache.log4j.Logger;

import cn.hexing.em.client.EmResult;
import cn.hexing.em.client.EmUtil;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.util.HexDump;

import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.cipher.CipherUtil;

public class EsamCipher implements IDlmsCipher {
	private int sgc = 1;		//Supply group code
	private static EsamCipher instance = new EsamCipher();
	TraceLog trace = TraceLog.getTracer(EsamCipher.class);
	public static final Logger log = Logger.getLogger(EsamCipher.class);
	public static final EsamCipher getInstance(){
		return instance;
	}
	private EsamCipher(){}
	
	public static final String KEY_TYPE_MK = "10";
	public static final String KEY_TYPE_EK = "11";
	public static final String KEY_TYPE_AK = "12";
	public static final String KEY_TYPE_CAST = "13";
	
	private static final byte[] makeAssociationData(byte[] authKey, byte sc){
		if( null == authKey || authKey.length == 0 )
			return null;
		ByteBuffer buf = ByteBuffer.allocate(authKey.length+1);
		buf.put(sc).put(authKey);
		buf.flip();
		return buf.array();
	}
	
	private final String getMeterFactor(DlmsContext context, String keyType){
		if( StringUtils.isEmpty(context.meterId))
			return "";
		StringBuilder sb = new StringBuilder(32);
		int len = context.meterId.length();
		if( len == 12 )
			sb.append(context.meterId);
		else if( len > 12 ){
			sb.append(context.meterId.substring(len-12));
		}
		else{
			for(int i=0; i<12-len; i++)
				sb.append('0');
			sb.append(context.meterId);
		}
		sb.append(HexDump.toHex(sgc).substring(2));
		sb.append(keyType);
		sb.append(HexDump.toHex((byte)(context.keyVersion & 0xFF)));
		while( sb.length()<32 )
			sb.append('0');
		return sb.toString();
	}
	
	private final String getDCFactor(DlmsContext context, String keyType){
		if( ! context.isRelay )
			return "00000000000100000110000000000000";
		StringBuilder sb = new StringBuilder(32);
		if( context.meterType == 0 && context.meterId.length()==8 ){
			if( context.keyVersion == 0 )
				sb.append("000000000001");
			else
				sb.append("0000").append(context.meterId);
		}
		else{
			if( context.webReqList.size()==0 )
				return "00000000000100000110000000000000";
			DlmsEvent evt = (DlmsEvent)context.webReqList.get(0);
			RelayParam relayParam = evt.getDlmsRequest().getRelayParam();
			if( null == relayParam )
				return "00000000000100000110000000000000";
			if( context.keyVersion == 0 )
				sb.append("000000000001");
			else
				sb.append("0000").append(relayParam.getDcLogicalAddress());
		}
		sb.append(HexDump.toHex(sgc).substring(2));
		sb.append(keyType);
		sb.append(HexDump.toHex((byte)(context.keyVersion & 0xFF)));
		while( sb.length()<32 )
			sb.append('0');
		return "00000000000100000110000000000000";
	}

	@Override
	public byte[] auth(DlmsContext context, byte[] plain, byte[] initVector)
			throws IOException {
		String meterFactor = "";
		if(context.keyVersion ==0){
			meterFactor = "00000000000100000111000000000000";
		}else{
			 meterFactor= this.getMeterFactor(context, KEY_TYPE_EK);
		}
		String dcFactor = this.getDCFactor(context, KEY_TYPE_EK);
		try {
			byte[] associatedText = makeAssociationData(context.authKey,(byte)0x10);
			associatedText = CipherUtil.cat(associatedText,plain);
			int devType = context.isRelay ? 2 : 1;
			int initFlag = context.keyVersion == 0 ? 0 : context.isRelay ? 2:1;
			int keyType = 0;
			String keyVersion = HexDump.toHex((byte)(context.keyVersion&0xFF));
			EmResult er = EmUtil.gCMEncryption(devType,initFlag,keyType,meterFactor,dcFactor,keyVersion,HexDump.toHex(initVector),HexDump.toHex(associatedText),"");
			if(er.isSuccess()){
				return HexDump.toArray(er.getRtnMap().get("pCipherData").substring(0,12*2));
			}
			log.error("auth failed.Result code:"+er.getResultCode());
			throw new RuntimeException("auth failed.Result code:"+er.getResultCode());
		} catch (RemoteException exp) {
			throw new IOException(exp);
		}
	}

	@Override
	public byte[] encrypt(DlmsContext context, byte[] plain, byte[] initVector)
			throws IOException {
		String meterFactor = "";
		if(context.keyVersion ==0){
			meterFactor = "00000000000100000111000000000000";
		}else{
			 meterFactor= this.getMeterFactor(context, KEY_TYPE_EK);
		}
		String dcFactor = this.getDCFactor(context, KEY_TYPE_EK);
		try {
			byte[] at = makeAssociationData(context.authKey,(byte)0x30 );
			
			int devType = context.isRelay ? 2 : 1;
			if( context.meterType == 0 )
				devType = 0;
			int initFlag = context.keyVersion == 0 ? 0 : context.isRelay ? 2:1;
			int keyType = 0;
			String keyVersion = HexDump.toHex((byte)(context.keyVersion&0xFF));
			EmResult er = EmUtil.gCMEncryption(devType,initFlag,keyType,meterFactor,dcFactor,keyVersion,HexDump.toHex(initVector),HexDump.toHex(at),HexDump.toHex(plain));
			if(er.isSuccess()){
				return HexDump.toArray(er.getRtnMap().get("pCipherData").substring(0, (plain.length+12)*2));
			}
			log.error("encrypt failed.Result code:"+er.getResultCode());
			throw new RuntimeException("encrypt failed.Result code:"+er.getResultCode());
		} catch (RemoteException exp) {
			throw new IOException(exp);
		}
	}

	@Override
	public byte[] decrypt(DlmsContext context, byte[] ciphered, byte[] initVector) throws IOException {
		String meterFactor = "";
		if(context.keyVersion ==0){
			meterFactor = "00000000000100000111000000000000";
		}else{
			 meterFactor= this.getMeterFactor(context, KEY_TYPE_EK);
		}
		String dcFactor = this.getDCFactor(context, KEY_TYPE_EK);
		try {
			byte[] ad = makeAssociationData(context.authKey,(byte)0x30);
			
			int devType = context.isRelay ? 2 : 1;
			if( context.meterType == 0 )
				devType = 0;
			int initFlag = context.keyVersion == 0 ? 0 : context.isRelay ? 2:1;
			int keyType = 0;
			String keyVersion = HexDump.toHex((byte)(context.keyVersion&0xFF));
			EmResult er = EmUtil.gCMDecryption(devType,initFlag,keyType,meterFactor,dcFactor,keyVersion,HexDump.toHex(initVector),HexDump.toHex(ad),HexDump.toHex(ciphered));
			if(er.isSuccess()){
				return HexDump.toArray(er.getRtnMap().get("pPlainData"));
			}
			log.error("decrypt failed.Result code:"+er.getResultCode());
			throw new RuntimeException("decrypt failed.Result code:"+er.getResultCode());
		} catch (RemoteException exp) {
			throw new IOException(exp);
		}
	}
	
	public byte[] createGcmNewKey(DlmsContext context, byte[] plain, byte[] initVector)throws IOException{
		String meterFactor = this.getMeterFactor(context, KEY_TYPE_EK);
		String dcFactor = this.getDCFactor(context, KEY_TYPE_EK);
		try {
			byte[] ad = makeAssociationData(context.authKey,(byte)0x30);
			
			int devType = context.isRelay ? 2 : 1;
			if( context.meterType == 0 )
				devType = 0;
			int initFlag = context.isRelay?2:1;// 2 表示 集中器不更新，电表更新(目前集中器不更新，所以此处设置为2)    1表示 都更新过(目前集中器不更新密钥，此处不考虑)。
			int keyType = context.keyVersion == 0 ? 0 : 1;  
			int kv = context.keyVersion+1;
			if( kv>255 )
				kv = 1;
			String keyVer = HexDump.toHex((byte)kv);
			if(trace.isEnabled()){
				trace.trace("meter:"+context.meterId+" update keyVersion, current keyVersion:"+context.keyVersion+", next keyVersion:"+kv);
			}
			EmResult er = EmUtil.updateSYMKey(devType,initFlag,keyType,meterFactor,dcFactor,keyVer,HexDump.toHex(initVector),HexDump.toHex(ad));
			if(er.isSuccess()){
				return HexDump.toArray(er.getRtnMap().get("pCipherKey"));
			}
			log.error("createGcmNewKey failed.Result code:"+er.getResultCode());
			throw new RuntimeException("createGcmNewKey failed.Result code:"+er.getResultCode());
		} catch (RemoteException exp) {
			throw new IOException(exp);
		}
	}

}
