/**
 * 监控系统的命令定义。
 */
package cn.hexing.fk.monitor;

/**
 * 由于采用 请求/应答模式，应答的命令号与请求相同。
 * 对于有结果的应答，则body部分为结果内容。对于无结果的命令，如模块启动、停止,则在body部分返回字符串信息。
 */
public interface MonitorCommand {

	public static final short CMD_INVALID = 0x00;		//非法命令
	
	//日志相关命令
	public static final short CMD_LOG_LIST 			= 0x01;		//日志文件列表
	//系统远程配置管理
	public static final short CMD_CONFIG_LIST		= 0x02;		//配置文件列表
	public static final short CMD_GET_FILE 			= 0x03;		//文件分块请求。
	public static final short CMD_PUT_FILE 			= 0x04;		//文件上传。一般用于配置文件上传
	
	//模块监控
	public static final short CMD_GATHER_PROFILE   	= 0x1f;	//前置机采集所有连接的系统的profile。
	public static final short CMD_SYS_PROFILE 	 	= 0x10;	//系统级别所有监控对象的profile xml
	public static final short CMD_MODULE_PROFILE 	= 0x11;	//模块的profile
	//事件处理器的监控。
	public static final short CMD_EVENT_HOOK_PROFILE= 0x12;	//事件处理器profile

	//模块管理
	public static final short CMD_MODULE_START	 = 0x13;	//某个模块启动
	public static final short CMD_MODULE_STOP	 = 0x14;	//某个模块停止
	//系统管理
	public static final short CMD_SYS_START 	 = 0x15;	//系统启动
	public static final short CMD_SYS_STOP	 	 = 0x16;	//系统停止
	
	//某个终端的通信跟踪
	public static final short CMD_TRACE_RTU		 = 0x17;	//启动终端通信跟踪
	public static final short CMD_TRACE_ABORT	 = 0x18;	//停止终端通信跟踪
	public static final short CMD_TRACE_IND		 = 0X19;	//终端通信跟踪反馈

}
