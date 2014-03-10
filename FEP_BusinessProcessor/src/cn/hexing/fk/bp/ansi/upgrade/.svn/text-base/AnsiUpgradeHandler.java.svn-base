package cn.hexing.fk.bp.ansi.upgrade;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.AnsiRequest;
import cn.hexing.fas.model.AnsiRequest.ANSI_OP_TYPE;
import cn.hexing.fk.bp.ansi.ANSIContextManager;
import cn.hexing.fk.bp.ansi.AnsiEventProcessor;
import cn.hexing.fk.bp.ansi.LocalAnsiContext;
import cn.hexing.fk.bp.ansi.events.AnsiEvent;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

import com.hx.ansi.ansiElements.AnsiContext;
import com.hx.ansi.ansiElements.AnsiDataItem;
import com.hx.ansi.parse.AnsiDataSwitch;

/** 
 * @Description  ANSI 远程升级
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time：2013-7-16 上午09:43:11
 * @version 1.0 
 */

public class AnsiUpgradeHandler {
	private static final Logger log=Logger.getLogger(AnsiUpgradeHandler.class);
	private static final TraceLog Trace=TraceLog.getTracer("ANSI.Upgrade");
	
	private MasterDbService masterDbService ;
	
	private AsyncService service;
	/**当到达第六步的时候休息时间*/
	private int sleepTimeStay06Step=15;
	private ANSIContextManager contextManager = LocalAnsiContext.getInstance();
	
	private AnsiUpgradeHandler(){}
	
	public void init(){
		AnsiUpgradeAssisant.getInstance().setService(service);
		AnsiUpgradeAssisant.getInstance().setDbservice(masterDbService);
	}
	
	private static AnsiUpgradeHandler instance;
	public static AnsiUpgradeHandler getInstance(){
		if(instance==null){
			instance = new AnsiUpgradeHandler();
		}
		return instance;
	}
	
	
	/**
	 * 
	 * @param processor
	 * @param req
	 * @param context
	 */
	public void handleUpgrade(AnsiEventProcessor processor,AnsiRequest req,AnsiContext context){
		try {
			boolean isFinished = false;//是否升级结束
			boolean isSuccess = true;//命令是否成功
			
			AnsiRequest ar = new AnsiRequest();
			ar.setMeterId(context.meterId);
			ar.addAllAppendParmas(req.getAllParam());
			ar.setServiceType(req.getServiceType());//服务类别
			ar.setUpgradeType(req.getUpgradeType());//upgrade type 1:gprs moudle 2:meter
			
			if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_01)){
				isSuccess=upgradeStep_02(req, ar);
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_02)){
				isSuccess=upgradeStep_03(req,ar);				
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_03) || 
					 req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_RESSIUE)){
				isSuccess=blockTransfering(req, ar);
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_READMAP)){
				upgradeStep_04(req, ar);
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_04)){
				if(checkBitMap(req, ar)){//如果没有漏点，进入第五步
					upgradeStep_05(req,ar);
				}
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_05)){//检测验证是否成功
				if(Integer.parseInt(req.getDataItem()[0].resultData)==2){ //如果返回的是temporary-failure,休息一会再读
					try {
						Thread.sleep(sleepTimeStay06Step*1000);
					} catch (InterruptedException e) {
					}
					upgradeStep_05(req,ar); 
				}else if(Integer.parseInt(req.getDataItem()[0].resultData)==0){
					upgradeStep_06(req,ar);
				}else{//除了上面两种情况，都是失败
					isSuccess=false;
				}
				
			}
			else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_06)){
				//读到的数据与主站下发做比较
				//如果成功，执行第7步
				{
					upgradeStep_07(req,ar);
				}
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_07)){
				//如果结束 设置finished=true
				if(Integer.parseInt(req.getDataItem()[0].resultData)==0)
					isFinished = true;
				else{
					isSuccess=false;
				}
			}
			
			//查询待发送列表中，是否有要升级的请求，如果有，则当前请求判定为失败
			if(context.webReqList.size()>0){
					AnsiEvent evt = (AnsiEvent) context.webReqList.get(0);
					AnsiRequest request=(AnsiRequest) evt.getRequest();
					if(request.getOperator()!=null && request.getOperator().contains("UPGRADE")){
						isSuccess = false;
						log.error("meterid="+context.meterId+".New upgrade request is comming,current upgrade set fail");
					}
			}
			
			if(!isSuccess){
				AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//设置为失败
				AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
				Trace.trace("Upgrade Failed. MeterId="+req.getMeterId()+",Operator="+req.getOperator());
				return;
			}
			if(!isFinished){
				AnsiUpgradeAssisant.getInstance().onUpgradeSuccess(ar);
				processor.postWebRequest(ar, null);
			}else{
				//收尾阶段
				req.addAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM, (Integer)req.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT));
				AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.SUCCESS);//设置为成功
				AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"02");
			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
 			AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//设置为失败
			AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
		}
	
	}
	
	
	
	/**
	 * 读取服务端支持的最大字节数
	 * @param req
	 */
	public void upgradeEnable(AnsiRequest req) {
		req.setOperator(AnsiUpgradeAssisant.UPGRADE_01);
		req.setOpType(ANSI_OP_TYPE.OP_READ);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801000";
		datas[0].offset="000000";
		datas[0].count="0002";
		req.setDataItem(datas);
		req.setTable(2064);
		req.setServiceTag("3F");
		req.setFull(false);
	}
	
	/**
	 * 将升级信息下发到表计
	 * @param request
	 * @param ar
	 * @return
	 */
	private boolean upgradeStep_02(AnsiRequest request,AnsiRequest ar){
//		if(!request.getDataItem()[0].resultData.equals("00")) return false;
		int i_maxSize=Integer.parseInt(request.getDataItem()[0].resultData, 16);//获得每帧传送最大字节数
		log.info("========================="+request.getMeterId()+" i_maxSize:"+i_maxSize);
		upgradeInit(request, ar, i_maxSize);
		return true;
	}
	
	/**
	 * 升级信息初始化 
	 * @param request
	 * @param ar
	 * @param maxsize
	 */
	private void upgradeInit(AnsiRequest request,AnsiRequest ar,int maxsize){
		ar.removeAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM);
		ar.addAppendParam(AnsiUpgradeAssisant.MAX_SIZE, maxsize);
		ar.setOpType(ANSI_OP_TYPE.OP_WRITE);
		byte[] content = AnsiUpgradeAssisant.getInstance().getFtpFileContent(request);
		ar.addAppendParam(AnsiUpgradeAssisant.UPGRADE_CONTENT, content);
		ar.addAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT, content.length/maxsize); //块数
		
		String strFileHead=(String) request.getAppendParam(AnsiUpgradeAssisant.FILE_HEAD);
		String dataContentLen="00000000".substring((Integer.toHexString(content.length)).length())+Integer.toHexString(content.length);
		ar.addAppendParam(AnsiUpgradeAssisant.FILE_HEAD,dataContentLen+strFileHead);
		
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801001";
		datas[0].offset="000002";
		datas[0].count="002B";
		datas[0].data=(String) ar.getAppendParam(AnsiUpgradeAssisant.FILE_HEAD);
		datas[0].data=datas[0].data+HexDump.toHex(AnsiDataSwitch.calculateCS(HexDump.toArray(datas[0].data), 0, HexDump.toArray(datas[0].data).length));
		ar.setDataItem(datas);
		ar.setTable(2064);
		ar.setServiceTag("4F");
		ar.setFull(false);
		ar.setOperator(AnsiUpgradeAssisant.UPGRADE_02);
	}
	/**
	 * 下发升级文件
	 * @param request
	 * @param ar
	 * @return
	 */
	private boolean upgradeStep_03(AnsiRequest request,AnsiRequest ar){
		if(Integer.parseInt(request.getDataItem()[0].resultData)!=0) return false;
		ar.setOperator(AnsiUpgradeAssisant.UPGRADE_03);
		ar.setOpType(ANSI_OP_TYPE.OP_ACTION);
		ar.setTable(7);
		int i_maxSize=(Integer) ar.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE);
		byte[] content=(byte[]) ar.getAppendParam(AnsiUpgradeAssisant.UPGRADE_CONTENT);
		int currentBlockNum = 0;
		if(!ar.containsKey(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM)) currentBlockNum=0;
		else currentBlockNum = (Integer) ar.getAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM)+1;
		ar.addAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM, currentBlockNum);
		if(Trace.isEnabled()){
			Trace.trace("MeterId:"+ar.getMeterId()+",BlockCount:"+(Integer)ar.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT)+",CurrentBlockNum:"+currentBlockNum);
		}
		System.out.println("BlockCount:"+(Integer)ar.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT)+",CurrentBlockNum:"+currentBlockNum);
		
		if (currentBlockNum == (Integer) ar.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT)
				||( content.length % i_maxSize == 0
				&& currentBlockNum +1 == (Integer) ar.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT))) {
			// 当前发送是最后一帧
			ar.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		
		byte[] currentSendBlock = getCurUpgradeBlock(content,currentBlockNum,i_maxSize);
		String logicAddr = request.getMeterId();
		int tn = 0;
		Trace.trace("meterId:"+logicAddr+",tn:"+tn+",currentBlockNum:"+currentBlockNum+",blockContent:"+HexDump.toHex(currentSendBlock));
		blockTransferRequest(ar, currentBlockNum, currentSendBlock);
		return true;
	}
	
	/**
	 * 升级过程传输数据块
	 * 判断是否传输完毕，如果传输完毕直接进入第4步,否则继续传输
	 * @param req
	 * @param dr
	 * @return
	 */
	private boolean blockTransfering(AnsiRequest req, AnsiRequest dr) {
		if(Integer.parseInt(req.getDataItem()[0].resultData)!=0) return false;
		if(dr.containsKey(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED)){//发送完毕了,检查升级包是否完毕
			boolean isTransferFinished=(Boolean) dr.getAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED);
			if(isTransferFinished){
				upgradeStep_04(req,dr);					
			}else{
				reissueBlock(dr);
			}
		}else{
			upgradeStep_03(req,dr);
		}
		return true;
	}
	
	/**
	 * 升级第4步
	 * 检查升级包是否传输完毕，首先客户端读取image_transferred_blocks_status（image transfer对象的属性3）获取各个imageBlock是否已接收的bitmap，如发现有遗漏的block，则补发相应的block，直至所有的block都被表计接收；当(image_transferred_blocks/8)> ImageBlockSize时，客户端需采用选择性读（选择性参数=entry_descriptor）来实现分组读取所有image_transferred_blocks的bitmap，如ImageBlockSize=100，image_transferred_blocks=1800，则客户端需要分三次来读取bitmap(第一次的entry_descriptor中From_entry=0，to_entry=99；第二次entry_descriptor中From_entry=100，to_entry=199；第三次entry_descriptor中From_entry=200，to_entry=00。Entry_descriptor的使用详见《Ansi/COSEM应用层标准》)；
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_04(AnsiRequest req, AnsiRequest dr) {
		dr.setOpType(ANSI_OP_TYPE.OP_READ);
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_04);
		dr.setTable(2064);
		dr.setServiceTag("3F");
		dr.setFull(false);
		dr.removeAppendParam(AnsiUpgradeAssisant.CURRENT_RESSIUE_NUM);
		int i_maxSize=(Integer) dr.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE);
		int blockCount=(Integer) dr.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801005";
		datas[0].offset="00005C";
		int count=0;
		if(blockCount%8==0){
			count=blockCount/8;
		}else{
			count=blockCount/8+1;
		}
		count=count+2;
		datas[0].count="0000".substring(Integer.toHexString(count).length())+Integer.toHexString(count);
		dr.setDataItem(datas);
//		if(blockCount/8>i_maxSize){//按照新的标准:如果(image_transferred_blocks/8)> ImageBlockSize,选择性读，否则，直接读
//		}else{//直接读，读完之后转移到第4步
//			dr.setOperator(AnsiUpgradeAssisant.UPGRADE_04);
//		}
	}
	/**
	 * 补发漏点块
	 * @param req
	 * @param dr
	 */
	@SuppressWarnings("unchecked")
	private void reissueBlock(AnsiRequest dr) {
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_RESSIUE);
		dr.setOpType(ANSI_OP_TYPE.OP_ACTION);
		int i_maxSize=(Integer) dr.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE);
		byte[] content=(byte[]) dr.getAppendParam(AnsiUpgradeAssisant.UPGRADE_CONTENT);
		
		//获得补发列表
		List<Integer> reissueList=(List<Integer>) dr.getAppendParam(AnsiUpgradeAssisant.REISSUE_LIST);
		
		//从列表头开始发送
		Integer reissueBlockNum = reissueList.remove(0);
		dr.addAppendParam(AnsiUpgradeAssisant.CURRENT_RESSIUE_NUM, reissueBlockNum);
		dr.addAppendParam(AnsiUpgradeAssisant.REISSUE_LIST, reissueList);
		//如果是最后一个
		byte[] reissueBlock=getCurUpgradeBlock(content, reissueBlockNum, i_maxSize);
		if(reissueList.size()==0){
			dr.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		blockTransferRequest(dr, reissueBlockNum, reissueBlock);
	}
	
	@SuppressWarnings("unchecked")
	private boolean checkBitMap(AnsiRequest req, AnsiRequest dr) {

		
		getRessiueBlocks(req,dr);
		
		List<Integer>ressiueBlocks = (List<Integer>) dr.getAppendParam(AnsiUpgradeAssisant.REISSUE_LIST);
		
		if(ressiueBlocks.size()!=0){//进入补发
			dr.addAppendParam(AnsiUpgradeAssisant.REISSUE_LIST, ressiueBlocks);
			dr.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, false);
			reissueBlock(dr);
			return false;
		}
		return true;
	}
	/**
	 * 第五步:验证升级包文件，客户端通过action操作image transfer对象的方法3来触发表计对升级包文件进行验证，表计回复的结果可能为successs、temporary-failure或other-reason，如结果为other-reason则终止升级过程；如结果为success则表明验证成功，进入下一步；如结果为temporary-failure则延时片刻后获取image_transfer_status(读image transfer对象的属性6)，状态为image verification successful时表明验证成功，状态为image verification failed时表明验证失败，状态为image verification initiated时表明表计正在验证，需延时片刻再读取image_transfer_status；验证过程中表计需要计算升级包的散列值，并与image_identifier中散列值校验码或解密散列校验码密文进行比对，当不一致时，在步骤  image_verify时表计回复 other-reason表示升级包验证失败
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_05(AnsiRequest req, AnsiRequest dr) {
		dr.setOpType(ANSI_OP_TYPE.OP_READ);
		dr.setTable(2064);
		dr.setServiceTag("3F");
		dr.setFull(false);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801002";
		datas[0].offset="00002D";
		datas[0].count="0001";
		dr.setDataItem(datas);
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_05);
	}
	
	/**
	 * 验证升级包
	 * @param dr
	 */
	private void upgradeStep_06(AnsiRequest req,AnsiRequest dr){
		dr.setOpType(ANSI_OP_TYPE.OP_READ);
		dr.setTable(2064);
		dr.setServiceTag("3F");
		dr.setFull(false);
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801002";
		datas[0].offset="00002E";
		datas[0].count="0028";
		dr.setDataItem(datas);
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_06);
	}
	
	/**
	 * step7 写入执行时间
	 *  新版本软件生效，通过action操作image transfer对象的方法4激活之前步骤中传输的升级包文件。
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_07(AnsiRequest req, AnsiRequest dr) {
		//写执行时间EffectiveTime=2013-06-26 14:58:00
		String strEffcDate=(String)dr.getAppendParam(AnsiUpgradeAssisant.EFFECTIVE_TIME);
		strEffcDate=AnsiDataSwitch.toHexString(strEffcDate);
		dr.setOpType(ANSI_OP_TYPE.OP_WRITE);
		dr.setTable(2064);
		dr.setServiceTag("4F");
		dr.setFull(false);
		AnsiDataItem datas[]=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].offset="000056";
		datas[0].count="0006";
		datas[0].data=strEffcDate+HexDump.toHex(AnsiDataSwitch.calculateCS(HexDump.toArray(strEffcDate), 0, HexDump.toArray(strEffcDate).length));
		dr.setDataItem(datas);
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_07);
	}
	
	
	/**
	 * 主站请求远程升级业务处理器
	 * @param request
	 * @return
	 */
	public boolean upgradeProcessor(AnsiRequest request){
		String meterAddr=request.getMeterId();
		int tn=0;//GPRS表测量点默认为0
		//根据请求的升级ID 查询数据库获取升级信息
		try {
		List<UpgradeInfo> infos = masterDbService.getUpgradeInfo(new Long((String) request.getAppendParam(AnsiUpgradeAssisant.UPGRADE_ID)));
		if(1!=infos.size()){
			log.error("Upgrade request Fail:Info.size="+infos.size()+".MeterId="+meterAddr+".Tn="+tn);
			Trace.trace("Upgrade request Fail:Info.size="+infos.size()+".MeterId="+meterAddr+".Tn="+tn);
		}
		UpgradeInfo info=infos.get(0);
		request.addAppendParam(AnsiUpgradeAssisant.UPGRADE_ID, info.getSoftUpgradeID());
		
		AnsiUpgradeAssisant.getInstance().addRequestAppendParams(info, request);
		//02 表计通信模块升级 FD表计升级
		String fileType=((String) request.getAppendParam(AnsiUpgradeAssisant.FILE_HEAD)).substring(0, 2);
		if(fileType.equals("02")){//gprs moudle upgrade
			request.setUpgradeType(1);
		}
		else if(fileType.equals("FD")) {//meter upgrade 
			request.setUpgradeType(2);
		}
		
		boolean isRessiueSuccess=true;
		int status=info.getStatus();//获取升级状态
		switch(status){
		case UpgradeInfo.WAIT_UPGRADE:
			upgradeEnable(request);
			break;
		case UpgradeInfo.WAIT_UPGARDEINIT:
			upgradeInit(request, request, (Integer)request.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE));
			break;
		case UpgradeInfo.UPGRADE_PAUSE:
			upgradeReissueInit(request);
			break;
		case UpgradeInfo.RESSIUE_PAUSE:
			request.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, false);
			reissueBlock(request);
			break;
		case UpgradeInfo.CHECK_MAP_FAIL:
			request.setOperator(AnsiUpgradeAssisant.UPGRADE_RESSIUE);
			upgradeStep_04(request, request);
			break;
//		case UpgradeInfo.READ_STATUS_FAIL:
//			readTransferStatus(request);
//			break;
		case UpgradeInfo.VERFIY_FILE_FAIL:
			upgradeStep_05(request, request);
			break;
		case UpgradeInfo.CHECK_FILE_FAIL:
			upgradeStep_06(request, request);
			break;
		case UpgradeInfo.SET_EFFECTTIME_FAIL:
			upgradeStep_07(request, request);
			break;
		default:
			isRessiueSuccess=false;
			log.error("Upgrade File.Status:"+status+" .MeterId="+meterAddr+" .Tn="+tn);
			break;
		}
		if(isRessiueSuccess){
			AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(request, "01");
			return true;
		}
		return false;
	} catch (Exception e) {
		//设置升级失败
		log.error("update stoped.",e);
			AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(request, UpgradeInfo.FAIL);//设置为失败
		AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(request,"00");
		return false;
	}

}
	
	/**
	 * 获得当前块数据内容
	 * @param content
	 * @param currentBlockNum
	 * @param i_maxSize
	 * @return
	 */
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
	
	/**
	 * 下发升级数据块
	 * @param req
	 * @param dr
	 * @param blockNum
	 * @param block
	 */
	public void blockTransferRequest( AnsiRequest ar,
			Integer blockNum, byte[] block) {
		AnsiDataItem[] datas=new AnsiDataItem[1];
		datas[0]=new AnsiDataItem();
		datas[0].dataCode="11801006";
		datas[0].data=HexDump.toHex(blockNum)+HexDump.toHex(block);
		datas[0].count="0000".substring((Integer.toHexString((datas[0].data).length()/2)).length())+Integer.toHexString((datas[0].data).length()/2+3);
		datas[0].data=datas[0].data+HexDump.toHex(AnsiDataSwitch.calculateCS(HexDump.toArray(datas[0].data), 0, HexDump.toArray(datas[0].data).length));
		ar.setDataItem(datas);
		ar.setTable(7);
		ar.setServiceTag("40");
		ar.setFull(true);
		ar.setOperator(AnsiUpgradeAssisant.UPGRADE_03);
	}
	
	@SuppressWarnings("unchecked")
	private int getRessiueBlocks(AnsiRequest req,AnsiRequest dr) {
		int offset = 0;
		List<Integer> ressiueBlocks = null; 
		if(req.containsKey(AnsiUpgradeAssisant.REISSUE_LIST)){
			ressiueBlocks=(List<Integer>) req.getAppendParam(AnsiUpgradeAssisant.REISSUE_LIST);
		}else{
			ressiueBlocks = new ArrayList<Integer>();
		}
		if(AnsiUpgradeAssisant.UPGRADE_03.equals(req.getOperator()) || AnsiUpgradeAssisant.UPGRADE_RESSIUE.equals(req.getOperator())){
			return offset;
		}
		byte[] bitValue=HexDump.toArray(req.getDataItem()[0].resultData);
		//获得有效位数
		int blockCount = (bitValue.length-1)*8-bitValue[0];

		getRessiueBlocks(bitValue, blockCount, ressiueBlocks, offset);
		if(req.containsKey(AnsiUpgradeAssisant.CURRENT_INDEX)){
			offset=(Integer) req.getAppendParam(AnsiUpgradeAssisant.CURRENT_INDEX);
		}else{
			offset=0;
		}
		dr.addAppendParam(AnsiUpgradeAssisant.REISSUE_LIST, ressiueBlocks);
		return offset;
	}
	/**
	 * 根据位图获得需要补发的块
	 * @param bitMap
	 * @param blockCount
	 * @return
	 */
	private void getRessiueBlocks(byte[] bitMap, int blockCount,List<Integer> ressiueBlocks,int offset){
		//低位在前，高位在后
		byte[] bitBytes = new byte[]{0x01,0x02,0x04,0x08,0x10,0x20,0x40,(byte) 0x80};
		for(int i = offset;i<bitMap.length;i++){
			byte bit=bitMap[i];
			for(int b=0;b<bitBytes.length;b++){
				int currentBlock = 0;
				if(offset==0){
					currentBlock=i*8+7-b;
				}else{
					currentBlock=(i-1)*8+7-b;
				}
				if(currentBlock+1 >blockCount) 
					continue;
				if((bit&bitBytes[b])==0){
					ressiueBlocks.add(currentBlock);
				}
			}
		}
	}
	
	/**
	 * 软件升级断点续传初始化
	 * @param req
	 */
	public void upgradeReissueInit(AnsiRequest req) {
		//软件升级补发
		req.setOperator(AnsiUpgradeAssisant.UPGRADE_03);
		byte[] content = AnsiUpgradeAssisant.getInstance().getFtpFileContent(req);
		Integer currentBlockNum = (Integer)req.getAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM);
		Integer i_maxSize = (Integer)req.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE);
		byte[] currentBlock=AnsiUpgradeHandler.getInstance().getCurUpgradeBlock(content, currentBlockNum, i_maxSize);
		int blockCount = content.length/i_maxSize;
		req.addAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT, blockCount);
		if (currentBlockNum == blockCount
				|| content.length % i_maxSize == 0
				&& currentBlockNum - 1 == blockCount) {
			req.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		AnsiUpgradeHandler.getInstance().blockTransferRequest(req, currentBlockNum, currentBlock);
		req.addAppendParam(AnsiUpgradeAssisant.UPGRADE_CONTENT, content);
		req.setOpType(ANSI_OP_TYPE.OP_ACTION);
	}
	
	
	public final void setMasterDbService(MasterDbService masterDbService) {
		this.masterDbService = masterDbService;
	}

	public final void setService(AsyncService service) {
		this.service = service;
	}
	public final void setSleepTimeStay06Step(int sleepTimeStay06Step){
		this.sleepTimeStay06Step=sleepTimeStay06Step;
	} 
	
	
}
