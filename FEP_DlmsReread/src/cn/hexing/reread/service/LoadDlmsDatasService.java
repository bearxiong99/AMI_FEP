package cn.hexing.reread.service;


/**
 * 数据库访问Service层
 * @ClassName:LoadDatasService
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午11:03:29
 *
 */
public class LoadDlmsDatasService extends LoadDatasServiceParent{
	private static String PROTOCOL= "03";
	
    private static LoadDlmsDatasService instance;
	
	public static LoadDlmsDatasService getInstance(){
		if(instance==null){
			synchronized(LoadDlmsDatasService.class) {
				if (instance==null){
					instance=new LoadDlmsDatasService();
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
