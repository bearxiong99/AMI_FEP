package cn.hexing.fk.gate;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import cn.hexing.fk.common.spi.socket.IChannel;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-13 下午8:09:17
 *
 * @info 外置模块的终端通道管理,含有心跳上报
 */
public class RTUExChannelManager {
	private static BidiMap channelMap = new TreeBidiMap();
	private static BidiMap peerAddrMeterIdMap = new TreeBidiMap();
	
	public static final void setClientChannel(String logicalAddr,IChannel client){
		if( null == client )
			return;
		if( !channelMap.containsKey(logicalAddr) )
			synchronized(channelMap){
				channelMap.put(logicalAddr, client);
			}
		else{
			IChannel c = (IChannel)channelMap.get(logicalAddr);
			if( c != client ){
				synchronized(channelMap){
					channelMap.put(logicalAddr, client);
				}
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

	public static final void setDlmsPeerAddr(String peerAddr,String meterId ){
		if( !peerAddrMeterIdMap.containsKey( peerAddr) )
			peerAddrMeterIdMap.put(peerAddr, meterId);
		else{
			String oldMeterId = (String)peerAddrMeterIdMap.get(peerAddr);
			if( ! oldMeterId.equals(meterId) )
				peerAddrMeterIdMap.put(peerAddr, meterId);
		}
	}
	
	public static final String getMeterId(String peerAddr){
		return (String)peerAddrMeterIdMap.get(peerAddr);
	}

	public static final String removeClient(IChannel client){
		String logicAddr = (String)channelMap.getKey(client);
		if( null != logicAddr ){
			synchronized(channelMap){
				channelMap.remove(logicAddr);
				peerAddrMeterIdMap.removeValue(logicAddr);
			}
		}
		return logicAddr;
	}
}
