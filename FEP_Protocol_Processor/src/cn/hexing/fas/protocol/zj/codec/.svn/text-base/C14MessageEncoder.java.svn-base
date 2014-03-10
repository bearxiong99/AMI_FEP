package cn.hexing.fas.protocol.zj.codec;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.FaalGGKZM14Request;
import cn.hexing.fas.model.FaalRequestParam;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.MeasuredPoint;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.HexDump;

/**
 * 控制操作类(功能码：14H)响应消息编码器
 * 
 * @author Administrator
 * 
 */
public class C14MessageEncoder extends AbstractMessageEncoder {
	private static Log log = LogFactory.getLog(C14MessageEncoder.class);

	public IMessage[] encode(Object obj) {
		List<MessageZj> rt = null;
		try {
			if (obj instanceof FaalGGKZM14Request) {
				FaalGGKZM14Request para = (FaalGGKZM14Request) obj;
				rt = new ArrayList<MessageZj>();
				if (para.getRtuParams() != null) {
					// String
					// meterAddr=DataItemCoder.constructor(para.getMeterAddr(),"HEX6");;
					SimpleDateFormat df = new SimpleDateFormat("yyMMddHHmm");
					if (null == para.getDataTime()) {
						para.setDataTime(new Date());
					}
					String dataTime = df.format(para.getDataTime());
					
					String effectiveTime = HexDump.toHex((byte)para.getEffectiveTime());
					
					for (FaalRequestRtuParam frp : para.getRtuParams()) {
						if (frp.getRtuId() == null) {
							log.info("rtuId=" + frp.getRtuId());
							continue;
						}
						if (frp.getCmdId() == null) {
							log.info("cmdId=" + frp.getCmdId());
							continue;
						}
						BizRtu rtu = (RtuManage.getInstance()
								.getBizRtuInCache(frp.getRtuId()));
						if (rtu == null) {
							log.info("rtu can not find in cache,rtuId=："
									+ frp.getRtuId());
							continue;
						}
						// 取测量点号
						int tn[] = frp.getTn();
						String meterAddr = "";
						// 通过测量点号查找电表通信地址
						MeasuredPoint mp = rtu.getMeasuredPoint(String
								.valueOf(tn[0]));
						meterAddr = DataSwitch.ReverseStringByByte(mp
								.getTnAddr());
						String code = "", name = "", value = "";
						for (int i = 0; i < frp.getParams().size(); i++) {
							name = ((FaalRequestParam) frp.getParams().get(i))
									.getName();
							value = ((FaalRequestParam) frp.getParams().get(i))
									.getValue();
							if ("8031".equals(name)) {
								SimpleDateFormat sdf = new SimpleDateFormat(
										"yyyy-MM-dd HH:mm:ss");
								Date dvalue = sdf.parse(value);
								sdf = new SimpleDateFormat("yyMMddHHmmss");
								value = sdf.format(dvalue);
							}
							ProtocolDataItemConfig pdc = (ProtocolDataItemConfig) super.dataConfig
									.getDataItemConfig(name);
							if (pdc != null) {
								if (pdc.getParentCode() != null) // 测量点数据,主站使用内部标识
									code = code
											+ DataSwitch
													.ReverseStringByByte(pdc
															.getParentCode());
							}
							log.info("name=" + name + ",code=" + code);
						}
						String data = meterAddr + "11111111" + dataTime
								+ effectiveTime + code + (value==null?"":value);
						
						MessageZjHead head = createHead(rtu);
						head.dlen = (short) (data.length() / 2);
						MessageZj msg = new MessageZj();
						msg.setCmdId(frp.getCmdId());
						msg.data = HexDump.toByteBuffer(data);
						msg.head = head;
						msg.setMsgCount(1);
						rt.add(msg);
					}
				} else {
					throw new MessageEncodeException("未传值");
				}
			}
		} catch (Exception e) {
			throw new MessageEncodeException(e);
		}
		if (rt != null) {
			IMessage[] msgs = new IMessage[rt.size()];
			rt.toArray(msgs);
			return msgs;
		}
		return null;
	}

	private MessageZjHead createHead(BizRtu rtu) {
		// 帧头数据
		MessageZjHead head = new MessageZjHead();
		head.c_dir = 0; // 主站下发
		head.c_expflag = 0; // 异常码
		head.c_func = (byte) 0x14; // 功能码
		head.rtua = rtu.getRtua();
		head.iseq = 0; // 帧内序号
		return head;
	}
}
