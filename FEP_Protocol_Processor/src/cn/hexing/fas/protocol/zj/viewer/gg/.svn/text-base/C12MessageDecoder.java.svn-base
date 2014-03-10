package cn.hexing.fas.protocol.zj.viewer.gg;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.conf.ProtocolDataItemConfig;
import cn.hexing.fas.protocol.data.DataMappingZJ;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.parse.DataItemParser;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.utils.HexDump;

public class C12MessageDecoder extends AbstractMessageDecoder {
	private static Log log = LogFactory.getLog(C12MessageDecoder.class);

	public Object decode(IMessage message) {
		HostCommand hc = new HostCommand();

		try {
			if (ParseTool.getOrientation(message) == DataMappingZJ.ORIENTATION_TO_APP) { // 是终端应答
				// 应答类型
				int rtype = (ParseTool.getErrCode(message));
				if (rtype == DataMappingZJ.ERROR_CODE_OK) { // 正常终端应答
					hc.setStatus(HostCommand.STATUS_SUCCESS);
					// 取应答数据
					String data = ParseTool.getDataString(message);
					if (data != null && data.length() >= 42) {
						String code = data.substring(0, 4);
						int datakey = Integer.parseInt(code, 16);
						ProtocolDataItemConfig dic = getDataItemConfig(datakey);
						int dlen = dic.getLength();
						data = data.substring(14); // 2字节选项域S+5字节时间
													// 将这7个字节取出来，剩下的都是数据区：包含有每一块电表的数据。
						// 电表数据格式： 6字节电表编号 + 1字节数据长度 +2字节电笔状态字+ 5字节数据时标 +
						// dlen字节数据块内容
						if (datakey == 2049) {
							dlen = 20; // 预付费数据，实际值是16个字节，但另外四个字节也是打包送上来的，那四个字节移除。
						}
						int len = (6 + 1 + 2 + 5 + dlen) * 2;
						byte[] datas = null;
						String sdata = "";
						String meterNo = "";
						int count = data.length() / len; // 数据区中带有count块电表的数据
						for (int i = 0; i < count; i++) {
							List<HostCommandResult> value = new ArrayList<HostCommandResult>();
							String stime = null;
							sdata = data.substring(0, len);
							meterNo = DataSwitch.ReverseStringByByte(sdata
									.substring(0, 12));
							datas = HexDump.toArray(sdata);
							stime = sdata.substring(18, 28);
							int index = 14;
							while (index < datas.length) {
								if (2 < (datas.length - index)) { // 至少要有3字节数据（2字节数据标示+至少1字节数据）
									if (dic != null) {
										int loc = index;
										int itemlen = 0;
										itemlen = parseBlockData(datas, loc,
												dic, meterNo, value);
										loc += itemlen;
										index = loc;
									} else {
										// 不支持的数据
										log.info("不支持的数据:"
												+ ParseTool.IntToHex(datakey));
										break; // 高科的任务数据比较特殊，暂时做如此处理
									}
								} else {
									// 错误帧数据
									throw new MessageDecodeException("帧数据太少");
								}
							}
							SimpleDateFormat sdf = new SimpleDateFormat(
									"yyMMddHHmm");
							Date time = sdf.parse(stime);
							sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:Ss");
							stime = sdf.format(time);
							for (HostCommandResult result : value) {
								result.setValue(stime + "#" + result.getValue());
								hc.addResult(result);
							}
							data = data.substring(68); // 任务格式固定
						}
					}
				}
			}
		} catch (Exception e) {
			throw new MessageDecodeException(e);
		}
		return hc;

	}

	/**
	 * 解析块数据
	 * 
	 * @param data
	 *            数据帧
	 * @param loc
	 *            解析开始位置
	 * @param pdc
	 *            数据项配置
	 * @param points
	 *            召测的测量点数组
	 * @param pnum
	 *            召测的测量点个数
	 * @param result
	 *            结果集合
	 */
	private int parseBlockData(byte[] data, int loc,
			ProtocolDataItemConfig pdc, String point,
			List<HostCommandResult> result) {
		int rt = 0;
		try {
			List children = pdc.getChildItems();
			int index = loc;
			if ((children != null) && (children.size() > 0)) { // 数据块召测
				for (int i = 0; i < children.size(); i++) {
					ProtocolDataItemConfig cpdc = (ProtocolDataItemConfig) children
							.get(i);
					int dlen = parseBlockData(data, index, cpdc, point, result);
					index += dlen;
					rt += dlen;
				}
			} else {
				int dlen = parseItem(data, loc, pdc, point, result);
				rt += dlen;
			}
		} catch (Exception e) {
			throw new MessageDecodeException(e);
		}
		return rt;
	}

	private int parseItem(byte[] data, int loc, ProtocolDataItemConfig pdc,
			String point, List<HostCommandResult> result) {
		int rt = 0;
		try {
			int itemlen = 0;
			itemlen = pdc.getLength();
			if (itemlen <= (data.length - loc)) { // 有足够数据
				Object di = DataItemParser.parsevalue(data, loc, itemlen,
						pdc.getFraction(), pdc.getParserno());
				HostCommandResult hcr = new HostCommandResult();
				hcr.setCode(pdc.getCode());
				if (di != null) {
					hcr.setValue(di.toString());
				}
				hcr.setMeterAddr(point);
				result.add(hcr);
				rt = itemlen;
			} else {
				// 错误数据
				if ((data.length - loc) == 0) {
					// 没有更多字节解析，可能是终端中块数据不全，或者数据丢失

				} else {
					throw new MessageDecodeException("错误数据长度，数据项："
							+ pdc.getCode() + " 期望数据长度：" + itemlen + " 解析长度："
							+ (data.length - loc));
				}
			}
		} catch (Exception e) {
			throw new MessageDecodeException(e);
		}
		return rt;
	}

	private ProtocolDataItemConfig getDataItemConfig(int datakey) {
		return super.dataConfig.getDataItemConfig(ParseTool.IntToHex(datakey));
	}
}
