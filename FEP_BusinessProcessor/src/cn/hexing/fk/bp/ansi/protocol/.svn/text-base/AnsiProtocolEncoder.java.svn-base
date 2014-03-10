package cn.hexing.fk.bp.ansi.protocol;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.ansiElements.AnsiTaskInf;
import com.hx.ansi.ansiElements.ansiElements.AnsiTableItem;
import com.hx.ansi.ansiElements.ansiElements.Table;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2050;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2051;
import com.hx.ansi.ansiElements.ansiElements.MTtable.Table2053;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table2049;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table2057;
import com.hx.ansi.ansiElements.ansiElements.basicTable.Table7;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table2060;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table61;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table62;
import com.hx.ansi.ansiElements.ansiElements.loadTable.Table64;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table23;
import com.hx.ansi.ansiElements.ansiElements.registerTable.Table26;
import com.hx.ansi.ansiElements.ansiElements.touTable.Table54;
import com.hx.ansi.element.CallingAPInvocationIdElement;
import com.hx.ansi.element.UserInformationElement;
import com.hx.ansi.message.AnsiMessage;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  ANSI encoder
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-3-19 上午08:39:52
 * @version 1.0 
 */

public class AnsiProtocolEncoder {
	private static final Logger log = Logger.getLogger(AnsiProtocolEncoder.class);
	private static final TraceLog trace = TraceLog.getTracer("ANSI");
	private static final AnsiProtocolEncoder instance = new AnsiProtocolEncoder();
	public static final AnsiProtocolEncoder getInstance(){ return instance; }
	
	private MasterDbService masterDbService;  //spring 配置实现。
	//Current request encoded into multiple down-link messages.
	public ArrayList<IMessage> reqDownMessages = new ArrayList<IMessage>();
	private AnsiProtocolEncoder(){}
	private boolean IsStander=false;

	
	public void build(FaalRequest request, AnsiContext context) throws IOException{
		if( request instanceof AnsiRequest ){
			AnsiRequest req = (AnsiRequest)request;
			if( ! req.validate() ){
				log.error("ANSI request is invalid.");
			}
			switch(req.getOpType()){
			case OP_READ:
				encodeRead(req,context);
				break;
			case OP_WRITE:
				encodeWrite(req,context);
				break;
			case OP_ACTION:
				encodeAction(req,context);
				break;
			case OP_UPGRADE:
				encodeUpgrade(req,context);
				break;
			case OP_NA:
				log.error("ANSIRequest operationType is Not Applicable.");
				context.webReqList.remove(0);
				break;
			default:
				context.webReqList.remove(0);
				break;
			}
		}
		else{
			log.error("ANSIProtocolEncoder not support requestType:"+request.getClass().getName());
		}
	}


	private void encodeRead(AnsiRequest req, AnsiContext context) {
		switch(req.getTable()){
			case 0://基本配置(只读)
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 1://厂商标识表(只读)
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 3://状态表(只读)
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 5://表计标识表(只读)
				constructorMessage( req,  context,req.getTable(),null);
				break;
//			case 7://发起过程执行表(只写)
//				constructorMessage( req,  context,req.getTable());
//				break;
			case 8://过程执行结果表(只读)
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 23://当前电量、最大需量、累计需量(只读)
				constructorReadEnergy(req,context);
				break;
			case 26://结算数据
				constructorReadEnergy(req,context);
				break;
			case 28://实时量
				constructorReadEnergy(req,context);
				break;
			case 33://显示
				if(null==context.table0)
					constructorMessage( req,  context,0,null);
				if(null==context.table12)
					constructorMessage( req,  context,31,null);
				if(null==context.table16)
					constructorMessage( req,  context,32,null);
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 51:
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 52:
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 54:
				constructorReadTOU( req,  context);
				break;
			case 64:
				constructorReadLoadProfile(req,context);
				break;
			case 2057://GPRS参数
				Table2057 table2057=new Table2057();
				getIndex(req, table2057, context,null);
				req.setServiceTag("3F");
				req.setFull(false);
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 2050:
				constructorReadEnergy(req,context);
				break;
			case 2051:
				Table2051 table2051=new Table2051();
				getIndex(req, table2051, context,null);
				req.setFull(false);
				req.setServiceTag("3F");
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 2053:
				Table2053 table2053=new Table2053();
				getIndex(req, table2053, context,null);
				req.setFull(false);
				req.setServiceTag("3F");
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 2064:
				constructorMessage( req,  context,req.getTable(),null);
				break;
			case 2049://monthly frozen time
				Table2049 table2049=new Table2049();
				getIndex(req,table2049,context,null);
				req.setFull(false);
				req.setServiceTag("3F");
				constructorMessage(req,context,req.getTable(),null);
				break;
			case 2060://load control
				Table2060 table2060=new Table2060();
				getIndex(req,table2060,context,null);
				req.setFull(false);
				req.setServiceTag("3F");
				constructorMessage(req,context,req.getTable(),null);
				break;
			default:
				context.webReqList.remove(0);
				break;	
				}
		
	}
	private void encodeWrite(AnsiRequest req, AnsiContext context) {
		switch (req.getTable()){
		case 2057://GPRS参数
			Table2057 table2057=new Table2057();
			setData(req,table2057,context);
			req.setServiceTag("4F");
			req.setFull(false);
			constructorMessage( req,  context,req.getTable(),null);
			break;
		case 2049://monthly frozen time
			Table2049 table2049=new Table2049();
			setData(req,table2049,context);
			req.setServiceTag("4F");
			req.setFull(false);
			constructorMessage(req, context, req.getTable(),null);
		case 2060://load control
			Table2060 table2060=new Table2060();
			setData(req,table2060,context);
			req.setServiceTag("4F");
			req.setFull(false);
			constructorMessage(req, context, req.getTable(),null);
			break;
		case 62:
			constructorSetLoadProfile(req,context);
			break;
		case 54:
			constructorSetTOU(req,context);
			break;
		case 2064:
			constructorMessage( req,  context,req.getTable(),null);
			break;
		
		default:
			context.webReqList.remove(0);
			break;
		}
		
	}
	
	/**
	 * ActionRequest 即Table7 发起过程执行表，
	 * ActionResponse 00或者01...
	 * 需要主站查询 Table8 过程执行结果表，查询Table7中的过程执行情况
	 * @param req
	 * @param context
	 */
	public void encodeAction(AnsiRequest req, AnsiContext context) {
		Table7 table7=new Table7();
		setData(req,table7,context);
		req.setFull(true);
		req.setServiceTag("40");
		req.setTable(7);
		constructorMessage( req,  context,req.getTable(),null);
	}
	/**
	 * 远程升级
	 * @param req
	 * @param context
	 */
	public void encodeUpgrade(AnsiRequest req, AnsiContext context){
		constructorMessage( req,  context,req.getTable(),null);
	}
	/**
	 * 读取电量和需量实时量（table23 table28）结算数据 table26
	 * @param req
	 * @param context
	 */
	private void constructorReadEnergy(AnsiRequest req, AnsiContext context){
		req.setServiceTag("30");//读取电量之前要读取的table数据是要全读的。
		req.setFull(true);//全部读取将fullread设置为true。
		AnsiTableItem []tableItem=req.getTableItem();
		int len=0;//计算有效的tableItem长度
		for(int i=0;i<tableItem.length;i++){
			if(tableItem[i] != null){
				len++;
			}
		}
		if(len==1){
			constructorMessage(req,  context,tableItem[0].table,tableItem[0]);
		}
		for(int i=0;i<len-1;i++){//最后的message需要等前面的都回来了再发送
			constructorMessage(req,  context,tableItem[i].table,tableItem[1]);
		}
	}
	/**
	 * 读取负荷记录
	 * @param req
	 * @param context
	 */
	private void constructorReadLoadProfile(AnsiRequest req, AnsiContext context){
		req.setServiceTag("30");//读取电量之前要读取的table数据是要全读的。
		req.setFull(true);//全部读取将fullread设置为true。
		AnsiTableItem []tableItem=req.getTableItem();
		int len=0;//计算有效的tableItem长度
		for(int i=0;i<tableItem.length;i++){
			if(tableItem[i] != null){
				len++;
			}
		}
		if(len==1){
			constructorMessage(req,  context,tableItem[0].table,tableItem[0]);
		}
		for(int i=0;i<len-1;i++){//最后的message需要等前面的都回来了再发送
			constructorMessage(req,  context,tableItem[i].table,tableItem[i]);
		}

	
	}
	/**
	 * 设置负荷数据
	 * @param req
	 * @param context
	 */
	private void constructorSetLoadProfile(AnsiRequest req, AnsiContext context){
		req.setServiceTag("30");//读取电量之前要读取的table数据是要全读的。
		req.setFull(true);//全部读取将fullread设置为true。
		AnsiTableItem []tableItem=req.getTableItem();
		int len=0;//计算有效的tableItem长度
		for(int i=0;i<tableItem.length;i++){
			if(tableItem[i] != null){
				len++;
			}
		}
		if(len==2){//len=2---62  7
			constructorMessage(req,  context,tableItem[0].table,tableItem[0]);
		}
		for(int i=0;i<len-2;i++){//最后的message需要等前面的都回来了再发送
			constructorMessage(req,  context,tableItem[i].table,tableItem[i]);
		}
	}
	/**
	 * 读取TOU
	 * @param req
	 * @param context
	 */
	private void constructorReadTOU(AnsiRequest req,AnsiContext context){
		req.setServiceTag("30");//读取电量之前要读取的table数据是要全读的。
		req.setFull(true);//全部读取将fullread设置为true。
		AnsiTableItem []tableItem=req.getTableItem();
		int len=0;//计算有效的tableItem长度
		for(int i=0;i<tableItem.length;i++){
			if(tableItem[i] != null){
				len++;
			}
		}
		if(len==1){//len=1  54
			constructorMessage(req,  context,tableItem[0].table,tableItem[0]);
		}
		for(int i=0;i<len-1;i++){//最后的message需要等前面的都回来了再发送
			constructorMessage(req,  context,tableItem[i].table,tableItem[i]);
		}
	}
	/**
	 * 设置TOU
	 * @param req
	 * @param context
	 */
	private void constructorSetTOU(AnsiRequest req,AnsiContext context){
		req.setServiceTag("4F");//读取电量之前要读取的table数据是要全读的。
		req.setFull(false);//全部读取将fullread设置为true。
		AnsiTableItem []tableItem=req.getTableItem();
		int len=0;//计算有效的tableItem长度
		for(int i=0;i<tableItem.length;i++){
			if(tableItem[i] != null){
				len++;
			}
		}
		if(len==1){//len=1  54
			constructorMessage(req,  context,tableItem[0].table,tableItem[0]);
		}
		for(int i=0;i<len-1;i++){//最后的message需要等前面的都回来了再发送
			constructorMessage(req,  context,tableItem[i].table,tableItem[i]);
		}
	}
	
	/**
	 * encode Association Control Service Element userInformation
	 * @param req
	 * @param context
	 * @return
	 */
	private List<String>  encodeUserInformation(AnsiRequest req, AnsiContext context,int table,AnsiTableItem ansiTableItem){
		List<String> userInformationElements=new ArrayList<String>();
		String userInformationElement=null;
		List<String> userInfs=new ArrayList<String>();
		String userInf="";
		//目前userInf（data）是不加密下通信，所以data没做处理，mode1  需要带有4个字节mac，mode2模式下data为加密且带有4个字节mac
		if (req.isFull()){
			//read --30  write--40
			if("30".equals(req.getServiceTag())){
			
				userInf=req.getServiceTag()+"00"+parseInt2HexString(table);
				userInfs.add(userInf);
			}else if("40".equals(req.getServiceTag())){
				
				userInf=req.getServiceTag()+"00"+parseInt2HexString(table)+req.getDataItem()[0].count+req.getDataItem()[0].data;//对于写请求或者操作请求需要带数据data
				userInfs.add(userInf);
			}else{
				context.webReqList.remove(0);
				log.info("can't find ServiceTag");
			}
		}
		else{
			//offsetRead--3F  offsetWrtite--4F
			if("3F".equals(req.getServiceTag())){
				
				//偏移读，需要找到读取的索引
				//在seekindex中找到要读取的数据项的索引，将偏移量放到req里面，这样主站读取数据只需要下发数据项，通过seekIndex设置读取索引
				AnsiDataItem[] datas=req.getDataItem();
				if(req.getTable()==2057){//对于gprs表，虽然是要读取多个但是一次读取
					userInf=req.getServiceTag()+parseInt2HexString(table)+datas[0].offset+datas[0].count;
					userInfs.add(userInf);
				}
				if(req.getTable()==64){
				
					if(ansiTableItem.readLatestDate){
						for(int i=0;i<1;i++){
							String stable=parseInt2HexString(table);
							userInf=req.getServiceTag()+"0000".substring(stable.length())+stable+datas[i].offset+datas[i].count;
							userInfs.add(userInf);
						}
					}
					else if(ansiTableItem.readLoadProfileData){
						for(int i=1;i<datas.length;i++){
							String stable=parseInt2HexString(table);
							userInf=req.getServiceTag()+"0000".substring(stable.length())+stable+datas[i].offset+datas[i].count;
							userInfs.add(userInf);
						}
					}
				}
				else{
					for(int i=0;i<datas.length;i++){
						String stable=parseInt2HexString(table);
						userInf=req.getServiceTag()+"0000".substring(stable.length())+stable+datas[i].offset+datas[i].count;
						userInfs.add(userInf);
					}
				}
			}else if("4F".equals(req.getServiceTag())){
				
				AnsiDataItem[] datas=req.getDataItem();
				int len=0;//计算有效的DataItem长度
				for(int i=0;i<datas.length;i++){
					if(datas[i] != null){
						len++;
					}
				}
				for(int i=0;i<len;i++){
//					if(req.getTable()==62){//负荷任务设置处理
//						AnsiDataItem dataItem=req.getDataItem()[0];
//						String offset[]=dataItem.offset.split("#");
//						String data[]=dataItem.data.split("#");
//						for(int k=0;k<data.length;k++){
//							userInf=req.getServiceTag()+"00"+parseInt2HexString(table)+offset[i]+dataItem.count+data[i];
//							userInfs.add(userInf);
//						}
//					}else{
						if(parseInt2HexString(table).length()==2){
							userInf=req.getServiceTag()+"00"+parseInt2HexString(table)+datas[i].offset+datas[i].count+datas[i].data;
						}else if(parseInt2HexString(table).length()==4){
							userInf=req.getServiceTag()+parseInt2HexString(table)+datas[i].offset+datas[i].count+datas[i].data;
						}	
						userInfs.add(userInf);
//					}
				}
			}else{
				context.webReqList.remove(0);
				log.info("can't find ServiceTag");
			}
		}
			for(String uf:userInfs){
				UserInformationElement userInformation=new UserInformationElement();
				userInformation.encode(uf,context, req.getServiceTag());
				userInformationElement=userInformation.getUserInformationElement();
				userInformationElements.add(userInformationElement);
			}
		return userInformationElements;
	}
	/**
	 * 	Action和Write 需要传参数，也就是数据区不为空，所以这里组数据区
	 * @param req
	 * @param context
	 * @param table
	 * @return
	 */
	@SuppressWarnings("unused")
	private String encodeData(AnsiRequest req, AnsiContext context,int table){
		String data="";
		switch(req.getOpType()){
		case OP_ACTION :
			Table7 table7=new Table7();
			table7.setActionName(req.getActionName());//如果是执行过程，那么需要传一个过程名 
			table7.setCommandId(table7.getNextCommandId());
			table7.setParamRCD(req.getParams());//过程如果带有参数 还需要传参数
			table7.encode();
			data=table7.TABLE_IDB_BFLD;
			break;
		case OP_WRITE:
			
			
			
			break;
		}
		
		return data;
	}
	
///**
// * 对于主站发送过来一个请求而bp这边需要分多个request发送的情况，在这里处理
// * @param reqs
// * @param context
// * @param table
// */
//	private void constructorMessage(List<AnsiRequest> reqs, AnsiContext context,int table){
//		for(AnsiRequest req:reqs){//遍历reqs列表里面所有的请求，下发
//			constructorMessage( req,  context,req.getTable());
//		}
//	}
	/**
	 * constructorMessage--message 构造函数，组ANSI message
	 * @param req
	 * @param context
	 * @param table
	 */
	public  void constructorMessage(AnsiRequest req, AnsiContext context,int table,AnsiTableItem ansiTableItem){
//		if(null==context.acseTitle){//如果context里面没有带有acseTitle的信息，则要组一个acseTitle
//			CalledAPTitleElement calledAPTitle=new CalledAPTitleElement();
//			calledAPTitle.setPeerAddr(context.peerAddr);
//			calledAPTitle.encode();
//			CallingAPTitleElement callingAPTitle=new CallingAPTitleElement();
//			callingAPTitle.setPeerAddr(context.localAddr);
//			callingAPTitle.encode();
//			String apduTitle=calledAPTitle.getCalledAPTitleElement()+callingAPTitle.getCallingAPTitleElement();
//			context.acseTitle=apduTitle;
//		}
		switch(table){
		case 23:
			Table23 table23=new Table23();
			getIndex(req,table23,context,null);
			req.setFull(false);
			req.setServiceTag("3F");
			break;
		case 26:
			Table26 table26=new Table26();
			getIndex(req, table26, context,null);
			req.setFull(false);
			req.setServiceTag("3F");
			break;
		case 28:
			break;
		case 54:
			Table54 table54=new Table54();
			switch(req.getOpType()){
			case OP_WRITE:
				setTOU(req,table54,context);
			case OP_READ:
				break;
			}
			break;
		case 64:
			Table64 table64=new Table64();
			getIndex(req,table64,context,ansiTableItem);
			req.setFull(false);
			req.setServiceTag("3F");
			break;
//		case 61:
//			Table61 table61=new Table61();
//			setLoadProfile(req,table61,context);
//			break;
		case 62:
			Table62 table62=new Table62();
			switch(req.getOpType()){
			case OP_WRITE:
				setLoadProfile(req,table62,context);
			case OP_READ:
				break;
			}
			break;
		case 2050:
			Table2050 table2050=new Table2050();
			getIndex(req, table2050, context,ansiTableItem);
			req.setFull(false);
			req.setServiceTag("3F");
			break;
		}
		//encodeUserInformation方法组数据区，将索引和偏移放到数据区
		List<String> userInformations=encodeUserInformation(req, context, table,ansiTableItem);
		//判断是不是标准
		if(IsStander){
			for(String userInformation:userInformations){//遍历request里面带有的每一个datacode下发
				String acse="";
				CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
				callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
				callingAPInvocationId.encode();
				acse=context.acseTitle+callingAPInvocationId.getCallingAPInvocationIdElement()+userInformation;
				acse="60"+parseInt2HexString(acse.length()/2).toUpperCase()+acse;
				AnsiMessage msg = new AnsiMessage();
				msg.setApdu(HexDump.toArray(acse));
				msg.setPeerAddr(context.peerAddr);
				msg.setLogicalAddress(context.meterId);
				context.reqDownMessages.add(msg);
			}
		}else if(req.containsKey("MeterUpgrade")){
			//远程升级功能需要带A2下发
			if(req.getUpgradeType()==1){
				for(String userInformation:userInformations){//遍历request里面带有的每一个datacode下发
					String acse="";
					CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
					callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
					callingAPInvocationId.encode();
					acse="A203800170"+callingAPInvocationId.getCallingAPInvocationIdElement()+userInformation;
					acse="60"+parseInt2HexString(acse.length()/2).toUpperCase()+acse;
					AnsiMessage msg = new AnsiMessage();
					msg.setApdu(HexDump.toArray(acse));
					msg.setPeerAddr(context.peerAddr);
					msg.setLogicalAddress(context.meterId);
					context.reqDownMessages.add(msg);
				}
			}
			else if(req.getUpgradeType()==2){
				for(String userInformation:userInformations){//遍历request里面带有的每一个datacode下发
					String acse="";
					CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
					callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
					callingAPInvocationId.encode();
					acse=callingAPInvocationId.getCallingAPInvocationIdElement()+userInformation;
					acse="60"+parseInt2HexString(acse.length()/2).toUpperCase()+acse;
					AnsiMessage msg = new AnsiMessage();
					msg.setApdu(HexDump.toArray(acse));
					msg.setPeerAddr(context.peerAddr);
					msg.setLogicalAddress(context.meterId);
					context.reqDownMessages.add(msg);
				}
			}
			
		}
		else{//海兴采用非标准，不带context.acseTitle
			for(String userInformation:userInformations){//遍历request里面带有的每一个datacode下发
				String acse="";
				CallingAPInvocationIdElement callingAPInvocationId=new CallingAPInvocationIdElement();
				callingAPInvocationId.setCallingAPInvocationId(context.getNextInvokeId());
				callingAPInvocationId.encode();
				acse=callingAPInvocationId.getCallingAPInvocationIdElement()+userInformation;
				acse="60"+parseInt2HexString(acse.length()/2).toUpperCase()+acse;
				AnsiMessage msg = new AnsiMessage();
				msg.setApdu(HexDump.toArray(acse));
				msg.setPeerAddr(context.peerAddr);
				msg.setLogicalAddress(context.meterId);
				context.reqDownMessages.add(msg);
			}
		}

	}
	/**
	 * 为要读取的每一个数据项找到索引
	 * @param request
	 * @param table
	 * @param context
	 */
	private void getIndex(AnsiRequest request,Table table, AnsiContext context,AnsiTableItem ansiTableItem){
		AnsiDataItem[] datas=request.getDataItem();
		if(table instanceof Table64){

			if(ansiTableItem.readLatestDate){
				for(int i=0;i<1;i++){
					datas[i]=table.getIndex(datas[i],table,context);//给每一个数据项datacode找到索引值并赋值
				}
			}
			else if(ansiTableItem.readLoadProfileData){
				for(int i=1;i<datas.length;i++){
					datas[i]=table.getIndex(datas[i],table,context);//给每一个数据项datacode找到索引值并赋值
				}
			}
		}
		else {
			for(int i=0;i<datas.length;i++){
				datas[i]=table.getIndex(datas[i],table,context);//给每一个数据项datacode找到索引值并赋值
			}
		}
		
		
	}
	/**
	 * 为设置赋值
	 * @param request
	 * @param table
	 * @param context
	 */
	private void setData(AnsiRequest request,Table table, AnsiContext context){
		AnsiDataItem[] datas=request.getDataItem();
		int len=0;//计算有效的DataItem长度
		for(int i=0;i<datas.length;i++){
			if(datas[i] != null){
				len++;
			}
		}
		for(int i=0;i<len;i++){
			datas[i]=table.encode(datas[i],table,context);//给每一个数据项datacode找到索引值并赋值
		}
	}
	/**
	 * 为设置TOU赋值
	 * @param request
	 * @param table
	 * @param context
	 */
	private void setTOU(AnsiRequest request,Table table, AnsiContext context){
		if(table instanceof Table54){
			Table54 table54=(Table54)table;
			AnsiDataItem[] datas=request.getDataItem();
			int len=0;//计算有效的DataItem长度
			for(int i=0;i<datas.length;i++){
				if(datas[i] != null){
					len++;
				}
			}
			for(int i=0;i<len;i++){
				datas[i]=table54.encode(datas[i],table,context);//给每一个数据项datacode找到索引值并赋值
			
			}
		}
		
	}
	
	/**
	 * 设置负荷数据
	 * @param request
	 * @param table
	 * @param context
	 */
	private void setLoadProfile(AnsiRequest request,Table table, AnsiContext context){
		if(table instanceof Table61){
			AnsiDataItem[] datas=request.getDataItem();
			AnsiTaskInf taskInf=datas[0].taskInf;
			request.setFull(true);
			request.setServiceTag("40");
			int interval=taskInf.interval;
			if("02".equals(taskInf.intervalUint)){
				interval=interval*1;
			}else if("03".equals(taskInf.intervalUint)){
				interval=interval*60;
			}else if("04".equals(taskInf.intervalUint)){
				interval=interval*60*24;
			}
			datas[0].data="1027000000004002001800"+parseInt2HexString(taskInf.codeCount)+parseInt2HexString(interval);
			datas[0].data=datas[0].data+HexDump.toHex(AnsiDataSwitch.calculateCS(HexDump.toArray(datas[0].data), 0, HexDump.toArray(datas[0].data).length));
			datas[0].count="1A";
		}else if(table instanceof Table62){
			request.setFull(false);
			request.setServiceTag("4F");
			AnsiDataItem[] datas=request.getDataItem();
			String data="";
			String offset="";
			int len=0;//计算有效的DataItem长度
			for(int i=0;i<datas.length;i++){
				if(datas[i] != null){
					len++;
				}
			}
			for(int i=0;i<len;i++){
				AnsiTaskInf taskInf=datas[i].taskInf;
				Iterator itor=context.table12.dataItemMap.entrySet().iterator();
				while(itor.hasNext()){
					Map.Entry<Integer,String> entry=(Map.Entry<Integer,String>)itor.next();
					if(entry.getValue().equals(taskInf.taskCodes)){
						data=parseInt2HexString(entry.getKey());
						data=data+HexDump.toHex(AnsiDataSwitch.calculateCS(HexDump.toArray(data), 0, HexDump.toArray(data).length));
						offset="0000"+parseInt2HexString(i*3+1);
						datas[i].offset=offset;
						datas[i].count="0001";
						datas[i].data=data;
					}
				}
			}
		}
	}
	public String  parseInt2HexString(int i){
		String ss=Integer.toHexString(i);
		if(1==(ss.length()%2)){
			ss=0+ss;
		 }
		return ss;
	}
}
