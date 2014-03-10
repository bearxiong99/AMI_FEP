package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleManager;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;

import com.hx.dlms.ASN1SequenceOf;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
/**
 * 
 * @author gaoll
 *
 * @time 2013-6-15 上午11:13:47
 *
 * @info U32型数组转换
 */
public class DlmsU32StructureDataConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{

	@Override
	public DlmsData downLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {

		IDlmsScaleConvert convert = getConvert(request, item);
		
		if(convert==null){
			convert = new DlmsScaleItem();
			((DlmsScaleItem)convert).dlmsDataType = DlmsDataType.DOUBLE_LONG_UNSIGNED;
		}
		
		DlmsData destData = new DlmsData();
		
		String stringValue = data.getStringValue();
		
		String[] values = stringValue.split(",");
		DlmsData[] datas = new DlmsData[values.length];
		
		for(int i=0;i<values.length;i++){
			String value = values[i];
			datas[i] = new DlmsData();
			datas[i].setVisiableString(value);
			datas[i]=convert.downLinkConvert(request, datas[i], null);
		}
		
		
		ASN1SequenceOf struct = new ASN1SequenceOf(datas);
		destData.setArray(struct);
		
		return destData;
	}

	
	private IDlmsScaleConvert getConvert(DlmsRequest request, DlmsObisItem item) {
		IDlmsScaleConvert convert = null;
		if(item!=null){
			convert = DlmsScaleManager.getInstance().getConvert(request.getSubprotocol(), item.classId, item.obisString, item.attributeId);
			if(convert!=null && convert instanceof DlmsAbstractConvert){
				String structs=((DlmsAbstractConvert)convert).getArrayStructItems();
				if(structs!=null){
					String[] items = structs.split("#");
					if(items.length>=1){
						convert = DlmsScaleManager.getInstance().getConvert((request.getSubprotocol()==null?"":request.getSubprotocol()+".")+items[0]);
						return convert;
					}
				}
			}
		}else{
			log.warn("getDemand item is null.can't find convert");
		}
		return null;
	}


	@Override
	public String getStringValue(DlmsData[] arrays, DlmsRequest request,
			DlmsObisItem item) throws IOException {
		
		IDlmsScaleConvert convert = getConvert(request, item);
		if(convert == null){
			convert = new DlmsScaleItem();
			((DlmsScaleItem)convert).callingDataType = DlmsDataType.DOUBLE_LONG_UNSIGNED;
		}
		
		StringBuilder sb = new StringBuilder();
		
		for(DlmsData data:arrays){
			data=convert.upLinkConvert(request, data, item);
			sb.append(data.getStringValue()).append(",");
		}
		
		if(sb.length()>0){
			sb.deleteCharAt(sb.length()-1);
		}
		
		return sb.toString();
	}


	
}
