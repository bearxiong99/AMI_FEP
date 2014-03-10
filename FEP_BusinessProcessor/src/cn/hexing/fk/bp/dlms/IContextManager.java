/**
 * DlmsContext management interface.
 * User can implement customized Context-manager.
 */
package cn.hexing.fk.bp.dlms;

import com.hx.dlms.aa.DlmsContext;

/**
 * @author: Adam Bao, hbao2k@gmail.com
 *
 */
public interface IContextManager {

	/**
	 * Use meter-id to get DLMS-context. 
	 * @param meterId
	 * @return
	 */
	DlmsContext getContext(String meterId);
	
	/**
	 * Get DlmsContext by peerAddr, which format is ip@port string.
	 * @param ipPortAddr
	 * @return
	 */
	DlmsContext getContextByAddr(String ipPortAddr);
	
	void updateOrSetContext(String meterId, DlmsContext context);
	
	void removeContext( String meterId );
}
