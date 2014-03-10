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
			if (ParseTool.getOrientation(message) == DataMappingZJ.ORIENTATION_TO_APP) { // ���ն�Ӧ��
				// Ӧ������
				int rtype = (ParseTool.getErrCode(message));
				if (rtype == DataMappingZJ.ERROR_CODE_OK) { // �����ն�Ӧ��
					hc.setStatus(HostCommand.STATUS_SUCCESS);
					// ȡӦ������
					String data = ParseTool.getDataString(message);
					if (data != null && data.length() >= 42) {
						String code = data.substring(0, 4);
						int datakey = Integer.parseInt(code, 16);
						ProtocolDataItemConfig dic = getDataItemConfig(datakey);
						int dlen = dic.getLength();
						data = data.substring(14); // 2�ֽ�ѡ����S+5�ֽ�ʱ��
													// ����7���ֽ�ȡ������ʣ�µĶ�����������������ÿһ��������ݡ�
						// ������ݸ�ʽ�� 6�ֽڵ���� + 1�ֽ����ݳ��� +2�ֽڵ��״̬��+ 5�ֽ�����ʱ�� +
						// dlen�ֽ����ݿ�����
						if (datakey == 2049) {
							dlen = 20; // Ԥ�������ݣ�ʵ��ֵ��16���ֽڣ��������ĸ��ֽ�Ҳ�Ǵ���������ģ����ĸ��ֽ��Ƴ���
						}
						int len = (6 + 1 + 2 + 5 + dlen) * 2;
						byte[] datas = null;
						String sdata = "";
						String meterNo = "";
						int count = data.length() / len; // �������д���count���������
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
								if (2 < (datas.length - index)) { // ����Ҫ��3�ֽ����ݣ�2�ֽ����ݱ�ʾ+����1�ֽ����ݣ�
									if (dic != null) {
										int loc = index;
										int itemlen = 0;
										itemlen = parseBlockData(datas, loc,
												dic, meterNo, value);
										loc += itemlen;
										index = loc;
									} else {
										// ��֧�ֵ�����
										log.info("��֧�ֵ�����:"
												+ ParseTool.IntToHex(datakey));
										break; // �߿Ƶ��������ݱȽ����⣬��ʱ����˴���
									}
								} else {
									// ����֡����
									throw new MessageDecodeException("֡����̫��");
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
							data = data.substring(68); // �����ʽ�̶�
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
	 * ����������
	 * 
	 * @param data
	 *            ����֡
	 * @param loc
	 *            ������ʼλ��
	 * @param pdc
	 *            ����������
	 * @param points
	 *            �ٲ�Ĳ���������
	 * @param pnum
	 *            �ٲ�Ĳ��������
	 * @param result
	 *            �������
	 */
	private int parseBlockData(byte[] data, int loc,
			ProtocolDataItemConfig pdc, String point,
			List<HostCommandResult> result) {
		int rt = 0;
		try {
			List children = pdc.getChildItems();
			int index = loc;
			if ((children != null) && (children.size() > 0)) { // ���ݿ��ٲ�
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
			if (itemlen <= (data.length - loc)) { // ���㹻����
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
				// ��������
				if ((data.length - loc) == 0) {
					// û�и����ֽڽ������������ն��п����ݲ�ȫ���������ݶ�ʧ

				} else {
					throw new MessageDecodeException("�������ݳ��ȣ������"
							+ pdc.getCode() + " �������ݳ��ȣ�" + itemlen + " �������ȣ�"
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
