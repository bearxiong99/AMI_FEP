package cn.hexing.dp.bpserver;

public class TPConstant {
	
	private static TPConstant instance = new TPConstant();
	
	public static TPConstant getInstance(){
		if(instance==null){
			instance =new TPConstant();
		}
		return instance;
	}
	
	private TPConstant(){}
	
	/**一次最大发送个数*/
	private int maxSendOneTime=0;
	
	/**当一次发送超过最大发送个数休息时间长度*/
	private long sleepWhenOverMaxSendTime=0;

	public final int getMaxSendOneTime() {
		return maxSendOneTime;
	}

	public final void setMaxSendOneTime(int maxSendOneTime) {
		this.maxSendOneTime = maxSendOneTime;
	}

	public final long getSleepWhenOverMaxSendTime() {
		return sleepWhenOverMaxSendTime;
	}

	public final void setSleepWhenOverMaxSendTime(long sleepWhenOverMaxSendTime) {
		this.sleepWhenOverMaxSendTime = sleepWhenOverMaxSendTime*1000;
	}
	
	
}
