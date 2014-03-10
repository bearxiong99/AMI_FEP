package cn.hexing.dp;

import cn.hexing.fk.utils.ClassLoaderUtil;

public class Application {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ClassLoaderUtil.initializeClassPath();
		TaskPollingApp.main(args);
	}

}
