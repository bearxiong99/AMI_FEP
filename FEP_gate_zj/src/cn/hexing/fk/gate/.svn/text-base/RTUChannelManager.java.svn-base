package cn.hexing.fk.gate;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.FileUtil;

public class RTUChannelManager {
	private static final TraceLog trace = TraceLog.getTracer("LoginTrace");

	private static BidiMap channelMap = new TreeBidiMap();
	private static BidiMap peerAddrMeterIdMap = new TreeBidiMap();
	
	//用于记录是否在线
	private static String destDir="login";
	
	public static final void setClientChannel(String logicalAddr,
			IChannel client) {
		if (null == client)
			return;
			if (!channelMap.containsKey(logicalAddr)) {
				recordLogin(logicalAddr);
				synchronized (channelMap) {
					channelMap.put(logicalAddr, client);
				}
			} else {
				IChannel c = (IChannel) channelMap.get(logicalAddr);
				if (c != client) {
					synchronized (channelMap) {
						channelMap.put(logicalAddr, client);
					}
				}
			}
	}

	
	
	/**
	 * 记录表计登陆信息
	 * @param logicalAddr
	 */
	private static void recordLogin(String logicalAddr) {
		if(!trace.isEnabled()) return ;
		File dir = FileUtil.mkdirs(destDir);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		String fileName = logicalAddr+"-"+sdf.format(new Date());
		boolean isFind = false;
		if(dir.exists() && dir.isDirectory()){
			for(File file : dir.listFiles()){
				if(file.getName().startsWith(logicalAddr)){
					file.renameTo(new File(destDir+File.separator+fileName));
					isFind = true;
					break;
				}
			}
			if(!isFind){
				FileUtil.openFile(destDir, fileName);
			}
		}
	}
	
	public static final IChannel getClient(String logicalAddOrPeerAddr){
		IChannel c = (IChannel)channelMap.get(logicalAddOrPeerAddr);
		if( null == c ){
			if(peerAddrMeterIdMap.get(logicalAddOrPeerAddr)==null) return null;
			c = (IChannel)channelMap.get(peerAddrMeterIdMap.get(logicalAddOrPeerAddr));
		}
		return c;
	}

	public static final void setDlmsPeerAddr(String peerAddr, String meterId) {
		if (!peerAddrMeterIdMap.containsKey(peerAddr)){
			synchronized (peerAddrMeterIdMap) {
				peerAddrMeterIdMap.put(peerAddr, meterId);
			}
		}
		else {
			String oldMeterId = (String) peerAddrMeterIdMap.get(peerAddr);
			if (!oldMeterId.equals(meterId)) {
				synchronized (peerAddrMeterIdMap) {
					peerAddrMeterIdMap.put(peerAddr, meterId);
				}
				recordLogin(meterId);
			}
		}
	}
	
	public static final String getMeterId(String peerAddr){
		return (String)peerAddrMeterIdMap.get(peerAddr);
	}

	public static final String removeClient(IChannel client) {
		String logicAddr = null;
		logicAddr = (String) channelMap.getKey(client);
		if (null != logicAddr) {
			synchronized (channelMap) {
				channelMap.remove(logicAddr);
			}
			synchronized (peerAddrMeterIdMap) {
				peerAddrMeterIdMap.removeValue(logicAddr);
			}
		}
		return logicAddr;
	}
}
