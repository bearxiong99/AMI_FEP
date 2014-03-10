package cn.hexing.fas.model.dlms;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fk.message.IMessage;

public class DlmsRequest extends FaalRequest {
	private static final long serialVersionUID = 7535502981828814065L;
	public static enum DLMS_OP_TYPE { OP_NA, OP_GET, OP_SET, OP_ACTION, OP_EVENT_NOTIFY, OP_CHANGE_KEY,OP_UPGRADE,OP_HEXINGEXPAND};
	public static enum BUSINESS_CATEGORY { BIZ_GENERIC, BIZ_SECURITY, BIZ_RELAY };
	public static enum REQUEST_MODE{ONLY_GPRS,ONLY_CSD,CSD_GPRS_MIXTURE,GPRS_SERVER };
	
	
	private long commId = -1;
	
	private REQUEST_MODE requestMode = REQUEST_MODE.ONLY_GPRS;
	
	
	private long taskNo = -1;
	
	private String meterId = null;
	private String peerIp = null;	// Only used when DLMS device using fixed-IP module.
	private DLMS_OP_TYPE opType = DLMS_OP_TYPE.OP_NA;  //DLMS support GET,SET,ACTION,EVENT operations.
	private BUSINESS_CATEGORY category = BUSINESS_CATEGORY.BIZ_GENERIC;	//Generic request of get,set,action.
	private DlmsObisItem[] params = null;
	private SecurityKeyParam keyParam = null;
	//表计型号
	private String meterModel;
	
	private String subprotocol = null;
	//DC relay commands related attributes
	private RelayParam relayParam = null;
	
	//dlms relay commands related attributes
	private DlmsRelayParam[] dlmsRelayParams=null;

	private long requestTimeOut = 0;
	
	private short destAddr = 0x0001;//0x0070  is to module

	private int invokeId;
	
	private boolean isOvertime = false;
	
	
	//use to append some params 
	private Map<String,Object> appendParams = new HashMap<String, Object>();

	private ArrayList<IMessage> reqDownMessages = new ArrayList<IMessage>();
	
	private ArrayList<Object> blockReplys = new ArrayList<Object>();

	public final String getMeterId() {
		return meterId;
	}
	public final void setMeterId(String meterId) {
		this.meterId = meterId;
	}
	
	public final String getPeerIp() {
		return peerIp;
	}
	public final void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}

	public final DlmsObisItem[] getParams() {
		return params;
	}
	public final void setParams(DlmsObisItem[] params) {
		this.params = params;
	}
	public final DLMS_OP_TYPE getOpType() {
		return opType;
	}
	public final void setOpType(DLMS_OP_TYPE opType) {
		this.opType = opType;
	}
	
	public boolean validate(){
		return opType != DLMS_OP_TYPE.OP_NA && meterId != null && params != null && params.length>0 ;
	}
	
	public final BUSINESS_CATEGORY getCategory() {
		return category;
	}
	
	public final void setCategory(BUSINESS_CATEGORY category) {
		this.category = category;
	}
	public long getCommId() {
		return commId;
	}
	public void setCommId(long commId) {
		this.commId = commId;
	}
	
	public final RelayParam getRelayParam() {
		return relayParam;
	}
	
	public final boolean isRelay(){
		return null != relayParam;
	}
	
	public final void setRelayParam(String dcLogicalAddress , int measurePoint ) {
		setRelayParam(dcLogicalAddress,measurePoint,31); //Default is PLC
	}
	
	public final void setRelayParam(String dcLogicalAddress,int measurePoint, int port) {
		RelayParam p = new RelayParam();
		p.setDcLogicalAddress(dcLogicalAddress);
		p.setMeasurePoint(measurePoint);
		p.setPort(port);
		
		setRelayParam(p);
	}
	
	public final void setRelayParam(RelayParam relayParam) {
		category = BUSINESS_CATEGORY.BIZ_RELAY;
		this.relayParam = relayParam;
	}
	
	public final SecurityKeyParam getKeyParam() {
		return keyParam;
	}
	
	public final void setESAMGcmKeyChange(){
		this.keyParam = new SecurityKeyParam();
		keyParam.changeESAMGcm();
		category = BUSINESS_CATEGORY.BIZ_SECURITY;
		opType = DLMS_OP_TYPE.OP_CHANGE_KEY;
	}
	
	public final void setESAMKeyPairKeyChange(){
		this.keyParam = new SecurityKeyParam();
		keyParam.changeESAMKeyPair();
		category = BUSINESS_CATEGORY.BIZ_SECURITY;
		opType = DLMS_OP_TYPE.OP_CHANGE_KEY;
	}
	
	public final void setESAMMsPairKeyChange(){
		this.keyParam = new SecurityKeyParam();
		keyParam.changeESAMMsPair();
		category = BUSINESS_CATEGORY.BIZ_SECURITY;
		opType = DLMS_OP_TYPE.OP_CHANGE_KEY;
	}
	
	/**
	 * Used for Encryption-key-Change event. 
	 * @param newEncKey, length is 16 bytes
	 * @param rootKey : Meter root-key used to encrypt newEncKey.
	 */
	public final void setKeyParam(byte[] newEncKey, byte[] rootKey){
		this.keyParam = new SecurityKeyParam();
		keyParam.setNewEncKey(newEncKey);
		keyParam.setRootKey(rootKey);
		category = BUSINESS_CATEGORY.BIZ_SECURITY;
		opType = DLMS_OP_TYPE.OP_CHANGE_KEY;
	}
	
	/**
	 * Used for AES_GCM mode encryption KEY_CHANGE event.
	 * @param newEncKey
	 * @param newAuthKey
	 * @param cipheredEncKey
	 * @param cipheredAuthKey
	 */
	public final void setKeyParam(byte[] newEncKey, byte[] newAuthKey, byte[] cipheredEncKey, byte[] cipheredAuthKey ) {
		this.keyParam = new SecurityKeyParam();
		keyParam.setNewEncKey(newEncKey);
		keyParam.setNewAuthKey(newAuthKey);
		keyParam.setCipheredEncKey(cipheredEncKey);
		keyParam.setCipheredAuthKey(cipheredAuthKey);
		category = BUSINESS_CATEGORY.BIZ_SECURITY;
		opType = DLMS_OP_TYPE.OP_CHANGE_KEY;
	}
	public final String getSubprotocol() {
		return subprotocol;
	}
	public final void setSubprotocol(String subprotocol) {
		this.subprotocol = subprotocol;
	}
	/**获得附加属性*/
	public final Object getAppendParam(String key) {
		if(appendParams ==null) return null;
		
		return appendParams.get(key);
	}
	
	/**
	 * 获得所有参数
	 * @return
	 */
	public final Map<String,Object> getAllParam(){
		return appendParams;
	}
	
	public final void addAllAppendParmas(Map<String,Object> appendParams ){
		if(this.appendParams==null)
			this.appendParams = new HashMap<String, Object>();
		this.appendParams.putAll(appendParams);
	}
	
	public boolean containsKey(String key){
		if(appendParams==null) return false;
		return appendParams.containsKey(key);
	}
	
	public final void removeAppendParam(String key){
		appendParams.remove(key);
	}
	
	/**添加附加属性*/
	public final void addAppendParam(String key,Object value) {
		
		if(appendParams ==null)
			this.appendParams = new HashMap<String, Object>();
		appendParams.put(key,value);
	}
	public final long getTaskNo() {
		return taskNo;
	}
	public final void setTaskNo(long taskNo) {
		this.taskNo = taskNo;
	}
	public final long getRequestTimeOut() {
		return requestTimeOut;
	}
	public final void setRequestTimeOut(long requestTimeOut) {
		this.requestTimeOut = requestTimeOut;
	}
	public String getMeterModel() {
		return meterModel;
	}
	public void setMeterModel(String meterModel) {
		this.meterModel = meterModel;
	}
	public REQUEST_MODE getRequestMode() {
		return requestMode;
	}
	public void setRequestMode(REQUEST_MODE requestMode) {
		this.requestMode = requestMode;
	}
	public DlmsRelayParam[] getDlmsRelayParams() {
		return dlmsRelayParams;
	}
	public void setDlmsRelayParams(DlmsRelayParam[] dlmsRelayParams) {
		this.dlmsRelayParams = dlmsRelayParams;
	}
	public short getDestAddr() {
		return destAddr;
	}
	public void setDestAddr(short destAddr) {
		this.destAddr = destAddr;
	}
	public int getInvokeId() {
		return invokeId;
	}
	public void setInvokeId(int invokeId) {
		this.invokeId = invokeId;
	}
	public void setOvertime(boolean overTime) {
		this.isOvertime = overTime;
	}
	public boolean isOverTime(){
		return isOvertime;
	}
	public ArrayList<IMessage> getReqDownMessages() {
		return reqDownMessages;
	}
	public void setReqDownMessages(ArrayList<IMessage> reqDownMessages) {
		this.reqDownMessages = reqDownMessages;
	}
	public ArrayList<Object> getBlockReplys() {
		return blockReplys;
	}
	public void setBlockReplys(ArrayList<Object> blockReplys) {
		this.blockReplys = blockReplys;
	}
}
