package cn.hexing.fas.protocol.data;

import java.util.ArrayList;
import java.util.List;

public class RTUReply {
	private int type;	/*应答类型*/
	private List data;	/*应答数据*/
	private String des;
	
	public RTUReply(){
		this(0,null);
	}
	
	public RTUReply(int type,List data){
		this.type=type;
		this.data=data;
	}
	
	/**
	 * 添加数据项
	 * @param item
	 */
	public void addDataItem(DataItem item){
		if(data==null){
			data=new ArrayList();
		}
		data.add(item);
	}
	
	/**
	 * @return Returns the data.
	 */
	public List getData() {
		return data;
	}


	/**
	 * @param data The data to set.
	 */
	public void setData(List data) {
		this.data = data;
	}


	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}


	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}


	public String toString(){
		return des;
	}
}
