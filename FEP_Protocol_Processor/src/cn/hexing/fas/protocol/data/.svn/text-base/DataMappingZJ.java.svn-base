package cn.hexing.fas.protocol.data;

import java.util.Hashtable;

public class DataMappingZJ implements IMapping{
	//帧传输方向
	public static final int ORIENTATION_TO_RTU=0;	/*主站召测*/
	public static final int ORIENTATION_TO_APP=1;	/*终端应答*/
	//错误编码
	public static final int ERROR_CODE_OK=0x0;				/*正确，无错误*/
	public static final int ERROR_CODE_NOREP=0x1;			/*中继命令没有返回*/
	public static final int ERROR_CODE_INVALIDCONT=0x2;		/*设置内容非法*/
	public static final int ERROR_CODE_LOWRIGHTS=0x3;		/*权限不足*/
	public static final int ERROR_CODE_NOITEM=0x4;			/*无数据项*/
	public static final int ERROR_CODE_NOTARGET=0x11;		/*目标地址不存在*/
	public static final int ERROR_CODE_SENDFAILUER=0x12;	/*发送失败*/
	public static final int ERROR_CODE_SMSLONG=0x13;		/*短消息太长*/
	
	private Hashtable dataitems;		/*数据项定义集合*/
	
	/**
	 * 默认构造器
	 *
	 */
	public DataMappingZJ(){
		loadMapping();
	}
	
	/**
	 * 加载数据集合
	 *
	 */
	private void loadMapping(){
		
	}
	
	/**
	 * 取dataitem
	 */
	public DataItem getDataItem(String key) {
		DataItem rt=null;
		try{
			if(dataitems.containsKey(key)){
				rt=(DataItem)dataitems.get(key);
			}
		}catch(Exception e){
			//
		}
		return rt;
	}
}
