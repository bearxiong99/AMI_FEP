package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1Type;
import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.get.GetResponseNormal;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-25 下午06:16:09
 *
 * @info 谐波解析
 * Structure
	{
	C相总电压 12 xx xx；
	C相3次谐波电压 12 xx xx；
	C相5次谐波电压 12 xx xx;
	…
	C相19洗谐波电压 12 xx xx;
	C相21洗谐波电压 12 xx xx;
	}
 */
public class DlmsHarmonicDataConvert extends DlmsAbstractConvert{

	@Override
	public DlmsData upLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		

		IDlmsScaleConvert convert = DlmsScaleManager.getInstance().getConvert(request.getSubprotocol(), item.classId, item.obisString, item.attributeId);
		
		if(convert==null){
			convert = new DlmsScaleItem();
		}
		
		ASN1Type[] members = (ASN1Type[]) data.getStructure().getMembers();
		int scale=getScale(convert,request);
		StringBuilder sb = new StringBuilder();
		DlmsScaleItem si = new DlmsScaleItem();
		si.scale=scale;
		si.callingDataType=DlmsDataType.FLOAT64;
		for(ASN1Type member:members){
			DlmsData value=(DlmsData) member;
			value=si.upLinkConvert(request, value, item);
			sb.append(value.getStringValue()).append("#");
		}
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		data.setVisiableString(sb.toString());
		return data;
	}
	
	public static void main(String[] args) throws IOException {
		String frame = "C4010E000206120001120002120003120004120005120006";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsHarmonicDataConvert demand = new DlmsHarmonicDataConvert();
		demand.upLinkConvert(new DlmsRequest(), resutlData, new DlmsObisItem());
	}
	

}
