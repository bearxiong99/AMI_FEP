package cn.hexing.fk.bp.ansi.masterDbService;

import java.util.Date;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fk.bp.ansi.time.SaveAutoTimeResult;
import cn.hexing.fk.bp.model.HostCommandDb;
import cn.hexing.fk.bp.model.HostCommandItemDb;

import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.AnsiCommandResult;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-5-13 下午02:55:25
 * @version 1.0 
 */

public class MasterDbServiceAssistance {
	private static final MasterDbServiceAssistance instance=new MasterDbServiceAssistance();
	public  static final MasterDbServiceAssistance getInstance(){return instance;}
	
	MasterDbService masterDbService;
	private AsyncService service;
	
	private MasterDbServiceAssistance(){};
	
	
	public void operationDatabase(AnsiRequest request){
		switch(request.getOpType()){
		case OP_READ:
			operationReadResponse(request);
			break;
		case OP_WRITE:
			operationWriteResponse(request);
			break;
		case OP_ACTION:
			operationActionResponse(request);
			break;
		}
	}
	/**
	 * 对读请求的返回进行处理
	 * @param request
	 */
	private void operationReadResponse(AnsiRequest request){
		if(null!=request.getOperator()&&"TimeSyn".equals(request.getOperator())){
			SaveAutoTimeResult.getInstance().saveAutoTimeResult(request,request.getDataItem()[0] ,masterDbService);
			return;
		}
		AnsiDataItem []datas=request.getDataItem();
		for(AnsiDataItem data :datas){
			//插入召测结果
			insertCallResult(request, data);
		}
		/*//修改命令状态
		updateCommandStatus("1", ""+request.getCommId(),request.getMeterId());
		//修改任务状态
		updateTaskStatus("1", ""+request.getCommId());*/
		//所有都回来之后才更新任务状态
		procUpdateCommandStatus(request.getCommId(),"1");
//		Map<Integer,Integer>mlslMap=new HashMap<Integer,Integer>();
//		int mlsl=getTaskStatus(""+request.getCommId());
//		int rwid=getTaskIDByComID(""+request.getCommId());
//		if(mlslMap.containsKey(rwid)){
//			int temp=mlslMap.get(rwid);
//			mlslMap.put(rwid,temp-1);
//		}else{
//			mlslMap.put(rwid, mlsl-1);
//		}
//		if(null!=mlslMap.get(rwid)&&0==mlslMap.get(rwid)){
			//修改任务状态
//			updateTaskStatus("1", ""+request.getCommId());
//		}else{
//			System.out.println("can't find RWID...");
//		}

	}
	/**
	 *对写请求的返回进行处理
	 * @param request
	 */
	private void operationWriteResponse(AnsiRequest request){
		AnsiDataItem []datas=request.getDataItem();
		String ssdata="";
		String sdata="";
		for(AnsiDataItem data :datas){
			if(null!=data){
				if(data.resultData!=null){
					ssdata=data.dataCode+":"+data.resultData+",";
				}else{
//					sdata=data.dataCode+data.dataCode+":";
				}
				sdata=sdata+ssdata;
			}
		}
		//插入召测结果
		insertSetResult(request, sdata.substring(0, sdata.length()-1));
		/*//修改命令状态
		updateCommandStatus("1", ""+request.getCommId(),request.getMeterId());*/
		//修改任务状态
		procUpdateCommandStatus(request.getCommId(),"1");
	}
	/**
	 * 对操作的返回进行处理
	 * @param request
	 */
	private void operationActionResponse(AnsiRequest request){
		AnsiDataItem []datas=request.getDataItem();
		String sdata="";
		for(AnsiDataItem data :datas){
			if(data.resultData!=null){
				sdata=data.dataCode+":"+data.resultData;
			}else{
//				sdata=data.dataCode+data.dataCode+":";
			}
			sdata=sdata+",";
		}
		//插入召测结果
		insertSetResult(request,  sdata.substring(0, sdata.length()-1));
		/*//修改命令状态
		updateCommandStatus("1", ""+request.getCommId(),request.getMeterId());*/
		//修改任务状态
		procUpdateCommandStatus(request.getCommId(),"1");
	}
	/**
	 * 插入设置结果
	 * @param req
	 * @param value
	 * @return
	 */
	private int insertSetResult(AnsiRequest request,String  sdata){
		String s[]=sdata.split(",");
		if(s[0].equals("00100716:00")){
			sdata="00100716:00";
			//更新任务状态
			masterDbService.updateTaskSet(request.getMeterId(), (String)request.getAppendParam("taskNo"));
		}
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(request.getCommId());
		commandItemDb.setTime(new Date());
		commandItemDb.setLogicAddress(request.getMeterId());
		commandItemDb.setTn("0");
		commandItemDb.setValue(sdata);
		if(sdata.equals("00005200:00")||s[0].equals("00005200:00")){
			HostCommandItemDb commandItemDbTime = new HostCommandItemDb();
			commandItemDbTime.setStatus(2);
			commandItemDbTime.setLogicAddress(request.getMeterId());
			commandItemDbTime.setTn("0");
		    masterDbService.updateAutoTimeResult(commandItemDbTime);
		    if(null!=request.getOperator()&&"TimeSyn".equals(request.getOperator())){return 0;}
		}
		return masterDbService.insertCommandSetResult(commandItemDb);
	}
	/**
	 *	插入召测结果
	 * @param request
	 * @param data
	 * @return
	 */
	public int insertCallResult(AnsiRequest request,AnsiDataItem data){
		HostCommandItemDb commandItemDb = new HostCommandItemDb();
		commandItemDb.setCommandId(request.getCommId());
		commandItemDb.setTime(new Date());
		commandItemDb.setLogicAddress(request.getMeterId());
		commandItemDb.setTn("0");
		if(data.commandResult!=null){
			int len=data.commandResult.size();
			for(int i=0;i<len;i++){
				AnsiCommandResult commandResult=data.commandResult.remove(0);
				commandItemDb.setCode(commandResult.getCode());
				commandItemDb.setValue(commandResult.getValue());
				masterDbService.insertCommandCallResult(commandItemDb);
			}
		}else{
			if(data.resultData!=null){
				commandItemDb.setValue(data.resultData);
			}else{
//				commandItemDb.setValue("null:"+data.resultCode);
			}
			commandItemDb.setCode(data.dataCode);
			masterDbService.insertCommandCallResult(commandItemDb);
		}
		return 0;
	}
	/**
	 * 1个任务对应多个请求
	 * @param comdId
	 * @param status
	 */
	private void procUpdateCommandStatus(long comdId,String status){
		HostCommandDb commandDb=new HostCommandDb();
        commandDb.setId(comdId);
        commandDb.setMessageCount(1);
        commandDb.setStatus(status);
        commandDb.setErrcode(status);
        masterDbService.procUpdateCommandStatus(commandDb);
	}
	
	
	/**
	 * 更新命令状态
	 * @param zt 0:正在执行   1：成功     2：失败      3：部分成功
	 * @param req
	 */
	public void updateCommandStatus(String zt,String commandID,String zdjh)
	{
		masterDbService.updateCommandStatus(zt,zdjh,commandID);
	}
	/**
	 * 修改数据库中的任务状态
	 * @param zt  0 正在执行,1：成功 2：失败 3：部分成功
	 * @param req
	 */
	public void updateTaskStatus(String zt, String commandId) 
	{
		masterDbService.updateTaskStatus(zt, commandId);
	}
	public int getTaskStatus(String commandId){
		return masterDbService.getTaskStatus(commandId);
	}
	public int getTaskIDByComID(String commandId){
		return masterDbService.getTaskIDByComID(commandId);
	}
	public MasterDbService getMasterDbService() {
		return masterDbService;
	}
	public void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}
	public AsyncService getService() {
		return service;
	}
	public void setService(AsyncService service) {
		this.service = service;
	}

	
}
