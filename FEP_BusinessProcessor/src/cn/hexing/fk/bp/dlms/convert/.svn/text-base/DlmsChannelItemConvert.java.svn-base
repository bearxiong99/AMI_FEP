package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;

/**
 * Dlms通道数据项的转换
 * classID+obis+attrId#classID+obis+attrId#classID+obis+attrId
 * 
 * 
 */
public class DlmsChannelItemConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{

	
	@Override
	public DlmsData upLinkConvert(DlmsRequest request,DlmsData reqData,DlmsObisItem param) throws IOException {
		
		DlmsData resultData = new DlmsData();
		StringBuilder sb = new StringBuilder();
		
		if(reqData.type()!=DlmsData.ARRAY) return reqData;
		
		DlmsData[] members = reqData.getArray();
		for(int j=1 ;j<members.length;j++){  //第一个是时间，不需要显示
			DlmsData dd = members[j];
			ASN1Type[] asnMems=dd.getStructure().getMembers();
			for(int i = 0; i < asnMems.length-1;i++){
				DlmsData data = (DlmsData) asnMems[i];
				if(i==0 || i==2){//classId | attrId
					sb.append(Integer.parseInt(data.getStringValue(),16)).append("#");
				}else{
					byte[] b=HexDump.toArray(data.getStringValue());
					for(int k = 0;k<b.length;k++){
						sb.append(Integer.parseInt(HexDump.toHex(b[k]),16)).append(".");
					}
					sb.setCharAt(sb.length()-1, '#');
				}
			}
			sb.setCharAt(sb.length()-1, ',');
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		resultData.setVisiableString(sb.toString());
		return resultData;
	}

}
