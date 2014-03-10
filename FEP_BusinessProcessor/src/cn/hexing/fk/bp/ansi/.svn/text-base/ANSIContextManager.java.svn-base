package cn.hexing.fk.bp.ansi;

import com.hx.ansi.ansiElements.AnsiContext;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time£º2013-3-18 ÏÂÎç06:50:40
 * @version 1.0 
 */

public interface ANSIContextManager {



	/**
	 * Use meter-id to get ANSI-context. 
	 * @param meterId
	 * @return
	 */
	AnsiContext getContext(String meterId);
	
	/**
	 * Get AnsiContext by peerAddr, which format is ip@port string.
	 * @param ipPortAddr
	 * @return
	 */
	AnsiContext getContextByAddr(String ipPortAddr);
	
	void updateOrSetContext(String meterId, AnsiContext context);
	
	void removeContext( String meterId );

	
}
