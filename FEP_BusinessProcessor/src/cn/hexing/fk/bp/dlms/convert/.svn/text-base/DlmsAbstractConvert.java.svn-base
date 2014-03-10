package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.hx.dlms.DlmsData;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;

/**
 * 
 * @author gaoll
 *
 * @time 2012-11-22 下午1:46:20
 *
 * @info Dlms转换抽象类
 */
public abstract class DlmsAbstractConvert extends DlmsScaleItem{
	protected static final Logger log = Logger.getLogger(DlmsDemandOfMonthConvert.class);

	@Override
	public DlmsData downLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		return data;
	}

	@Override
	public DlmsData upLinkConvert(DlmsRequest request, DlmsData data,
			DlmsObisItem item) throws IOException {
		DlmsData dest = new DlmsData();
		if(data.type()!=1)
			return dest;
		DlmsData[] arrays=data.getArray();
		dest.setVisiableString(getStringValue(arrays,request,item));
		return dest;
	}
	
	public String getStringValue(DlmsData[] arrays)  throws IOException {
		return "";
	}
	
	public String getStringValue(DlmsData[] arrays,DlmsRequest request) throws IOException{
		return getStringValue(arrays);
	}
	
	public String getStringValue(DlmsData[] arrays,DlmsRequest request,DlmsObisItem item) throws IOException{
		return getStringValue(arrays, request);
	}
	
	public int getScale(IDlmsScaleConvert convert,DlmsRequest req){
		if(convert==null) return 0;
		if(!(convert instanceof DlmsScaleItem)) return 0;
		int tempScale = ((DlmsScaleItem)convert).scale;
		if(req==null) {
			return tempScale;	
		}
		if(null!=((DlmsScaleItem)convert).multiScale.get(req.getMeterModel())){
			tempScale = ((DlmsScaleItem)convert).multiScale.get(req.getMeterModel());
		}
		return tempScale;
		
	}

	public int getScale() {
		return scale;
	}

	public void setScale(int scale) {
		this.scale = scale;
	}

	public String getArrayStructItems() {
		return arrayStructItems;
	}

	public void setArrayStructItems(String arrayStructItems) {
		this.arrayStructItems = arrayStructItems;
	}
	
	
}
