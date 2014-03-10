package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * 
 * @author gaoll
 *
 * @time 2013-2-27 下午01:07:03
 *
 * @info 继电器阀值表解析
 * 
 *   OCTS
 * 	   时段数;   U8
 *	   时 1;    BCD(1)
 *	   分 1;  BCD(1)
 *	  阀值 1; U16
 *	   …… max number = 24
 *	C401060009050100000258
 */
public class DlmsRelayThresholdConvert extends DlmsAbstractConvert{

	@Override
	public DlmsData upLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		IDlmsScaleConvert convert = DlmsScaleManager.getInstance().getConvert(request.getSubprotocol(), 71, "0.0.17.0.0.255", 3);
		
		if(convert==null){
			convert = new DlmsScaleItem();
		}
		
		byte[] value=data.getOctetString();
		int count=value[0];
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<count;i++){
			int offset = i*4+1;
			byte[] thresholdTable =new byte[4];
			System.arraycopy(value, offset, thresholdTable, 0, 4);
			byte[] threshold = new byte[2];
			System.arraycopy(thresholdTable, 2, threshold, 0, 2);
			DlmsData thresholdData = new DlmsData();
			thresholdData.setUnsignedLong(threshold);
			thresholdData=convert.upLinkConvert(request, thresholdData, null);
			sb.append(HexDump.toHex(thresholdTable[0])).append(":");
			sb.append(HexDump.toHex(thresholdTable[1])).append("@");
			sb.append(thresholdData.getStringValue()).append(";");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		data.setVisiableString(sb.toString());
		return data;
	}
	
	public static void main(String[] args) throws IOException {
		String frame = "C40106000909020600003218000064";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsRelayThresholdConvert demand = new DlmsRelayThresholdConvert();
		demand.upLinkConvert(new DlmsRequest(), resutlData, null);
	}

}
