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
 * @Description  ANSI Զ������
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-7-16 ����09:43:11
 * @version 1.0 
 */

public class AnsiUpgradeHandler {
	private static final Logger log=Logger.getLogger(AnsiUpgradeHandler.class);
	private static final TraceLog Trace=TraceLog.getTracer("ANSI.Upgrade");
	
	private MasterDbService masterDbService ;
	
	private AsyncService service;
	/**�������������ʱ����Ϣʱ��*/
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
			boolean isFinished = false;//�Ƿ���������
			boolean isSuccess = true;//�����Ƿ�ɹ�
			
			AnsiRequest ar = new AnsiRequest();
			ar.setMeterId(context.meterId);
			ar.addAllAppendParmas(req.getAllParam());
			ar.setServiceType(req.getServiceType());//�������
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
				if(checkBitMap(req, ar)){//���û��©�㣬������岽
					upgradeStep_05(req,ar);
				}
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_05)){//�����֤�Ƿ�ɹ�
				if(Integer.parseInt(req.getDataItem()[0].resultData)==2){ //������ص���temporary-failure,��Ϣһ���ٶ�
					try {
						Thread.sleep(sleepTimeStay06Step*1000);
					} catch (InterruptedException e) {
					}
					upgradeStep_05(req,ar); 
				}else if(Integer.parseInt(req.getDataItem()[0].resultData)==0){
					upgradeStep_06(req,ar);
				}else{//���������������������ʧ��
					isSuccess=false;
				}
				
			}
			else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_06)){
				//��������������վ�·����Ƚ�
				//����ɹ���ִ�е�7��
				{
					upgradeStep_07(req,ar);
				}
			}else if(req.getOperator().equals(AnsiUpgradeAssisant.UPGRADE_07)){
				//������� ����finished=true
				if(Integer.parseInt(req.getDataItem()[0].resultData)==0)
					isFinished = true;
				else{
					isSuccess=false;
				}
			}
			
			//��ѯ�������б��У��Ƿ���Ҫ��������������У���ǰ�����ж�Ϊʧ��
			if(context.webReqList.size()>0){
					AnsiEvent evt = (AnsiEvent) context.webReqList.get(0);
					AnsiRequest request=(AnsiRequest) evt.getRequest();
					if(request.getOperator()!=null && request.getOperator().contains("UPGRADE")){
						isSuccess = false;
						log.error("meterid="+context.meterId+".New upgrade request is comming,current upgrade set fail");
					}
			}
			
			if(!isSuccess){
				AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//����Ϊʧ��
				AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
				Trace.trace("Upgrade Failed. MeterId="+req.getMeterId()+",Operator="+req.getOperator());
				return;
			}
			if(!isFinished){
				AnsiUpgradeAssisant.getInstance().onUpgradeSuccess(ar);
				processor.postWebRequest(ar, null);
			}else{
				//��β�׶�
				req.addAppendParam(AnsiUpgradeAssisant.CURRENT_BLOCK_NUM, (Integer)req.getAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT));
				AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.SUCCESS);//����Ϊ�ɹ�
				AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"02");
			}
		} catch (Exception e) {
			log.error(StringUtil.getExceptionDetailInfo(e));
 			AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(req, UpgradeInfo.FAIL);//����Ϊʧ��
			AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(req,"00");
		}
	
	}
	
	
	
	/**
	 * ��ȡ�����֧�ֵ�����ֽ���
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
	 * ��������Ϣ�·������
	 * @param request
	 * @param ar
	 * @return
	 */
	private boolean upgradeStep_02(AnsiRequest request,AnsiRequest ar){
//		if(!request.getDataItem()[0].resultData.equals("00")) return false;
		int i_maxSize=Integer.parseInt(request.getDataItem()[0].resultData, 16);//���ÿ֡��������ֽ���
		log.info("========================="+request.getMeterId()+" i_maxSize:"+i_maxSize);
		upgradeInit(request, ar, i_maxSize);
		return true;
	}
	
	/**
	 * ������Ϣ��ʼ�� 
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
		ar.addAppendParam(AnsiUpgradeAssisant.BLOCK_COUNT, content.length/maxsize); //����
		
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
	 * �·������ļ�
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
			// ��ǰ���������һ֡
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
	 * �������̴������ݿ�
	 * �ж��Ƿ�����ϣ�����������ֱ�ӽ����4��,�����������
	 * @param req
	 * @param dr
	 * @return
	 */
	private boolean blockTransfering(AnsiRequest req, AnsiRequest dr) {
		if(Integer.parseInt(req.getDataItem()[0].resultData)!=0) return false;
		if(dr.containsKey(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED)){//���������,����������Ƿ����
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
	 * ������4��
	 * ����������Ƿ�����ϣ����ȿͻ��˶�ȡimage_transferred_blocks_status��image transfer���������3����ȡ����imageBlock�Ƿ��ѽ��յ�bitmap���緢������©��block���򲹷���Ӧ��block��ֱ�����е�block������ƽ��գ���(image_transferred_blocks/8)> ImageBlockSizeʱ���ͻ��������ѡ���Զ���ѡ���Բ���=entry_descriptor����ʵ�ַ����ȡ����image_transferred_blocks��bitmap����ImageBlockSize=100��image_transferred_blocks=1800����ͻ�����Ҫ����������ȡbitmap(��һ�ε�entry_descriptor��From_entry=0��to_entry=99���ڶ���entry_descriptor��From_entry=100��to_entry=199��������entry_descriptor��From_entry=200��to_entry=00��Entry_descriptor��ʹ�������Ansi/COSEMӦ�ò��׼��)��
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
//		if(blockCount/8>i_maxSize){//�����µı�׼:���(image_transferred_blocks/8)> ImageBlockSize,ѡ���Զ�������ֱ�Ӷ�
//		}else{//ֱ�Ӷ�������֮��ת�Ƶ���4��
//			dr.setOperator(AnsiUpgradeAssisant.UPGRADE_04);
//		}
	}
	/**
	 * ����©���
	 * @param req
	 * @param dr
	 */
	@SuppressWarnings("unchecked")
	private void reissueBlock(AnsiRequest dr) {
		dr.setOperator(AnsiUpgradeAssisant.UPGRADE_RESSIUE);
		dr.setOpType(ANSI_OP_TYPE.OP_ACTION);
		int i_maxSize=(Integer) dr.getAppendParam(AnsiUpgradeAssisant.MAX_SIZE);
		byte[] content=(byte[]) dr.getAppendParam(AnsiUpgradeAssisant.UPGRADE_CONTENT);
		
		//��ò����б�
		List<Integer> reissueList=(List<Integer>) dr.getAppendParam(AnsiUpgradeAssisant.REISSUE_LIST);
		
		//���б�ͷ��ʼ����
		Integer reissueBlockNum = reissueList.remove(0);
		dr.addAppendParam(AnsiUpgradeAssisant.CURRENT_RESSIUE_NUM, reissueBlockNum);
		dr.addAppendParam(AnsiUpgradeAssisant.REISSUE_LIST, reissueList);
		//��������һ��
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
		
		if(ressiueBlocks.size()!=0){//���벹��
			dr.addAppendParam(AnsiUpgradeAssisant.REISSUE_LIST, ressiueBlocks);
			dr.addAppendParam(AnsiUpgradeAssisant.IS_TRANSFER_FINISHED, false);
			reissueBlock(dr);
			return false;
		}
		return true;
	}
	/**
	 * ���岽:��֤�������ļ����ͻ���ͨ��action����image transfer����ķ���3��������ƶ��������ļ�������֤����ƻظ��Ľ������Ϊsuccesss��temporary-failure��other-reason������Ϊother-reason����ֹ�������̣�����Ϊsuccess�������֤�ɹ���������һ��������Ϊtemporary-failure����ʱƬ�̺��ȡimage_transfer_status(��image transfer���������6)��״̬Ϊimage verification successfulʱ������֤�ɹ���״̬Ϊimage verification failedʱ������֤ʧ�ܣ�״̬Ϊimage verification initiatedʱ�������������֤������ʱƬ���ٶ�ȡimage_transfer_status����֤�����б����Ҫ������������ɢ��ֵ������image_identifier��ɢ��ֵУ��������ɢ��У�������Ľ��бȶԣ�����һ��ʱ���ڲ���  image_verifyʱ��ƻظ� other-reason��ʾ��������֤ʧ��
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
	 * ��֤������
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
	 * step7 д��ִ��ʱ��
	 *  �°汾�����Ч��ͨ��action����image transfer����ķ���4����֮ǰ�����д�����������ļ���
	 * @param req
	 * @param dr
	 */
	private void upgradeStep_07(AnsiRequest req, AnsiRequest dr) {
		//дִ��ʱ��EffectiveTime=2013-06-26 14:58:00
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
	 * ��վ����Զ������ҵ������
	 * @param request
	 * @return
	 */
	public boolean upgradeProcessor(AnsiRequest request){
		String meterAddr=request.getMeterId();
		int tn=0;//GPRS�������Ĭ��Ϊ0
		//�������������ID ��ѯ���ݿ��ȡ������Ϣ
		try {
		List<UpgradeInfo> infos = masterDbService.getUpgradeInfo(new Long((String) request.getAppendParam(AnsiUpgradeAssisant.UPGRADE_ID)));
		if(1!=infos.size()){
			log.error("Upgrade request Fail:Info.size="+infos.size()+".MeterId="+meterAddr+".Tn="+tn);
			Trace.trace("Upgrade request Fail:Info.size="+infos.size()+".MeterId="+meterAddr+".Tn="+tn);
		}
		UpgradeInfo info=infos.get(0);
		request.addAppendParam(AnsiUpgradeAssisant.UPGRADE_ID, info.getSoftUpgradeID());
		
		AnsiUpgradeAssisant.getInstance().addRequestAppendParams(info, request);
		//02 ���ͨ��ģ������ FD�������
		String fileType=((String) request.getAppendParam(AnsiUpgradeAssisant.FILE_HEAD)).substring(0, 2);
		if(fileType.equals("02")){//gprs moudle upgrade
			request.setUpgradeType(1);
		}
		else if(fileType.equals("FD")) {//meter upgrade 
			request.setUpgradeType(2);
		}
		
		boolean isRessiueSuccess=true;
		int status=info.getStatus();//��ȡ����״̬
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
		//��������ʧ��
		log.error("update stoped.",e);
			AnsiUpgradeAssisant.getInstance().updateUpgradeInfo(request, UpgradeInfo.FAIL);//����Ϊʧ��
		AnsiUpgradeAssisant.getInstance().updateUpgradeStatus(request,"00");
		return false;
	}

}
	
	/**
	 * ��õ�ǰ����������
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
		if(size == currentBlockNum){//���һ֡�����ܲ���i_maxSize
			destSize = i_maxSize-((currentBlockNum+1)*i_maxSize-length);
		}
		byte[] dest = new byte[destSize];
		System.arraycopy(content, currentBlockNum*i_maxSize, dest, 0, destSize);
		return dest;
	}
	
	/**
	 * �·��������ݿ�
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
		//�����Чλ��
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
	 * ����λͼ�����Ҫ�����Ŀ�
	 * @param bitMap
	 * @param blockCount
	 * @return
	 */
	private void getRessiueBlocks(byte[] bitMap, int blockCount,List<Integer> ressiueBlocks,int offset){
		//��λ��ǰ����λ�ں�
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
	 * ��������ϵ�������ʼ��
	 * @param req
	 */
	public void upgradeReissueInit(AnsiRequest req) {
		//�����������
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
