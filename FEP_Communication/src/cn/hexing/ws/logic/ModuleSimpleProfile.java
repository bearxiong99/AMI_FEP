package cn.hexing.ws.logic;

public class ModuleSimpleProfile {
	private String moduleType,name;
	private boolean running;
	private long totalReceive,totalSend;
	private int perMinuteReceive,perMinuteSend;
	private long lastReceiveTime;

	public final String getModuleType() {
		return moduleType;
	}
	public final void setModuleType(String moduleType) {
		this.moduleType = moduleType;
	}
	public final String getName() {
		return name;
	}
	public final void setName(String name) {
		this.name = name;
	}
	public final boolean isRunning() {
		return running;
	}
	public final void setRunning(boolean running) {
		this.running = running;
	}
	public final long getTotalReceive() {
		return totalReceive;
	}
	public final void setTotalReceive(long totalReceive) {
		this.totalReceive = totalReceive;
	}
	public final long getTotalSend() {
		return totalSend;
	}
	public final void setTotalSend(long totalSend) {
		this.totalSend = totalSend;
	}
	public final int getPerMinuteReceive() {
		return perMinuteReceive;
	}
	public final void setPerMinuteReceive(int perMinuteReceive) {
		this.perMinuteReceive = perMinuteReceive;
	}
	public final int getPerMinuteSend() {
		return perMinuteSend;
	}
	public final void setPerMinuteSend(int perMinuteSend) {
		this.perMinuteSend = perMinuteSend;
	}
	public final long getLastReceiveTime() {
		return lastReceiveTime;
	}
	public final void setLastReceiveTime(long lastReceiveTime) {
		this.lastReceiveTime = lastReceiveTime;
	}
	
	public String toString(){
		StringBuffer sb = new StringBuffer(512);
		sb.append("\r\ntype=").append(this.moduleType);
		sb.append("; name=").append(name);
		sb.append("; running=").append(running);
		sb.append("; totalReceive=").append(totalReceive);
		sb.append("; totalSend=").append(totalSend);
		sb.append("; perMinuteReceive=").append(perMinuteReceive);
		sb.append("; perMinuteSend=").append(perMinuteSend);
		sb.append("; lastReceiveTime=").append(lastReceiveTime);
		return sb.toString();
	}
	
}
