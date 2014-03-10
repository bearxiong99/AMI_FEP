package cn.hexing.fk.bp.dlms.upgrade;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.InvalidCipherTextException;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.db.bizprocess.MasterDbService;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fas.model.dlms.DlmsRequest.DLMS_OP_TYPE;
import cn.hexing.fas.model.dlms.RelayParam;
import cn.hexing.fk.bp.dlms.DlmsEventProcessor;
import cn.hexing.fk.bp.dlms.IContextManager;
import cn.hexing.fk.bp.dlms.LocalDlmsContextManager;
import cn.hexing.fk.bp.dlms.events.DlmsEvent;
import cn.hexing.fk.model.UpgradeInfo;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1BitString;
import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.aa.DlmsContext;
import com.hx.dlms.applayer.SelectiveAccessDescriptor;
import com.hx.dlms.applayer.get.GetDataResult;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;
import com.hx.dlms.cipher.AESGcm128;
import com.hx.dlms.message.DlmsMessage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-2-3 上午11:49:56
 *
 * @info DLMS软件升级处理器
 */
public class DlmsUpgradeHandler {

	private static final TraceLog tracer = TraceLog.getTracer("DLMS.UPGRADE");
	private static final Logger log = Logger.getLogger(DlmsUpgradeHandler.class);

	private static DlmsUpgradeHandler instance;
	
	private MasterDbService dbService ;
	
	private AsyncService service;
	/**当到达第六步的时候休息时间*/
	private int sleepTimeStay06Step=15;
	
	private IContextManager contextManager = LocalDlmsContextManager.getInstance();
	
	public void setSleepTimeStay06Step(int sleepTimeStay06Step) {
		this.sleepTimeStay06Step = sleepTimeStay06Step;
	}

	private DlmsUpgradeHandler(){}
	
	public void init(){
		DlmsUpgradeAssisant.getInstance().setService(service);
		DlmsUpgradeAssisant.getInstance().setDbservice(dbService);
	}
	
	public static DlmsUpgradeHandler getInstance(){
		if(instance==null){
			instance = new DlmsUpgradeHandler();
		}
		return instance;
	}
	
	private void setRequestParam(DlmsRequest req,ObisDescription obisDesc){
		setRequestParam(req, new ObisDescription[]{obisDesc});
	}
	
	private void setRequestParam(DlmsRequest req,ObisDescription[] obisDesc){
		DlmsObisItem[] params = new DlmsObisItem[obisDesc.length];
		for(int i=0;i<params.length;i++){
			params[i] = new DlmsObisItem();
			params[i].classId = obisDesc[i].classId;
			params[i].attributeId = obisDesc[i].attrId;
			params[i].obisString  = obisDesc[i].obis;
		}
		req.setParams(params);
	}
	public void handleUpgrade(DlmsEventProcessor processor,DlmsRequest req,DlmsContext context){
		
		try {
			boolean isFinished = false;//是否升级结束
			boolean isSuccess = true;//命令是否成功
			
			DlmsRequest dr = new DlmsRequest();
			dr.setRelayParam(req.getRelayParam());
			dr.setMeterId(context.meterId);
			dr.addAllAppendParmas(req.getAllParam());
			dr.setDestAddr(req.getDestAddr());
			
			if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_00)){
				isSuccess=updateStep_01_01(req, dr);
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_01_01)){
				isSuccess=upgradeStep_01(req, dr);
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_01)){
				isSuccess=upgradeStep_02(req, dr);
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_02)){
				isSuccess=upgradeStep_03(req,dr);				
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_03) || 
					 req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_RESSIUE)){
				isSuccess=blockTransfering(req, dr);
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_READMAP)){
				upgradeStep_04(req, dr);
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_04)){
				if(checkBitMap(req, dr)){//如果没有漏点，进入第五步
					upgradeStep_05(req,dr);
				}
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_05)){//检测验证是否成功
				if(req.getParams()[0].resultCode==2){ //如果返回的是temporary-failure,休息一会再读
					try {
						Thread.sleep(sleepTimeStay06Step*1000);
					} catch (InterruptedException e) {
					}
				}
				readTransferStatus(dr); //这里无论返回什么，都去读升级状态
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_READ_STATUS)){
				int verfiyFlg = req.getParams()[0].resultData.getEnum();
				if(verfiyFlg==3){
					upgradeStep_06(req,dr);
				}else if(verfiyFlg==2){
					try {
						Thread.sleep(sleepTimeStay06Step*1000);
					} catch (InterruptedException e) {
					}
					readTransferStatus(dr);
				}else{//除了上面两种情况，都是失败
					isSuccess=false;
				}
			}
			else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_06)){
				//读到的数据与主站下发做比较
				//如果成功，执行第7步
				{
					upgradeStep_07(req,dr);
				}
			}else if(req.getOperator().equals(DlmsUpgradeAssisant.UPGRADE_07)){
				//如果结束 设置finished=true
				if(req.getParams()[0].resultCode==0)
					isFinished = true;
				else{
					isSuccess=false;
				}
			}
			
			//查询待发送列表中，是否有要升级的请求，如果有，则当前请求判定为失败
			if(context.webReqList.size()>0){
				DlmsEvent evt = (DlmsEvent) context.webReqList.get(0);
				DlmsRequest request=(DlmsRequest) evt.getRequest();
				if(request.getOperator()!=null && request.getOperator().contains("UPGRADE")){
					isSuccess = false;
					log.error("meterid="+context.meterId+".New upgrade request is comming,current upgrade set fail");
				}
			}
			
			if(!isSuccess){
				DlmsUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//设置为失败
				DlmsUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
				if(tracer.isEnabled())
					tracer.trace("Upgrade Failed. MeterId="+req.getMeterId()+",Operator="+req.getOperator());
				return;
			}
			if(!isFinished){
				DlmsUpgradeAssisant.getInstance().onUpgradeSuccess(dr);
				processor.postWebRequest(dr, null);
			}else{
				//收尾阶段
				req.addAppendParam(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM, (Integer)req.getAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT));
				DlmsUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.SUCCESS);//设置为成功
				DlmsUpgradeAssisant.getInstance().updateUpgradeStatus(req,"02");
			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
 			DlmsUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//设置为失败
			DlmsUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
		}
	}

	/**
	 * 升级过程传输数据块
	 * 判断是否传输完毕，如果传输完毕直接进入第4步,否则继续传输
	 * @param req
	 * @param dr
	 * @return
	 */
	private boolean blockTransfering(DlmsRequest req, DlmsRequest dr) {
		if(req.getParams()[0].resultCode!=0) return false;
		if(dr.containsKey(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED)){//发送完毕了,检查升级包是否完毕
			boolean isTransferFinished=(Boolean) dr.getAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED);
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

	@SuppressWarnings("unchecked")
	private boolean checkBitMap(DlmsRequest req, DlmsRequest dr) {

		
		getRessiueBlocks(req,dr);
		
		List<Integer>ressiueBlocks = (List<Integer>) dr.getAppendParam(DlmsUpgradeAssisant.REISSUE_LIST);
		
		if(ressiueBlocks.size()!=0){//进入补发
			dr.addAppendParam(DlmsUpgradeAssisant.REISSUE_LIST, ressiueBlocks);
			dr.addAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED, false);
			reissueBlock(dr);
			return false;
		}
		return true;
	}
	
	@SuppressWarnings("unchecked")
	private int getRessiueBlocks(DlmsRequest req,DlmsRequest dr) {
		int offset = 0;
		List<Integer> ressiueBlocks = null; 
		if(req.containsKey(DlmsUpgradeAssisant.REISSUE_LIST)){
			ressiueBlocks=(List<Integer>) req.getAppendParam(DlmsUpgradeAssisant.REISSUE_LIST);
		}else{
			ressiueBlocks = new ArrayList<Integer>();
		}
		if(DlmsUpgradeAssisant.UPGRADE_03.equals(req.getOperator()) || DlmsUpgradeAssisant.UPGRADE_RESSIUE.equals(req.getOperator())){
			return offset;
		}
		ASN1BitString bitString = req.getParams()[0].resultData.getBitString();
		byte[] bitValue=bitString.getValue();
		//获得有效位数
		int blockCount = (bitValue.length-1)*8-bitValue[0];
		
		getRessiueBlocks(bitValue, blockCount, ressiueBlocks, offset+1);
		if(req.containsKey(DlmsUpgradeAssisant.CURRENT_INDEX)){
			offset=(Integer) req.getAppendParam(DlmsUpgradeAssisant.CURRENT_INDEX);
		}else{
			offset=0;
		}
		dr.addAppendParam(DlmsUpgradeAssisant.REISSUE_LIST, ressiueBlocks);
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
	 * 读升级状态
	 * @param dr
	 */
	private void readTransferStatus(DlmsRequest dr){
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 6));
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_READ_STATUS);
	}
	
//	private boolean upgradeStep_temp(DlmsRequest req, DlmsRequest dr) {
//		if(req.getParams()[0].resultCode!=0) return false;
//		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 6));
//		dr.setOpType(DLMS_OP_TYPE.OP_GET);
//		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_TEMP);
//		return true;
//	}

	/**
	 * step7 写入执行时间
	 *  新版本软件生效，通过action操作image transfer对象的方法4激活之前步骤中传输的升级包文件。
	 *  软件升级时间的格式为array[1]
		{
		   structure
		   include
		   {
		       time; octet-string[4]
		       date; octet-string[5]     
		   }
		}
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_07(DlmsRequest req, DlmsRequest dr) {
		//写执行时间
		String strEffcDate=(String)dr.getAppendParam(DlmsUpgradeAssisant.EFFECTIVE_TIME);
		String[] dateTime = strEffcDate.split(" ");
		DlmsData[] effcDate = new DlmsData[2];
		effcDate[0] = new DlmsData();
		effcDate[0].setDlmsTime(dateTime[1]);
		effcDate[1] = new DlmsData();
		effcDate[1].setDlmsDate(dateTime[0]);
		DlmsData[] array = new DlmsData[]{new DlmsData()};
		ASN1SequenceOf struct = new ASN1SequenceOf(effcDate);
		array[0].setStructure(struct);
		DlmsData data = new DlmsData();
		try {
			data.setArray(array);
		} catch (IOException e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		
		setRequestParam(dr, new ObisDescription(22, "0.0.15.0.2.255", 4));
		dr.getParams()[0].data = data;
		dr.setOpType(DLMS_OP_TYPE.OP_SET);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_07);
	}
	/**
	 * 第六步:设定升级包生效前，主站检查表计中升级包文件，主站通过读取image transfer对象的属性7获取表计中收到的升级包文件的版本号、字节数、散列校验码或散列校验码密文，与主站保存的升级包文件的这些值进行比对，如符合则表明表计中升级包文件完整无误；
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_06(DlmsRequest req, DlmsRequest dr) {
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 7));
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_06);
	}
	
	/**
	 * 第五步:验证升级包文件，客户端通过action操作image transfer对象的方法3来触发表计对升级包文件进行验证，表计回复的结果可能为successs、temporary-failure或other-reason，如结果为other-reason则终止升级过程；如结果为success则表明验证成功，进入下一步；如结果为temporary-failure则延时片刻后获取image_transfer_status(读image transfer对象的属性6)，状态为image verification successful时表明验证成功，状态为image verification failed时表明验证失败，状态为image verification initiated时表明表计正在验证，需延时片刻再读取image_transfer_status；验证过程中表计需要计算升级包的散列值，并与image_identifier中散列值校验码或解密散列校验码密文进行比对，当不一致时，在步骤  image_verify时表计回复 other-reason表示升级包验证失败
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_05(DlmsRequest req, DlmsRequest dr) {
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 3));
		dr.setOpType(DLMS_OP_TYPE.OP_ACTION);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_05);
	}

	/**
	 * 补发漏点块
	 * @param req
	 * @param dr
	 */
	@SuppressWarnings("unchecked")
	private void reissueBlock(DlmsRequest dr) {
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_RESSIUE);
		dr.setOpType(DLMS_OP_TYPE.OP_ACTION);
		int i_maxSize=(Integer) dr.getAppendParam(DlmsUpgradeAssisant.MAX_SIZE);
		byte[] content=(byte[]) dr.getAppendParam(DlmsUpgradeAssisant.UPGRADE_CONTENT);
		
		//获得补发列表
		List<Integer> reissueList=(List<Integer>) dr.getAppendParam(DlmsUpgradeAssisant.REISSUE_LIST);
		
		//从列表头开始发送
		Integer reissueBlockNum = reissueList.remove(0);
		dr.addAppendParam(DlmsUpgradeAssisant.CURRENT_RESSIUE_NUM, reissueBlockNum);
		dr.addAppendParam(DlmsUpgradeAssisant.REISSUE_LIST, reissueList);
		//如果是最后一个
		byte[] reissueBlock=getCurUpgradeBlock(content, reissueBlockNum, i_maxSize);
		if(reissueList.size()==0){
			dr.addAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		blockTransferRequest(dr, reissueBlockNum, reissueBlock);
	}
	
	/**
	 * 传输位图块请求对象
	 * @param req
	 * @param dr
	 * @param blockNum
	 * @param block
	 */
	public void blockTransferRequest( DlmsRequest dr,
			Integer blockNum, byte[] block) {
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 2));
		DlmsData data = new DlmsData();
		DlmsData[] datas = new DlmsData[2];
		datas[0] = new DlmsData();
		datas[0].setDoubleLongUnsigned(blockNum);
		datas[1] = new DlmsData();
		datas[1].setOctetString(block);
		ASN1SequenceOf struct = new ASN1SequenceOf(datas);
		data.setStructure(struct);
		dr.getParams()[0].data = data;
	}
	/**
	 * 升级第一步
	 * 从表计获取表支持的ImageBlockSize(即表计支持每帧接收的升级包数据块的字节数，表计根据具体的通信信道给出合适的字节数)，ImageBlockSize可通过读取image transfer的属性2获取；
	 * @param req
	 * @param dr
	 * @return 
	 */
	private boolean upgradeStep_01(DlmsRequest req, DlmsRequest dr) {
		if(req.getParams()[0].resultCode!=0)return false;
		
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_01);
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 2));//读取一次发送的最大字节数
		
		return true;
	}
	
	/**
	 * 写升级模式.目前要求至少支持两种模式：0x01升级模式一为针对载波、RF等通信方式，imageBlockSize固定为64字节；0x02升级模式二为针对RS485、GPRS等通信方式，imageBlockSize固定为192字节。 
	 * @param req
	 * @param dr
	 * @return
	 */
	private boolean updateStep_01_01(DlmsRequest req,DlmsRequest dr){
		
		if(req.getParams()[0].resultCode!=0)return false;
		
		dr.setOpType(DLMS_OP_TYPE.OP_SET);
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_01_01);
		DlmsData data = new DlmsData();
		data.setUnsigned(0x02);
		setRequestParam(dr, new ObisDescription(1, "0.0.44.128.0.255", 2));
		dr.getParams()[0].data=data;
		
		return true;
	}

	/**
	 * 升级第二步
	 * 初始化，客户端通过action操作image transfer对象的方法1来告之表计将要传输的升级包的image_identifier以及升级包的字节数，其中image_identifier包含升级文件头中的升级文件标识+文件类型+对散列校验码加密的加密类型+校验类型+	版本控制+散列校验码（当对散列校验码加密的加密类型=00时）或散列校验码密文（当对散列校验码加密的加密类型不等于00时）
	 * 1)	Byte1-byte2升级文件标识：2字节，固定HX的ASCII码；
	   2)	Byte3文件类型：1字节
			a)	00H：清除传输文件，恢复到升级前状态。
			b)	01H：终端升级文件（适用于终端、集中器、计量柜）
			c)	02H：远程（上行）通讯模块升级文件。
			d)	03H：本地通讯模块升级文件。
			e)	04H：采集器升级的采集器地址文件。
			f)	05H：采集器升级的采集器程序文件。
			g)	06H：采集器通信模块升级的地址文件。
			h)	07H：采集器通信模块升级的程序文件。
			i)	08H：表计升级文件
			j)	FFH：代表主站下发任意文件程序（其中文件的第一帧中包含文件的相关信息，目前采用该格式升级集中器程序和表计的程序）
	   3)	Byte4对散列校验码加密的加密类型：1字节； 定义（
			00：不加密；
	     	01：AES－GCM-128加密，。其中IV固定为12字节00，当表计通信的身份验证机制为NONE时，密钥固定为16字节00；当表计通信的身份验证机制为LLS时，密钥为8字节00+LLS密码，如LLS密码为12345678时，此处的密钥为00 00 00 00 00 00 00 0031 32 33 34 35 36 37 38；当身份验证机制为HLS时，又分为两种情况，通信加密或不加密，通信加密时，此处的密钥为通信密钥EK，通信不加密时，此处的密钥为身份验证的密钥HLS Secret。密钥是通信密钥或者全0；）
	   4)	Byte5校验类型：1字节； 定义（0：无校验；1：MD5；2：CRC16；）
	   5)	Byte6-byte25版本控制：20字节，不足后补00；一般用于文件名来指定适用表计的类型和版本。
	   6)	Byte26-byte41散列校验码：16字节，不足后补00；
	 * @param req
	 * @param dr
	 * @return 
	 */
	private boolean upgradeStep_02(DlmsRequest req, DlmsRequest dr) {
		
		if(req.getParams()[0].resultData==null) return false;
		
		int i_maxSize=req.getParams()[0].resultData.getDoubleLong();//获得每帧传送最大字节数
		upgradeInit(req, dr, i_maxSize);
		
		return true;
	}
	
	/**
	 * 升级初始化 
	 * @param req
	 * @param dr
	 * @param i_maxSize
	 */
	private void upgradeInit(DlmsRequest req, DlmsRequest dr, int i_maxSize) {
		dr.removeAppendParam(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM);
		dr.addAppendParam(DlmsUpgradeAssisant.MAX_SIZE, i_maxSize);
		dr.setOpType(DLMS_OP_TYPE.OP_ACTION);
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 1));//初始化
		DlmsData data = new DlmsData();
		DlmsData[] datas = new DlmsData[2];
		byte[] content = DlmsUpgradeAssisant.getInstance().getFtpFileContent(req);
		dr.addAppendParam(DlmsUpgradeAssisant.UPGRADE_CONTENT, content);
		dr.addAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT, content.length/i_maxSize); //块数
		
		byte[] fileHead=fileHeadConstruct(req);
		dr.addAppendParam(DlmsUpgradeAssisant.FILE_HEAD, HexDump.toHex(fileHead));
		datas[0] = new DlmsData();//image_identifier
		datas[0].setOctetString(fileHead);
		datas[1] = new DlmsData();//image_szie
		datas[1].setDoubleLongUnsigned(content.length);
		ASN1SequenceOf struct = new ASN1SequenceOf(datas);
		data.setStructure(struct );
		dr.getParams()[0].data = data;
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_02);
	}
	public byte[] fileHeadConstruct(DlmsRequest req){
		String strFileHead=(String) req.getAppendParam(DlmsUpgradeAssisant.FILE_HEAD);
		if(tracer.isEnabled())
			tracer.trace("MeterId:"+req.getMeterId()+"FileHead:"+strFileHead);
		byte[] fileHead = HexDump.toArray(strFileHead);
		if(fileHead[1]==1){//加密
			ByteBuffer iv = ByteBuffer.allocate(12);
			DlmsContext context = contextManager.getContext(req.getMeterId());
			byte[] verfication = new byte[16];
			try {
			 System.arraycopy(fileHead, 23, verfication, 0, 16);
			 byte[] encryptVerfiy=AESGcm128.encrypt(context.encryptKey, iv.array(), verfication, null);
			 System.arraycopy(encryptVerfiy, 0,fileHead, 23, 16);
			} catch (InvalidCipherTextException e) {
				log.error(StringUtil.getExceptionDetailInfo(e));
			}
		}
		return fileHead;
	}
	/**
	 * 升级第4步
	 * 检查升级包是否传输完毕，首先客户端读取image_transferred_blocks_status（image transfer对象的属性3）获取各个imageBlock是否已接收的bitmap，如发现有遗漏的block，则补发相应的block，直至所有的block都被表计接收；当(image_transferred_blocks/8)> ImageBlockSize时，客户端需采用选择性读（选择性参数=entry_descriptor）来实现分组读取所有image_transferred_blocks的bitmap，如ImageBlockSize=100，image_transferred_blocks=1800，则客户端需要分三次来读取bitmap(第一次的entry_descriptor中From_entry=0，to_entry=99；第二次entry_descriptor中From_entry=100，to_entry=199；第三次entry_descriptor中From_entry=200，to_entry=00。Entry_descriptor的使用详见《DLMS/COSEM应用层标准》)；
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_04(DlmsRequest req, DlmsRequest dr) {
		setRequestParam(dr, new ObisDescription(18, "0.0.44.0.0.255", 3));
		dr.setOpType(DLMS_OP_TYPE.OP_GET);
		dr.removeAppendParam(DlmsUpgradeAssisant.CURRENT_RESSIUE_NUM);
		int i_maxSize=(Integer) dr.getAppendParam(DlmsUpgradeAssisant.MAX_SIZE);
		int blockCount=(Integer) dr.getAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT);
		
		if(blockCount/8>i_maxSize){//按照新的标准:如果(image_transferred_blocks/8)> ImageBlockSize,选择性读，否则，直接读
			
			int fromIndex=getRessiueBlocks(req,dr);
			
			int nextIndex=fromIndex+i_maxSize-1;//假如读3次的话[0,i_maxSize-1],[i_maxSize,199]
			if(nextIndex>(blockCount/8)){//最后一次读，读完之后，转移到第4步
				nextIndex=0;
				dr.setOperator(DlmsUpgradeAssisant.UPGRADE_04);
			}else{//继续读
				dr.setOperator(DlmsUpgradeAssisant.UPGRADE_READMAP);
				dr.addAppendParam(DlmsUpgradeAssisant.CURRENT_INDEX, nextIndex+1);
			}
			//分多次读
			SelectiveAccessDescriptor sad = new SelectiveAccessDescriptor();
			sad.selectByIndex(fromIndex, nextIndex);
			dr.getParams()[0].accessSelector=2;
			dr.getParams()[0].data.assignValue(sad.getParameter());
		}else{//直接读，读完之后转移到第4步
			dr.setOperator(DlmsUpgradeAssisant.UPGRADE_04);
		}
	}

	/**
	 * DLMS升级第三步
	 * 传输升级包文件，客户端按照Step 1中获取的imageBlockSize分帧传送升级包文件，升级包的传输通过action操作image transfer对象的方法2完成；
	 * @param req
	 * @param dr
	 */
	private boolean upgradeStep_03(DlmsRequest req, DlmsRequest dr) {
		
		if(req.getParams()[0].resultCode!=0) return false;
		
		dr.setOperator(DlmsUpgradeAssisant.UPGRADE_03);
		dr.setOpType(DLMS_OP_TYPE.OP_ACTION);
		int i_maxSize=(Integer) dr.getAppendParam(DlmsUpgradeAssisant.MAX_SIZE);
		byte[] content=(byte[]) dr.getAppendParam(DlmsUpgradeAssisant.UPGRADE_CONTENT);
		int currentBlockNum = 0;
		if(!dr.containsKey(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM)) currentBlockNum=0;
		else currentBlockNum = (Integer) dr.getAppendParam(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM)+1;
		dr.addAppendParam(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM, currentBlockNum);
		if(tracer.isEnabled()){
			tracer.trace("MeterId:"+dr.getMeterId()+",BlockCount:"+(Integer)dr.getAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT)+",CurrentBlockNum:"+currentBlockNum);
		}
		
		if (currentBlockNum == (Integer) dr.getAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT)
				||( content.length % i_maxSize == 0
				&& currentBlockNum +1 == (Integer) dr.getAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT))) {
			// 当前发送是最后一帧
			dr.addAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		byte[] currentSendBlock = getCurUpgradeBlock(content,currentBlockNum,i_maxSize);
		String logicAddr = req.getMeterId();
		int tn = 0;
		if(req.getRelayParam()!=null){
			logicAddr=req.getRelayParam().getDcLogicalAddress();
			tn =req.getRelayParam().getMeasurePoint();
		}
		if(tracer.isEnabled())
			tracer.trace("meterId:"+logicAddr+",tn:"+tn+",currentBlockNum:"+currentBlockNum+",blockContent:"+HexDump.toHex(currentSendBlock));
		blockTransferRequest(dr, currentBlockNum, currentSendBlock);
		return true;
	}

	/**
	 * 升级使能
	 * @param req
	 */
	public void upgradeEnable(DlmsRequest req) {
		//TODO:存储升级信息 状态为正在升级
		req.setOperator(DlmsUpgradeAssisant.UPGRADE_00);
		req.setOpType(DLMS_OP_TYPE.OP_SET);
		DlmsObisItem param = req.getParams()[0];
		param.classId = 18;
		param.attributeId=5;
		param.obisString="0.0.44.0.0.255";
		DlmsData data = new DlmsData();
		data.setBool(true);
		param.data = data;
	}
	
	/**
	 * 软件升级断点续传初始化
	 * @param req
	 */
	public void upgradeReissueInit(DlmsRequest req) {
		//软件升级补发
		req.setOperator(DlmsUpgradeAssisant.UPGRADE_03);
		byte[] content = DlmsUpgradeAssisant.getInstance().getFtpFileContent(req);
		Integer currentBlockNum = (Integer)req.getAppendParam(DlmsUpgradeAssisant.CURRENT_BLOCK_NUM);
		Integer i_maxSize = (Integer)req.getAppendParam(DlmsUpgradeAssisant.MAX_SIZE);
		byte[] currentBlock=DlmsUpgradeHandler.getInstance().getCurUpgradeBlock(content, currentBlockNum, i_maxSize);
		int blockCount = content.length/i_maxSize;
		req.addAppendParam(DlmsUpgradeAssisant.BLOCK_COUNT, blockCount);
		if (currentBlockNum == blockCount
				|| content.length % i_maxSize == 0
				&& currentBlockNum - 1 == blockCount) {
			req.addAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED, true);
		}
		DlmsUpgradeHandler.getInstance().blockTransferRequest(req, currentBlockNum, currentBlock);
		req.addAppendParam(DlmsUpgradeAssisant.UPGRADE_CONTENT, content);
		req.setOpType(DLMS_OP_TYPE.OP_ACTION);
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
	 * 收到请求，升级处理
	 * @param req
	 * @return 
	 */
	public boolean upgradeProcesser(DlmsRequest req){
		String logicAddress="";
		int measurePoint=0;
		if(req.getRelayParam()!=null){
			RelayParam relayParam = req.getRelayParam();
			logicAddress=relayParam.getDcLogicalAddress();
			measurePoint = relayParam.getMeasurePoint();
		}else{
			logicAddress=req.getMeterId();
			measurePoint = 0;
		}
		try {
			
			List<UpgradeInfo> infos = dbService.getUpgradeInfo(new Long((String)req.getAppendParam(DlmsUpgradeAssisant.UPGRADE_ID)));
			if(infos.size()!=1){
				log.error("Upgrade Request Fail:Info.size="+infos.size()+".MeterId="+logicAddress+".Tn="+measurePoint);
				if(tracer.isEnabled())
					tracer.trace("Upgrade Request Fail:Info.size="+infos.size()+".MeterId="+logicAddress+".Tn="+measurePoint);
			}
			req.addAppendParam(DlmsUpgradeAssisant.UPGRADE_ID, infos.get(0).getSoftUpgradeID());
			
			UpgradeInfo info = infos.get(0);
			int status = info.getStatus();
			DlmsUpgradeAssisant.getInstance().addRequestAppendParams(info,req);
			
			String fileHead=(String) req.getAppendParam(DlmsUpgradeAssisant.FILE_HEAD);
			String fileType=fileHead.substring(0, 2);
			
			//如果是给模块升级，设置dstAddr为  0x0070
			//根据地址文件头信息获得升级状态
			//req.setDestAddr(0x0070);
			
			if("02".equals(fileType)){//判断是否是对模块升级
				req.setDestAddr(DlmsMessage.DstAddrToModule);
			}
			
			boolean isRessiueSuccess = true;
			switch(status){
			case UpgradeInfo.WAIT_UPGRADE:
				upgradeEnable(req);
				break;
			case UpgradeInfo.WAIT_UPGARDEINIT:
				upgradeInit(req, req, (Integer)req.getAppendParam(DlmsUpgradeAssisant.MAX_SIZE));
				break;
			case UpgradeInfo.UPGRADE_PAUSE:
				upgradeReissueInit(req);
				break;
			case UpgradeInfo.RESSIUE_PAUSE:
				req.addAppendParam(DlmsUpgradeAssisant.IS_TRANSFER_FINISHED, false);
				reissueBlock(req);
				break;
			case UpgradeInfo.CHECK_MAP_FAIL:
				req.setOperator(DlmsUpgradeAssisant.UPGRADE_RESSIUE);
				upgradeStep_04(req, req);
				break;
			case UpgradeInfo.READ_STATUS_FAIL:
				readTransferStatus(req);
				break;
			case UpgradeInfo.VERFIY_FILE_FAIL:
				upgradeStep_05(req, req);
				break;
			case UpgradeInfo.CHECK_FILE_FAIL:
				upgradeStep_06(req, req);
				break;
			case UpgradeInfo.SET_EFFECTTIME_FAIL:
				upgradeStep_07(req, req);
				break;
			default:
				isRessiueSuccess=false;
				log.error("Upgrade File.Status:"+status+" .MeterId="+logicAddress+" .Tn="+measurePoint);
				break;
			}
			if(isRessiueSuccess){
				DlmsUpgradeAssisant.getInstance().updateUpgradeStatus(req, "01");
				return true;
			}
			return false;
		} catch (Exception e) {
			//设置升级失败
			log.error("update stoped.",e);
 			DlmsUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//设置为失败
			DlmsUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
			return false;
		}
	}
	
	
	
	private class ObisDescription{
		public int classId;
		
		public	int attrId;
		
		public String obis;
		
		public ObisDescription(int classId,String obis,int attrId){
			this.classId = classId;
			this.obis = obis;
			this.attrId = attrId;
		}
	}
	
	public static void main(String[] args) throws IOException {
//		DlmsUpgradeAssisant.getInstance().getFtpFileContent(new DlmsRequest());
//		for(int i=0;i<=400;i++){
//			byte[] b = DlmsUpgradeHandler.getInstance().getCurUpgradeBlock(DlmsUpgradeHandler.getInstance().getFtpFileContent(new DlmsRequest()), i, 198);
//			System.out.println(i+"-"+HexDump.toHex(b));
//		}

		System.out.println(	Integer.toBinaryString(0x0f));
		byte[] array = new byte[]{1,2,3,4,5,6,7,8,9,10,11};
		System.out.println(array.length/5+1);
		byte[] dest = new byte[5];
		System.arraycopy(array, 0*5, dest, 0, 5);
		System.out.println(HexDump.toHex(dest));
		System.arraycopy(array, 1*5, dest, 0, 5);
		System.out.println(HexDump.toHex(dest));
		if(3==array.length/5+1){ 
			int remainLen = 5-(3*5-array.length);
			System.arraycopy(array, 2*5, dest, 0, remainLen);
		}
		List<Integer> ressiueBlocks = new ArrayList<Integer>();
		byte[] bitMap = new byte[]{(byte)0x00,(byte)0x00};
		DlmsUpgradeHandler.getInstance().getRessiueBlocks(bitMap, 16, ressiueBlocks , 0);
//		System.arraycopy(array, 2*5, dest, 0, 5);
		System.out.println(HexDump.toHex(DlmsUpgradeHandler.getInstance().getCurUpgradeBlock(array, 2, 5)));
//		DlmsUpgradeHandler.getInstance().getRessiueBlocks(new byte[]{0x08}, 5);
		System.out.println(HexDump.toHex(DlmsUpgradeHandler.getInstance().fileHeadConstruct(null)));
		
		
		byte[] apdu = HexDump.toArray("C401010004820441FFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFFF80");
		GetResponse resp = new GetResponse();
		resp.decode(DecodeStream.wrap(apdu));
	
		GetResponseNormal grn = (GetResponseNormal) resp.getDecodedObject();
		
		GetDataResult dd = grn.getResult();
		DlmsData data = dd.getData();
		ASN1BitString s = data.getBitString();
		ressiueBlocks = new ArrayList<Integer>();
		DlmsUpgradeHandler.getInstance().getRessiueBlocks(s.getValue(), 1089, ressiueBlocks, 1);
		System.out.println(ressiueBlocks);
	}

	public final void setDbService(MasterDbService dbService) {
		this.dbService = dbService;
	}

	public final void setService(AsyncService service) {
		this.service = service;
	}

}
