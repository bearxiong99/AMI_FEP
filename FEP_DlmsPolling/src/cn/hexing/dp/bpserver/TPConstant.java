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
	
	/**һ������͸���*/
	private int maxSendOneTime=0;
	
	/**��һ�η��ͳ�������͸�����Ϣʱ�䳤��*/
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
