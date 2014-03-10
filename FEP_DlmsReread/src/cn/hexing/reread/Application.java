package cn.hexing.reread;

import cn.hexing.fk.utils.ClassLoaderUtil;
/**
 * 启动补召程序，入口
 * @ClassName:Application
 * @Description:TODO
 * @author kexl
 * @date 2012-9-24 上午10:26:06
 *
 */
public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassLoaderUtil.initializeClassPath();
		DlmsRereadApp.main(args);
	}

}
