package cn.hexing.fas.framework.spi;

import java.io.Serializable;

/**
 * 支持把对象序列化到string对象以及从string对象反序列化到实际对象。
 * 对象需要实现一个函数：object
 *
 */
public interface IStringSerializable extends Serializable {
	String serialzeToString()throws Exception;
	IStringSerializable deserializeFromString(String buffer)throws Exception;
}
