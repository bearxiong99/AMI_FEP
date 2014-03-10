package cn.hexing.fk.bp.ansi.protocol;

import java.io.IOException;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;
import cn.hexing.fk.bp.ansi.events.AnsiEvent;
import cn.hexing.fk.bp.ansi.masterDbService.AnsiTaskMessage;
import cn.hexing.fk.bp.ansi.masterDbService.MasterDbServiceAssistance;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiContext.AAState;
import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.ansiElements.AnsiTableItem;
import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2050;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2051;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2053;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2064;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table0;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table1;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table2049;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table2057;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table3;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table5;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table8;
import com.hx.ansi.ansiElements.ansiElements.dataTable.Table11;
import com.hx.ansi.ansiElements.ansiElements.dataTable.Table12;
import com.hx.ansi.ansiElements.ansiElements.dataTable.Table13;
import com.hx.ansi.ansiElements.ansiElements.dataTable.Table15;
import com.hx.ansi.ansiElements.ansiElements.dataTable.Table16;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table2060;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table61;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table62;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table63;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table64;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table21;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table22;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table23;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table26;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table27;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table28;
import com.hx.ansi.ansiElements.ansiElements.touTable.Table51;
import com.hx.ansi.ansiElements.ansiElements.touTable.Table52;
import com.hx.ansi.ansiElements.ansiElements.touTable.Table54;
import com.hx.ansi.message.AnsiMessage;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-3-19 上午08:40:18
 * @version 1.0 
 */

public class AnsiProtocolDecoder {
	private static final Logger log = Logger.getLogger(AnsiProtocolDecoder.class);
	private static final TraceLog trace = TraceLog.getTracer("ANSI");
	private static final AnsiProtocolDecoder instance = new AnsiProtocolDecoder();
	private AnsiProtocolDecoder(){}
	public static final AnsiProtocolDecoder getInstance(){ return instance; }
	
	private AnsiEventProcessor eventProcessor = null;

	public void setEventProcessor(AnsiEventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}

	/**
	 *  处理上行msg 带InvocationId的
	 * @param context
	 * @param userData
	 * @throws IOException 
	 */
	public void handleUpMessage(AnsiContext context, String userData,int SEQ,int InvocationId,int totalMessage,String A4) throws IOException {
		try{
			AnsiEvent evt = (AnsiEvent)context.webReqList.get(0);
			switch(evt.eventType()){
			case UNDEF:
				break;
			case WEB_REQ:
				handleWebResponse(context,userData,SEQ,InvocationId,totalMessage, A4);
				break;
			case LOGON:
				handleLogonResponse(context,userData);
				break;
			case LOGOFF:
				handleLogoffResponse(context,userData);
				break;
			case TERMINATE:
				handleTerminateResponse(context,userData);
				break;
			case WAIT:
				handleWaitResponse(context,userData);
				break;
			case DISCONNECT:
				handleDisconnectResponse(context,userData);
				break;
			default:
				break;
				}
			}catch(Exception exp){
				eventProcessor.onRequestFailed(context);//上行报文解析出错。
				log.error("upMessage decode error "+exp.getMessage());
			}
		}
	
	private void handleWebResponse(AnsiContext context, String userData,int SEQ,int InvocationId,int totalMessage,String A4)throws IOException {
		AnsiEvent evt=(AnsiEvent) context.webReqList.get(0);
		AnsiRequest request=evt.getAnsiRequest();
		int serviceLen=Integer.parseInt(userData.substring(14, 16), 16);
		int result=Integer.parseInt(userData.substring(16, 18), 16);
		int serviceTag=Integer.parseInt(request.getServiceTag(),16);
		if(serviceTag>=0x40 && serviceTag<=0x4F){
			if(userData.length()>=24){//for compatible with old gprs modem
				userData=userData.substring(22, userData.length()-2);//11个字节之后是内容。
			}
			else {//standard ansi write response
				userData=StringUtil.PadLeftForStr(String.valueOf(result), 2, "0") ;
			}
		}
		else {
			userData=userData.substring(22, userData.length()-2);//11个字节之后是内容。
		}
		
		
		if(totalMessage==0){
			log.info("Single message。。。。");
		}else{
			log.info("Have "+(totalMessage)+" message waiting recive，now remain "+InvocationId+" message。。。");
			request.messageMap.put(InvocationId, userData);
			if(totalMessage==request.messageMap.size()){
				 userData="";
				for(int i=totalMessage-1;i>-1;i--){
					userData+=request.messageMap.get(i);
				}
				request.messageMap.clear();
			}else{
				String apdu="6013A406"+A4+"A8030201"+parseInt2HexString(context.getNextInvokeId())+"BE0428028000";
				AnsiMessage nextmsg=new AnsiMessage();
				nextmsg.setApdu(HexDump.toArray(apdu));
				nextmsg.setPeerAddr(context.peerAddr);
				nextmsg.setLogicalAddress(context.meterId);
				eventProcessor.sendMessage(nextmsg, context);
				return;
			}
		}
		switch(result){
		case 0:
			handleSuccessResponse(context, userData,request);
			break;
		default ://失败原因分析
			context.aaState=AAState.IDLE;
		}
		request.messageMap.clear();//将context.userData设置为null;
		// TODO Auto-generated method stub
	}

		
	
	private void handleSuccessResponse(AnsiContext context, String userData,AnsiRequest request) throws IOException{
		ANSI_OP_TYPE opType= request.getOpType();//取出optype，判断是都操作还是写操作还是控制操作
		int table=request.getTable();//获取读取table
		switch(opType){
		case OP_READ:
//			int count=Integer.parseInt(userData.substring(18, 22), 16);//数据长度
			handleReadResponse(context,userData,request);
			break;
		case OP_ACTION:
			handleActionResponse(context,userData,request);
			break;
		case OP_WRITE:
			handleWriteResponse(context,userData,request);
			break;
		}
		
		
		
		
	}
	/**
	 * 保存结果
	 * @param request
	 * @param table
	 */
	private void saveResult(AnsiRequest request,Table table){
		AnsiDataItem[] datas=request.getDataItem();
			for(int i=0;i<datas.length;i++){
				if((datas[i].dataCode==null||datas[i].dataCode.endsWith("00100716"))&&request.getTable()==0){
					return;
				}
				if(datas[i].dataCode!=null&&datas[i].dataCode.equals("00075404")&&request.getTable()==52){
					return;
				}
				datas[i]=table.getResult(datas[i],table);//给resultdata赋值
			}
			MasterDbServiceAssistance.getInstance().operationDatabase(request);
	}
	/**
	 * 处理读请求返回
	 * @param context
	 * @param userData
	 * @param request
	 * @throws IOException
	 */
	private void handleReadResponse(AnsiContext context, String userData,AnsiRequest request)throws IOException{
		if(null!=request.getTableItem()){
			AnsiTableItem []tableItem=request.getTableItem();
				int len=0;//计算有效的tableItem长度
				int len64=0;//计算有效的dataItem长度
				for(int i=0;i<tableItem.length;i++){
					if(tableItem[i] != null){
					len++;
					}
				}
				int tableNum=request.messageCount.get(0);
				if(64==tableNum){
					AnsiDataItem[] dataItem=request.getDataItem();//计算有几个参数项，每一个参数项对应一条message
					for(int i=0;i<dataItem.length;i++){
						if(dataItem[i] != null){
							len64++;
						}
					}
					//modified by fangjianming on 2013-10-9,first read the time of the latest data block
					dataItem[request.loadTime].resultData=userData;
					AnsiTableItem ansiTableItem=request.getTableItem()[request.imessageCount];
					if(ansiTableItem.readLatestDate){
						context.flag=userData;
						len64=0;
						request.loadTime=0;
					}
					if(ansiTableItem.readLoadProfileData){
						request.setTable(64);
						handleTable(context,userData,request);
						len64=len64-1;
						request.loadTime++;
					}
					
					if(request.loadTime==len64){
						tableNum=request.messageCount.remove(0);
						request.imessageCount++;
						request.setTable(tableNum);
					}
				}else {
					tableNum=request.messageCount.remove(0);
					request.imessageCount++;
					request.setTable(tableNum);
					handleTable(context,userData,request);
				}
				if(request.imessageCount==len-1&&request.loadTime==len64){
					AnsiProtocolEncoder.getInstance().constructorMessage(request, context, tableItem[request.imessageCount].table,tableItem[request.imessageCount]);
				}
				if(request.imessageCount==len&&request.loadTime==len64){
					request.imessageCount=0;
					request.loadTime=0;
					eventProcessor.onRequestComplete(context);
				}else{
					context.waitReply.set(false);
					eventProcessor.sendNextMessage(context);
				}
		}else{
			handleTable(context,userData,request);
			eventProcessor.onRequestComplete(context);
		}
	}
	/**
	 * 处理写请求返回
	 * @param context
	 * @param userData
	 * @param request
	 * @throws IOException
	 */
	private void handleWriteResponse(AnsiContext context, String userData,AnsiRequest request)throws IOException{
		if(null!=request.getTableItem()){
			AnsiTableItem []tableItem=request.getTableItem();
			int len=0;//计算有效的tableItem长度
			for(int i=0;i<tableItem.length;i++){
				if(tableItem[i] != null){
				len++;
				}
			}
			int tableNum=request.messageCount.get(0);
			request.setTable(tableNum);
			if(tableNum==0){
				handleTable(context,userData,request);
				request.messageCount.remove(0);
				context.waitReply.set(false);
				eventProcessor.sendNextMessage(context);
			}else if(tableNum==12){
				handleTable(context,userData,request);
				request.messageCount.remove(0);
				AnsiProtocolEncoder.getInstance().constructorMessage(request, context, 62,null);
				context.waitReply.set(false);
				eventProcessor.sendNextMessage(context);
			//因为table61不可写 所以读完table12之后直接设置数据项
//			}
//			else if(tableNum==61){//setLoadPrifile
//				AnsiProtocolEncoder.getInstance().constructorMessage(request, context, 62);
			}else if(tableNum==62){
				if(userData.equals("00")){
					AnsiDataItem[] dataItem=request.getDataItem();//计算有几个参数项，每一个参数项对应一条message
					int datalen=0;//计算有效的dataItem长度
					for(int i=0;i<dataItem.length;i++){
						if(dataItem[i] != null){
							datalen++;
						}
					}
					for(int i=0;i<datalen;i++){
						dataItem[request.imessageCount].resultData=userData;
					}
					request.imessageCount++;
					if(request.imessageCount==datalen){
						request.imessageCount=0;
						//目前表计不支持开启负荷记录
//						AnsiProtocolEncoder.getInstance().encodeAction(request, context);
						request.messageCount.remove(0);
						eventProcessor.onRequestComplete(context);
						MasterDbServiceAssistance.getInstance().operationDatabase(request);
						context.waitReply.set(false);
						eventProcessor.sendNextMessage(context);
					}else{
						context.waitReply.set(false);
						eventProcessor.sendNextMessage(context);
					}
				}else{
					AnsiDataItem[] dataItem=request.getDataItem();
					dataItem[0].resultData=userData;
					MasterDbServiceAssistance.getInstance().operationDatabase(request);
				}

			}else if(tableNum==7){//开启或者关闭负荷记录
				AnsiDataItem[] dataItem=request.getDataItem();
				dataItem[0].resultData=userData;
				MasterDbServiceAssistance.getInstance().operationDatabase(request);
			}else if(tableNum==54){
				if(userData.equals("00")){
					AnsiDataItem[] dataItem=request.getDataItem();//计算有几个参数项，每一个参数项对应一条message
					int datalen=0;//计算有效的dataItem长度
					for(int i=0;i<dataItem.length;i++){
						if(dataItem[i] != null){
							datalen++;
						}
					}
					for(int i=0;i<datalen;i++){
						dataItem[request.imessageCount].resultData=userData;
					}
					request.imessageCount++;
					if(request.imessageCount==datalen){
						request.imessageCount=0;
						//目前表计不支持开启负荷记录
//						AnsiProtocolEncoder.getInstance().encodeAction(request, context);
						request.messageCount.remove(0);
						eventProcessor.onRequestComplete(context);
						MasterDbServiceAssistance.getInstance().operationDatabase(request);
						context.waitReply.set(false);
						eventProcessor.sendNextMessage(context);
					}else{
						context.waitReply.set(false);
						eventProcessor.sendNextMessage(context);
					}
				}
			}
			else{
				context.waitReply.set(false);
				eventProcessor.sendNextMessage(context);
			}
		}else{
			AnsiDataItem[] dataItem=request.getDataItem();//计算有几个参数项，每一个参数项对应一条message
			int len=0;//计算有效的dataItem长度
			for(int i=0;i<dataItem.length;i++){
				if(dataItem[i] != null){
				len++;
				}
			}
			for(int i=0;i<len;i++){
				dataItem[request.imessageCount].resultData=userData;
			}
			request.imessageCount++;
			if(request.imessageCount==len){
				request.imessageCount=0;
				eventProcessor.onRequestComplete(context);
				//只有所有的设置都成功了 ，才算是设置成功
				if(!request.containsKey("UpgradeId")){
					MasterDbServiceAssistance.getInstance().operationDatabase(request);
				}

			}else{
				context.waitReply.set(false);
				eventProcessor.sendNextMessage(context);
			}
		}

	}
	private void handleActionResponse(AnsiContext context, String userData,AnsiRequest request)throws IOException{
		//操作结果
		request.getDataItem()[0].resultData=userData;
		if(!request.containsKey("UpgradeId")){
			MasterDbServiceAssistance.getInstance().operationDatabase(request);
		}
		eventProcessor.onRequestComplete(context);
		
	}
	/**
	 * 处理table消息
	 * @param context
	 * @param userData
	 * @param request
	 */
	private void handleTable(AnsiContext context, String userData,AnsiRequest request) {
		switch(request.getTable()){
		case 0:
			Table0 table0=new Table0();
			//TODO:测试 userData=0A884845434F0200191901000A0202030100EFBDF1DF031EFCF0C11FC203C807000000E0A0400002244000000AC003
//			userData="0A884845434F0200191901000A0202030100EFBDF1DF031EFCF0C11FC203C807000000E0A0400002244000000AC003";
			table0.decode(userData);
			context.table0=table0;
			saveResult(request,table0);
			break;
		case 1:
			Table1 table1=new Table1();
			table1.decode(userData);
			context.table1=table1;
			saveResult(request,table1);			
			break;
		case 3:
			Table3 table3=new Table3();
			table3.decode(userData);
			context.table3=table3;
			saveResult(request,table3);	
			break;
		case 5:
			Table5 table5=new Table5();
			table5.decode(userData);
			context.table5=table5;
			saveResult(request, table5);
			break;
		case 8://执行过程结果查询
			Table8 table8=new Table8();
			table8.decode(userData);
			//入库
			break;
		case 11:
			Table11 table11=new Table11();
			table11.decode(userData);
			context.table11=table11;
			break;
		case 12:
			Table12 table12=new Table12();
			//table12中每一个数据元素的长度为4个字节。len计算表计计量的元素个数
			int len=userData.length()/8;
			for(int i=0;i<len;i++){
				table12.decode(AnsiDataSwitch.ReverseStringByByte(userData.substring(i*8, i*8+8)),i);
			}
			context.table12=table12;
			break;
		case 13:
			Table13 table13=new Table13();
			table13.decode(userData);
			context.table13=table13;
			break;	
		case 15:
			Table15 table15=new Table15();
			table15.decode(userData);
			context.table15=table15;
			break;	
		case 16:
			Table16 table16=new Table16();
			int len16=userData.length()/2;//数据源定义，每一个数据元素长度为1字节
			for(int i=0;i<len16;i++){
				table16.decode(userData.substring(i*2, i*2+2),i);
			}
			context.table16=table16;
			break;
		case 21:
			Table21 table21=new Table21();
			table21.decode(userData);
			context.table21=table21;
			break;	
		case 22:
			Table22 table22=new Table22();
			int NBR_SUMMATIONS=context.table21.NBR_SUMMATIONS;
			int NBR_DEMANDS=context.table21.NBR_DEMANDS;
			table22.decode(userData,NBR_SUMMATIONS,NBR_DEMANDS);
			context.table22=table22;
			break;	
		case 23:
			Table23 table23=new Table23();
			table23.decode(context,userData);
			saveResult(request,table23);
			break;
		case 26:
			Table26 table26=new Table26();
			table26.decode(context, userData);
			AnsiTaskMessage.getInstance().saveEnergy(context, request, table26);
			break;
		case 27:
			Table27 table27=new Table27();
			int NBR_PRESENT_DEMANDS=context.table21.NBR_PRESENT_DEMANDS ;
			int NBR_PRESENT_VALUES =context.table21.NBR_PRESENT_VALUES;
			log.info("==========table27========="+NBR_PRESENT_DEMANDS+"=====meterid "+context.getMeterId());
			log.info("==========table27========="+NBR_PRESENT_VALUES+"=====meterid "+context.getMeterId());
			table27.decode(userData,NBR_PRESENT_DEMANDS,NBR_PRESENT_VALUES);
			context.table27=table27;
			break;
		case 28:
			log.info("==========table27========="+context.table21.NBR_PRESENT_DEMANDS+"=====meterid "+context.getMeterId());
			log.info("==========table27========="+context.table21.NBR_PRESENT_VALUES+"=====meterid "+context.getMeterId());
			Table28 table28=new Table28();
			table28.decode(context,userData);
			saveResult(request,table28);
			break;
		case 51:
			Table51 table51=new Table51();
			table51.decode(userData);
			context.table51=table51;
			break;
		case 52:
			Table52 table52=new Table52();
			table52.decode(userData);
			saveResult(request,table52);
			break;
		case 54:
			Table54 table54=new Table54();
			table54.decode(context, userData);
			saveResult(request,table54);
			break;
		case 61:
			Table61 table61=new Table61();
			table61.decode(userData);
			context.table61=table61;
			break;
		case 62:
			Table62 table62=new Table62();
			table62.decode(context,userData);
			context.table62=table62;
			break;
		case 63:
			Table63 table63=new Table63();
			table63.decode(userData);
			context.table63=table63;
			break;
		case 64:
			Table64 table64=new Table64();
			AnsiTaskMessage.getInstance().saveLoadTask(context,request,userData,table64);
			break;
		case 2057:
			Table2057 table2057=new Table2057();
			table2057.decode(userData);
			saveResult(request,table2057);
			break;
		case 2050:
			Table2050 table2050=new Table2050();
			table2050.decode(context, userData);
			AnsiTaskMessage.getInstance().saveEnergy(context, request, table2050);
			break;
		case 2051:
			Table2051 table2051=new Table2051();
			table2051.decode(userData);
			saveResult(request,table2051);
			break;
		case 2053:
			Table2053 table2053=new Table2053();
			table2053.decode(userData);
			saveResult(request,table2053);
			break;
		case 2064:
			Table2064 table2064=new Table2064();
			request.getDataItem()[0]=table2064.getResult(request.getDataItem()[0],table2064,userData);
			break;
		case 2049:
			Table2049 table2049=new Table2049();
			table2049.decode(userData);
			saveResult(request,table2049);
			break;
		case 2060:
			Table2060 table2060=new Table2060();
			table2060.decode(userData);
			saveResult(request,table2060);
			break;
		}
	}
	private void handleLogonResponse(AnsiContext context, String userData) {
		//无论登陆请求是否失败都得将evt移除。
		context.webReqList.remove(0);
		int serviceLen=Integer.parseInt(userData.substring(0, 2), 16);
		int result=Integer.parseInt(userData.substring(2, 4), 16);
		switch(result){
		case 0:
			int timeOut=Integer.parseInt(userData.substring(4, 8), 16);
			context.aaState=AAState.SESSION;
			context.timeOut=timeOut;
		default :
			context.aaState=AAState.IDLE;//请求登陆失败，失败原因分析暂时不处理
		}
	}
	private void handleLogoffResponse(AnsiContext context, String userData) {
		//无论注销请求是否失败都得将evt移除。
		context.webReqList.remove(0);
		int serviceLen=Integer.parseInt(userData.substring(0, 2), 16);
		int result=Integer.parseInt(userData.substring(2, 4), 16);
		switch(result){
		case 0:
			context.aaState=AAState.IDLE;
		default :
			context.aaState=AAState.SESSION;//请求登陆失败，失败原因分析暂时不处理
		}
	}
	private void handleTerminateResponse(AnsiContext context, String userData) {
		// TODO Auto-generated method stub
		
	}
	private void handleWaitResponse(AnsiContext context, String userData) {
		// TODO Auto-generated method stub
		
	}
	private void handleDisconnectResponse(AnsiContext context, String userData) {
		// TODO Auto-generated method stub
		
	}
	/**
	 *  处理上行msg 不带InvocationId的
	 * @param context
	 * @param userData
	 */
	public void handleUpMessage(AnsiContext context, String userData) {
		
	}
	public String  parseInt2HexString(int i){
		String ss=Integer.toHexString(i);
		if(1==(ss.length()%2)){
			ss=0+ss;
		 }
		return ss;
	}
	
}
