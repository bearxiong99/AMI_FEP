package cn.hexing.fk.bp.dlms.convert;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;
import cn.hexing.fk.bp.dlms.persisit.TaskMessageService;
import cn.hexing.fk.bp.dlms.protocol.IDlmsScaleConvert;

import com.hx.dlms.DlmsData;
/**
 * 
 * ��������������String����ʽ��ţ������ʽ����
 * 
 * time1#value1#value2#value3;time2#value11#value22#value33
 * 
 * ��ͨ�����ݵ�ת��
 */
public class DlmsChannelDataConvert extends DlmsAbstractConvert implements IDlmsScaleConvert{

	@Override
	public DlmsData upLinkConvert(DlmsRequest request,DlmsData reqData,DlmsObisItem param) throws IOException {
		
		 return	TaskMessageService.getInstance().taskMessageConvert(null, request, param);
		
	}

}
