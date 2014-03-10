package cn.hexing.plm.updatertu;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.util.StringUtils;

import cn.hexing.db.batch.AsyncService;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.model.Master2FeRequest;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.plm.feintf.feserver.FeEventHandler;

/**
 * 监控终端升级文件. monite RTU update files.
 *
 */
public class UpdateRtuModule extends BaseModule {
	//static member
	private static final Logger log = Logger.getLogger(UpdateRtuModule.class);
	private static final UpdateRtuModule instance = new UpdateRtuModule();
	//configurable attributes
	private String updateFile = "update.";
	private String rtuaListFile = "rtua-list.";	//格式 rtualist.{批次号}
	private String monitePath = "data";
	private int packetLength = 500;
	private String password = "";
	private FeEventHandler eventHandler = null;
	private int updateTimeout = 120;					//分钟
	private int resendInterval = 15;					//重发间隔(秒)
	private AsyncService dbService = null;
	private int daoKey = 1;
	//initialize load data DAO
	private LoadUpdatingRtuDao loadDao = null;
	private boolean enableUpdateFlag = false;
	
	//private attributes
	private FileMonitorThread thread = null;
	//Next attribute is used to prevent same RTU multi-updating. 
	private Map<Integer,RtuStatus> updatingRtus = Collections.synchronizedMap(new HashMap<Integer,RtuStatus>());
	private List<RtuStatus> downQueue = new LinkedList<RtuStatus>();	//下行终端队列
	//Holder Zj Update-Message' channel
	private Map<Integer,IChannel> zjUpdateChannelMap = new HashMap<Integer,IChannel>();
	private List<MessageZj> zjUpdateMessages = Collections.synchronizedList(new LinkedList<MessageZj>());
	private Map<Integer,Integer> zjUpdateFlagMap = Collections.synchronizedMap(new HashMap<Integer,Integer>());

	private UpdateRtuModule(){
		
	}
	
	public static final UpdateRtuModule getInstance(){
		return instance;
	}
	
	public boolean forwardMaster2Fe(IMessage msg, Master2FeRequest req){
		if( req.getCommand() == Master2FeRequest.CMD_ENABLE_ZJUPDATE ){
			for( int rtua: req.getRtuaList() ){
				zjUpdateFlagMap.put(rtua, rtua);
			}
		}
		else if( req.getCommand() == Master2FeRequest.CMD_DISABLE_ZJUPDATE ){
			for( int rtua: req.getRtuaList() ){
				zjUpdateFlagMap.remove(rtua);
			}
		}
		else if ( req.getCommand() == Master2FeRequest.CMD_CLEAR_ZJUPDATE ){
			zjUpdateFlagMap.clear();
		}
		boolean ret = eventHandler.sendMessage(msg,true); 
		if( req.getCommand() == Master2FeRequest.CMD_FE_PROFILE ){
			log.info("send command to FE: profile. send="+ret);
		}
		else if( req.getCommand() == Master2FeRequest.CMD_UPDATE_SIM ){
			log.debug("send command to FE : update sims. send="+ret);
		}
		return ret;
	}
	
	public void forwardZjUpdate2Fe(MessageZj zjmsg){
		log.debug("收到厂家编码为"+zjmsg.head.msta+"的自定义下行报文:"+zjmsg);
		//1.取该终端是否允许远程升级标志
		if ( enableUpdateFlag && ! zjUpdateFlagMap.containsKey(zjmsg.head.rtua) ){
			log.warn("ZJ.UPDATE now allowed, rtua = "+ HexDump.toHex(zjmsg.getRtua()));
			return;
		}

		//2. 按照下行的报文，管理厂家编码与厂家解析模块到通信前置机之间的clientChannel。
		IChannel srcChannel = (IChannel)zjmsg.getSource();
//		int msta = 0xFF & zjmsg.head.msta; 
		zjUpdateChannelMap.put(zjmsg.getRtua(), srcChannel);
		
		
		//3. Put ZJ update-message into queue for later sending.
		if( !eventHandler.sendMessage(zjmsg,false) )
			zjUpdateMessages.add(zjmsg);
	}
	
	public void trySendNextPacket(){
		RtuStatus rtu = null;
		synchronized(downQueue){
			if( downQueue.size()>0 ){
				rtu = downQueue.get(0);
			}
		}
		if( null != rtu ){
			MessageGw msg = rtu.nextPacket();
			if( eventHandler.sendMessage(msg,false) ){
				synchronized(downQueue){
					downQueue.remove(rtu);
				}
				return;
			}
		}
		if( zjUpdateMessages.size()>0 ){
			MessageZj zjmsg = zjUpdateMessages.remove(0);
			if( ! eventHandler.sendMessage(zjmsg,false) ){
				zjUpdateMessages.add(0, zjmsg);
			}
			return;
		}
	}
	
	/**
	 * According update content and RTUA list ,generate packet and send to RTU
	 * @param update
	 * @param list
	 */
	private void handleUpdate(ByteBuffer content,List<String> list , String batchId){
		for(String s: list ){
			try{
				int rtua = (int)Long.parseLong(s, 16);
				RtuStatus rtu = new RtuStatus(batchId,rtua,content);
				if( ! updatingRtus.containsKey(rtua) ){
					//解决重复rtua
					synchronized(downQueue){
						downQueue.add(rtu);
					}
				}
				updatingRtus.put(rtua, rtu);
			}catch(Exception e){
				log.warn("RTUA解析异常,string="+s+e.getLocalizedMessage());
			}
		}
		trySendNextPacket();
	}
	
	private void removeRtuStatus(RtuStatus rtu){
		synchronized(downQueue){
			downQueue.remove(rtu);
		}
		updatingRtus.remove(rtu.getRtua());
	}
	
	public void precessReplyMessage(MessageGw msg){
		//如果所有终端都升级完成，那么需要在升级目录下生成文件"updateCompleted.txt"
		ByteBuffer data = msg.data;
		if( data.remaining()<8 ){
			log.error("RTUA="+HexDump.toHex(msg.getRtua())+",FILE应答数据区长度<8，不符合规约.");
			updatingRtus.remove(msg.getRtua());
			return;
		}
		int ipacket = data.get(4) & 0x00FF;
		ipacket |= (data.get(5) & 0x00FF) << 8 ;
		ipacket |= (data.get(6) & 0x00FF) << 16 ;
		ipacket |= (data.get(7) & 0x00FF) << 24 ;
		RtuStatus rtu = updatingRtus.get(msg.getRtua());
		if( null == rtu ){
			log.error("receive rtu updating reply but not find RTU Object");
			return;
		}
		rtu.setLastTime(System.currentTimeMillis());
		if( ipacket == 0xFFFFFFFF || ipacket<0 || ipacket >= rtu.getTotalPacket() ){
			//终端升级失败。
			removeRtuStatus(rtu);
			dbService.addToDao(new PojoUpdateState(rtu.getRtua(),rtu.getBatchId(),false,rtu.getTotalPacket(),rtu.getCurPacket()+1), daoKey);
		}
		else{
			if( rtu.isLastPacket() ){
				//终端升级完成。
				removeRtuStatus(rtu);
				dbService.addToDao(new PojoUpdateState(rtu.getRtua(),rtu.getBatchId(),true,rtu.getTotalPacket(),rtu.getCurPacket()+1), daoKey);
			}
			else{
				if (rtu.getCurPacketSendCount(ipacket)>=100){//终端发生异常,始终返回同一序号
					//终端升级失败。
					removeRtuStatus(rtu);
					dbService.addToDao(new PojoUpdateState(rtu.getRtua(),rtu.getBatchId(),false,rtu.getTotalPacket(),rtu.getCurPacket()+1), daoKey);
					log.error("rtu updating reply ipacket always is same:"+ipacket);
				}
				rtu.setCurPacket(ipacket);
				rtu.move(ipacket);
				synchronized(downQueue){
					if( !downQueue.contains(rtu) )
						downQueue.add(rtu);
				}
				log.debug("send next packet,rtu="+HexDump.toHex(msg.getRtua())+",ipacket="+ipacket);
				trySendNextPacket();
			}
		}
		if( updatingRtus.size() == 0 )	//全部升级完成,目录下设置complete.txt
			setCompleteFile();
	}
	
	private static int BCDToDecimal(byte bcd){
		int high=(bcd & 0xf0)>>>4;
		int low=(bcd & 0xf);
		if(high>9 || low>9){
			return -1;
		}
		return high*10+low;
	}
	
	//浙江规约升级报文上行。
	public void precessReplyMessage(MessageZj zjmsg){
		//当GPRS网关收到用户自定义报文上行，则把报文直接发送给厂家解析模块
		//按照厂家编码，把报文推送给厂家升级模块。
//		byte manuCode=zjmsg.head.msta;
//		if((manuCode & 0xff)==0){	//高科、万盛特殊处理
//	    	if (zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE) {
//	            manuCode = (byte)BCDToDecimal(zjmsg.data.get(0));
//	        }
//	    }
//		log.info("收到厂家编码为"+manuCode+"的自定义上行报文:" + zjmsg);
		IChannel srcChannel = zjUpdateChannelMap.get(zjmsg.getRtua());
		if( null == srcChannel ){
			log.error("收到厂家自定义报文，但厂家解析模块与通信前置机的连接找不到。msg=" + zjmsg.getRawPacketString());
			return;
		}
		srcChannel.send(zjmsg);
	}
	
	//定时更新升级工况。检查updatingRtus超过updateTimeout分钟没有应答的终端
	private void updateRtuStatus(){
		if( updatingRtus.size() == 0 )
			return;
		//待升级终端的超时检测。重发机制的实现。
		long timeout = updateTimeout * 60 * 1000;
		long now = System.currentTimeMillis();
		RtuStatus[] rtus = updatingRtus.values().toArray(new RtuStatus[0]);
		for(RtuStatus rtu: rtus ){
			long dif = now - rtu.getLastTime();
			if( dif > timeout ){
				//update timeout.升级超时，更新数据库升级失败
				removeRtuStatus(rtu);
				dbService.addToDao(new PojoUpdateState(rtu.getRtua(),rtu.getBatchId(),false,rtu.getTotalPacket(),rtu.getCurPacket()+1), daoKey);
				log.warn("RTU update failed in sake of timeout. RTUA="+HexDump.toHex(rtu.getRtua()));
				continue;
			}
			dbService.addToDao(new PojoUpdateState(rtu.getRtua(),rtu.getBatchId(),rtu.getTotalPacket(),rtu.getCurPacket()+1), daoKey);
			//reSend mechanism 重发机制
			int cnt = rtu.getResendCount()+1;		//初始化是0
			if( cnt > 10 )
				cnt = 10;
			if( dif > this.resendInterval * 1000 * cnt ){
				//1 minute without receive reply,then resend it.
				rtu.incResendCount();
				log.debug("timeout, resend: rtu="+HexDump.toHex(rtu.getRtua())+",resend count="+rtu.getResendCount());
				synchronized(downQueue){
					if( !downQueue.contains(rtu) )
						downQueue.add(rtu);
				}
			}
		}
		
		//待发队列继续发送
		trySendNextPacket();
	}
	
	@Override
	public boolean start() {
		if( StringUtils.hasLength( monitePath) ){
			if(  !(monitePath.startsWith("/") || monitePath.indexOf(":\\")>0 ) ){
				monitePath = System.getProperty("user.dir") + File.separator + monitePath ;
			}
		}
		System.out.println("monite RTU update-file at:"+monitePath);
		
		if( null != thread )
			thread.stopIt();
		thread = new FileMonitorThread();
		//load uncompleted updat
		if( null != loadDao ){
			List<RtuStatus> list = loadDao.load();
			log.info("Initialize load RTU count="+list.size());
			for(RtuStatus rtu: list){
				updatingRtus.put(rtu.getRtua(), rtu);
				synchronized(downQueue){
					downQueue.add(rtu);
				}
			}
		}
		log.info("RTU update module is started successfully.");
		return true;
	}

	@Override
	public void stop() {
		if( null != thread )
			thread.stopIt();
		thread = null;
	}

	public String getModuleType() {
		return UpdateRtuModule.class.getName();
	}


	public void setUpdateFile(String updateFile) {
		this.updateFile = updateFile;
	}

	public void setRtuaListFile(String rtuaListFile) {
		this.rtuaListFile = rtuaListFile;
	}

	public void setMonitePath(String monitePath) {
		this.monitePath = StringUtils.trimWhitespace(monitePath);
	}

	public ByteBuffer getContent(String batchId){
		File f = new File(monitePath + File.separator + this.updateFile + "." + batchId+".txt");
		if( !f.exists() )
			return null;
		ByteBuffer content = null;
		try{
			RandomAccessFile raf = new RandomAccessFile(f,"r");
			int flen = (int)raf.length();
			if( flen<10 )
				return null;
			byte[] buf = new byte[flen];
			raf.read(buf);
			raf.close();
			raf = null;
			
			boolean isHex = true;
			StringBuilder sb = new StringBuilder();
			for( byte c: buf ){
				if( Character.isLetterOrDigit(c) ){
					sb.append((char)c);
					continue;
				}
				if( Character.isWhitespace(c)){
					continue;
				}
				isHex = false;
				break;
			}
			if( isHex ){
				content = HexDump.toByteBuffer(sb.toString());
			}
			else
				content = ByteBuffer.wrap(buf);
		}catch(Exception e){}
		return content;
	}
	
	public void setUpdatingFile(){
		File complete = new File(monitePath + File.separator + "complete.txt");
		File updating = new File(monitePath + File.separator + "updating.txt");
		if( complete.exists() )
			complete.renameTo(updating);
		else{
			try{
			updating.createNewFile();
			}catch(Exception exp){}
		}
	}

	public void setCompleteFile(){
		File complete = new File(monitePath + File.separator + "complete.txt");
		File updating = new File(monitePath + File.separator + "updating.txt");
		if( updating.exists() )
			updating.renameTo(complete);
		else{
			try{
				complete.createNewFile();
			}catch(Exception exp){}
		}
	}
	
	class FileMonitorThread extends Thread {
		private volatile boolean stopping = false;
		public FileMonitorThread(){
			super("UpdateFileMonitor");
			setDaemon(true);
			start();
		}
		
		public void stopIt(){
			stopping = true;
		}
		
		@Override
		public void run() {
			long checkPoint = System.currentTimeMillis();
			while( ! stopping ){
				try{
					Thread.sleep(10*1000);					
					moniteFile();
					long now = System.currentTimeMillis();
					if( now-checkPoint > 1000 * 60 ){
						//每分钟更新终端升级工况，检查updatingRtus下超过30分钟没有响应的终端
						checkPoint = now;
						updateRtuStatus();
					}
				}catch(Exception e){
					log.error("monite file error: "+e.getLocalizedMessage(),e);
				}
			}
		}
		
		private void moniteFile() throws Exception{
			ByteBuffer content = null;
			List<String> rtuaList = new ArrayList<String>();
			String batchId = null;

			File baseDir = new File(monitePath);
			File[] allFile = baseDir.listFiles();
			if( null == allFile ){
				log.error("monitorPath error:"+monitePath);
				return;
			}
			File fupdate = null, frtua = null;
			//轮询远程更新文件及批次号
			for(File f: allFile){
				if( ! f.isFile() )
					continue;
				if( f.getName().startsWith(updateFile) ){
					String fname = f.getName();
					int index1 = fname.indexOf('.');
					if( index1<0 )
						continue;
					index1++;
					int index2 = fname.indexOf('.', index1);
					if( index2>0 )
						continue;
					batchId = fname.substring(index1).trim();
					
					RandomAccessFile raf = new RandomAccessFile(f,"r");
					try{
						int flen = (int)raf.length();
						if( flen<10 )
							continue;
						byte[] buf = new byte[flen];
						raf.read(buf);
						
						boolean isHex = true;
						StringBuilder sb = new StringBuilder();
						for( byte c: buf ){
							if( Character.isLetterOrDigit(c) ){
								sb.append((char)c);
								continue;
							}
							if( Character.isWhitespace(c)){
								continue;
							}
							isHex = false;
							break;
						}
						if( isHex ){
							content = HexDump.toByteBuffer(sb.toString());
						}
						else
							content = ByteBuffer.wrap(buf);
					}finally{
						raf.close();
						raf = null;
					}																				
					fupdate = f;
					log.info("fupdate="+fupdate.getName());
					if((fupdate != null)&&batchId!=null){
						String rtuasFile=rtuaListFile.trim()+batchId;//直接取匹配批次号的终端列表
						for(File fl: allFile){
							if( ! fl.isFile() )
								continue;					
							if( fl.getName().startsWith(rtuasFile) ){
								String flname = fl.getName();
								int indexfl1 = flname.indexOf('.');
								if( indexfl1<0 )
									continue;
								indexfl1++;
								int indexfl2 = flname.indexOf('.', indexfl1);
								if( indexfl2>0 )
									continue;						
								BufferedReader reader = new BufferedReader(new FileReader(fl));
								try{
									String line;
									while( null != (line = reader.readLine()) ){
										if( StringUtils.hasText(line))
											rtuaList.add(line.trim());
									}
								}finally{
									reader.close();
								}												
								frtua = fl;	
								log.info("frtua="+frtua.getName());								
								break;
							}
						}
						if( fupdate != null && frtua != null ){
							//rtualist.8870-->rtualist.8870.txt;update.8870-->update.8870.txt
							log.info("fupdate="+fupdate.getName()+",frtua="+frtua.getName());
							fupdate.renameTo(new File(fupdate.getAbsolutePath()+".txt"));
							frtua.renameTo(new File(frtua.getAbsolutePath()+".txt"));
							handleUpdate(content,rtuaList,batchId);
							setUpdatingFile();
						}
						break;
					}													
				}
			}
			
		}
		
	}

	public void setPacketLength(int packetLength) {
		this.packetLength = packetLength;
	}

	public int getPacketLength() {
		return packetLength;
	}

	public void setUpdateTimeout(int updateTimeout) {
		if( updateTimeout <=10 )
			updateTimeout = 120;
		this.updateTimeout = updateTimeout;
	}

	public void setDbService(AsyncService dbService) {
		this.dbService = dbService;
	}

	public void setDaoKey(int daoKey) {
		this.daoKey = daoKey;
	}

	public void setResendInterval(int resendInterval) {
		this.resendInterval = resendInterval;
	}

	public void setLoadDao(LoadUpdatingRtuDao loadDao) {
		this.loadDao = loadDao;
	}

	public void setEventHandler(FeEventHandler eventHandler) {
		this.eventHandler = eventHandler;
	}

	public void setEnableUpdateFlag(boolean enableUpdateFlag) {
		this.enableUpdateFlag = enableUpdateFlag;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

}
