package cn.hexing.reread.service;

public class LoadGwDatasService extends LoadDatasServiceParent{
	private static String PROTOCOL= "02";
	
	protected static LoadGwDatasService instance;
	public static LoadGwDatasService getInstance() {
		if (instance == null) {
			synchronized (LoadGwDatasService.class) {
				if (instance == null) {
					instance = new LoadGwDatasService();
				}
			}
		}
		return instance;
	}

	protected String getProtocol() {
		return PROTOCOL;
	}
}
