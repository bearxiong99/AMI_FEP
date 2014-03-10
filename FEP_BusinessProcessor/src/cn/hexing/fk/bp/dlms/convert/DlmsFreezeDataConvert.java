package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-22 下午1:17:09
 *
 * @info 冻结数据 解析
 */
public class DlmsFreezeDataConvert extends DlmsAbstractConvert{
	
	@Override
	public String getStringValue(DlmsData[] arrays,DlmsRequest req) throws IOException{
		
		StringBuilder sb = new StringBuilder();
		IDlmsScaleConvert convert = DlmsScaleManager.getInstance().getConvert(req.getSubprotocol(), 3, "1.0.1.8.0.255", 2);
		//电量值,由于这里考虑到量纲的问题,此处从数据库获得正向有功总的量纲,其他的电量默认按照这个来。
		if(convert==null){
			convert = new DlmsScaleItem();
		}
		for(DlmsData result:arrays){
			ASN1SequenceOf asnStructure = result.getStructure();
			ASN1Type[] members = asnStructure.getMembers();
			for(int i = 0 ; i <members.length;i++ ){
				DlmsData data=(DlmsData) members[i];
				if(i==0){
					//冻结时间
					sb.append(data.getDateTime().toString()).append("#");
				}else{
					data=convert.upLinkConvert(req, data, null);
					sb.append(data.getStringValue()).append("#");
				}
			}
			sb.setCharAt(sb.length()-1, ';');
		}
		if(sb.length()>0) sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		String frame = "C4010E0001010205090C056F080FFF17380000800001060000500C06000096D6060000011306000001B2";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsFreezeDataConvert demand = new DlmsFreezeDataConvert();
		demand.upLinkConvert(new DlmsRequest(), resutlData, null);
		
	}
}
