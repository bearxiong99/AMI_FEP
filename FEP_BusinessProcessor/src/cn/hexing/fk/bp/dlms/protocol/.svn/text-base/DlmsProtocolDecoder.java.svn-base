package cn.hexing.fk.bp.dlms.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRelayParam;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Enum;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.DataAccessResult;
import com.hx.dlms.applayer.action.ActionResponse;
import com.hx.dlms.applayer.action.ActionResponseNormal;
import com.hx.dlms.applayer.action.ActionResponseWithList;
import com.hx.dlms.applayer.action.ResponseOptionalData;
import com.hx.dlms.applayer.ex.HexingExResponse;
import com.hx.dlms.applayer.ex.HexingExResponseTransparent;
import com.hx.dlms.applayer.get.GetDataResult;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNext;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.applayer.get.GetResponseWithBlock;
import com.hx.dlms.applayer.get.GetResponseWithList;
import com.hx.dlms.applayer.set.SetResponse;
import com.hx.dlms.applayer.set.SetResponseLastBlock;
import com.hx.dlms.applayer.set.SetResponseNormal;
import com.hx.dlms.applayer.set.SetResponseWithBlock;
import com.hx.dlms.applayer.set.SetResponseWithList;
import com.hx.dlms.applayer.set.SetResponseWithListLastBlock;
import com.hx.dlms.message.DlmsMessage;

public class DlmsProtocolDecoder {
	private static final Logger log = Logger.getLogger(DlmsProtocolDecoder.class);
	private static final TraceLog trace = TraceLog.getTracer("DLMS");
	private static final DlmsProtocolDecoder instance = new DlmsProtocolDecoder();
	private DlmsProtocolDecoder(){}
	public static final DlmsProtocolDecoder getInstance(){ return instance; }
	
	private DlmsEventProcessor eventProcessor = null;

	public void setEventProcessor(DlmsEventProcessor eventProcessor) {
		this.eventProcessor = eventProcessor;
	}
	
	public void onGetResponse(GetResponse resp, DlmsContext context,DlmsEvent evt) throws IOException{
		ASN1Type selObj = resp.getDecodedObject();
		if( selObj instanceof GetResponseNormal)
			handleGetResponseNormal((GetResponseNormal)selObj,context,evt);
		else if( selObj instanceof GetResponseWithBlock )
			handleGetResponseBlock((GetResponseWithBlock)selObj,context,evt);
		else if( selObj instanceof GetResponseWithList)
			handleGetResponseList((GetResponseWithList)selObj,context,evt);
		else
			log.error("GetResponse UNKNOWN decodeObject");
	}
	
	private void handleGetResponseList(GetResponseWithList repList, DlmsContext context, DlmsEvent evt) throws IOException{
		GetDataResult[] results = repList.getResultList();
		if( null == results ){
			log.error("getResponseList result is null.");
			eventProcessor.onRequestFailed(context);
			return;
		}
		//The request message is replied completely
		DlmsRequest req = ((DlmsEvent)context.webReqList.get(0)).getDlmsRequest();
		if( req.getOpType() != DLMS_OP_TYPE.OP_GET ){
			log.error("up-message is GET-RESPONSE-LIST, but request type is"+req.getOpType());
			eventProcessor.onRequestFailed(context);
			return;
		}
		DlmsObisItem[] params = req.getParams();
		if( params.length != results.length ){
			log.error("getResponseList: result.length != params.length");
			eventProcessor.onRequestFailed(context);
			return;
		}
		for(int i=0; i<params.length; i++){
			ASN1Type resultObj = results[i].getDecodedObject() ;
			if( resultObj instanceof DlmsData )
				params[i].resultData = (DlmsData)resultObj;
			else if( resultObj instanceof ASN1Enum ){
				params[i].resultCode = ((ASN1Enum)resultObj).getEnumValue();
			}
			else{
				log.error("Unknown result object. tag="+resultObj.identifier());
				eventProcessor.onRequestFailed(context);
				break;
			}
		}
		eventProcessor.onRequestComplete(context,evt);
	}
	
	private void handleGetResponseBlock(GetResponseWithBlock repBlock, DlmsContext context, DlmsEvent upEvent) throws IOException{
		boolean lastBlock = repBlock.isLastBlock();
		int blockNum = repBlock.getBlockNumber();
		int invokeId = getRealInvokeId(repBlock.getInvokeId(),repBlock.isPriorityHigh());
		
		DlmsMessage dlmsMsg=(DlmsMessage) upEvent.getMessage();
		ConcurrentHashMap<Integer, Object> historyEvents = context.getHistoryEvents(dlmsMsg.getLogicalAddress());
		if(!historyEvents.containsKey(invokeId) && invokeId!=0x81){
			log.error("When handleGetResponseBlock HistoryEvents uncontains this invokeId = "+invokeId+",meterId:"+dlmsMsg.getLogicalAddress());
			return;
		}
		
		DlmsEvent event = null; 
		if(invokeId==0x81){
			if(context.webReqList.size()>0){
				event = (DlmsEvent) context.webReqList.get(0);				
			}else{
				log.error("return msg is 0x81,and webReqList's size is null. unhandle it .meterId:"+context.getMeterId());
				return;
			}
		}else{
			event = (DlmsEvent) historyEvents.get(invokeId);			
		}
		
		DlmsRequest request = event.getDlmsRequest();
		if(request.getBlockReplys()==null){
			request.setBlockReplys(new ArrayList<Object>());
		}
		boolean duplicated = false;
		byte[] temp = null;
		int len = 0;
		for(int i=0; i<request.getBlockReplys().size(); i++){
			GetResponseWithBlock b = (GetResponseWithBlock)request.getBlockReplys().get(i);
			temp = b.getRawData();
			len += null==temp ? 0 : temp.length ;
			if( b.getBlockNumber() == blockNum ){
				duplicated = true;
				break;
			}
		}
		if( duplicated ){
			log.warn("Duplicated GetResponseWithBlock:"+repBlock);
			eventProcessor.onRequestFailed(context);
			return;
		}
		temp = repBlock.getRawData();
		if( null == temp ){
			//Block reply resultCode for failure.
			ASN1Enum accessCode = new ASN1Enum(repBlock.getAccessResult().toInt());
			handleGetResultComplete(accessCode,context,invokeId,upEvent);
			if(log.isInfoEnabled())
				log.info("getBlockResp failed:"+repBlock.getAccessResult()+", getRepBlock="+repBlock);
			return;
		}
		else{
			len += temp.length ;
		}
		request.getBlockReplys().add(repBlock);
		if( !lastBlock ){
			//sendNextBlock
			GetRequestNext next = new GetRequestNext();
			next.setInvokeId(repBlock.getInvokeId());
			next.setPriorityHigh(repBlock.isPriorityHigh());
			next.setBlockNumber(blockNum);
			GetRequest reqGet = new GetRequest(next);
			byte[] apdu = reqGet.encode();
			DlmsRequest dr = null;
			if(context.webReqList.size()>0){
				DlmsEvent de=(DlmsEvent) context.webReqList.get(0);
				if(de == event){
					dr = de.getDlmsRequest();					
				}
			}else{
				//请求列表大小==0,可以继续发送
				context.webReqList.add(event);
				dr = request;
			}
			if(dr == null){
				log.warn("response block request is null,maybe return not match request,can't send any message.logicAddress:"+context.meterId+",blockNum:"+blockNum);
				return;
			}
			if(log.isDebugEnabled())
				log.debug("next-block-req:"+HexDump.toHex(apdu)+",meterId:"+context.meterId);
			IMessage msg = DlmsProtocolEncoder.getInstance().constructMessage(dr,DLMS_OP_TYPE.OP_GET, apdu, context,repBlock.getInvokeId());
			eventProcessor.sendMessage(msg,context);
		}
		else{
			//Last block, same as getResponseNormal invoked.
			ByteBuffer buf = ByteBuffer.allocate(len);
			for(int i=0; i<request.getBlockReplys().size(); i++){
				GetResponseWithBlock b = (GetResponseWithBlock)request.getBlockReplys().get(i);
				temp = b.getRawData();
				if( null != temp )
					buf.put(temp);
			}
			buf.flip();
			DlmsData data = new DlmsData();
			data.decode(DecodeStream.wrap(buf));
			handleGetResultComplete(data,context,invokeId,upEvent);
			request.getBlockReplys().clear();
		}
	}
	/**
	 * 这里是为了对有些表做的不规范，返回的帧序号不对应而做的处理，在测试过程中发现一些表无论发送什么数据，返回都是0x81
	 * 对于81要让下面处理程序知道,以便于特殊处理。
	 * @param invokeId
	 * @param isPriority
	 * @return
	 */
	private int getRealInvokeId(int invokeId,boolean isPriority){
		return !isPriority?invokeId:invokeId|0x80;
	}
	private void handleGetResponseNormal(GetResponseNormal normal, DlmsContext context, DlmsEvent evt) throws IOException{
		//The request message is replied completely
		ASN1Type selObj = normal.getResult().getDecodedObject();

		handleGetResultComplete(selObj,context,getRealInvokeId(normal.getInvokeId(),normal.isPriorityHigh()),evt);
	}
	
	private void handleGetResultComplete(ASN1Type resultObj, DlmsContext context,int invokeId,DlmsEvent evt) throws IOException{
		//The request message is replied completely
		ProcessModel pm = new ProcessModel(context, invokeId,evt);
		if(!pm.beforeProcess())
			return;
		
		DlmsRequest req = pm.event.getDlmsRequest();

		if( req.getOpType() != DLMS_OP_TYPE.OP_GET ){
			log.error("up-message is GET-RESPONSE-X, but request type is"+req.getOpType());
			eventProcessor.onRequestFailed(context);
			return;
		}
		DlmsObisItem[] params = req.getParams();
		boolean repComplete = false;
		if( resultObj instanceof DlmsData ){
			for(int i=0; i<params.length; i++){
				if( null == params[i].resultData && params[i].resultCode==-1){
					params[i].resultData = (DlmsData)resultObj;
					if(log.isInfoEnabled() || trace.isEnabled()){
						String logValue="the result data info \n\tclassId:"+params[i].classId+",obis:"+params[i].obisString+",attr:"+params[i].attributeId+".\n\tdata:"+params[i].resultData.toString()
								+"\n\tcurrentIndex:"+(i+1)+",allLength:"+params.length+",invokeId:"+invokeId+",meterId:"+context.meterId;
						log.info(logValue);
						trace.trace(logValue);
					}
					if( i == params.length -1 )
						repComplete = true;
					break;
				}
			}
		}
		else {
			for(int i=0; i<params.length; i++){
				if( null == params[i].resultData && params[i].resultCode==-1){
					params[i].resultCode = ((ASN1Enum)resultObj).getEnumValue();
					if(log.isInfoEnabled() || trace.isEnabled()){
						String logValue="the result data info \n\tclassId:"+params[i].classId+",obis:"+params[i].obisString+",attr:"+params[i].attributeId+".\n\tresult:"+DataAccessResult.parseResult(params[i].resultCode)
								+"\n\tcurrentIndex:"+(i+1)+",allLength:"+params.length+",invokeId:"+invokeId+",meterId:"+context.meterId;
						log.info(logValue);
						trace.trace(logValue);
					}
					if( i == params.length -1 )
						repComplete = true;
					break;
				}
			}
		}
		
		pm.afterProcess(repComplete);			
	}
	
	private void handleSetResponseNormal(SetResponseNormal repNormal,DlmsContext context, SetResponse resp,DlmsEvent event) throws IOException{
		handleSetResultComplete(repNormal.getAccessResultEnum(),context,resp,getRealInvokeId(repNormal.getInvokeId(),repNormal.isPriorityHigh()),event);
	}
	
	private void handleSetResponseWithList(SetResponseWithList repList,DlmsContext context,SetResponse resp,DlmsEvent event) throws IOException{
		handleSetResultComplete(repList.getResultListSequence(),context,resp,getRealInvokeId(repList.getInvokeId(),repList.isPriorityHigh()),event);
	}
	
	private void handleSetResponseWithBlock(SetResponseWithBlock repBlock,DlmsContext context,SetResponse resp, DlmsEvent event) throws IOException{
		// Remember that block request messages already in context message-list.
		//Go-ahead as if request is completed so as to send next message.
		//如果跟当前的不匹配,不处理了。
		int invokeId=getRealInvokeId(repBlock.getInvokeId(),repBlock.isPriorityHigh());

		if(invokeId!=0x81){
			if(context.webReqList.size()>0){
				DlmsEvent evt = (DlmsEvent) context.webReqList.get(0);
				if(evt.getDlmsRequest().getInvokeId()!=invokeId){
					log.error("return invokeId unequal request invokeId. request invokeId="+evt.getDlmsRequest().getInvokeId()+",return invokeId:"+invokeId);
					return;				
				}
			}
		}
		if(context.reqDownMessages.size()!=0){
			eventProcessor.sendMessage(context.reqDownMessages.remove(0), context);
		}else{
			eventProcessor.onRequestComplete(context,invokeId,event);	
		}
	}
	
	private void handleSetResponseLastBlock(SetResponseLastBlock lastBlock,DlmsContext context,SetResponse resp,DlmsEvent event) throws IOException{
		handleSetResultComplete(lastBlock.getAccessResultEnum(),context,resp,getRealInvokeId(lastBlock.getInvokeId(),lastBlock.isPriorityHigh()),event);
	}
	
	private void handleSetWithListLastBlock(SetResponseWithListLastBlock withListLastBlock,DlmsContext context,SetResponse resp,DlmsEvent event) throws IOException{
		handleSetResultComplete(withListLastBlock.getResultListSequence(),context,resp,getRealInvokeId(withListLastBlock.getInvokeId(),withListLastBlock.isPriorityHigh()),event);
	}

	private void handleSetResultComplete(ASN1Type resultObj, DlmsContext context, SetResponse resp,int invokeId,DlmsEvent upEvent) throws IOException{
		//The request message is replied completely
		ProcessModel pm = new ProcessModel(context, invokeId,upEvent);
		if(!pm.beforeProcess())
			return;
		DlmsRequest req = pm.event.getDlmsRequest();

		if( req.getOpType() != DLMS_OP_TYPE.OP_SET ){
			log.error("up-message is SET-RESPONSE-x, but request type is"+req.getOpType());
			eventProcessor.onRequestFailed(context);
			return;
		}
		ASN1Enum[] results = null;
		if( resultObj instanceof ASN1Enum) {
			results = new ASN1Enum[] { (ASN1Enum)resultObj };
		}
		else if( resultObj instanceof ASN1SequenceOf ){
			results = (ASN1Enum[])((ASN1SequenceOf)resultObj).getMembers();
		}
		else{
			log.error("resultObj is not ENUM: "+resultObj.getClass().getName());
			eventProcessor.onRequestFailed(context);
			return;
		}
		DlmsObisItem[] params = req.getParams();
		boolean repComplete = false;
		int offset = 0;
		for(int index=0; index<params.length && offset<results.length; index++){
			if( -1 == params[index].resultCode ){
				params[index].resultCode = results[offset++].getEnumValue();
				
				if(log.isInfoEnabled() || trace.isEnabled()){
					String logValue="the result data info \n\tclassId:"+params[index].classId+",obis:"+params[index].obisString+",attr:"+params[index].attributeId+".\n\tresultCode:"+DataAccessResult.parseResult(params[index].resultCode)
					+"\n\tcurrentIndex:"+(index+1)+",allLength:"+params.length+",invokeId:"+invokeId;
					log.info(logValue);		
					trace.trace(logValue);
				}
				
				if( resp.getDecodedObject() instanceof SetResponseNormal ){
					SetResponseNormal normal = (SetResponseNormal)resp.getDecodedObject();
					if( ! normal.getOptionalData().isEmpty() && !(normal.getOptionalData().getValue()==null)){
						params[index].resultData = normal.getOptionalData();
					}
				}
				if( index == params.length -1 ){
					repComplete = true;
				}
			}
		}
		pm.afterProcess(repComplete);			
	}

	public void onSetResponse(SetResponse resp, DlmsContext context,DlmsEvent event) throws IOException{
		ASN1Type selObj = resp.getDecodedObject();
		switch( selObj.getTagAdjunct().identifier() ){
		case 1:
			handleSetResponseNormal((SetResponseNormal)selObj,context,resp,event);
			break;
		case 2:
			handleSetResponseWithBlock((SetResponseWithBlock)selObj,context,resp,event);
			break;
		case 3:
			handleSetResponseLastBlock((SetResponseLastBlock)selObj,context,resp,event);
			break;
		case 4:
			handleSetWithListLastBlock((SetResponseWithListLastBlock)selObj,context,resp,event);
			break;
		case 5:
			handleSetResponseWithList((SetResponseWithList)selObj,context,resp,event);
			break;
		default:
			log.error("SetResponse UNKNOWN decodeObject:"+HexDump.hexDumpCompact(ByteBuffer.wrap(resp.encode())));
			eventProcessor.onRequestFailed(context);
		}
	}
	
	private void handleActionResponseNormal(ActionResponseNormal normal, DlmsContext context,DlmsEvent event) throws IOException {
		handleActionResponseComplete(normal.getRespWithOptionalData(),context,getRealInvokeId(normal.getInvokeId(),normal.isPriorityHigh()),event);
	}
	
	private void handleActionResponseWithList(ActionResponseWithList withList, DlmsContext context,DlmsEvent event) throws IOException{
		handleActionResponseComplete(withList.getResponseList(),context,withList.getInvokeId(),event);
	}
	
	private void handleActionResponseComplete(ASN1Type resultObj, DlmsContext context,int invokeId,DlmsEvent event) throws IOException{
		//The request message is replied completely
		//The request message is replied completely

		ProcessModel pm = new ProcessModel( context, invokeId,event);
		if(!pm.beforeProcess()){
			return ;
		}
		
		DlmsRequest req = pm.event.getDlmsRequest();
		if( req.getOpType() != DLMS_OP_TYPE.OP_ACTION ){
			log.error("up-message is ACTION-RESPONSE-x, but request type is"+req.getOpType());
			eventProcessor.onRequestFailed(context);
			return;
		}
		ASN1Enum[] resultCodes = null;
		DlmsData[] resultDatas = null;
		if( resultObj instanceof ResponseOptionalData ){
			ResponseOptionalData optData = (ResponseOptionalData)resultObj;
			resultCodes = new ASN1Enum[] { optData.getActionResultEnum() };
			resultDatas = new DlmsData[] { optData.getReturnParameters().getData() };
		}
		else if( resultObj instanceof ASN1SequenceOf ){
			ResponseOptionalData[] optDatas = (ResponseOptionalData[])((ASN1SequenceOf)resultObj).getMembers();
			resultCodes = new ASN1Enum[optDatas.length];
			resultDatas = new DlmsData[optDatas.length];
			for(int i=0 ; i < optDatas.length; i++ ){
				resultCodes[i] = optDatas[i].getActionResultEnum();
				resultDatas[i] = optDatas[i].getReturnParameters().getData();
			}
		}
		else{
			log.error("resultObj is not supported : "+resultObj.getClass().getName());
			eventProcessor.onRequestFailed(context);
			return;
		}
		DlmsObisItem[] params = req.getParams();
		boolean repComplete = false;
		int offset = 0;
		for(int i=0; i<params.length; i++){
			if( -1 == params[i].resultCode ){
				params[i].resultCode = resultCodes[offset].getEnumValue();
				params[i].resultData = resultDatas[offset];
				if(log.isInfoEnabled() || trace.isEnabled()){
					String logValue ="the result data info \n\tclassId:"+params[i].classId+",obis:"+params[i].obisString+",attr:"+params[i].attributeId+".\n\tresultCode:"+DataAccessResult.parseResult(params[i].resultCode)+"\n\tresutlData:"+params[i].resultData
					+"\n\tcurrentIndex:"+(i+1)+",allLength:"+params.length+",invokeId:"+invokeId;
					log.info(logValue);
					trace.trace(logValue);
				}
				offset++;
				if( i == params.length -1 ){
					repComplete = true;
				}
			}
		}
		pm.afterProcess(repComplete);
	}

	public void onActionResponse(ActionResponse resp, DlmsContext context,DlmsEvent event) throws IOException{
		ASN1Type selObj = resp.getDecodedObject();
		switch( selObj.getTagAdjunct().identifier() ){
		case 1:
			handleActionResponseNormal((ActionResponseNormal)selObj,context,event);
			break;
		case 3:
			handleActionResponseWithList((ActionResponseWithList)selObj,context,event);
			break;
		case 2:
//			handleActionResponseWithPblock((ActionResponseWithPblock)selObj,context);
		case 4:
//			handleActionResponseNextPblock((ActionResponseNextPblock)selObj,context);
		default:
			log.error("ActionResponse un-supported decodeObject:"+HexDump.hexDumpCompact(ByteBuffer.wrap(resp.encode())));
			eventProcessor.onRequestFailed(context);
		}
	}
	public void onHexingExResponse(HexingExResponse resp, DlmsContext cxt, DlmsEvent evt) throws IOException {
		ASN1Type selObj = resp.getDecodedObject();
		switch(selObj.getTagAdjunct().identifier()){
		case 0:
			handleHexingExResponseTransparent((HexingExResponseTransparent)selObj,cxt,evt);
			break;
		}
		
	}
	
	/**
	 * 处理中继请求返回
	 * @param selObj
	 * @param cxt
	 * @param evt 
	 * @throws IOException 
	 */
	private void handleHexingExResponseTransparent(HexingExResponseTransparent selObj, DlmsContext cxt, DlmsEvent evt) throws IOException {


		//The request message is replied completely
		
		if(cxt.webReqList.size()==0) {
			log.error("WebReqList.size is 0,some msgs must receive more than one time,meterId:"+cxt.meterId);
			return;
		}
		
		DlmsRequest req = ((DlmsEvent)cxt.webReqList.get(0)).getDlmsRequest();
		
		DlmsRelayParam[] relayParams = req.getDlmsRelayParams();
		boolean repComplete = false;
		DlmsData result = selObj.getData();
		String strValue=result.getStringValue();//strValue is 645 frame
		for(int i=0;i<relayParams.length;i++){
			if(null == relayParams[i].getUpRelayMessage()){
				relayParams[i].setUpRelayMessage(strValue);
				if(i==relayParams.length-1)
					repComplete=true;
				break;
			}
		}
		if( repComplete ){
			eventProcessor.onRequestComplete(cxt,evt);
		}
		else{
			cxt.enableSend();
			eventProcessor.sendNextMessage(cxt);
		}
	
		
		//是否所有的消息都发送
		
		
		//how to do ?
		//how to return?  
		//how to communication with web?
		
	}
	private class ProcessModel{
		public boolean isMatch = false;
		public DlmsEvent realEvent = null;
		public DlmsEvent event = null;
		public DlmsEvent upEvent =null;
		public String meterId;
		ConcurrentHashMap<Integer, Object> historyEvents;
		DlmsContext context;
		int invokeId;
		public ProcessModel(DlmsContext context,int invokeId, DlmsEvent evt){
			this.invokeId = invokeId;
			this.context = context;
			this.upEvent = evt;
		}
		
		private boolean beforeProcess(){
			DlmsMessage dlmsMsg=(DlmsMessage) upEvent.getMessage();
			meterId=dlmsMsg.getLogicalAddress();
			historyEvents = context.getHistoryEvents(meterId);
			if(context.webReqList.size()==0 && historyEvents.get(invokeId)==null) {
				log.error("WebReqList.size is 0 and HistoryEvents uncontains this invokeId:"+(invokeId)+",HistoryEvents.size="+historyEvents.size()+",meterId:"+this.meterId);
				return false;
			}else{
				if(context.webReqList.size()>0)
					realEvent=(DlmsEvent) context.webReqList.get(0);
			}
			//如果是由于表计返回的帧序号不对，而出现的不匹配的情况,应该如何处理?

			if(invokeId==0x81){ //在测试过程中发现0x81都是错误的帧序号
				String prefix="return invokeId is 0x81,maybe wrong,";
				if(realEvent == null){
					log.error(prefix+"and webReqList's size is null.so unhandle it . meterId:"+meterId);
					return false;
				}else{
					log.error(prefix+"handle this msg as WebReqList's first element. meterId:"+meterId);
					event = realEvent;
					isMatch = true;
					return true;
				}
			}
			
			if(historyEvents.containsKey(invokeId))
				event = (DlmsEvent) historyEvents.get(invokeId);
			else{
				log.warn("historyEvents uncontains invokeId="+invokeId+",meterId:"+meterId);
			}
			
			
			if(event == realEvent){
				isMatch = true;
			}
			if(event == null){
				isMatch = true;
				event = realEvent;
			}
			if(event == null)
				return false;
			return true;
		}

		public void afterProcess(boolean repComplete) throws IOException {

			if (repComplete) {
				// 请求结束处理

				if(log.isInfoEnabled()){
					long timeOut=System.currentTimeMillis()-context.lastSendTime;
					log.info("request finished,meterId:"+context.getMeterId()+",remaing sendlist size:"+context.webReqList.size()+",use time(ms):"+timeOut);					
				}

				eventProcessor.onRequestComplete(context, invokeId,upEvent);
				historyEvents.remove(invokeId);
			} else {
				// 如果未请求结束
				// 1.如果是match,发送下一个消息
				// 2.如果not match,有可能为空的情况,如果是空,当前请求可以发送,否则不能发送
				if (isMatch || realEvent == null) {
					if (realEvent == null) {
						if(log.isInfoEnabled()){
							log.info("realEvent is null and return msg uncomplete,add this event add webReq first position. meterId:"+context.meterId+",invokeId:"+invokeId);
						}
						context.webReqList.add(event);
					}
					context.enableSend();
					eventProcessor.sendNextMessage(context);
				} else {
					if(log.isInfoEnabled())
						log.info("unmatch return msg uncomplete,and webReqList.size>0,add this event at second poistion.meterId:"+context.meterId+",invokeId:"+invokeId);
					// 需要将请求放在队列的第二个位置
					context.webReqList.add(1, event);
				}
			}
		}
	}
}
