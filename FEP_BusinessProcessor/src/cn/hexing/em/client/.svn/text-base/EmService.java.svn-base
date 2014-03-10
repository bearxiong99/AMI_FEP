package cn.hexing.em.client;

import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.apache.log4j.Logger;

/**
 * @Description 加密机接口 Libs=axis.jar,commons-discovery-0.2.jar,dom4j-1.6.1.jar,jaxrpc.jar,saaj.jar,wsdl4j-1.6.2.jar
 * @author  jun
 * @Copyright 2012 hexing Inc. All rights reserved
 * @time：2012-9-9
 * @version AMI3.0
 */
public class EmService {
	private static Logger logger = Logger.getLogger(EmService.class.getName());
	private static EmService instance = null;
	private String serviceUrl = null;
	private String emHost = null;   //Encryption machine host ip
	private String emPort = null;
	private Service1Soap serviceSoap = null;
	private boolean connected = false;
	
	public static final EmService getInstance(){
		if( null == instance )
			instance = new EmService();
		return instance;
	}
	
	private EmService(){
		Service1Locator locator = new Service1Locator();
		try {
			if( null == serviceUrl ){
				serviceUrl = System.getProperty("encryption.service.url");
			}
			if( null == emHost ){
				emHost = System.getProperty("encryption.em.host");
				emPort = System.getProperty("encryption.em.port");
			}
			locator.setEndpointAddress("Service1Soap",serviceUrl);
			serviceSoap = locator.getService1Soap();
			String ret = serviceSoap.jmjConnect(emHost, Integer.parseInt(emPort));
			EmResult result = EmUtil.parseRtnXml(ret);
			if( result.isSuccess() )
				connected = true;
		} catch (ServiceException e) {
			logger.error("call encryption machine interface fail:"+e.getMessage());
		} catch (RemoteException e) {
			logger.error("call connecting to machine fail:"+e.getMessage());
		}
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	 /**
     * GCM加密
     */
	public String gCMEncryption(int iHSType, int iUpdateFlag, int ikeyType, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd, String pPlainData) throws java.rmi.RemoteException{
		return serviceSoap.gCMEncryption(iHSType,iUpdateFlag,ikeyType,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd,pPlainData);
	}


    /**
     * GCM解密
     */
	public String gCMDecryption(int iHSType, int iUpdateFlag, int ikeyType, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd, String pCipherData) throws java.rmi.RemoteException{
		return serviceSoap.gCMDecryption(iHSType,iUpdateFlag,ikeyType,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd,pCipherData);
    }

    /**
     * ECC私钥签名
     */
    public String eCCPriKeySign(int iUpdateFlag, String pData) throws java.rmi.RemoteException{
    	return serviceSoap.eCCPriKeySign(iUpdateFlag,pData);
    }

    /**
     * ECC公钥验签
     */
    public String eCCPubKeyVeri(int iHSType, int iUpdateFlag, String pEquipIdx, String pECCPubKey, String pData, String pSignature) throws java.rmi.RemoteException{
    	return serviceSoap.eCCPubKeyVeri(iHSType,iUpdateFlag,pEquipIdx,pECCPubKey,pData,pSignature);
    }

    /**
     * 生成主站公钥
     */
    public String createSPK(int iUpdateType, int iUpdateFlag, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	return serviceSoap.createSPK(iUpdateType,iUpdateFlag,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    }

    /**
     * 生成密钥对
     */
    public String createKeyPair(int iUpdateType, int iUpdateFlag, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	return serviceSoap.createKeyPair(iUpdateType,iUpdateFlag,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    }

    /**
     * 更新对称密钥
     */
    public String updateSYMKey(int iUpdateType, int iUpdateFlag, int iCurrKeyVersion, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	return serviceSoap.updateSYMKey(iUpdateType,iUpdateFlag,iCurrKeyVersion,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    }

    /**
     * 连接加密机
     */
    public String jmjConnect(String pIpaddr, int pPort) throws java.rmi.RemoteException{
    	return serviceSoap.jmjConnect(pIpaddr, pPort);
    }

    /**
     * 断开加密机
     * iUpdateType		类型				Input	0：集中器；1：GPRS表；2：普通电表
     * iUpdateFlag		标识				Input	0：首次；1：运行中
     * ikeyType			导出密钥类型		Input	0:外部认证密钥；1：集中器加密密钥2：设置密钥&验证密钥
     * pMeterFactor 	表计分散因子		Input
     * pConcentFactor	集中器分散因子	Input	
     * ikeyindex		密钥索引			Input	
     * pKeyVersion		密钥版本			Input		

     */
    public String jmjDisconnect() throws java.rmi.RemoteException{
    	return serviceSoap.jmjDisconnect();
    }
    
    /**
     * 密钥导出
     */
    public String exportsSYMKey(int iUpdateType, int iUpdateFlag,int ikeyType,String pMeterFactor,String pConcentFactor,
    		int ikeyindex, String pKeyVersion) throws java.rmi.RemoteException{
    	return serviceSoap.exportsSYMKey(iUpdateType, iUpdateFlag, ikeyType, pMeterFactor, pConcentFactor, ikeyindex, pKeyVersion);
    }
}
