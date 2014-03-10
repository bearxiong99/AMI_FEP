package cn.hexing.fas.protocol.conf;

import java.util.List;

public interface MeterConf {

	/**
	 * 
	 * @param datakey  标识对应的表配置
	 * @return
	 */
	public abstract List getMeterConf(String datakey);
}
