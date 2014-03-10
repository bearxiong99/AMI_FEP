package cn.hexing.rmi.client.control;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.rmi.client.model.LeftTreeNode;

public class RmiClientDbService {

	private RmiClientDb dbClient;
	
	private static RmiClientDbService instance;

	public void setDbClient(RmiClientDb dbClient) {
		this.dbClient = dbClient;
	}
	
	public static RmiClientDbService getInstance(){
		if(instance == null){
			instance = new RmiClientDbService();
		}
		return instance;
	}
	
	private RmiClientDbService(){}
	
	public List<String> queryTerminal(String zdljdz){
		return dbClient.queryTerminal(zdljdz);
	}
	
	public List<LeftTreeNode> getLeftTreeNodes(String type,String pid){
		List<LeftTreeNode> nodes = new ArrayList<LeftTreeNode>();
		if("root".equals(type)){
			nodes.addAll( dbClient.getRootDepartment());
		}else if("dw".equals(type)){
			
			nodes.addAll( dbClient.getDepartment(pid));
			nodes.addAll( dbClient.getCircuit(pid));
			
		}else if("xl".equals(type)){
			nodes.addAll( dbClient.getDistrict(pid));
		}else if("tq".equals(type)){
			nodes.addAll( dbClient.getTtransformer(pid));
		}else if("byq".equals(type)){
			nodes.addAll( dbClient.getTerminal(pid));
		}
		return nodes;
	}
	
	
	
}
