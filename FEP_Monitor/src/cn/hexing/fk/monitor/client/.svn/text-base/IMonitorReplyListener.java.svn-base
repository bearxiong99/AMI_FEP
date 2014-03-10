package cn.hexing.fk.monitor.client;

public interface IMonitorReplyListener {
	void onSystemProfile(String profile);
	void onModuleProfile(String profile);
	void onEventHookProfile(String profile);
	void onMultiSysProfile(String profile);
	
	void onListLog(String result);
	void onListConfig(String result);
	
	void onGetFile();		//文件下载成功
	void onPutFile();		//文件上传成功
	
	void onReplyOK();			//成功应答。如模块启动或者停止
	void onReplyFailed(String reason);		//失败应答.
	
	//RTU收、发事件跟踪
	void onRtuMessageInd(String ind);
	
	//连接事件
	void onConnect();
	void onClose();
}
