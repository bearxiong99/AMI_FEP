/**
 * 记录集的每个列，对应对象的属性配置。
 */
package cn.hexing.db.resultmap;

import java.lang.reflect.Method;

/**
 *
 */
public class ColumnMapper {
	private String property;	//对象的属性
	private String column;		//记录集的列名称
	private int index;			//记录集的列索引
	public Method method;

	public String getProperty() {
		return property;
	}
	public void setProperty(String property) {
		this.property = property;
	}
	public String getColumn() {
		return column;
	}
	public void setColumn(String column) {
		this.column = column;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
}
