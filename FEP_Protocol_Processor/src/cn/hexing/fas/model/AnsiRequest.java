package cn.hexing.fas.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.AnsiTableItem;


/** 
 * @Description  AnsiRequest
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-3-13 下午06:50:43
 * @version 1.0 
 */

public class AnsiRequest  extends FaalRequest{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 123456L;
	public static  enum ANSI_OP_TYPE  {OP_NA,OP_READ,OP_WRITE,OP_ACTION,OP_UPGRADE};
	private ANSI_OP_TYPE opType=ANSI_OP_TYPE.OP_NA;//初始化为not_app,ANSI支持read， write
	public ANSI_OP_TYPE getOpType() {
		return opType;
	}
	public void setOpType(ANSI_OP_TYPE opType) {
		this.opType = opType;
	}
	private String meterId ;
	private String peerIp ;
	private String webIP;
	private String serviceTag;//服务标示
	private int table;//table 类似数据项
	private String index;//索引读，带索引,每一级索引为2个字节
	private String offset;//带偏移量读取，偏移量3个字节,必须将full置为false
	private String count;//读取的字节数。
	private  boolean isFull=true;//默认为全部读，但是需要带偏移读取的需要将此处设置为false。
	private long requestTimeOut = 0;
	private String params;
	private int actionName;
//	private String dataItems;//这里传过来的数据项可以是一个包（主站自定义）也可以是一个单项,对于多个数据项的情况：每一个数据项之间用#分开
	private AnsiDataItem [] dataItem=null;
	private AnsiTableItem [] tableItem=null;
//	private List<Table> tableList;
	private String meterType;//不同的表计型号，有不一样的table类型
	private String serviceType;//00表计 70模块
	private long commId=-1;
	private long taskNo = -1;
	//use to append some params 
	private Map<String,Object> appendParams = new HashMap<String, Object>();
	/**消息总数列表*/
	 public ArrayList<Integer> messageCount = new ArrayList<Integer>();
	 /** 消息列表 */
	public Map<Integer,String> messageMap=new HashMap<Integer,String>();
	public int imessageCount=0;
	public int loadTime=0;
	private int upgradeType=1;
	
	
	public String getIndex() {
		return index;
	}
	/**
	 * 带索引读取，需要添加索引
	 * @param index
	 */
	public void setIndex(String index) {
		this.index = index;
	}
	public String getOffset() {
		return offset;
	}
	/**
	 * 带偏移量读取，放入偏移量值
	 * @param offset
	 */
	public void setOffset(String offset) {
		this.offset = offset;
	}
	public String getCount() {
		return count;
	}
	/**
	 * 要读取的字节数
	 * @param count
	 */
	public void setCount(String count) {
		this.count = count;
	}
	public String getServiceTag() {
		return serviceTag;
	}
	/**
	 * 选择服务标示
	 * @param serviceTag
	 */
	public void setServiceTag(String serviceTag) {
		this.serviceTag = serviceTag;
	}
	public int getTable() {
		return table;
	}
	/**
	 * 选择读取的table
	 * @param table
	 */
	public void setTable(int table) {
		this.table = table;
	}
	
	public void setWebIP(String webIP) {
		this.webIP = webIP;
	}
	public String getwebIP() {
		return webIP;
	}
	public String getMeterId() {
		return meterId;
	}
	public void setMeterId(String meterId) {
		this.meterId = meterId;
	}
	public String getParams() {
		return params;
	}
	public void setParams(String params) {
		this.params = params;
	}
	public final String getPeerIp() {
		return peerIp;
	}
	public final void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}
	public int getActionName() {
		return actionName;
	}
	public void setActionName(int actionName) {
		this.actionName = actionName;
	}
	public boolean validate() {
		return opType != ANSI_OP_TYPE.OP_NA && meterId != null && serviceTag != null ;
	}
	public boolean isFull() {
		return isFull;
	}
	public void setFull(boolean isFull) {
		this.isFull = isFull;
	}
	public final long getRequestTimeOut() {
		return requestTimeOut;
	}
	public final void setRequestTimeOut(long requestTimeOut) {
		this.requestTimeOut = requestTimeOut;
	}
	public AnsiDataItem[] getDataItem() {
		return dataItem;
	}
	public void setDataItem(AnsiDataItem[] dataItem) {
		this.dataItem = dataItem;
	}
	public String getMeterType() {
		return meterType;
	}
	public void setMeterType(String meterType) {
		this.meterType = meterType;
	}
	public long getCommId() {
		return commId;
	}
	public void setCommId(long comId) {
		this.commId = comId;
	}
	public AnsiTableItem[] getTableItem() {
		return tableItem;
	}
	public void setTableItem(AnsiTableItem[] tableItem) {
		this.tableItem = tableItem;
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
	/**
	 * 
	 * @param appendParams
	 */
	public final void addAllAppendParmas(Map<String,Object> appendParams ){
		if(this.appendParams==null)
			this.appendParams = new HashMap<String, Object>();
		this.appendParams.putAll(appendParams);
	}
	/**
	 * 
	 * @param key
	 * @return
	 */
	public boolean containsKey(String key){
		if(appendParams==null) return false;
		return appendParams.containsKey(key);
	}
	/**
	 * 
	 * @param key
	 */
	public final void removeAppendParam(String key){
		appendParams.remove(key);
	}
	/**添加附加属性*/
	public final void addAppendParam(String key,Object value) {
		
		if(appendParams ==null)
			this.appendParams = new HashMap<String, Object>();
		appendParams.put(key,value);
	}
	public long getTaskNo() {
		return taskNo;
	}
	public void setTaskNo(long taskNo) {
		this.taskNo = taskNo;
	}
	/** 服务端类别*/
	public String getServiceType() {
		return serviceType;
	}
	/** 服务端类别*/
	public void setServiceType(String serviceType) {
		this.serviceType = serviceType;
	}
	public int getUpgradeType() {
		return upgradeType;
	}
	public void setUpgradeType(int upgradeType) {
		this.upgradeType = upgradeType;
	}
	
	
	
	
//	public List<Table> getTableList() {
//		return tableList;
//	}
//	public void setTableList(List<Table> tableList) {
//		this.tableList = tableList;
//	}
//	public void addTable(Table table){
//        if (tableList == null) {
//        	tableList = new ArrayList<Table>();
//        }
//        tableList.add(table);
//    
//	}
	
	
}
