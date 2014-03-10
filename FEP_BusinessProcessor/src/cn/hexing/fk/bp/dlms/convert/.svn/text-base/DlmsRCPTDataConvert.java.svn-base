package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.util.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;

/**
 * 
 * @author gaoll
 *
 * @time 2013-3-8 下午03:47:19
 *
 * @info 继电器控制时段表
 * 
 * array[max=12]
	{
	   structure
	   include
	   {
	      hour;       unsigned
	      minute;     unsigned
	      action; unsigned .(disconnect--0x00; connect--0x01)
	   }
	}
 */
public class DlmsRCPTDataConvert extends DlmsAbstractConvert{

	@Override
	public DlmsData downLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		String stringValue=data.getStringValue();
		String[] periods = stringValue.split("#");
		DlmsData result = new DlmsData();
		DlmsData[] arrays = new DlmsData[periods.length];
		for(int i=0;i<periods.length;i++){
			arrays[i] = new DlmsData();
			String period = periods[i];
			
			DlmsData[] datas =new DlmsData[3];
			String[] values=period.split(";");
			
			String strHour=values[0];
			int hour = Integer.parseInt(strHour);
			datas[0] = new DlmsData();
			datas[0].setUnsigned(hour);
			String strMin=values[1];
			int minute = Integer.parseInt(strMin);
			datas[1] = new DlmsData();
			datas[1].setUnsigned(minute);
			String strStatus=values[2];
			int status = Integer.parseInt(strStatus);
			datas[2] = new DlmsData();
			datas[2].setUnsigned(status);
			
			ASN1SequenceOf seq = new ASN1SequenceOf(datas);
			arrays[i].setStructure(seq);
		}
		result.setArray(arrays);
		return result;
	}

	@Override
	public String getStringValue(DlmsData[] arrays) throws IOException {
		
		StringBuilder sb = new StringBuilder();
		
		for(DlmsData result:arrays){
			ASN1SequenceOf asnStructure = result.getStructure();
			ASN1Type[] members = asnStructure.getMembers();
			int hour=((DlmsData) members[0]).getUnsigned();
			int minute =((DlmsData) members[1]).getUnsigned();
			int action = ((DlmsData) members[2]).getUnsigned();
			sb.append(hour+";"+minute+";"+action);
			sb.append("#");
		}
		if(sb.length()>0) sb.deleteCharAt(sb.length()-1);
		
		return sb.toString();
	}
	
	public static void main(String[] args) throws IOException {
		DlmsRCPTDataConvert rcpt = new DlmsRCPTDataConvert();
		DlmsData dd = new DlmsData();
		dd.setVisiableString("1;2;1#1;2;1");
		dd=rcpt.downLinkConvert(null, dd, null);
		System.out.println(HexDump.toHex(dd.encode()));
	}
	
	
	
}
