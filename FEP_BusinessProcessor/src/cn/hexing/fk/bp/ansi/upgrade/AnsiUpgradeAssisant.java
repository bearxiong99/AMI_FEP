package cn.hexing.fk.bp.ansi.upgrade;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.FileAssistant;
import cn.hexing.fk.utils.FtpFileReader;

/** 
 * @Description  xxxxx
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-7-16 上午09:43:30
 * @version 1.0 
 */

public class AnsiUpgradeAssisant {
	
	private static final TraceLog tracer = TraceLog.getTracer("ANSI.UPGRADE");
	private static final Logger log = Logger.getLogger(AnsiUpgradeAssisant.class);
	private static AnsiUpgradeAssisant instance;
	private String fileDir="."+File.separator+"upgrade"+File.separator;
	
	private AsyncService service;
	private MasterDbService dbservice;
	
	/**Request附加信息域的名称*/
	public final static String CURRENT_BLOCK_NUM = "CurrentBlockNum";
	public final static String FILE_HEAD = "FileHead";
	public final static String UPGRADE_CONTENT = "UpgradeContent";
	public final static String MAX_SIZE = "MaxSize";
	public final static String EFFECTIVE_TIME = "EffectiveTime";
	public final static String CURRENT_INDEX = "CurrentIndex";
	public final static String REISSUE_LIST = "ReissueList";
	public final static String BLOCK_COUNT = "BlockCount";
	public final static String IS_TRANSFER_FINISHED = "IsTransferFinished";
	public final static String FTP_USERNAME="FtpUsername";
	public final static String FTP_PASSWORD="FtpPassword";
	public final static String FTP_PORT="FtpPort";
	public final static String FTP_IP="FtpIp";
	public final static String FTP_DIR="FtpDir";
	public final static String FILENAME="FileName";
	public final static String UPGRADE_STATUS="UpgradeStatus";
	public final static String CURRENT_RESSIUE_NUM="CurrentRessiueNum";
	public final static String UPGRADE_ID="UpgradeId";
	
	/**每一个步骤的名称*/
	public final static String UPGRADE_07 = "UPGRADE-07";
	public final static String UPGRADE_06 = "UPGRADE-06";
	public final static String UPGRADE_READ_STATUS = "UPGRADE-READ-STATUS";
	public final static String UPGRADE_05 = "UPGRADE-05";
	public final static String UPGRADE_04 = "UPGRADE-04";
	public final static String UPGRADE_RESSIUE = "UPGRADE-RESSIUE";
	public final static String UPGRADE_03 = "UPGRADE-03";
	public final static String UPGRADE_READMAP="UPGRADE-READMAP";
	public final static String UPGRADE_TEMP = "UPGRADE-TEMP";
	public final static String UPGRADE_02 = "UPGRADE-02";
	public final static String UPGRADE_01 = "UPGRADE-01";
	public final static String UPGRADE_01_01="UPGRADE-01-01";
	public final static String UPGRADE_00 = "UPGRADE-00";
	

	/**每一步失败，对应的状态*/
	private Map<String,Integer> stepFailMappingStatus = new HashMap<String, Integer>();
	/**每一个状态，应该对应的步骤*/
	private Map<Integer,String> statusMappingStep = new HashMap<Integer, String>();
	
	
	private AnsiUpgradeAssisant(){
		
		stepFailMappingStatus.put(UPGRADE_00, UpgradeInfo.WAIT_UPGRADE);
		stepFailMappingStatus.put(UPGRADE_01,UpgradeInfo.WAIT_UPGRADE);
		stepFailMappingStatus.put(UPGRADE_02,UpgradeInfo.WAIT_UPGARDEINIT);
		stepFailMappingStatus.put(UPGRADE_TEMP,UpgradeInfo.WAIT_UPGRADE);
		stepFailMappingStatus.put(UPGRADE_03,UpgradeInfo.UPGRADE_PAUSE);
		stepFailMappingStatus.put(UPGRADE_RESSIUE, UpgradeInfo.RESSIUE_PAUSE);
		stepFailMappingStatus.put(UPGRADE_04, UpgradeInfo.CHECK_MAP_FAIL);
		stepFailMappingStatus.put(UPGRADE_05, UpgradeInfo.VERFIY_FILE_FAIL);
		stepFailMappingStatus.put(UPGRADE_06, UpgradeInfo.CHECK_FILE_FAIL);
		stepFailMappingStatus.put(UPGRADE_07, UpgradeInfo.SET_EFFECTTIME_FAIL);
		stepFailMappingStatus.put(UPGRADE_READ_STATUS, UpgradeInfo.READ_STATUS_FAIL);
		stepFailMappingStatus.put(UPGRADE_READMAP, UpgradeInfo.CHECK_MAP_FAIL);
		
		statusMappingStep.put(UpgradeInfo.WAIT_UPGRADE, UPGRADE_00);
		statusMappingStep.put(UpgradeInfo.UPGRADE_PAUSE, UPGRADE_03);
		statusMappingStep.put(UpgradeInfo.RESSIUE_PAUSE, UPGRADE_RESSIUE);
		statusMappingStep.put(UpgradeInfo.CHECK_MAP_FAIL, UPGRADE_04);
		statusMappingStep.put(UpgradeInfo.VERFIY_FILE_FAIL, UPGRADE_05);
		statusMappingStep.put(UpgradeInfo.CHECK_FILE_FAIL, UPGRADE_06);
		statusMappingStep.put(UpgradeInfo.SET_EFFECTTIME_FAIL, UPGRADE_07);
		statusMappingStep.put(UpgradeInfo.READ_STATUS_FAIL, UPGRADE_READ_STATUS);
	}
	
	public static AnsiUpgradeAssisant getInstance(){
		if(instance==null)
			instance= new AnsiUpgradeAssisant();
		return instance;
	}
	
	/**
	 * 更新升级信息
	 * @param req
	 * @param status
	 */
	public void updateUpgradeInfo(AnsiRequest req,int status){
		UpgradeInfo info = new UpgradeInfo();
		info.setProtocol("03");
		info.setStatus(status);//设置升级状态
		info.setLogicAddr(req.getMeterId());
		info.setTn(0);
		constructUpgradeInfo(req, info);
		service.addToDao(info, 4004);
	}
	/**
	 * 保存每一步的状态
	 * @param req
	 */
	public void onUpgradeSuccess(AnsiRequest req){
		if(req.containsKey(CURRENT_RESSIUE_NUM)){
			updateUpgradeInfo(req, UpgradeInfo.RESSIUEING);
		}else{
			updateUpgradeInfo(req, UpgradeInfo.UPGRADEING);			
		}
	}
	
	/**
	 * 从Ftp上获得文件内容
	 * 下载到本地临时文件目录
	 * @param req
	 * @return
	 */
	public byte[] getFtpFileContent(AnsiRequest req) {
		FtpFileReader ftpReader = new FtpFileReader();
		String tempfileDir = fileDir+UUID.randomUUID().toString()+File.separator;
		//从request里获得IP,PORT,USERNAME,PASSWORD,FTP_DIR,FILENAME,LOCALDIR
		String fileName = (String) req.getAppendParam(FILENAME);
		String ftpUrl = (String) req.getAppendParam(FTP_IP);
		Integer port=(Integer) req.getAppendParam(FTP_PORT);
		String username=(String) req.getAppendParam(FTP_USERNAME);
		String password=(String) req.getAppendParam(FTP_PASSWORD);
		String ftpDir = (String) req.getAppendParam(FTP_DIR);
		boolean downSuccess=ftpReader.downFile(ftpUrl,port , username,
				password, ftpDir, fileName, tempfileDir);
		FileAssistant fr = new FileAssistant();
		byte[] bytes =null;
		if(downSuccess){
			bytes = fr.readFile(tempfileDir+File.separator+fileName);
			if(bytes==null){
				throw new RuntimeException("read file from ftp fail. meterId="+req.getMeterId()+",fileName="+fileName);
			}else{
				fr.deleteFolder(tempfileDir);
			}
		}else{
			tracer.trace("download file from ftp fail. meterId="+req.getMeterId()+",fileName="+fileName);
			updateUpgradeInfo(req, UpgradeInfo.FAIL);
			throw new RuntimeException("download file from ftp fail. meterId="+req.getMeterId()+",fileName="+fileName);
		}
		return bytes;
	}
	/**
	 * 从数据库里读出来升级信息，添加到req里
	 * @param info
	 * @param req
	 */
	public void addRequestAppendParams(UpgradeInfo info, AnsiRequest req) {
		req.addAppendParam(BLOCK_COUNT, info.getBlockCount());
		if(info.getStatus()!=1)
			req.addAppendParam(CURRENT_BLOCK_NUM, info.getCurBlockNum());
		req.addAppendParam(FTP_PORT, info.getFtpPort());
		req.addAppendParam(FILENAME, info.getFileName());
		req.addAppendParam(FTP_DIR, info.getFtpDir());
		req.addAppendParam(FTP_IP, info.getFtpIp());
		req.addAppendParam(FTP_PASSWORD, info.getFtpPassword());
		req.addAppendParam(FTP_USERNAME, info.getFtpUserName());
		byte[] content = getFtpFileContent(req);
		req.addAppendParam(UPGRADE_CONTENT, content);
		Date effectDate=info.getEffectDate();
		if(effectDate!=null){
			SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
			String strDate=sdf.format(effectDate);
			req.addAppendParam(EFFECTIVE_TIME, strDate);
		}
		if(null!=info.getReissueBlock()&&!"".equals(info.getReissueBlock())){
			String[] blocks=info.getReissueBlock().split(",");
			List<Integer> reissueBlocks = new ArrayList<Integer>();
			for(String block : blocks){
				reissueBlocks.add(Integer.parseInt(block));
			}
			req.addAppendParam(REISSUE_LIST, reissueBlocks);
		}
		if(info.getStatus()!=1 && info.getMaxSize()<=0) {
			tracer.trace("Upgrade Fail.Each frame max size must above 0");
			log.error("Upgrade Fail.Each frame max size must above 0");
		}
		req.addAppendParam(MAX_SIZE, info.getMaxSize());
		req.addAppendParam(FILE_HEAD, info.getFileHead().toUpperCase());
		
	}
	
	/**
	 * 从request里获得升级信息，组成升级信息
	 * @param req
	 * @param info
	 */
	@SuppressWarnings("unchecked")
	public void constructUpgradeInfo(AnsiRequest req, UpgradeInfo info) {
		if(req.containsKey(FILENAME)){ 
			info.setFileName((String)req.getAppendParam(FILENAME));
		}
		if(req.containsKey(REISSUE_LIST)){
			List<Integer> list=(List<Integer>) req.getAppendParam(REISSUE_LIST);
			StringBuffer sb = new StringBuffer();
			for(Integer i:list){
				sb.append(i).append(",");
			}
			if(req.containsKey(CURRENT_RESSIUE_NUM)){
				Integer currentRessiueNum=(Integer) req.getAppendParam(CURRENT_RESSIUE_NUM);
				sb.append(currentRessiueNum);
			}
			info.setReissueBlock(sb.toString());
		}
		if(req.containsKey(BLOCK_COUNT)){
			info.setBlockCount((Integer) req.getAppendParam(BLOCK_COUNT));
		}
		if(req.containsKey(CURRENT_BLOCK_NUM)){
			info.setCurBlockNum((Integer) req.getAppendParam(CURRENT_BLOCK_NUM));
		}
		if(req.containsKey(MAX_SIZE)){
			info.setMaxSize((Integer) req.getAppendParam(MAX_SIZE));
		}
		if(req.containsKey(UPGRADE_ID)){
			info.setSoftUpgradeID((Long)req.getAppendParam(UPGRADE_ID));
		}
	}
	
	/**
	 * 更新的过程中,电表没有返回
	 * @param AnsiRequest
	 */
	public void onUpgradeFailed(AnsiRequest req) {
		String operator=req.getOperator();
		int status=stepFailMappingStatus.get(operator);
		updateUpgradeInfo(req, status);
		updateUpgradeStatus(req,"03");//设置可补发状态
	}

	/**
	 * 更新升级状态
	 * 02表示成功，00表示失败,03表示可以补发
	 * 2表示 表计升级
	 * @param req
	 * @param status
	 */
	public void updateUpgradeStatus(AnsiRequest req, String status) {
		dbservice.updateSoftUpgradeByRjsjId((Long)req.getAppendParam(UPGRADE_ID), status);
	}
	
	public void setDbservice(MasterDbService dbservice) {
		this.dbservice = dbservice;
	}
	
	public final void setService(AsyncService service) {
		this.service = service;
	}
}
