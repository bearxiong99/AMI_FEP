package cn.hexing.em.client;

import java.rmi.RemoteException;

public class Service1SoapProxy implements cn.hexing.em.client.Service1Soap {
  private String _endpoint = null;
  private cn.hexing.em.client.Service1Soap service1Soap = null;
  
  public Service1SoapProxy() {
    _initService1SoapProxy();
  }
  
  public Service1SoapProxy(String endpoint) {
    _endpoint = endpoint;
    _initService1SoapProxy();
  }
  
  private void _initService1SoapProxy() {
    try {
      service1Soap = (new cn.hexing.em.client.Service1Locator()).getService1Soap();
      if (service1Soap != null) {
        if (_endpoint != null)
          ((javax.xml.rpc.Stub)service1Soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
        else
          _endpoint = (String)((javax.xml.rpc.Stub)service1Soap)._getProperty("javax.xml.rpc.service.endpoint.address");
      }
      
    }
    catch (javax.xml.rpc.ServiceException serviceException) {}
  }
  
  public String getEndpoint() {
    return _endpoint;
  }
  
  public void setEndpoint(String endpoint) {
    _endpoint = endpoint;
    if (service1Soap != null)
      ((javax.xml.rpc.Stub)service1Soap)._setProperty("javax.xml.rpc.service.endpoint.address", _endpoint);
    
  }
  
  public cn.hexing.em.client.Service1Soap getService1Soap() {
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap;
  }
  
  public java.lang.String gCMEncryption(int iHSType, int iUpdateFlag, int ikeyType, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd, java.lang.String pPlainData) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.gCMEncryption(iHSType, iUpdateFlag, ikeyType, pMeterFactor, pConcentFactor, pKeyVersion, pIv, pAdd, pPlainData);
  }
  
  public java.lang.String gCMDecryption(int iHSType, int iUpdateFlag, int ikeyType, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd, java.lang.String pCipherData) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.gCMDecryption(iHSType, iUpdateFlag, ikeyType, pMeterFactor, pConcentFactor, pKeyVersion, pIv, pAdd, pCipherData);
  }
  
  public java.lang.String eCCPriKeySign(int iUpdateFlag, java.lang.String pData) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.eCCPriKeySign(iUpdateFlag, pData);
  }
  
  public java.lang.String eCCPubKeyVeri(int iHSType, int iUpdateFlag, java.lang.String pEquipIdx, java.lang.String pECCPubKey, java.lang.String pData, java.lang.String pSignature) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.eCCPubKeyVeri(iHSType, iUpdateFlag, pEquipIdx, pECCPubKey, pData, pSignature);
  }
  
  public java.lang.String createSPK(int iUpdateType, int iUpdateFlag, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.createSPK(iUpdateType, iUpdateFlag, pMeterFactor, pConcentFactor, pKeyVersion, pIv, pAdd);
  }
  
  public java.lang.String createKeyPair(int iUpdateType, int iUpdateFlag, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.createKeyPair(iUpdateType, iUpdateFlag, pMeterFactor, pConcentFactor, pKeyVersion, pIv, pAdd);
  }
  
  public java.lang.String updateSYMKey(int iUpdateType, int iUpdateFlag, int iCurrKeyVersion, java.lang.String pMeterFactor, java.lang.String pConcentFactor, java.lang.String pKeyVersion, java.lang.String pIv, java.lang.String pAdd) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.updateSYMKey(iUpdateType, iUpdateFlag, iCurrKeyVersion, pMeterFactor, pConcentFactor, pKeyVersion, pIv, pAdd);
  }
  
  public java.lang.String jmjConnect(java.lang.String pIpaddr, int pPort) throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.jmjConnect(pIpaddr, pPort);
  }
  
  public java.lang.String jmjDisconnect() throws java.rmi.RemoteException{
    if (service1Soap == null)
      _initService1SoapProxy();
    return service1Soap.jmjDisconnect();
  }

	public String exportsSYMKey(int iUpdateType, int iUpdateFlag, int ikeyType,
			String pMeterFactor, String pConcentFactor, int ikeyindex,
			String pKeyVersion) throws RemoteException {
		if (service1Soap == null)
		      _initService1SoapProxy();
		return service1Soap.exportsSYMKey(iUpdateType, iUpdateFlag, ikeyType, pMeterFactor, pConcentFactor, ikeyindex, pKeyVersion);
	}
  
  
}