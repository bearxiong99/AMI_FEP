/**
 * DlmsContextManager is used to manage all DlmsContext which may be store in memcached server.
 * Cluster design is supported in the future.
 */
package cn.hexing.fk.bp.dlms;
import java.util.concurrent.ConcurrentHashMap;

import com.hx.dlms.aa.DlmsContext;
/**
 * @author Adam Bao,  hbao2k@gmail.com
 */

public final class LocalDlmsContextManager implements IContextManager{
	private static final LocalDlmsContextManager instance = new LocalDlmsContextManager();
	public  static final LocalDlmsContextManager getInstance(){ return instance; }
	
	private final ConcurrentHashMap<String, DlmsContext> contextMap = new ConcurrentHashMap <String,DlmsContext>(1024+37);
	private final ConcurrentHashMap<String, DlmsContext> peerAddrMap =new  ConcurrentHashMap<String, DlmsContext>(1024+37);
	
	private LocalDlmsContextManager(){}
	
	@Override
	public DlmsContext getContext(String meterId) {
		DlmsContext cxt = contextMap.get(meterId);
		if( null == cxt )
			cxt = peerAddrMap.get(meterId);
		return cxt;
	}
	
	@Override
	public DlmsContext getContextByAddr(String ipPortAddr){
		return peerAddrMap.get(ipPortAddr);
	}

	@Override
	public void updateOrSetContext(String meterId, DlmsContext context) {
		DlmsContext cxt = contextMap.get(meterId);
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
		DlmsContext cxt = contextMap.remove(meterId);
		if( null != cxt && null != cxt.peerAddr ){
			peerAddrMap.remove(cxt.peerAddr);
		}
	}
}
