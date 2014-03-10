/**
 * 厂家自定义报文的上行队列以及下行通道。
 * 
 */
package cn.hexing.fk.fe.userdefine;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.fe.ChannelManage;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;

/**
 *
 */
public class UserDefineMessageQueue {
	private static final Logger log = Logger.getLogger(UserDefineMessageQueue.class);
	private static final UserDefineMessageQueue instance = new UserDefineMessageQueue();
	private Map<Integer,IChannel> userMap = new HashMap<Integer,IChannel>();
	private UserDefineMessageQueue(){}
	
	public static final UserDefineMessageQueue getInstance(){
		return instance;
	}
	
	public void offer(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			//当GPRS网关收到用户自定义报文上行，则把报文直接发送给厂家解析模块
			//按照厂家编码，把报文推送给厂家升级模块。
			byte manuCode=zjmsg.head.msta;
			if((manuCode & 0xff)==0){	//高科、万盛特殊处理
		    	if (zjmsg.head.c_func == MessageConst.ZJ_FUNC_USER_DEFINE) {
		            manuCode = (byte)BCDToDecimal(zjmsg.data.get(0));
		        }
		    }
			log.info("收到厂家编码为"+manuCode+"的自定义上行报文:"+msg);
			IChannel srcChannel = userMap.get(new Integer(manuCode));
			if( null == srcChannel ){
				log.error("收到厂家自定义报文，但厂家解析模块与通信前置机的连接找不到。msg="+msg.getRawPacketString());
				return;
			}
			srcChannel.send(msg);
			//厂家自定义报文记录日志
			log.info("厂家自定义报文应答："+msg.getRawPacketString());
		}
		else if( msg.getMessageType() == MessageType.MSG_GW_10 ){
			MessageGw gwmsg = (MessageGw)msg;
			IChannel srcChannel = userMap.get(gwmsg.getRtua());
			if( null == srcChannel ){
				log.error("收到厂家升级报文，但厂家升级模块与通信前置机的连接找不到。msg="+msg.getRawPacketString());
				return;
			}
			srcChannel.send(msg);
			//厂家自定义报文记录日志
			log.info("厂家自定义报文应答："+msg.getRawPacketString());
		}
	}
	
	/**
	 * 厂家解析模块，连接到通信前置机的某个服务端口。
	 * 因此这里的MessageZj的source，一定是异步socket client对象。
	 * @param msg
	 * @return
	 */
	public boolean sendMessageDown(IMessage msg){
		if( msg.getMessageType() == MessageType.MSG_ZJ ){
			MessageZj zjmsg = (MessageZj)msg;
			log.info("收到厂家编码为"+zjmsg.head.msta+"的自定义下行报文:"+msg);
			//1. 按照下行的报文，管理厂家编码与厂家解析模块到通信前置机之间的clientChannel。
			IChannel srcChannel = (IChannel)msg.getSource();
			int msta = 0xFF & zjmsg.head.msta; 
			userMap.put(msta, srcChannel);
			//2.取该终端是否允许远程升级标志
//			if (RtuManage.getInstance().getRemoteUpateRtuaTag(zjmsg.head.rtua)){
//			}	
			//3. 选择网关通道，直接下行。
			IChannel channel = ChannelManage.getInstance().getGPRSChannel(zjmsg.getLogicalAddress());
			if( null == channel )
				return false;
			//短信终端不支持自动升级
			return channel.send(msg);
		}
		else if( msg.getMessageType() == MessageType.MSG_GW_10 ){
			MessageGw gwmsg = (MessageGw)msg;
			//1. 按照下行的报文，管理RTUA与厂家解析模块到通信前置机之间的clientChannel。
			IChannel srcChannel = (IChannel)msg.getSource();
			userMap.put(gwmsg.getRtua(), srcChannel);
			//2. 选择网关通道，直接下行。
			IChannel channel = ChannelManage.getInstance().getGPRSChannel(gwmsg.getLogicalAddress());
			if( null == channel )
				return false;
			//短信终端不支持自动升级
			channel.send(msg);
			return true;
		}
		return false;
	}
	
	
	/**
	 * 1字节BCD转化为十进制
	 * @param bcd
	 * @return 无效数据返回负数
	 */
	public static int BCDToDecimal(byte bcd){
		int high=(bcd & 0xf0)>>>4;
		int low=(bcd & 0xf);
		if(high>9 || low>9){
			return -1;
		}
		return high*10+low;
	}
}
