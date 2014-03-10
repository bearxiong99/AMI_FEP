/**
 * Encode the web request into multiple DLMS messages and saved in DLMS-context.
 */
package cn.hexing.fk.bp.dlms.protocol;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRelayParam;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_OPERATION;
import cn.hexing.fas.model.dlms.DlmsRelayParam.RELAY_PROTOCOL;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.meter.BbMeterParser;
import cn.hexing.fas.protocol.meter.ModbusParser;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.DlmsMeterRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Oid;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.aa.AarqApdu.CipherMechanism;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.CosemAttributeDescriptorSelection;
import com.hx.dlms.applayer.CosemMethodDescriptor;
import com.hx.dlms.applayer.action.ActionRequest;
import com.hx.dlms.applayer.action.ActionRequestNormal;
import com.hx.dlms.applayer.action.ActionRequestWithList;
import com.hx.dlms.applayer.ex.HexingExRequest;
import com.hx.dlms.applayer.ex.HexingExRequestTransparent;
import com.hx.dlms.applayer.get.GetRequest;
import com.hx.dlms.applayer.get.GetRequestNormal;
import com.hx.dlms.applayer.get.GetRequestWithList;
import com.hx.dlms.applayer.set.SetRequest;
import com.hx.dlms.applayer.set.SetRequestFirstBlock;
import com.hx.dlms.applayer.set.SetRequestNextBlock;
import com.hx.dlms.applayer.set.SetRequestNormal;
import com.hx.dlms.applayer.set.SetRequestWithList;
import com.hx.dlms.message.DlmsMessage;

/**
 * @author: Adam Bao, hbao2k@gmail.com
 *
 */
public class DlmsProtocolEncoder {
	private static final Logger log = Logger.getLogger(DlmsProtocolEncoder.class);
	private static final TraceLog trace = TraceLog.getTracer("DLMS");
	private static final DlmsProtocolEncoder instance = new DlmsProtocolEncoder();
	private String oldProtocols  = "";
	private DlmsProtocolEncoder(){
		oldProtocols = System.getProperty("bp.protocol.old");
	}

	public static final DlmsProtocolEncoder getInstance(){ return instance; }
	
	public void build(FaalRequest request, DlmsContext context) throws IOException{
		if( request instanceof DlmsRequest ){
			DlmsRequest req = (DlmsRequest)request;
			
			if(req.getOpType()!=DLMS_OP_TYPE.OP_HEXINGEXPAND){
				DlmsObisItem[] params = req.getParams();
				String subProtocol=req.getSubprotocol();
				for( DlmsObisItem param : params){
					IDlmsScaleConvert cvt = DlmsScaleManager.getInstance().getConvert(subProtocol, param.classId, param.obisString, param.attributeId);
					boolean convertRequired = true;
					convertRequired = null != cvt;
					if( (req.getOpType() == DLMS_OP_TYPE.OP_GET || req.getOpType() == DLMS_OP_TYPE.OP_ACTION )&& param.data.isEmpty() )
						convertRequired = false;
					if( convertRequired )
						param.data = cvt.downLinkConvert((DlmsRequest)request,param.data,param);
				}
			}
			switch(req.getOpType()){
			case OP_ACTION:
				encodeAction(req,context);
				break;
			case OP_GET:
				encodeGet(req,context);
				break;
			case OP_SET:
				encodeSet(req,context);
				break;
			case OP_HEXINGEXPAND:
				encodeHexingExpand(req,context);
				break;
			case OP_NA:
				log.error("DlmsRequest operationType is Not Applicable.");
				context.webReqList.remove(0);
				break;
			default:
				context.webReqList.remove(0);
				break;
			}
		}
		else{
			log.error("DlmsProtocolEncoder not support requestType:"+request.getClass().getName());
		}
	}
	
	/**
	 * Dlms中继转发
	 * @param req
	 * @param context
	 * @throws IOException 
	 */
	private void encodeHexingExpand(DlmsRequest req, DlmsContext context) throws IOException {
		//how to build request.
		DlmsRelayParam[] relayParams=req.getDlmsRelayParams();
		if(relayParams!=null){
			for(DlmsRelayParam relayParam:relayParams){
				byte[] relayApdu = null;
				if(relayParam.getRelayProtocol() == RELAY_PROTOCOL.METER_97){
					String id = relayParam.getItemId();
					String meterId = relayParam.getDeviceId();
					RELAY_OPERATION op = relayParam.getOperation();
					String params = relayParam.getParams();
					//create 645规约
					BbMeterParser coder = new BbMeterParser();
					DataItem di = new DataItem();
					di.addProperty("point", meterId);
					di.addProperty("params", params);
					String frameOf645 = HexDump.toHex(coder.constructor(id, di, op==RELAY_OPERATION.OP_SET?true:false));
					relayApdu = HexDump.toArray(frameOf645);
					
				}else if(relayParam.getRelayProtocol() == RELAY_PROTOCOL.MODBUS){
					ModbusParser coder = new ModbusParser();
					int operation =relayParam.getOperation()==RELAY_OPERATION.OP_GET?0X03:(relayParam.getOperation()==RELAY_OPERATION.OP_SET?0x10:0x00);
					relayApdu = coder.construct(relayParam.getStartPos(),relayParam.getParams(), 
							operation, relayParam.getOffset(),relayParam.getRequestNum());
				}
				
				HexingExRequest hx = new HexingExRequest();
				DlmsData data = new DlmsData();
				data.setOctetString(relayApdu);
				HexingExRequestTransparent transparent = new HexingExRequestTransparent(data);
				hx.choose(transparent);
				byte[] apdu = hx.encode();

				IMessage msg=constructMessage(req, req.getOpType(), apdu, context);
				context.reqDownMessages.add(msg);

			}
		}
	}

	public static final byte[] convertOBIS(String obis){
		int[] intOids = ASN1Oid.parse(obis);
		if( null == intOids || intOids.length != 6 )
			throw new RuntimeException("Invalid OBIS:"+obis);
		byte[] ret = new byte[6];
		for(int i=0; i<ret.length; i++ )
			ret[i] = (byte) intOids[i];
		return ret;
	}
	
	public IMessage constructMessage(DlmsRequest req,DLMS_OP_TYPE opType,byte[] apdu,DlmsContext context,int invokeId) throws IOException{
		req.setInvokeId(invokeId);
		return constructMessage(req, opType, apdu, context);
	}
	
	public IMessage constructMessage(DlmsRequest req, DLMS_OP_TYPE opType,byte[] apdu, DlmsContext context) throws IOException{
		
		if(trace.isEnabled()){
			trace.trace("After Encode:"+"MeterId:"+context.getMeterId()+",Msg:"+HexDump.toHex(apdu));
		}
		
		if( null != req && req.isRelay() ){
			RelayParam param = req.getRelayParam();
			if( param.getProtocol().equals("gw")){
				if(DlmsEventProcessor.getInstance().isRelayCiphered()){
					try {
						if(isNeedCipher(req)){
							apdu =  DlmsEventProcessor.getInstance().cipher(opType, apdu, context);
						}
					} catch (Exception e) {
						throw new IOException(e);
					}
				}
				MessageGw gwmsg = createRelayMsg( context, apdu,param);
				/*if( !this.messageQueue.sendMessage(gwmsg) ){
					context.waitReply.set(false);
					//send failed.
					this.onRequestFailed(context);
				}*/
				return gwmsg;
			}
			else{
				//Special handler for GuangGui/Zhejiang protocol or DLMS concentrator
				throw new RuntimeException("Can not supported Relay protocol");
			}
		}
		DlmsMessage msg = new DlmsMessage();
		msg.setPeerAddr(context.peerAddr);
		msg.setDstAddr(req.getDestAddr());
		if( context.aaMechanism == CipherMechanism.NO_SECURITY 
				|| context.aaMechanism == CipherMechanism.HLS_2 || context.aaMechanism == CipherMechanism.BENGAL ){
			msg.setApdu(apdu);
		} else if( context.aaMechanism == CipherMechanism.HLS_GMAC ){
				try {
					//读密钥版本号，不需要加密
					if(req==null || isNeedCipher(req))
						apdu = DlmsEventProcessor.getInstance().cipher(opType, apdu, context);
				} catch (Exception e) {
					throw new IOException(e);
				}
				msg.setApdu( apdu );
		} else{
			throw new RuntimeException("Not support Cipher");
		}
		return msg;
	}

	private boolean isNeedCipher(DlmsRequest req) {
		//能到达这里的要判断在通信的过程中，通信是加密的，但对于某些操作是不加密的
		if(req.getOperator()==null || "".equals(req.getOperator())) return true;
		
		if("ReadKeyVersion".equals(req.getOperator()) 
		||"UPGRADE-03".equals(req.getOperator()) 
		||"UPGRADE-RESSIUE".equals(req.getOperator())
		||req.getDestAddr() == DlmsMessage.DstAddrToModule    //对模块升级不加密
		) {
			return false;
		}
		
		return true;
	}

	public static  MessageGw createRelayMsg(DlmsContext context,byte[] apdu, 
			RelayParam param) {
		MessageGw gwmsg = new MessageGw();
		gwmsg.setStatus( MessageConst.DLMS_RELAY_FLAG );
		gwmsg.head.rtua = (int)Long.parseLong(param.getDcLogicalAddress(), 16);
		gwmsg.setAFN( MessageConst.GW_FUNC_RELAY_READ );
		gwmsg.setSEQ((byte)context.getNextInvokeId());
		byte[] pwd = HexDump.toArray(param.getPassword());
		int len = 12 + pwd.length + apdu.length;
		ByteBuffer relayData = ByteBuffer.allocate(len);
		relayData.putInt(0x0100);
		relayData.put((byte)context.port);
		relayData.put((byte)0x4b);		//Transparent forward control-word
		relayData.put((byte)0x85);
		relayData.put((byte)0x64);
		short apduLen = (short)((((apdu.length+2) & 0xFF)<<8) | (((apdu.length+2)>>8)&0xFF));
		relayData.putShort(apduLen);
		apduLen = (short) ( (param.getMeasurePoint()&0xFF)<<8 | ( (param.getMeasurePoint()>>8)&0xFF ) );
		relayData.putShort(apduLen);
		relayData.put(apdu);
		relayData.put(pwd);
		relayData.flip();
		gwmsg.data = relayData;
		if(log.isDebugEnabled()){
			log.debug("dcLogicAddress:"+param.getDcLogicalAddress()+",measurePoint:"+param.getMeasurePoint()+",port:"+context.port+",msg:"+gwmsg);			
		}
		return gwmsg;
	}
	
	private void encodeGet(DlmsRequest req, DlmsContext context) throws IOException{
		if( !context.isCanGet() )
			throw new RuntimeException("DLMS device not support GET");
		String meterId=req.getMeterId();
		//If get multiple attributes, negotiates withList
		DlmsObisItem[] params = req.getParams();
		if( params.length>1 ){
			if( context.isMultipleReference() ){
				GetRequestWithList getWithList = new GetRequestWithList();
				getWithList.setInvokeId(context.getNextInvokeId());
				CosemAttributeDescriptorSelection[] attrs = new CosemAttributeDescriptorSelection[params.length];
				for( int i=0; i<params.length; i++ ){
					attrs[i] = new CosemAttributeDescriptorSelection(params[i].classId,convertOBIS(params[i].obisString),params[i].attributeId);
					if( params[i].accessSelector != -1 ){
						attrs[i].setSelector(params[i].accessSelector, params[i].data);
					}
				}
				getWithList.setAttributeList(attrs);
				GetRequest getReq = new GetRequest(getWithList);
				
				byte[] apdu = getReq.encode();
				
				IMessage msg = constructMessage(req,DLMS_OP_TYPE.OP_GET, apdu, context,getWithList.getInvokeId());
				context.reqDownMessages.add(msg);
				return;
			}
		}
		int seq = context.getNextInvokeId(meterId);
		
		String subProtocol = null;
		DlmsMeterRtu rtu = RtuManage.getInstance().getDlmsMeterRtu(req.getMeterId());
		if(rtu != null){
			subProtocol=rtu.getSubProtocol();
		}else{
			subProtocol = req.getSubprotocol();
		}
		
		for(int i=0; i<params.length; i++ ){
			
			byte[] obis = convertOBIS(params[i].obisString);
			GetRequestNormal getNormal = new GetRequestNormal(seq,params[i].classId,obis,params[i].attributeId);
			
			if( params[i].accessSelector != -1 ){
				if(subProtocol==null ||subProtocol.equals("0")|| oldProtocols.indexOf(subProtocol)<0){
					//如果当前的req没有子规约或者子规约不在老规约之内。按规约流程走
					getNormal.getSelectiveAccess().setParameter(params[i].accessSelector, params[i].data);
				}else{
					//带时间读处理
					//如果请求中不携带子规约号，则需要重组request
					ASN1SequenceOf struct = params[i].data.getStructure();
					if(struct.isEncodeLength){
						DlmsData[] members = (DlmsData[]) struct.getMembers();
						members[1].changeToOldTime();
						members[2].changeToOldTime();
						members[3] = new DlmsData();
						struct = new ASN1SequenceOf(members);
						struct.isEncodeLength =false;
						params[i].data.setStructure(struct);
					}
					
					//否则，按老规约组帧
					getNormal.getSelectiveAccess().setParameter( params[i].data);
				}
			}
			GetRequest getReq = new GetRequest(getNormal);
			byte[] apdu = getReq.encode();

			IMessage msg = null;
			msg = constructMessage(req,DLMS_OP_TYPE.OP_GET, apdu, context,seq);
			
			context.reqDownMessages.add(msg);
		}
		context.putHistoryEvent(seq,meterId ,context.webReqList.get(0));
	}
	
	private void encodeSet(DlmsRequest req, DlmsContext context) throws IOException{
		if( !context.isCanSet() )
			throw new RuntimeException("DLMS device not support SET");
		String meterId=req.getMeterId();

		DlmsObisItem[] params = req.getParams();
		//This version, we support with list
		if( params.length>1 ){
			if( context.isMultipleReference() ){
				SetRequestWithList setWithList = new SetRequestWithList();
				setWithList.setInvokeId(context.getNextInvokeId());
				CosemAttributeDescriptorSelection[] attrs = new CosemAttributeDescriptorSelection[params.length];
				DlmsData[] values = new DlmsData[params.length];
				for(int i=0; i<params.length; i++ ){
					byte[] obis = convertOBIS(params[i].obisString);
					attrs[i] = new CosemAttributeDescriptorSelection();
					attrs[i].setAttribute(params[i].classId, obis, params[i].attributeId);
					values[i] = params[i].data;
				}
				setWithList.setAttributeList(attrs);
				setWithList.setValueList(values);
				
				SetRequest setReq = new SetRequest();
				setReq.choose(setWithList);
				
				byte[] apdu = setReq.encode();
				//With-List but block not support yet.
				IMessage msg = constructMessage(req,DLMS_OP_TYPE.OP_SET, apdu, context,setWithList.getInvokeId());
				context.reqDownMessages.add(msg);
				return;
			}
		}
		
		int seq = context.getNextInvokeId(meterId);

		for(int i=0; i<params.length; i++ ){
			byte[] obis = convertOBIS(params[i].obisString);
			int clsId = params[i].classId;
			int attId = params[i].attributeId;
			
			byte[] paramData = params[i].data.encode();
			//加密比不加密数据多20字节,这个地方有可能会有问题，因为之前测试的过程中，有些表返回的最大pduSize是500多，但是模块支持不了。
			int maxApduSize = context.getMaxPduSize()-66; //分帧的长度，为了保证设置通道帧不分帧，而测试出来的最大分帧长度。原来是256-80
			if(req.isRelay()){
				maxApduSize = 200-80;
			}
			if( paramData.length>= maxApduSize ){
				//SetRequestNormal exceeds max-pdu-size
				int blockNum = 1;
				int offset = 0;
				while( offset< paramData.length ){
					int len = Math.min(maxApduSize, paramData.length-offset);
					byte[] blockData = new byte[len];
					for(int j=0; j<len; j++ ){
						blockData[j] = paramData[ offset++ ];
					}
					byte[] apdu = null;
					if( blockNum == 1 ){
						SetRequestFirstBlock first = new SetRequestFirstBlock(seq,clsId,obis,attId,blockData);
						SetRequest setReq = new SetRequest();
						setReq.choose(first);
						apdu = setReq.encode();
					}
					else{
						SetRequestNextBlock next = new SetRequestNextBlock(seq, paramData.length==offset, blockNum,blockData);
						SetRequest setReq = new SetRequest();
						setReq.choose(next);
						apdu = setReq.encode();
					}
					blockNum++;
					if(log.isDebugEnabled())
						log.debug("set with block:"+HexDump.toHex(apdu)+".\n" +
								"classId:"+params[i].classId+",obis:"+params[i].obisString+",attr:"+params[i].attributeId+",meterId:"+context.meterId);
					IMessage msg =constructMessage(req,DLMS_OP_TYPE.OP_SET, apdu, context,seq); 
					context.reqDownMessages.add(msg);
				}
			}
			else{
				SetRequestNormal setNormal = new SetRequestNormal(seq,clsId,obis,attId,params[i].data);
				SetRequest setReq = new SetRequest();
				setReq.choose(setNormal);

				byte[] apdu = setReq.encode();
				IMessage msg =constructMessage(req,DLMS_OP_TYPE.OP_SET, apdu, context,seq); 
				context.reqDownMessages.add(msg);
			}
		}
		context.putHistoryEvent(seq,meterId ,context.webReqList.get(0));
	}
	
	private void encodeAction(DlmsRequest req, DlmsContext context) throws IOException{
		if( !context.isCanAction() )
			throw new RuntimeException("DLMS device not support ACTION");
		String meterId=req.getMeterId();
		DlmsObisItem[] reqParams = req.getParams();
		//This version, we support ActionNormal & ActionWithList
		if( reqParams.length>1 ){
			if( context.isMultipleReference() ){
				CosemMethodDescriptor[] methods = new CosemMethodDescriptor[reqParams.length];
				DlmsData[] actionParams = new DlmsData[reqParams.length];
				for(int i=0; i<reqParams.length; i++ ){
					byte[] obis = convertOBIS(reqParams[i].obisString);
					methods[i] = new CosemMethodDescriptor(reqParams[i].classId, obis, reqParams[i].attributeId);
					actionParams[i] = reqParams[i].data;
				}
				ActionRequestWithList actionWithList = new ActionRequestWithList(methods,actionParams);
				actionWithList.setInvokeId(context.getNextInvokeId());
				
				ActionRequest actionReq = new ActionRequest();
				actionReq.choose(actionWithList);
				
				byte[] apdu = actionReq.encode();
				//With-List but block not support yet.
				IMessage msg = constructMessage(req,DLMS_OP_TYPE.OP_ACTION, apdu, context,actionWithList.getInvokeId());
				context.reqDownMessages.add(msg);
				return;
			}
		}
		int seq = context.getNextInvokeId(meterId);
		for(int i=0; i<reqParams.length; i++ ){
			byte[] obis = convertOBIS(reqParams[i].obisString);
			int clsId = reqParams[i].classId;
			int attId = reqParams[i].attributeId;
			
			byte[] paramData = reqParams[i].data.encode();
			int maxApduSize = context.getMaxPduSize() - 20;
			if( paramData.length>= maxApduSize ){
				//SetRequestNormal exceeds max-pdu-size
				throw new RuntimeException("action-request: paramData.length>maxApduSize, meterId="+context.meterId);
			}
			else{
				ActionRequestNormal actionNormal = new ActionRequestNormal(seq,clsId,obis,attId,reqParams[i].data);
				ActionRequest actionReq = new ActionRequest();
				actionReq.choose(actionNormal);
				
				byte[] apdu = actionReq.encode();
				IMessage msg =constructMessage(req,DLMS_OP_TYPE.OP_ACTION, apdu, context,seq); 
				context.reqDownMessages.add(msg);
			}
		}
		context.putHistoryEvent(seq,meterId ,context.webReqList.get(0));
	}
}
