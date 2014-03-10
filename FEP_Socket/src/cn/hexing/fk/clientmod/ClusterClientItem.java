package cn.hexing.fk.clientmod;

import org.springframework.util.StringUtils;

public class ClusterClientItem {
	private String usage = "any";
	private String deptCode = null;
	public long count = 0;
	
	private ClientModule client = new ClientModule();

	public String getUsage() {
		return usage;
	}

	public void setUsage(String usage) {
		this.usage = StringUtils.trimAllWhitespace(usage);
	}

	public String getDeptCode() {
		return deptCode;
	}

	public void setDeptCode(String deptCode) {
		this.deptCode = StringUtils.trimAllWhitespace(deptCode);
	}

	public ClientModule getClient() {
		return client;
	}

	public void setHostIp(String hostIp) {
		client.setHostIp( StringUtils.trimAllWhitespace(hostIp) );
	}

	public void setHostPort(int hostPort) {
		client.setHostPort(hostPort);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("$ClusterClientItem:@usage=").append(usage);
		sb.append(",@dwdm=").append(deptCode).append(",@count=").append(count);
		sb.append(",@client.alive=").append(client.isActive());
		sb.append(",@client.ip=").append(client.getHostIp());
		sb.append(",@client.port=").append(client.getHostPort());
		
		return sb.toString();
	}
}
