package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGWAFN0FRequest;
import cn.hexing.fas.model.FaalRequest;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.gw.MessageGwHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.utils.FtpFileReader;
import cn.hexing.util.HexDump;


/**
 * 文件传输（AFN=0FH） 下行消息编码器
 * @author Administrator
 *
 */
public class C0FMessageEncoder  extends AbstractMessageEncoder {
	@SuppressWarnings("unused")
	private static Log log=LogFactory.getLog(C0FMessageEncoder.class);
	public IMessage[] encode(Object obj) {
		List<MessageGw> rt=new ArrayList<MessageGw>();		
		try{
			if(obj instanceof FaalRequest){
				FaalGWAFN0FRequest request=(FaalGWAFN0FRequest)obj;
				List<FaalRequestRtuParam> rtuParams=request.getRtuParams();
				String sDADT="",sValue="",sdata="",tp="",pw="";
				//时间标签包括：帧计数器+帧发送时间+允许传输延时时间
				if (request.getTpSendTime()!=null&&request.getTpTimeout()>0){
					tp="00"+DataItemCoder.constructor(request.getTpSendTime(),"A16")+DataItemCoder.constructor(""+request.getTpTimeout(),"HTB1");
				}
				for (FaalRequestRtuParam rp:rtuParams){
					//判断是发送文件升级请求还是补发请求
//					if(request.getOperator()!=null&&request.getOperator().equals("Reissue")){
//						//如果是补发请求，那么只需要将未发送的消息取出来，放到消息列表中再次发送
//						BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
//						if(rtu==null){
//							throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
//						}
//						int messagecount=rtu.getParamFromMessageCountMap(1);
//						for(int i=0;i<=messagecount;i++){
//							MessageGw msg=(MessageGw) rtu.getParamFromFileMap(i);
//							if(null!=msg){
//								rt.add(msg);
//							}
//						}
////						Iterator itor=rtu.getFilemessageMap().entrySet().iterator();
////						while(itor.hasNext()){
////							Map.Entry<Integer,MessageGw> entry=(Map.Entry<Integer,MessageGw>)itor.next();
////							rt.add(entry.getValue());
////						}
////						break;
//					break;
//					}
					//如果不是补发请求，那么就是升级文件请求，需要组帧下发
					int[] tn=rp.getTn();
					List<FaalRequestParam> params=rp.getParams();
						for (FaalRequestParam pm:params){
							String value=pm.getValue(); //文件传输value是41个字节的文件配置信息+IP地址+端口+userName+password+filePath+fileName
							String[] valueParams = value.split("#");
							ProtocolDataItemConfig pdc=(ProtocolDataItemConfig)super.dataConfig.getDataItemConfig(pm.getName());
							//获取配置文件中每一帧报文的数据长度 XXXX字节，定义dataLength为HEX字符串的长度，dataLength=字节数*2
							int dataLength=pdc.getLength()*2;
							//判断升级文件指令    00H：报文方式传输；01H：FTP方式传输；02H：启动组地址升级。
							if(request.getFileCommand().equals("01")){
								
							}else if(request.getFileCommand().equals("02")){
								
							}
							else{	//报文方式传输
								//通过主站传过来的FTP地址将文件读出来。
								FtpFileReader ftp = new FtpFileReader();
								String fileData="";
								if(request.getFileTag().endsWith("FC")){//白名单表地址下发,不用从ftp下载文件
									fileData=valueParams[1];
								} else{
									fileData=ftp.readFile(valueParams[1],Integer.parseInt(valueParams[2]),valueParams[3],valueParams[4],valueParams[5],request.getFileName());
								}
								if("".equals(fileData)){
									log.error("fileData is null!,can't creat any message。。。");
									throw new MessageEncodeException("fileData is null!,can't creat any message。。。");
								}
								//头文件41字节，主站传送过来的是39个字节，两个字节的4858（HX的ASCII码值）在这里添加进去。
								String fileDataHead="4858"+valueParams[0];
								fileData=fileDataHead+fileData;
								int len=fileData.length();
								//最后一帧要特殊处理，先下发n-1帧，报文数量为messageCouts(出最后一帧message的总数)
								int messageCouts=0;
								boolean isLast=false;
								// 最后一帧的长度
								int lastMessageLength=0;
								if(len%dataLength==0){
								 messageCouts=len/dataLength-1;
								 lastMessageLength=dataLength;
								 }else{
								 messageCouts=len/dataLength;
								 lastMessageLength=len-messageCouts*dataLength;
								 }
								sDADT=DataItemCoder.getCodeFrom1To1(0,pm.getName());//数据单元标识
									for(int i=0;i<messageCouts;i++){
										sdata="";
										String cdata="";
										cdata=getMessageDataHead(request, isLast, lastMessageLength,messageCouts+1, i,dataLength)+fileData.substring(0, dataLength);
										fileData=fileData.substring(dataLength);
										sdata=sDADT+cdata;
										BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
										if(rtu==null){
											throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
										}
										if (rtu.getHiAuthPassword()!=null&&rtu.getHiAuthPassword().length()==32)
											pw=DataSwitch.ReverseStringByByte(rtu.getHiAuthPassword());//终端密码
										else
											throw new MessageEncodeException("rtu HiAuthPassword is null");
										MessageGwHead head=new MessageGwHead();
										//设置报文头相关信息
										head.rtua=rtu.getRtua();
										MessageGw msg=new MessageGw();
										msg.head=head;
										msg.setAFN((byte)request.getType());
										msg.data=HexDump.toByteBuffer(sdata+pw);
										if (!tp.equals(""))//下行带时间标签则设置Aux
											msg.setAux(HexDump.toByteBuffer(tp), true);
										msg.setCmdId(rp.getCmdId());
										msg.setMsgCount(1);
										rt.add(msg);
										rtu.addParamToFileMap(i, msg);
									}
									//下发最后一帧
									sdata="";
									String cdata="";
									cdata=getMessageDataHead(request, true, lastMessageLength, messageCouts+1, messageCouts,dataLength)+fileData.substring(0, lastMessageLength);
									fileData=fileData.substring(lastMessageLength);
									sdata=sDADT+cdata;
									BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(rp.getRtuId()));
									if(rtu==null){
										throw new MessageEncodeException("终端信息未在缓存列表："+ParseTool.IntToHex4(rtu.getRtua()));
									}
									if (rtu.getHiAuthPassword()!=null&&rtu.getHiAuthPassword().length()==32)
										pw=DataSwitch.ReverseStringByByte(rtu.getHiAuthPassword());//终端密码
									else{
										log.warn("Terminal "+rp.getRtuId()+" hiAuthPassword is null,use default password.");
										pw="00000000000000000000000000000000";
									}
									MessageGwHead head=new MessageGwHead();
									//设置报文头相关信息
									head.rtua=rtu.getRtua();
									MessageGw msg=new MessageGw();
									msg.head=head;
									msg.setAFN((byte)request.getType());
									msg.data=HexDump.toByteBuffer(sdata+pw);
									if (!tp.equals(""))//下行带时间标签则设置Aux
										msg.setAux(HexDump.toByteBuffer(tp), true);
									msg.setCmdId(rp.getCmdId());
									msg.setMsgCount(1);
									rt.add(msg);
									rtu.addParamToFileMap(messageCouts, msg);
									rtu.addParamToMessageCountMap(1, messageCouts+1);
									//判断是发送文件升级请求还是补发请求
									if(request.getOperator()!=null&&request.getOperator().equals("Reissue")){
										int count=rtu.getParamFromCurrentMessageCountMap(1);
										for(int ii=0;ii<count;ii++){
											rtu.removeParamFromFileMap(ii);
											rt.remove(0);
										}
									}
								
							}
					  } 
				}				
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		if(rt!=null&&rt.size()>0){
			IMessage[] msgs=new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
        }
        return null;  
	}
	
//	public String getFileDataTitle(FaalGWAFN0FRequest request){
//		String fileDataTitle="";
//		//升级文件格式为41个字节的文件头和具体烧写码fileData
//		//升级文件标志固定2个字节HX的ASCII码值 0X48 0X58
//		//升级文件类型1个字节 request.getFileTag()
//		//对散列校验码加密的加密类型1个字节 00 不加密 01 AES－GCM-128加密
//		//校验类型：1字节； 定义（0：无校验；1：MD5；2：CRC16；）
//		//版本控制：20字节，不足后补00；一般用于文件名来指定适用表计的类型和版本。
//		//散列校验码：16字节，不足后补00，这里使用全0；
//		//TODO:表地址文件内容
//		if(request.getFileTag().endsWith("FE")){
//			fileDataTitle="4858"+request.getFileTag()+"00"+"01"+DataItemCoder.constructor(request.getSoftVersion(),"ASC20")+"02681464fde356a564f08b82c50c5329";
//		}//8933a1519149fe839b1e48305c89f6a7  02681464fde356a564f08b82c50c5329
//		else{
//			fileDataTitle="4858"+request.getFileTag()+"00"+"01"+DataItemCoder.constructor(request.getSoftVersion(),"ASC20")+"8933a1519149fe839b1e48305c89f6a7";
//		}
//		return fileDataTitle;
//	}
	/**
	 * 获取每帧数据的数据报文头
	 * @param request
	 * @param isLast 是否是最后一帧
	 * @param lastMessageLength  最后一帧数据长度（其他帧的数据长度是固定的）	
	 * @param count 数据总段数（每一段一个帧）
	 * @param currentCount  当前帧段数
	 * @param dataLength  当前每帧的数据长度字符长度
	 * @return	messageHead= 1字节文件标示+1字节文件属性+1字节文件指令+2字节总段数+4字节当前段数+当前段数的数据长度
	 */
	public String getMessageDataHead(FaalGWAFN0FRequest request,boolean isLast,int lastMessageLength,int count,int currentCount,int dataLength){
		String messageDataHead="";
		String sCount="";
		String sLastMessageLength="";
		String sCurrentCount="";
		String sDataLength="";
		sLastMessageLength=HexDump.toHex(lastMessageLength/2);
		sLastMessageLength=sLastMessageLength.substring(4);
		sCount=HexDump.toHex(count);
		sCount=sCount.substring(4);
		sCurrentCount=HexDump.toHex(currentCount);
		sDataLength=HexDump.toHex(dataLength/2);
		sDataLength=sDataLength.substring(4);
		//文件数据只有一帧，那么文件属性为00 起始 而不是01 结束
		if(1==count&&isLast){
			messageDataHead=request.getFileTag()+"00"+request.getFileCommand()+DataSwitch.ReverseStringByByte(sCount)
								+DataSwitch.ReverseStringByByte(sCurrentCount)+DataSwitch.ReverseStringByByte(sLastMessageLength);
		 
		return messageDataHead;
		}
		if(isLast){
			messageDataHead=request.getFileTag()+"01"+request.getFileCommand()+DataSwitch.ReverseStringByByte(sCount)
								+DataSwitch.ReverseStringByByte(sCurrentCount)+DataSwitch.ReverseStringByByte(sLastMessageLength);
		 }else{
			messageDataHead=request.getFileTag()+"00"+request.getFileCommand()+DataSwitch.ReverseStringByByte(sCount)
								+DataSwitch.ReverseStringByByte(sCurrentCount)+DataSwitch.ReverseStringByByte(sDataLength);
		 }
		return messageDataHead;
	}
}	
