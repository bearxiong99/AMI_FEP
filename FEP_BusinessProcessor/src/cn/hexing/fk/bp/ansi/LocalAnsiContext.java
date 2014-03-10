package cn.hexing.fk.bp.ansi;

import java.util.HashMap;

import com.hx.ansi.ansiElements.AnsiContext;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time£º2013-3-18 ÏÂÎç06:52:15
 * @version 1.0 
 */

public class LocalAnsiContext implements ANSIContextManager{
	private static final LocalAnsiContext instance = new LocalAnsiContext();
	public  static final LocalAnsiContext getInstance(){ return instance; }
	
	private final HashMap<String,AnsiContext> contextMap = new HashMap<String,AnsiContext>(1024+37);
	private final HashMap<String,AnsiContext> peerAddrMap = new HashMap<String,AnsiContext>(1024+37);
	
	private LocalAnsiContext(){}
	
	@Override
	public AnsiContext getContext(String meterId) {
		AnsiContext cxt = contextMap.get(meterId);
		if( null == cxt )
			cxt = peerAddrMap.get(meterId);
		return cxt;
	}
	
	@Override
	public AnsiContext getContextByAddr(String ipPortAddr){
		return peerAddrMap.get(ipPortAddr);
	}

	@Override
	public void updateOrSetContext(String meterId, AnsiContext context) {
		AnsiContext cxt = contextMap.get(meterId);
		if( null != cxt ){
			cxt.update(context);
			if( (null != context.peerAddr) && (! context.peerAddr.equals(cxt.peerAddr)) ){
				if( null != cxt.peerAddr )
					peerAddrMap.remove(cxt.peerAddr);
				
				cxt.peerAddr = context.peerAddr;
				peerAddrMap.put(cxt.peerAddr, cxt);
			}
		}
		else{
			contextMap.put(meterId, context);
			if( null != context.peerAddr )
				peerAddrMap.put(context.peerAddr, context);
		}
	}
	
	@Override
	public void removeContext( String meterId ){
		AnsiContext cxt = contextMap.remove(meterId);
		if( null != cxt && null != cxt.peerAddr ){
			peerAddrMap.remove(cxt.peerAddr);
		}
	}
}
