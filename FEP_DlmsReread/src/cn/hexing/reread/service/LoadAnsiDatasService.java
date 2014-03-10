package cn.hexing.reread.service;

import cn.hexing.fas.protocol.Protocol;


/**
 * 数据库访问Service层
 * @ClassName:LoadDatasService
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午11:03:29
 *
 */
public class LoadAnsiDatasService extends LoadDatasServiceParent{
	private static String PROTOCOL= Protocol.ANSI;
	
    private static LoadAnsiDatasService instance;
	
	public static LoadAnsiDatasService getInstance(){
		if(instance==null){
			synchronized(LoadAnsiDatasService.class) {
				if (instance==null){
					instance=new LoadAnsiDatasService();
				}
			}
		}
		return instance;
	}

	@Override
	protected String getProtocol() {
		return PROTOCOL;
	}
}
