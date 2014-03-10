package cn.hexing.fk.bp.processor.gg;

import java.util.concurrent.ConcurrentHashMap;


/**
 * 
 * @author gaoll
 *
 * @time 2013-8-15 ÉÏÎç09:45:37
 *
 * @info local manager
 */
public class LocalGgContextManager implements IGgContextManager {

	
	
	private static final LocalGgContextManager instance = new LocalGgContextManager();
	public  static final LocalGgContextManager getInstance(){ return instance; }
	
	private final ConcurrentHashMap<String, GgContext> contextMap = new ConcurrentHashMap <String,GgContext>();

	private LocalGgContextManager(){}
	
	@Override
	public GgContext getContext(String rtuId) {
		if(!contextMap.containsKey(rtuId)){
			updateOrSetContext(rtuId, new GgContext());
		} 
		
		return contextMap.get(rtuId);
		
	}

	@Override
	public void updateOrSetContext(String meterId, GgContext context) {
		context.logicAddress = meterId;
		contextMap.put(meterId, context);
	}

}
