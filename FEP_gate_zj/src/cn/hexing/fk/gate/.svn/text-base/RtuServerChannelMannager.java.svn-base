package cn.hexing.fk.gate;

import org.apache.commons.collections.BidiMap;
import org.apache.commons.collections.bidimap.TreeBidiMap;

import cn.hexing.fk.common.spi.IClientModule;
import cn.hexing.fk.common.spi.socket.IChannel;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-15 上午11:09:43
 *
 * @info 网关主动连接的通道管理,终端作为服务端，网关作为客户端
 */
public class RtuServerChannelMannager {
	private static BidiMap channelMap = new TreeBidiMap();
	private static BidiMap peerAddrMeterIdMap = new TreeBidiMap();
	
	public static final void setClientChannel(String logicalAddr,IClientModule client){
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
	
	public static final IClientModule getClient(String logicalAddOrPeerAddr){
		IClientModule c = (IClientModule)channelMap.get(logicalAddOrPeerAddr);
		if( null == c ){
			if(peerAddrMeterIdMap.get(logicalAddOrPeerAddr)==null) return null;
			c = (IClientModule)channelMap.get(peerAddrMeterIdMap.get(logicalAddOrPeerAddr));
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

	public static final String removeClient(IClientModule client){
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
