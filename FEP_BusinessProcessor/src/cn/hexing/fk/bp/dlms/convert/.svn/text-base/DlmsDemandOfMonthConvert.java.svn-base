package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;
import cn.hexing.fk.bp.dlms.util.DlmsConstant;
import cn.hexing.fk.utils.DateConvert;
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
 * @time 2012-11-19 上午10:51:48
 *
 * @info 月冻结最大需量及发生时间
 * 
 * @format 冻结时间#最大需量#最大需量发生时间(多个)  或者 冻结时间#最大需量   需量包量纲的解析，取的量纲是arrayItem第一个数据项 的值
 */
public class DlmsDemandOfMonthConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{
	
	@Override
	public String getStringValue(DlmsData[] results,DlmsRequest req,DlmsObisItem item) throws IOException{
		StringBuilder sb = new StringBuilder();
		for(DlmsData result:results){
			ASN1SequenceOf asnStructure = result.getStructure();
			ASN1Type[] members = asnStructure.getMembers();
			for(int i = 0 ; i <members.length;i++ ){
				DlmsData data=(DlmsData) members[i];
				String strValue="";
				if(i==0){
					//冻结时间
					strValue = data.getDateTime().toString()+"#";
				}else{
					//最大需量及发生时间 
					strValue = HexDump.toHex(data.getValue());
					if(strValue.length()==4*2){
						String bcd = strValue.substring(0,8);
						bcd=getDemand(bcd,req,item);
						strValue = bcd+"#";
					}else{
						String bcd = strValue.substring(0,6);
						String time= strValue.substring(6);
						bcd=getDemand(bcd,req,item);
						time=getDemandTime(time);
						strValue = bcd+"#"+time+"#";						
					}
				}
				sb.append(strValue);
			}
			sb.setCharAt(sb.length()-1, ';');
		}
		if(sb.length()>0) sb.deleteCharAt(sb.length()-1);
		return sb.toString();
	}
	/**
	 * 获得最大需量发生时间
	 * yyMMddhhmmss
	 * @param time  9107251157
	 * @return yyyy-MM-dd HH:mm:ss
	 */
	private String getDemandTime(String time) {
		if("0000000000".equals(time)) return "";
		String allTime =time.substring(0,6)+"01"+time.substring(6)+"00";
		time=DlmsConstant.getInstance().isIranTime?DateConvert.TOU_IRANToGregorian(allTime):allTime;
		time = time.substring(0, 6)+time.substring(8);
		SimpleDateFormat sdf = new SimpleDateFormat("yyMMddHHmmss");
		Date date = null;
		try {
			 date= sdf.parse(time);
		} catch (ParseException e) {
			//e.printStackTrace();
			return "";
		}
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		time=sdf.format(date);
		time = DlmsConstant.getInstance().isIranTime?DateConvert.gregorianToIran(time):time;
		return time;
	}
	/**
	 * 获得需量值
	 * @param bcd
	 * @param req
	 * @return
	 * @throws IOException
	 */
	private String getDemand(String bcd, DlmsRequest req,DlmsObisItem item) throws IOException {
		IDlmsScaleConvert convert = null;
		if(item!=null){
			convert = DlmsScaleManager.getInstance().getConvert(req.getSubprotocol(), item.classId, item.obisString, item.attributeId);
			if(convert!=null && convert instanceof DlmsAbstractConvert){
				String structs=((DlmsAbstractConvert)convert).getArrayStructItems();
				if(structs!=null){
					String[] items = structs.split("#");
					if(items.length>2){
						convert = DlmsScaleManager.getInstance().getConvert((req.getSubprotocol()==null?"":req.getSubprotocol()+".")+items[1]);
					}
				}
			}
		}else{
			log.warn("getDemand item is null.can't find convert");
		}
		//这里需量查询量纲是查找当前大类的量纲
		if(convert==null){
			convert = new DlmsScaleItem();
		}
		String frame = "C40101000D"+HexDump.toHex((byte)(bcd.length()/2))+bcd;
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		resutlData=convert.upLinkConvert(req, resutlData, null);
		return resutlData.getStringValue();
	}

	public static void main(String[] args) throws IOException {
		String frame = "C4014D000101020A090C07DD0701FF000000008000010D0800058713062815280D0800058713062815280D0800001513060419120D0800000000000000000D0800000000000000000D0800000000000000000D0800000000000000000D0800000000000000000D080000000000000000";
		GetResponse gr = new GetResponse();
		gr.decode(new DecodeStream(HexDump.toArray(frame)));
	
		
		GetResponseNormal result= (GetResponseNormal) gr.getDecodedObject();
		DlmsData resutlData=result.getResult().getData();
		DlmsDemandOfMonthConvert demand = new DlmsDemandOfMonthConvert();
		demand.upLinkConvert(new DlmsRequest(), resutlData, null);
	}

}
