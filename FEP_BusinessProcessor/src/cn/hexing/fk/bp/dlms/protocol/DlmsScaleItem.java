package cn.hexing.fk.bp.dlms.protocol;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.ASN1Type;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.DlmsDateTime;

public class DlmsScaleItem implements IDlmsScaleConvert {
	private static final Logger log = Logger.getLogger(DlmsScaleItem.class);
	// All attributes use protected just for convenient access members by
	// scale-item-manager.
	protected static final HashMap<String, Integer> typeMap = new HashMap<String, Integer>();

	public static void initTypes() {
		typeMap.put("bcd", DlmsDataType.BCD);
		typeMap.put("array_struct", DlmsDataType.ARRAY_STRUCT);
		typeMap.put("array_structure", DlmsDataType.ARRAY_STRUCT);
		typeMap.put("bool", DlmsDataType.BOOL);
		typeMap.put("double_long", DlmsDataType.DOUBLE_LONG);
		typeMap.put("int", DlmsDataType.DOUBLE_LONG);
		typeMap.put("double_long_unsigned", DlmsDataType.DOUBLE_LONG_UNSIGNED);
		typeMap.put("u32", DlmsDataType.DOUBLE_LONG_UNSIGNED);
		typeMap.put("u8", DlmsDataType.UNSIGNED);
		typeMap.put("u16", DlmsDataType.UNSIGNED_LONG);
		typeMap.put("enum", DlmsDataType.ENUM);
		typeMap.put("float", DlmsDataType.FLOAT32);
		typeMap.put("float32", DlmsDataType.FLOAT32);
		typeMap.put("float64", DlmsDataType.FLOAT64);
		typeMap.put("double", DlmsDataType.FLOAT64);
		typeMap.put("byte", DlmsDataType.INTEGER);
		typeMap.put("short", DlmsDataType.LONG);
		typeMap.put("long", DlmsDataType.LONG64);
		typeMap.put("int64", DlmsDataType.LONG64);
		typeMap.put("byte[]", DlmsDataType.OCTET_STRING);
		typeMap.put("octet", DlmsDataType.OCTET_STRING);
		typeMap.put("octet_raw", DlmsDataType.OCTET_RAW);
		typeMap.put("struct", DlmsDataType.STRUCTURE);
		typeMap.put("structure", DlmsDataType.STRUCTURE);
		typeMap.put("string", DlmsDataType.VISIABLE_STRING);
		typeMap.put("compact_array", DlmsDataType.COMPACT_ARRAY);
		typeMap.put("time",DlmsDataType.TIME);
		typeMap.put("date",DlmsDataType.DATE);
		typeMap.put("date_time",DlmsDataType.DATE_TIME);
	}

	public String id;
	public String subProtocol = "0";
	public int classId = -1;
	public String obis;
	public int attrId = -1;
	public int scale = 0;
	/**表类型-量纲*/
	public Map<String,Integer> multiScale = new HashMap<String, Integer>();
	
	public int dlmsDataType;
	public int callingDataType;
	public int arrayStructLen = -1;
	public String arrayStructItems = null;
	public String customizeClass = null;

	protected ArrayList<DlmsScaleItem> subHandlers = null; // Sub-protocol
															// items.
	protected IDlmsScaleConvert customizer = null;

	public String itemKey() {
		return classId + "." + obis + "." + attrId;
	}

	private double multiplyScale(boolean downLink,int tempScale) {
		if (tempScale == 0)
			return 1;
		int sc = tempScale;
		if (downLink)
			sc = -1 * sc;
		int amp = 1;
		int counts = Math.abs(sc);
		for (int i = 0; i < counts; i++)
			amp = amp * 10;
		return sc > 0 ? amp : 1.0 / amp;
	}

	private DlmsData doConvert(DlmsRequest request, DlmsData srcData,DlmsObisItem param, int destType, boolean downLink)
			throws IOException {
		DlmsData destData = new DlmsData();
		
		int tempScale = getScaleValue(request);
			
		//带有小数位的格式化
		NumberFormat nf = NumberFormat.getInstance(); 
		nf.setGroupingUsed(false);
		nf.setMinimumFractionDigits(Math.abs(tempScale));
		
		switch (srcData.getDataType()) {
		case DlmsDataType.NULL:
		case DlmsDataType.DONT_CARE:
			return srcData;
		case DlmsDataType.BOOL:
			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Boolean.toString(srcData.getBool()));
			} else if (destType == DlmsDataType.UNSIGNED) {
				destData.setUnsigned(srcData.getBool() ? 1 : 0);
			} else
				destData = srcData;
			return destData;
		case DlmsDataType.BCD: {
			byte[] bcd = srcData.getBcd();
			int val = 0, b = 0;
			for (int i = 0; i < bcd.length; i++) {
				b = (bcd[i] >> 4 & 0x0F) * 10 + (bcd[i] & 0x0F);
				val = val * 100 + b;
			}
			double dbl =val;
			if (tempScale != 0)
				dbl = multiplyScale(downLink,tempScale) * val;
			if (destType == DlmsDataType.VISIABLE_STRING) {
				String str = tempScale != 0 ? Double.toString(dbl) : Integer.toString(val);
				destData.setVisiableString(str);
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				destData.setDoubleLong(val);
			} else if (destType == DlmsDataType.FLOAT64 || (!downLink && tempScale<0)) {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				}
				destData.setVisiableString(nf.format(dbl));
			} else
				destData.setBcd(val);
			return destData;
		}
		case DlmsDataType.INTEGER:
			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Integer.toString(srcData
						.getDlmsInteger()));
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				destData.setDoubleLong(srcData.getDlmsInteger());
			} else
				destData = srcData;
			return destData;
		case DlmsDataType.UNSIGNED:
		case DlmsDataType.ENUM:
			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Integer.toString(srcData
						.getUnsigned()));
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				destData.setDoubleLong(srcData.getUnsigned());
			} else
				destData = srcData;
			return destData;
		case DlmsDataType.LONG: {
			int val = srcData.getDlmsLong();
			double dbl = val;
			if (tempScale != 0)
				dbl = val * multiplyScale(downLink,tempScale);
			if (destType == DlmsDataType.VISIABLE_STRING) {
				if (tempScale != 0)
					destData.setVisiableString(Double.toString(dbl));
				else
					destData.setVisiableString(Integer.toString(val));
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				if (tempScale != 0)
					destData.setDoubleLong((int) dbl);
				else
					destData.setDoubleLong(val);
			} else if (destType == DlmsDataType.FLOAT64 || (!downLink && tempScale<0)) {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				}
				destData.setVisiableString(nf.format(dbl));
			} else {
				destData.setDlmsLong((int) dbl);
			}
			return destData;
		}
		case DlmsDataType.UNSIGNED_LONG: {
			int val = srcData.getUnsignedLong();
			double dbl = val;
			if (tempScale != 0)
				dbl = val * multiplyScale(downLink,tempScale);
			if (destType == DlmsDataType.VISIABLE_STRING) {
				String str = tempScale != 0 ? Double.toString(dbl) : Integer
						.toString(val);
				destData.setVisiableString(str);
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				destData.setDoubleLong((int) dbl);
			} else if (destType == DlmsDataType.FLOAT64 || (!downLink && tempScale<0)) {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				}
				destData.setVisiableString(nf.format(dbl));
			} else
				destData.setUnsignedLong((int) dbl);
			return destData;
		}
		case DlmsDataType.DOUBLE_LONG:
		case DlmsDataType.DOUBLE_LONG_UNSIGNED: {
			int val = srcData.getDoubleLong();
			double dbl = val;
			if (tempScale != 0)
				dbl = val * multiplyScale(downLink,tempScale);
			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Integer.toHexString(val));
			} else if (destType == DlmsDataType.FLOAT64 || (!downLink && tempScale<0)) {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				} 
				destData.setVisiableString(nf.format(dbl));
			} else if (destType == DlmsDataType.DOUBLE_LONG_UNSIGNED) {
				destData.setDoubleLongUnsigned((long) dbl);
			} else
				destData.setDoubleLong((int) dbl);
			return destData;
		}
		case DlmsDataType.FLOATING_POINT:
		case DlmsDataType.FLOAT32: {
			double dbl = srcData.getFloat32() * multiplyScale(downLink,tempScale);

			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Double.toString(dbl));
			} else if (destType == DlmsDataType.DOUBLE_LONG) {
				destData.setDoubleLong((int) dbl);
			} else if (destType == DlmsDataType.FLOAT64) {
				destData.setFloat64(dbl);
			} else if (destType ==DlmsDataType.FLOAT32){
				destData.setFloat32((float) dbl);
			}else {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				} 
				destData.setVisiableString(nf.format(dbl));
			}
			return destData;
		}
		case DlmsDataType.LONG64:
		case DlmsDataType.UNSIGNED64: {
			long val = srcData.getLong64();
			double dbl = val;
			if (tempScale != 0)
				dbl = val * multiplyScale(downLink,tempScale);
			if (destType == DlmsDataType.VISIABLE_STRING) {
				String str = tempScale != 0 ? Double.toString(dbl) : Long
						.toString(val);
				destData.setVisiableString(str);
			} else if (destType == DlmsDataType.FLOAT64 || (!downLink && tempScale<0)) {
				if(tempScale<0){
					BigDecimal   bd   =   new   BigDecimal(dbl); 
					dbl=bd.setScale(Math.abs(tempScale),   BigDecimal.ROUND_HALF_UP).doubleValue(); 
				}
				destData.setVisiableString(nf.format(dbl));
			} else if (destType == DlmsDataType.UNSIGNED64)
				destData.setUnsigned64((long) dbl);
			else
				destData.setLong64((long) dbl);
			return destData;
		}
		case DlmsDataType.FLOAT64: {
			double dbl = srcData.getFloat64() * multiplyScale(downLink,tempScale);
			if (destType == DlmsDataType.VISIABLE_STRING) {
				destData.setVisiableString(Double.toString(dbl));
			} else if (destType == DlmsDataType.LONG64) {
				destData.setLong64((long) dbl);
			} else
				destData.setFloat64(dbl);
			return destData;
		}
		case DlmsDataType.OCTET_STRING:
			if(destType ==DlmsDataType.DATE_TIME|| destType ==DlmsDataType.DATE|| destType ==DlmsDataType.TIME){
				if(downLink){
					return srcData;
				}else{
					return stringToDlmsData(request, srcData, param, destType, downLink);
				}
			}
			if(destType==DlmsDataType.OCTET_STRING){
				return srcData;
			}else{
				return stringToDlmsData(request,srcData, param,destType, downLink);
			}
		case DlmsDataType.VISIABLE_STRING:
			return stringToDlmsData(request,srcData,param, destType, downLink);
		case DlmsDataType.BIT_STRING:
			destData = srcData;
			break;
		case DlmsDataType.ARRAY:
		case DlmsDataType.STRUCTURE: {
			if (destType == DlmsDataType.ARRAY
					|| destType == DlmsDataType.STRUCTURE
					|| destType == DlmsDataType.VISIABLE_STRING) {
				if (null == arrayStructItems || arrayStructItems.length() == 0 || downLink){
					destData = srcData;
					break;
				}
				String[] itemKeys = arrayStructItems.split("#");
				DlmsData[] _members=null;
				if(srcData.getDataType() == DlmsDataType.ARRAY){
					DlmsData[] array = srcData.getArray();
					if(array.length==0) {
						destData = srcData;
						break;
					}
					ASN1Type[] members = array[0].getStructure().getMembers();
					_members = new DlmsData[members.length];
					for(int i = 0;i<_members.length;i++){
						_members[i] = (DlmsData) members[i];
					}
				}else{
					ASN1Type[] members = srcData.getStructure()
							.getMembers();
					_members = new DlmsData[members.length];
					for(int i = 0;i<_members.length;i++){
						_members[i] = (DlmsData) members[i];
					}
				}
				
				if (itemKeys.length != _members.length) {
					String errInfo = "Can not convert string to struture. config="
							+ arrayStructItems
							+ ",data.length="
							+ _members.length;
					log.error(errInfo);
					throw new RuntimeException(errInfo);
				}
				
				if(destType == DlmsDataType.VISIABLE_STRING && srcData.type()==DlmsDataType.ARRAY){
					arrayToStringValue(itemKeys,srcData,destData,request,param);
					break;
				}
				
				for (int i = 0; i < itemKeys.length; i++) {
					IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
							.getConvert(itemKeys[i]);
					if (downLink)
						_members[i] = cvt.downLinkConvert(request,_members[i],param);
					else
						_members[i] = cvt.upLinkConvert(request,_members[i],param);
				}
				if (destType == DlmsDataType.ARRAY)
					destData.setArray(_members);
				else if (destType == DlmsDataType.STRUCTURE) {
					ASN1SequenceOf seqof = new ASN1SequenceOf(_members);
					destData.setStructure(seqof);
				} else {
					StringBuilder sb = new StringBuilder();
					for (int i = 0; i < _members.length; i++) {
						if (i > 0)
							sb.append("#");
						if(_members[i].type()==0){
							sb.append("FF");
						}else{
							sb.append(_members[i].getStringValue());
						}
					}
					destData.setVisiableString(sb.toString());
				}
			} else
				destData = srcData;
			break;
		}
		case DlmsDataType.ARRAY_STRUCT: {
			if (destType == DlmsDataType.ARRAY
					|| destType == DlmsDataType.VISIABLE_STRING) {
				if (null == arrayStructItems || arrayStructItems.length() == 0)
					throw new RuntimeException(
							"arrayStructItems is null. Can not convert STRUCTURE to ARRAY/STRUCTURE.");
				String[] itemKeys = arrayStructItems.split("#");
				DlmsData[] array = srcData.getArray();
				StringBuilder sb = null;
				if (destType == DlmsDataType.VISIABLE_STRING)
					sb = new StringBuilder(512);

				for (int i = 0; i < array.length; i++) {
					DlmsData[] _members = (DlmsData[]) array[i].getStructure()
							.getMembers();
					if (itemKeys.length != _members.length) {
						String errInfo = "Can not convert string to struture. config="
								+ arrayStructItems
								+ ",data.length="
								+ _members.length;
						log.error(errInfo);
						throw new RuntimeException(errInfo);
					}
					if (i > 0 && null != sb)
						sb.append("#");
					for (int j = 0; j < itemKeys.length; j++) {
						IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
								.getConvert(itemKeys[j]);
						if (downLink)
							_members[j] = cvt.downLinkConvert(request,_members[j],param);
						else
							_members[j] = cvt.upLinkConvert(request,_members[j],param);
						if (null != sb) {
							if (j > 0)
								sb.append(";");
							sb.append(_members[j].getStringValue());
						}
					}
					if (null == sb && destType == DlmsDataType.ARRAY
							&& array.length == 1) {
						destData.setArray(_members);
						break;
					}
					if (null == sb) {
						ASN1SequenceOf seqof = new ASN1SequenceOf(_members);
						array[i].setStructure(seqof);
					}
				}
				if (null != sb) {
					destData.setVisiableString(sb.toString());
				}
			} else
				throw new RuntimeException("Not support yet.");
			break;
		}
		case DlmsDataType.COMPACT_ARRAY:
			break;
		default:
			throw new RuntimeException("Not support yet.type:"+srcData.getDataType());
		}
		if (destData.getDataType() == DlmsDataType.NULL)
			throw new RuntimeException("conver DLMS-DATA failed.");
		
		return destData;
	}

	private int getScaleValue(DlmsRequest request) {
		int tempScale = scale;
		if(request==null) {
			log.warn("getScaleValue request is null.");
			return tempScale;	
		}
		if(null!=multiScale.get(request.getMeterModel())){
			tempScale = multiScale.get(request.getMeterModel());
		}
		return tempScale;
	}

	private void arrayToStringValue(String[] itemKeys, DlmsData srcData,
			DlmsData destData,DlmsRequest request,DlmsObisItem item) throws IOException {
		
		DlmsData[] array=srcData.getArray();
		StringBuilder sb = new StringBuilder();
		for(DlmsData data:array){
			ASN1Type[] asn1Types=data.getStructure().getMembers();
			for(int i=0;i<asn1Types.length;i++){
				DlmsData member = (DlmsData) asn1Types[i];
				IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
						.getConvert(itemKeys[i]);
				member=cvt.upLinkConvert(request, member, item);
				sb.append(member.getStringValue()).append("#");
			}
			sb.setCharAt(sb.length()-1, ';');
		}
		sb.deleteCharAt(sb.length()-1);
		destData.setVisiableString(sb.toString());
	}

	/**
	 * Mainly used by WebRequest to convert string into DlmsData.
	 * 
	 * @param srcData
	 *            : value contain by WebRequest.
	 * @param destType
	 *            : destination DLMS data-type
	 * @param downLink
	 * @return
	 * @throws IOException
	 */
	private DlmsData stringToDlmsData(DlmsRequest request,DlmsData srcData,DlmsObisItem param, int destType,
			boolean downLink) throws IOException {
		
		int tempScale = getScaleValue(request);
		
		DlmsData data = new DlmsData();
		String str = srcData.getVisiableString();
		switch (destType) {
		case DlmsDataType.OCTET_RAW:
			if(downLink){
				data.setOctetString(HexDump.toArray(str));				
			}else{
				data.setOctetString(srcData.getOctetString());
			}
			return data;
		case DlmsDataType.VISIABLE_STRING:
			data.setVisiableString(str);
			return data;
		case DlmsDataType.OCTET_STRING:
			data.setOctetString( str.getBytes() );
			return data;
		case DlmsData.ENUM:
			data.setEnum(Integer.parseInt(str));
			break;
		case DlmsDataType.BCD: {
			if (tempScale == 0)
				data.setBcd(Integer.parseInt(str));
			else {
				data.setBcd((int) (Integer.parseInt(str) * multiplyScale(downLink,tempScale)));
			}
			break;
		}
		case DlmsDataType.INTEGER: {
			data.setDlmsInteger((byte) Integer.parseInt(str));
			break;
		}
		case DlmsDataType.UNSIGNED:
			data.setUnsigned(Integer.parseInt(str));
			break;
		case DlmsDataType.LONG: {
			if (tempScale == 0)
				data.setDlmsLong(Integer.parseInt(str));
			else
				data.setDlmsLong((int) (Integer.parseInt(str) * multiplyScale(downLink,tempScale)));
			break;
		}
		case DlmsDataType.UNSIGNED_LONG: {
			if (tempScale == 0)
				data.setUnsignedLong(Integer.parseInt(str));
			else
				data.setUnsignedLong((int) (Integer.parseInt(str) * multiplyScale(downLink,tempScale)));
			break;
		}
		case DlmsDataType.DOUBLE_LONG: {
			if (tempScale == 0)
				data.setDoubleLong((int) Long.parseLong(str));
			else
				data.setDoubleLong((int) (Long.parseLong(str) * multiplyScale(downLink,tempScale)));
			break;
		}
		case DlmsDataType.DOUBLE_LONG_UNSIGNED: {
			if (tempScale == 0)
				data.setDoubleLongUnsigned(Long.parseLong(str));
			else
				data.setDoubleLongUnsigned((long) (Float.parseFloat(str) * (long)multiplyScale(downLink,tempScale)));
			break;
		}
		case DlmsDataType.FLOAT32: {
			if (tempScale == 0)
				data.setFloat32(Float.parseFloat(str));
			else
				data.setFloat32((float) (Float.parseFloat(str) * multiplyScale(downLink,tempScale)));
			break;
		}
		case DlmsDataType.FLOAT64: {
			if (tempScale == 0)
				data.setFloat64(Double.parseDouble(str));
			else
				data.setFloat64(Double.parseDouble(str)
						* multiplyScale(downLink,tempScale));
			break;
		}
		case DlmsDataType.STRUCTURE: {
			if (null == arrayStructItems || arrayStructItems.length() == 0)
				throw new RuntimeException(
						"arrayStructItems is null. Can not convert string to STRUCTURE.");
			String[] itemKeys = arrayStructItems.split(";");
			String[] strDatas = str.split(";");
			if (itemKeys.length != strDatas.length) {
				String errInfo = "Can not convert string to struture. config="
						+ arrayStructItems + ",data=" + str;
				log.error(errInfo);
				throw new RuntimeException(errInfo);
			}
			DlmsData[] itemDatas = new DlmsData[itemKeys.length];
			for (int i = 0; i < itemKeys.length; i++) {
				IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
						.getConvert(itemKeys[i]);
				if (downLink) {
					DlmsData strData = new DlmsData();
					strData.setVisiableString(strDatas[i]);
					itemDatas[i] = cvt.downLinkConvert(request,strData,param);
				} else {
					throw new RuntimeException(
							"Can not convert up-link string to DLMS-struct.");
				}
			}
			ASN1SequenceOf seqof = new ASN1SequenceOf(itemDatas);
			data.setStructure(seqof);
			break;
		}
		case DlmsDataType.ARRAY_STRUCT: {
			if (null == arrayStructItems || arrayStructItems.length() == 0)
				throw new RuntimeException(
						"arrayStructItems is null. Can not convert string to ARRAY_STRUCT.");
			if (null == arrayStructItems || arrayStructItems.length() == 0)
				throw new RuntimeException(
						"arrayStructItems is null. Can not convert string to STRUCTURE.");
			String[] itemKeys = arrayStructItems.split(";");
			String[] strDatas = str.split(";");
			if (itemKeys.length != strDatas.length) {
				String errInfo = "Can not convert string to struture. config="
						+ arrayStructItems + ",data=" + str;
				log.error(errInfo);
				throw new RuntimeException(errInfo);
			}
			DlmsData[] itemDatas = new DlmsData[itemKeys.length];
			for (int i = 0; i < itemKeys.length; i++) {
				IDlmsScaleConvert cvt = DlmsScaleManager.getInstance()
						.getConvert(itemKeys[i]);
				if (downLink) {
					DlmsData strData = new DlmsData();
					strData.setVisiableString(strDatas[i]);
					itemDatas[i] = cvt.downLinkConvert(request,strData,param);
				} else {
					throw new RuntimeException(
							"Can not convert up-link string to DLMS-struct.");
				}
			}
			ASN1SequenceOf seqof = new ASN1SequenceOf(itemDatas);
			DlmsData struct = new DlmsData();
			struct.setStructure(seqof);
			DlmsData[] array = new DlmsData[] { struct };
			data.setArray(array);
			break;
		}
		case DlmsData.DATE_TIME:
		case DlmsData.TIME:
		case DlmsData.DATE:
			DlmsDateTime dateTime = new DlmsDateTime();
			byte[] rawData = new byte[srcData.getValue().length-1];
			for(int i = 0 ;i<rawData.length;i++){
				rawData[i] = srcData.getValue()[i+1];
			}
			dateTime.setDlmsDataValue(rawData, 0);
			data.setVisiableString(dateTime.toString());
			break;
		default:
			throw new RuntimeException("Can not convert string to other type.");
		}
		return data;
	}

	@Override
	public DlmsData downLinkConvert(DlmsRequest request,DlmsData reqData,DlmsObisItem param) throws IOException {
		if (null != customizer)
			return customizer.downLinkConvert(request,reqData,param);
		return doConvert(request,reqData,param, dlmsDataType, true);
	}

	@Override
	public DlmsData upLinkConvert(DlmsRequest request,DlmsData dlmsData,DlmsObisItem param) throws IOException {
		if (null != customizer)
			return customizer.upLinkConvert(request,dlmsData, param);
		return doConvert(request,dlmsData,param, callingDataType, false);
	}

	public final void setDlmsDataType(String dlmsDataType) {
		if (typeMap.isEmpty())
			initTypes();
		if(dlmsDataType ==null) return;
		dlmsDataType = dlmsDataType.toLowerCase();
		if (!typeMap.containsKey(dlmsDataType))
			this.dlmsDataType = DlmsDataType.DOUBLE_LONG_UNSIGNED;
		else
			this.dlmsDataType = typeMap.get(dlmsDataType);
	}

	public final void setCallingDataType(String callingDataType) {
		if (typeMap.isEmpty())
			initTypes();
		if(callingDataType ==null) return;
		callingDataType = callingDataType.toLowerCase();
		if (!typeMap.containsKey(callingDataType))
			this.callingDataType = DlmsDataType.DOUBLE_LONG_UNSIGNED;
		else
			this.callingDataType = typeMap.get(callingDataType);
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSubProtocol() {
		return subProtocol;
	}

	public void setSubProtocol(String subProtocol) {
		this.subProtocol = subProtocol;
	}

	public int getClassId() {
		return classId;
	}

	public void setClassId(int classId) {
		this.classId = classId;
	}

	public String getObis() {
		return obis;
	}

	public void setObis(String obis) {
		this.obis = obis;
	}

	public int getAttrId() {
		return attrId;
	}

	public void setAttrId(int attrId) {
		this.attrId = attrId;
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public int getDlmsDataType() {
		return dlmsDataType;
	}

	public int getCallingDataType() {
		return callingDataType;
	}

	public String getArrayStructItems() {
		return arrayStructItems;
	}

	public void setArrayStructItems(String arrayStructItems) {
		this.arrayStructItems = arrayStructItems;
	}

	public String getCustomizeClass() {
		return customizeClass;
	}

	public void setCustomizeClass(String customizeClass) {
		this.customizeClass = customizeClass;
	}

}
