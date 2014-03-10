package cn.hexing.fk.bp.processor.gg;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.FaalGGKZM30Request;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gate.GateHead;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.utils.FileAssistant;
import cn.hexing.fk.utils.FtpFileReader;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.fk.utils.StringUtil;

/**
 * 
 * @author gaoll
 *
 * @time 2013-8-19 上午09:01:51
 *
 * @info 广规升级处理
 */
public class GgUpgradeProcessor {
	
	//传递给集中器是固定的
	private static final String UPGRADE_FILE_NAME = "update.tbz";

	private static final String UPGRADE_RESEND_TIME = "ResendTime";

	private static final String UPGRADE_MD5 = "md5";

	private static final String UPGRADE_INFO = "Info";

	private static final String UPGRADE_ID = "UpgradeId";

	private static final String UPGRADE_CONTENT = "Content";
	
	private static final String UPGRADE_FILETYPE="FileType";

	private static final Logger log = Logger.getLogger(GgUpgradeProcessor.class);

	private static GgUpgradeProcessor instance = new GgUpgradeProcessor();
	
	public static GgUpgradeProcessor getInstance(){return instance;};

	public static final int MAX_SIZE=256;
	
	
	private GgUpgradeProcessor(){};
	
	private MasterDbService dbService ;
	
	private AsyncService service;

	private String fileDir="."+File.separator+"upgrade"+File.separator;

	
	public void processWebRequest(FaalRequest request,IChannel client){

		try {
			if(!(request instanceof FaalGGKZM30Request)) return; 
			
			FaalGGKZM30Request req = (FaalGGKZM30Request) request;
			BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache(req.getLogicAddress());
			if(bizRtu == null){
				log.error("logicAddress="+req.getLogicAddress()+",bizRtu is null.");
				return;
			}
			//get upgrade record from db
			List<UpgradeInfo> infos = dbService.getUpgradeInfo(req.getUpgradeId());
			if(infos==null || infos.size()!=1){
				log.error("UpgradeId = "+req.getUpgradeId()+",Upgrade record Size !=1");
				return;			
			} 
			
			UpgradeInfo info = infos.get(0);
			
			//if can't upgrade ,return		
			if(info.getStatus()==2 ||info.getStatus()==255){
				log.error("UpgradeId = "+req.getUpgradeId()+",Record Status is "+info.getStatus());
				return;
			} 
			//download file from ftp
			
			byte[] content = getFtpFileContent(info);
			//calc block size
			Map<String,Object> params = new HashMap<String, Object>();
			info.setMaxSize(MAX_SIZE);
			info.setStatus(2);
			info.setBlockCount(content.length/MAX_SIZE);
			params.put(UPGRADE_CONTENT, content);
			params.put(UPGRADE_ID, req.getUpgradeId());
			params.put(UPGRADE_INFO, info);
			int fileType=Integer.parseInt(info.getFileHead().substring(0, 2));
			params.put(UPGRADE_FILETYPE, fileType);
			params.put(UPGRADE_MD5, info.getFileHead().substring(2));
			params.put(UPGRADE_RESEND_TIME,0);
			
			//get current file content.  max file size is 256 byte
			byte[] currentContent=getCurUpgradeBlock(content, info.getCurBlockNum(), MAX_SIZE);
			
			//rebuild request
			req.setCurrentContent(currentContent);
			req.setContentNum(info.getCurBlockNum());
			req.setFileName(UPGRADE_FILE_NAME);
			req.setFileType(fileType);
			req.setIfseq(1);
			bizRtu.setUpgradeParams(params);
			
			//send request
			sendRequest(req,client);
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
			return;
		}
	}
	
	public void onUpgradeReturn(MessageZj zjmsg) {
		
		try {
			int rtua = zjmsg.rtua;
			//get BizRtu
			BizRtu bizRtu = RtuManage.getInstance().getBizRtuInCache(rtua);
			if(bizRtu==null) //throw new runtimeException
				return;
			
			GgMessageProcessor.getInstance().onRequestComplete(bizRtu.getLogicAddress());

			Map<String, Object> params = bizRtu.getUpgradeParams();
			if(params == null || params.size()==0) return;

			ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
			ProtocolHandler handler = factory.getProtocolHandler(MessageGg.class);                	
			Object value = handler.process(zjmsg);
			if(value == null) return ;
			if("FAIL".equals(value)){
				log.error("Upgrade Fail! Last Message is "+zjmsg);
				if(params != null){
					updateUpgradeRecord(params,UpgradeInfo.FAIL);
					bizRtu.setUpgradeParams(null); //实际失败将升级信息移除
				}
				return;
			}
			//判断是否升级结束
			if(zjmsg.head.iseq==7) {
				//升级结束
				updateUpgradeRecord(params,UpgradeInfo.SUCCESS);
				bizRtu.setUpgradeParams(null); //实际成功将升级信息移除
				return;
			}
			
			
			UpgradeInfo info = (UpgradeInfo) params .get(UPGRADE_INFO);
			int currentBlock=info.getCurBlockNum();
			String[] values = ((String)value).split("#");
			int currentNum=Integer.parseInt(values[1]);
			
			if(currentNum!=currentBlock){
				log.error("Upgrade Return Info Can't Match Info in Catch");
				return;
			}
			byte[] content=(byte[]) params.get(UPGRADE_CONTENT);		
			
			byte[] nextContent = null;
			//判断是否是最后一帧文件返回
			int ifseq = zjmsg.head.iseq;
			if(currentBlock == info.getBlockCount()
					||( content.length % MAX_SIZE == 0
					&& currentBlock +1 ==  info.getBlockCount())){
				//获得校验码
				String md5=(String) params.get(UPGRADE_MD5);
				nextContent = HexDump.toArray(md5);
				ifseq = 7;
				currentBlock = currentBlock-1;
			}else{
				nextContent=getCurUpgradeBlock(content, currentBlock+1, MAX_SIZE);
				
				ifseq=ifseq==6?1:++ifseq;
			}
			info.setCurBlockNum(currentBlock+1);
			
			params.put(UPGRADE_INFO, info);
			params.put(UPGRADE_RESEND_TIME,0); //收到请求将重发次数设置为0
			bizRtu.setUpgradeParams(params);
			updateUpgradeRecord(params,UpgradeInfo.UPGRADEING);
			FaalGGKZM30Request request = new FaalGGKZM30Request();
			request.setContentNum(currentBlock+1);
			request.setFileName(UPGRADE_FILE_NAME);
			request.setCurrentContent(nextContent);
			request.setLogicAddress(bizRtu.getLogicAddress());
			request.setIfseq(ifseq);
			request.setFileType((Integer) params.get(UPGRADE_FILETYPE));
			sendRequest(request, null);
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
	}

	private void updateUpgradeRecord(Map<String, Object> params,int status) {
		if(params != null){
			long upgradeId=(Long) params.get(UPGRADE_ID);
			UpgradeInfo info=(UpgradeInfo) params.get(UPGRADE_INFO);
			if(info!=null){
				info.setStatus(status);
				service.addToDao(info, 4004);
				if(status != UpgradeInfo.FAIL){
					dbService.updateSoftUpgradeByRjsjId(upgradeId, UpgradeInfo.FAIL==status?"00":"02");
				}
			}
		}
	}
	
	/**
	 * 升级没有返回处理
	 * @param msg
	 */
	public void onUpgradeUnReturn(IMessage msg) {

		BizRtu rtu = RtuManage.getInstance().getBizRtuInCache(((MessageZj)msg).head.rtua);
		Map<String, Object> params = rtu.getUpgradeParams();
		if(params.containsKey(UPGRADE_RESEND_TIME)){
			//一定包含UPGRADE_RESEND_TIME
			int resendTime = (Integer) params.get(UPGRADE_RESEND_TIME);
			if(resendTime++>=3){
				//不再进行发送，设置为升级为失败
				updateUpgradeRecord(params,UpgradeInfo.FAIL);
				rtu.setUpgradeParams(null); //实际失败将升级信息移除
				log.error("logicAddress:"+rtu.getLogicAddress()+"Resend time is above 3,Upgrade Fail.");
			}else{
				GgMessageProcessor.getInstance().addMessage(rtu.getLogicAddress(),msg);
				GgMessageProcessor.getInstance().sendNextMessage(rtu.getLogicAddress());
				params.put(UPGRADE_RESEND_TIME, resendTime);
				log.warn("logicAddress:"+rtu.getLogicAddress()+",Upgrade unReturn. Send again.");
			}
		}else{
			log.error("rtu:"+rtu.getLogicAddress()+" No ResendTime");
		}
		
		
	}
	
	private void sendRequest(FaalGGKZM30Request req,IChannel client) {
		
		
		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();
        ProtocolHandler handler = factory.getProtocolHandler(MessageGg.class);
        IMessage[] messages = handler.createMessage(req);
        if (log.isDebugEnabled()) {
        	if(messages==null){
        		log.warn("Encode message error, no message create.");
        	}else{
        		log.debug("Encode to Message, protocol: " + req.getProtocol()
                        + ", message count: " + messages.length);
        	}
            
        }            
        if(messages == null){
        	throw new RuntimeException("Can't Create Messages...");
        }
        if(messages.length!=1){
        	throw new RuntimeException("Create Message Error.Message.length must=1");
        }
        BizRtu rtu=null;
        for (int i = 0; i < messages.length; i++) {
			MessageZj zjmsg=(MessageZj)messages[i];
			zjmsg.setPeerAddr(client!=null?client.getPeerAddr():"");
			int rtua=zjmsg.head.rtua;
			rtu = RtuManage.getInstance().getBizRtuInCache(rtua);  
			int ifseq=0;
            if("04".equals(rtu.getRtuProtocol())){
            	ifseq = dbService.getRtuCommandSeq(HexDump.toHex(rtua),"01");
            }else{
            	ifseq = dbService.getRtuCommandSeq(HexDump.toHex(rtua),rtu.getRtuProtocol());
            }
            zjmsg.head.fseq=(byte)ifseq;
            zjmsg.head.iseq=(byte) req.getIfseq();
            MessageGate gateMsg = new MessageGate();
    		gateMsg.setDownInnerMessage(messages[i]);
    		gateMsg.getHead().setAttribute(GateHead.ATT_DOWN_CHANNEL,req.getTxfs());
    		GgMessageProcessor.getInstance().addMessage(rtu.getLogicAddress(),messages[i]);
			GgMessageProcessor.getInstance().sendNextMessage(rtu.getLogicAddress());
        }
	}

	public byte[] getCurUpgradeBlock(byte[] content, int currentBlockNum,
			int i_maxSize) {
		int length =content.length;
		int size = length/i_maxSize;
		int destSize= i_maxSize;
		if(size == currentBlockNum){//最后一帧，可能不足i_maxSize
			destSize = i_maxSize-((currentBlockNum+1)*i_maxSize-length);
		}
		byte[] dest = new byte[destSize];
		System.arraycopy(content, currentBlockNum*i_maxSize, dest, 0, destSize);
		return dest;
	}
	
	
	
	
	public byte[] getFtpFileContent(UpgradeInfo info) {
		FtpFileReader ftpReader = new FtpFileReader();
		String tempfileDir = fileDir+UUID.randomUUID().toString()+File.separator;
		String fileName = info.getFileName();
		String ftpUrl = info.getFtpIp();
		Integer port=info.getFtpPort();
		String username=info.getFtpUserName();
		String password=info.getFtpPassword();
		String ftpDir = info.getFtpDir();
		boolean downSuccess=ftpReader.downFile(ftpUrl,port , username,
				password, ftpDir, fileName, tempfileDir);
		FileAssistant fr = new FileAssistant();
		byte[] bytes =null;
		if(downSuccess){
			bytes = fr.readFile(tempfileDir+File.separator+fileName);
			if(bytes==null){
				throw new RuntimeException("read file from ftp fail. meterId="+info.getLogicAddr()+",fileName="+fileName);
			}else{
				fr.deleteFolder(tempfileDir);
			}
		}else{
			info.setStatus(255);
			service.addToDao(info, 4004);
			throw new RuntimeException("download file from ftp fail. meterId="+info.getLogicAddr()+",fileName="+fileName);
		}
		return bytes;
	}

	public AsyncService getService() {
		return service;
	}

	public void setService(AsyncService service) {
		this.service = service;
	}
	
	public MasterDbService getDbService() {
		return dbService;
	}

	public void setDbService(MasterDbService dbService) {
		this.dbService = dbService;
	}

	
	
}
