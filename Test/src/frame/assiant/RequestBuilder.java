package frame.assiant;

import model.ReqDecription;
import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;

public class RequestBuilder {
	
	private static RequestBuilder instance = new RequestBuilder();
	
	private RequestBuilder(){};
	
	public static RequestBuilder getInstance(){
		return instance;
	}
	
	public DlmsRequest build(ReqDecription od){
		DlmsRequest dr = new DlmsRequest();
		
		dr.setMeterId(od.meterId);
		dr.setParams(new DlmsObisItem[]{new DlmsObisItem()});
		dr.getParams()[0].classId=od.classId;
		dr.getParams()[0].attributeId=od.attrId;
		dr.getParams()[0].obisString=od.obis;
		dr.getParams()[0].data = od.data;
		return dr;
		
	}
}
