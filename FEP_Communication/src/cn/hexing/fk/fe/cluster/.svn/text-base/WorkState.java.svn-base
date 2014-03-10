/**
 * 终端工况定义。
 * 对象基本数据长度: 4 × 9 ＋ 8 × 4 ＝ 17×4 ＝ 68 bytes
 * 150万终端，则需要100M。JAVA对象内存预计150M左右。
 */
package cn.hexing.fk.fe.cluster;

import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.hexing.fk.fe.filecache.HeartbeatPersist;
import cn.hexing.fk.utils.CalendarUtil;

/**
 *
 */
public class WorkState implements Serializable{
	private static final long serialVersionUID = -3627807686848868530L;
	private int updateCount = 0;
	
	private String rtua;
	private String terminalType;
	
    /** GPRS/CDMA当前流量 */
    private int upGprsFlowmeter = 0;	//上行gprs流量
    /** 短信通道当前条数 */
    private int upSmsCount = 0;			//上行sms条数
    private int downGprsFlowmeter = 0;	//下行GPRS流量
    private int downSmsCount = 0;		//下行sms条数
    private long lastGprsTime = 0;		//最近gprs上行时间
    private long lastSmsTime = 0;		//最近SMS上行时间
    private int taskCount = 0;			//当天任务上行数量
    private int upGprsCount = 0;		//上行GPRS报文数量
    private int downGprsCount = 0;		//下行GPRS报文数量
    private int heartbeatCount=0;
    private long lastHeartbeat = 0;
    private long lastReqTime = 0;		//增加内部属性，支持上次请求的时间
    private int heartGprsFlowmeter=0;//心跳流量，包含返回确认帧
    private int loginGprsFlowmeter=0;//登陆登出流量
    
    private String lastCommunicationIp;
    
    public static final int tcpIpLen=74;
    
    private static final int tcpLoginLen = 371;
    
    
    
    private AtomicBoolean isConnect=new AtomicBoolean(false);
    
    
    public synchronized void update(RtuWorkStateItem item){
    	updateCount++;
    	if( item.getTxfs() == RtuWorkStateItem.TXFS_GPRS ){
    		if( item.isUp() ){
    			if(RtuWorkStateItem.FUNC_DLMS_HEART!=item.getFunc()&& RtuWorkStateItem.FUNC_HEART!=item.getFunc() &&RtuWorkStateItem.FUNC_GW_HEART!=item.getFunc()){
        			lastGprsTime = item.getIoTime();
    			}
        		upGprsCount++; upGprsFlowmeter += (item.getLen()+tcpIpLen);
    		}
    		else{
    			if( item.isDownReq() )
    				lastReqTime = item.getIoTime();
        		downGprsCount++; downGprsFlowmeter += (item.getLen()+tcpIpLen);
    		}
        	switch(item.getFunc()){
        	case RtuWorkStateItem.FUNC_DLMS_HEART:
        		//Dlms心跳报文
        		--upGprsCount; upGprsFlowmeter-=(item.getLen()+tcpIpLen);
        		heartGprsFlowmeter+=(item.getLen()+tcpIpLen); //+心跳
        		heartGprsFlowmeter+=(9+tcpIpLen);//+确认
        		heartbeatCount++;
        		if(isConnect==null)
        			isConnect = new AtomicBoolean(false);
        		if(isConnect.compareAndSet(false, true) || lastCommunicationIp==null || (lastCommunicationIp!=null && !lastCommunicationIp.equals(item.getLastCommunicationIp()))){
        			loginGprsFlowmeter+=tcpLoginLen*1;
        			lastCommunicationIp = item.getLastCommunicationIp();
        		}
        		lastHeartbeat = item.getIoTime();
        		break;
        	case RtuWorkStateItem.FUNC_DLMS_AA:
//    			--upGprsCount;upGprsFlowmeter-=(item.getLen()+tcpIpLen);
//    			//我怎么才能知道这次是登陆？？？？？？？,由什么判断
//    			//只有登陆的情况下 才会+1024
//    			//否则不加
//        		loginGprsFlowmeter+=(item.getLen()+tcpIpLen);
        		break;
        	case RtuWorkStateItem.FUNC_HEART:
        	case RtuWorkStateItem.FUNC_GW_HEART:
        		--upGprsCount; upGprsFlowmeter-=(item.getLen()+tcpIpLen);
        		heartGprsFlowmeter+=(item.getLen()+tcpIpLen);
        		lastHeartbeat = item.getIoTime();
        		heartbeatCount++;
        		try{
            		if( rtua.length() == 8 ){
            			int r = (int)Long.parseLong(rtua,16);
            			HeartbeatPersist.getInstance().handleHeartbeat(r,lastHeartbeat);
            		}
        		}catch(Exception e){}
        		break;
        	case RtuWorkStateItem.FUNC_LOGIN:
        	case RtuWorkStateItem.FUNC_GW_LOGIN:
        		--upGprsCount;upGprsFlowmeter-=(item.getLen()+tcpIpLen);
        		loginGprsFlowmeter+=((item.getLen()+tcpIpLen)+tcpLoginLen*1);
        		break;
        	case RtuWorkStateItem.FUNC_TASK:
        		taskCount++;
        		break;
        	case RtuWorkStateItem.FUNC_ALARM:
        		break;
        	case RtuWorkStateItem.FUNC_GW_NEED_CFM:
        		downGprsCount++;downGprsFlowmeter+=20;
        		break;
        	case RtuWorkStateItem.FUNC_DLMS_NEED_CFM:
        		downGprsCount++;downGprsFlowmeter+=(10+tcpIpLen);
        		break;
        	case RtuWorkStateItem.FUNC_REPLY:
        		break;
        	case RtuWorkStateItem.FUNC_DOWN_REQ:		//主站下行
        		break;
        	}
    	}
    	else if( item.getTxfs() == RtuWorkStateItem.TXFS_SMS ){
    		if( item.isUp() ){
    			lastSmsTime = item.getIoTime();
    			upSmsCount++;
    		}
    		else{
        		lastReqTime = item.getIoTime();
        		downSmsCount++;
    		}
        	switch(item.getFunc()){
        	case RtuWorkStateItem.FUNC_HEART:
        	case RtuWorkStateItem.FUNC_GW_HEART:
        		downSmsCount++;
        		lastHeartbeat = item.getIoTime();
        		heartbeatCount++;
        		break;
        	case RtuWorkStateItem.FUNC_LOGIN:
        		downSmsCount++;
        		break;
        	case RtuWorkStateItem.FUNC_TASK:
        		taskCount++;
        		break;
        	case RtuWorkStateItem.FUNC_ALARM:
        		break;
        	case RtuWorkStateItem.FUNC_GW_NEED_CFM:
        		break;
        	case RtuWorkStateItem.FUNC_REPLY:
        		break;
        	case RtuWorkStateItem.FUNC_DOWN_REQ:		//主站下行
        		break;
        	}
    	}
    	
//    	RtuInfoServer.getInstance().setWorkState(this);
    	
    }
    
	public void clearStatus(){
	    upGprsFlowmeter = 0;	//上行gprs流量
	    upSmsCount = 0;			//上行sms条数
	    downGprsFlowmeter = 0;	//下行GPRS流量
	    downSmsCount = 0;		//下行sms条数
//	    lastGprsTime = 0;		//最近gprs上行时间
//	    lastSmsTime = 0;		//最近SMS上行时间
	    taskCount = 0;			//当天任务上行数量
	    upGprsCount = 0;		//上行GPRS报文数量
	    downGprsCount = 0;		//下行GPRS报文数量
	    //增加终端的通信参数不一致的处理
	    heartbeatCount = 0;
	    heartGprsFlowmeter=0; //心跳GPRS流量
	    loginGprsFlowmeter=0; //登陆帧流量
//	    lastHeartbeat = 0;
	}
	
	public Date getLastGprsRecvTime(){
		if( 0 != lastGprsTime )
			return new Date(lastGprsTime);
		else
			return null;
	}
	
	public Date getLastSmsRecvTime(){
		if( 0 != lastSmsTime )
			return new Date(lastSmsTime);
		else
			return null;
	}
	
	public Date getLastHeartbeatTime(){
		if( 0 != lastHeartbeat )
			return new Date(lastHeartbeat);
		else
			return null;
	}
	
	public String getRtua() {
		return rtua;
	}
	public void setRtua(String logicalAddr) {
		this.rtua = logicalAddr;
	}
	public int getUpGprsFlowmeter() {
		return upGprsFlowmeter;
	}
	public void setUpGprsFlowmeter(int upGprsFlowmeter) {
		this.upGprsFlowmeter = upGprsFlowmeter;
	}
	public int getUpSmsCount() {
		return upSmsCount;
	}
	public void setUpSmsCount(int upSmsCount) {
		this.upSmsCount = upSmsCount;
	}
	public int getDownGprsFlowmeter() {
		return downGprsFlowmeter;
	}
	public void setDownGprsFlowmeter(int downGprsFlowmeter) {
		this.downGprsFlowmeter = downGprsFlowmeter;
	}
	public int getDownSmsCount() {
		return downSmsCount;
	}
	public void setDownSmsCount(int downSmsCount) {
		this.downSmsCount = downSmsCount;
	}
	public long getLastGprsTime() {
		return lastGprsTime;
	}
	public void setLastGprsTime(long lastGprsTime) {
		this.lastGprsTime = lastGprsTime;
	}
	public long getLastSmsTime() {
		return lastSmsTime;
	}
	public void setLastSmsTime(long lastSmsTime) {
		this.lastSmsTime = lastSmsTime;
	}
	public int getTaskCount() {
		return taskCount;
	}
	public void setTaskCount(int taskCount) {
		this.taskCount = taskCount;
	}
	public int getUpGprsCount() {
		return upGprsCount;
	}
	public void setUpGprsCount(int upGprsCount) {
		this.upGprsCount = upGprsCount;
	}
	public int getDownGprsCount() {
		return downGprsCount;
	}
	public void setDownGprsCount(int downGprsCount) {
		this.downGprsCount = downGprsCount;
	}
	public int getHeartbeatCount() {
		return heartbeatCount;
	}
	public void setHeartbeatCount(int heartbeatCount) {
		this.heartbeatCount = heartbeatCount;
	}
	public long getLastReqTime() {
		return lastReqTime;
	}
	public void setLastReqTime(long lastReqTime) {
		this.lastReqTime = lastReqTime;
	}

	public long getLastHeartbeat() {
		return lastHeartbeat;
	}

	public void setLastHeartbeat(long lastHeartbeat) {
		this.lastHeartbeat = lastHeartbeat;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("RTUA=").append(rtua);
		sb.append(",update=").append(updateCount);
		sb.append(",taskCount=").append(taskCount);
		sb.append(",upGprsFlow=").append(this.upGprsFlowmeter);
		sb.append(",upGprsCount=").append(this.upGprsCount);
		sb.append(",lastGprs=").append(CalendarUtil.getDateTimeString(this.lastGprsTime));
		return sb.toString();
	}

	public int getHeartGprsFlowmeter() {
		return heartGprsFlowmeter;
	}

	public void setHeartGprsFlowmeter(int heartGprsFlowmeter) {
		this.heartGprsFlowmeter = heartGprsFlowmeter;
	}

	public int getLoginGprsFlowmeter() {
		return loginGprsFlowmeter;
	}

	public void setLoginGprsFlowmeter(int loginGprsFlowmeter) {
		this.loginGprsFlowmeter = loginGprsFlowmeter;
	}

	public String getTerminalType() {
		return terminalType;
	}

	public void setTerminalType(String terminalType) {
		this.terminalType = terminalType;
	}

	public boolean isConnect() {
		return isConnect.get();
	}

	public void setConnect(boolean isConnect) {
		this.isConnect.set(isConnect);
	}

}
