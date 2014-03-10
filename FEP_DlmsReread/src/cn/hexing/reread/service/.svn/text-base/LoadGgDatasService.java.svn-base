package cn.hexing.reread.service;

/**
 * 	数据库访问Service层
 * @author Administrator
 *
 */
public class LoadGgDatasService extends LoadDatasServiceParent{

	private static String PROTOCOL = "04";

	private static LoadGgDatasService instance;

	public static LoadGgDatasService getInstance() {
		if (instance == null) {
			synchronized (LoadGgDatasService.class) {
				if (instance == null) {
					instance = new LoadGgDatasService();
				}
			}
		}
		return instance;

	}

	@Override
	protected String getProtocol() {
		// TODO Auto-generated method stub
		return PROTOCOL;
	}
}
