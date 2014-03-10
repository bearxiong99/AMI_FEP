package cn.hexing.em.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

/** 
 * @Description 加密机内部调用接口
 * @author  jun
 * @Copyright 2012 hexing Inc. All rights reserved
 * @time：2012-9-9
 * @version AMI3.0 
 */
public class EmUtil {
	private static Logger logger = Logger.getLogger(EmUtil.class.getName());
	
	/**
	 * 解析接口返回xml
	 * @param xml
	 * @return
	 */
	public static EmResult parseRtnXml(String xml){
		EmResult emResult = new EmResult();
		Map<String,String> paramMap = new HashMap<String,String>();
		Element root;
		try {
			Document document = DocumentHelper.parseText(xml);
			root = document.getRootElement();
			
			//0---正常返回
			//0以外---异常返回
			Element resultNode = root.element("result");
			String resultText = resultNode.getText()==null?"":resultNode.getText();
			if (resultText.equals("0")) {
				emResult.setSuccess(true);
				
				List<?> elementsList = root.elements();
				for (int i = 0; i < elementsList.size(); i++) {
					Element elementTmp = (Element)elementsList.get(i);
					//过滤result节点
					if (!elementTmp.getName().equals("result")) {
						paramMap.put(elementTmp.getName(), elementTmp.getText());
					}
				}
				emResult.setRtnMap(paramMap);
			}else{
				emResult.setSuccess(false);
				emResult.setRtnMap(paramMap);
				emResult.setResultCode(Integer.parseInt(resultText));
			}
		} catch (DocumentException e) {
			logger.error("Encript machine explain frame error："+e.getMessage());
		}
		
		return emResult;
	}
	/**
     * GCM加密
     */
    public static EmResult gCMEncryption(int iHSType, int iUpdateFlag, int ikeyType, String pMeterFactor, String pConcentFactor,String pKeyVersion, String pIv, String pAdd, String pPlainData) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.gCMEncryption(iHSType,iUpdateFlag,ikeyType,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd,pPlainData);
    	return parseRtnXml(xml);
    }

    /**
     * GCM解密
     */
    public static EmResult gCMDecryption(int iHSType, int iUpdateFlag, int ikeyType, String pMeterFactor, String pConcentFactor,String pKeyVersion, String pIv, String pAdd, String pCipherData) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.gCMDecryption(iHSType,iUpdateFlag,ikeyType,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd,pCipherData);
    	return parseRtnXml(xml);
    }

    /**
     * ECC私钥签名
     */
    public static EmResult eCCPriKeySign(int iUpdateFlag, String pData) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.eCCPriKeySign(iUpdateFlag,pData);
    	return parseRtnXml(xml);
    }

    /**
     * ECC公钥验签
     */
    public static EmResult eCCPubKeyVeri(int iHSType, int iUpdateFlag, String pEquipIdx, String pECCPubKey, String pData, String pSignature) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.eCCPubKeyVeri(iHSType,iUpdateFlag,pEquipIdx,pECCPubKey,pData,pSignature);
    	return parseRtnXml(xml);
    }

    /**
     * 生成主站公钥
     */
    public static EmResult createSPK(int iUpdateType, int iUpdateFlag, String pMeterFactor, String pConcentFactor,String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.createSPK(iUpdateType,iUpdateFlag,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    	return parseRtnXml(xml);
    }

    /**
     * 生成密钥对
     */
    public static EmResult createKeyPair(int iUpdateType, int iUpdateFlag, String pMeterFactor, String pConcentFactor,String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.createKeyPair(iUpdateType,iUpdateFlag,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    	return parseRtnXml(xml);
    }

    /**
     * 更新对称密钥
     */
    public static EmResult updateSYMKey(int iUpdateType, int iUpdateFlag, int iCurrKeyVersion, String pMeterFactor, String pConcentFactor, String pKeyVersion, String pIv, String pAdd) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.updateSYMKey(iUpdateType,iUpdateFlag,iCurrKeyVersion,pMeterFactor,pConcentFactor,pKeyVersion,pIv,pAdd);
    	return parseRtnXml(xml);
    }

    /**
     * 连接加密机
     */
    public static EmResult jmjConnect(String  pIpaddr, int pPort) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.jmjConnect(pIpaddr, pPort);
    	return parseRtnXml(xml);
    }

    /**
     * 断开加密机
     */
    public static EmResult jmjDisconnect() throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.jmjDisconnect();
    	return parseRtnXml(xml);
    }
    
    /**
     * 密钥导出 
     * EmResult.rtnMap{CipherKey=}
     */
    public static EmResult exportsSYMKey(int iUpdateType, int iUpdateFlag,int ikeyType,String pMeterFactor,String pConcentFactor,
    		int ikeyindex, String pKeyVersion) throws java.rmi.RemoteException{
    	EmService service =  EmService.getInstance();
    	String xml = service.exportsSYMKey(iUpdateType, iUpdateFlag, ikeyType, pMeterFactor, pConcentFactor, ikeyindex, pKeyVersion);
    	return parseRtnXml(xml);
    }
}
