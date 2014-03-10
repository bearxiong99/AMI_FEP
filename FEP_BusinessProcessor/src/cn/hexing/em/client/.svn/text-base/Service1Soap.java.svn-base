/**
 * Service1Soap.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package cn.hexing.em.client;

public interface Service1Soap extends java.rmi.Remote {

    /**
     * GCM加密
     */
    public java.lang.String gCMEncryption(int iHSType, int iUpdateFlag, int ikeyType, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd, java.lang.String pPlainData) throws java.rmi.RemoteException;

    /**
     * GCM解密
     */
    public java.lang.String gCMDecryption(int iHSType, int iUpdateFlag, int ikeyType, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd, java.lang.String pCipherData) throws java.rmi.RemoteException;

    /**
     * ECC私钥签名
     */
    public java.lang.String eCCPriKeySign(int iUpdateFlag, java.lang.String pData) throws java.rmi.RemoteException;

    /**
     * ECC公钥验签
     */
    public java.lang.String eCCPubKeyVeri(int iHSType, int iUpdateFlag, java.lang.String pEquipIdx, java.lang.String pECCPubKey, java.lang.String pData, java.lang.String pSignature) throws java.rmi.RemoteException;

    /**
     * 生成主站公钥
     */
    public java.lang.String createSPK(int iUpdateType, int iUpdateFlag, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException;

    /**
     * 生成密钥对
     */
    public java.lang.String createKeyPair(int iUpdateType, int iUpdateFlag, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException;

    /**
     * 更新对称密钥
     */
    public java.lang.String updateSYMKey(int iUpdateType, int iUpdateFlag, int iCurrKeyVersion, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException;

    /**
     * 连接加密机
     */
    public java.lang.String jmjConnect(java.lang.String pIpaddr, int pPort) throws java.rmi.RemoteException;

    /**
     * 断开加密机
     */
    public java.lang.String jmjDisconnect() throws java.rmi.RemoteException;

    /**
     * HHU密钥导出
     */
    public java.lang.String exportsSYMKey(int iUpdateType, int iUpdateFlag, int ikeyType, java.lang.String pMeterFactor, java.lang.String pConcentFactor, int ikeyindex, java.lang.String pKeyVersion) throws java.rmi.RemoteException;
}
