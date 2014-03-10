package cn.hexing.util;

import java.nio.ByteBuffer;
import java.rmi.RemoteException;

import cn.hexing.em.client.EmResult;
import cn.hexing.em.client.EmUtil;
import cn.hexing.fk.model.BizRtu;

public class EsamUtil {

	private static EsamUtil instance  = new EsamUtil();
	
	private EsamUtil(){};
	
	public static EsamUtil getInstance(){return instance;}; 
	
	/**
	 * 生成主站公钥
	 * @return
	 * @throws RemoteException 
	 */
	public String createSPK(BizRtu rtu,int fseq) throws RemoteException{
		
		String meterFactor = getMeterFactor();
		
		String concentFactor = getDCFactor(rtu, "0");
		
		//这里只对集中器操作,默认为0
		int iUpdateType = 0;
		
		//这个地方默认为1
		int iUpdateFlag= 1 ;
		
		String keyVersion = "0"; //TODO:这里未来可变,如果更新了集中器非对称密钥，这里要判断
		
		String iv = getIv(rtu.getLogicAddress(),fseq);
		
		EmResult result = EmUtil.createSPK(iUpdateType, iUpdateFlag, meterFactor, concentFactor, keyVersion, iv, "");
		if(result.isSuccess()){
			return result.getRtnMap().get("pCipherSPK");
		}else{
			throw new RuntimeException("CreateSPK Failed,Error Code "+result.getResultCode());
		}
	}
	
	/**
	 * 解密函数
	 * @return
	 * @throws RemoteException 
	 */
	public String decript(BizRtu rtu,String cipherData,int fseq,int iKeyType,String pAdd) throws RemoteException{

		String meterFactor = getMeterFactor();
		
		String concentFactor = getDCFactor(rtu, "0");
		
		String iv = getIv(rtu.getLogicAddress(),fseq);
		
		int iKeyVersion = rtu.getSymmetricKeyVersion();
		int iUpdateFlag = iKeyVersion>0?1:0;
		String keyVersion = HexDump.toHex((byte)iKeyVersion);
		
		//这里只对集中器操作,默认为0
		int iUpdateType = 0;

		
		EmResult result=EmUtil.gCMEncryption(iUpdateType, iUpdateFlag, iKeyType, meterFactor, concentFactor, keyVersion, iv, pAdd,cipherData);
		
		if(result.isSuccess()){
			return result.getRtnMap().get("pCipherData");
		}else{
			throw new RuntimeException("Decript Failed,Error Code "+result.getResultCode());
		}
		
	}
	
	
	
	
	/**
	 * 
	 * @param rtu
	 * @param plainData
	 * @param fseq
	 * @param iKeyType  加密类型  2表示主控密钥
	 * @return
	 * @throws RemoteException
	 */
	public String encript(BizRtu rtu,String plainData,int fseq,int iKeyType,String pAdd) throws RemoteException{
		
		String meterFactor = getMeterFactor();
		
		String concentFactor = getDCFactor(rtu, "0");
		
		String iv = getIv(rtu.getLogicAddress(),fseq);
		
		//TODO:这里未来可变,如果更新了集中器非对称密钥，这里要判断
		String keyVersion = "00"; 
		
		//这里根据keyVersion判断,keyVersion>0,这里设置为1，否则设置为0
		int iUpdateFlag = 0;
		
		//这里只对集中器操作,默认为0
		int iUpdateType = 0;

		//生成主站公钥不传ADD,只加密，无TAG
		EmResult result=EmUtil.gCMEncryption(iUpdateType, iUpdateFlag, iKeyType, meterFactor, concentFactor, keyVersion, iv, pAdd,plainData);
		
		if(result.isSuccess()){
			return result.getRtnMap().get("pCipherData");
		}else{
			throw new RuntimeException("Encript Failed,Error Code "+result.getResultCode());
		}
		
	}
	
	/**
	 * 私钥签名
	 * @param updateFlag
	 * @param pData
	 * @return
	 * @throws RemoteException
	 */
	public String priKeySign(int updateFlag,String pData) throws RemoteException{
		EmResult result = EmUtil.eCCPriKeySign(updateFlag, pData);
		
		if(result.isSuccess()){
			return result.getRtnMap().get("pSignature");
		}else{
			throw new RuntimeException("PriKeySign Failed,Error Code "+result.getResultCode());
		}
		
	}
	
	/**
	 * 公钥验签
	 * @param string2 
	 * @param string 
	 * @param rtu 
	 * @return
	 * @throws RemoteException 
	 */
	public boolean pubKeyVerify(BizRtu rtu, String pData, String pSignature,String pubKey) throws RemoteException{
		
		int iUpdateFlag = 0;
		
		int iHSType = 0;
		
		EmResult result = EmUtil.eCCPubKeyVeri(iHSType, iUpdateFlag, rtu.getRtuId(), pubKey, pData, pSignature);
		
		if(result.isSuccess()){
			return true;
		}
		return false;
	}

	
	private final String getDCFactor(BizRtu rtu, String keyType){
		return "00000000000100000110000000000000";
	}
	
	/**
	 * 获得表计因子
	 * @return 由于这里与集中器操作，表计因子为默认 
	 */
	private final String getMeterFactor(){
		return "00000000000100000111000000000000";
	}
	
	/**
	 * 获得iv   48 58 45 11 0x xx xx xx 00 00 00 01
	 * 逻辑地址+seqId
	 * @param rtuId
	 * @param fseq
	 * @return iv
	 */
	public String getIv(String rtuId,int fseq)
	{
		ByteBuffer buf = ByteBuffer.allocate(12);
		buf.put((byte)0x48).put((byte)0x58).put((byte)0x45).put((byte)0x11);
		byte[] bytes = HexDump.toArray(rtuId);
		bytes[0] = (byte) (bytes[0] & 0x0F);
		buf.put(bytes);
		buf.putInt( fseq );
		buf.flip();
		return HexDump.toHex(buf.array());
	}
}


