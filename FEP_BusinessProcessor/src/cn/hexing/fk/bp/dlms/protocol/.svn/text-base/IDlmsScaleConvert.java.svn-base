/**
 * DLMS scale convert
 */
package cn.hexing.fk.bp.dlms.protocol;

import java.io.IOException;

import cn.hexing.fas.model.dlms.DlmsObisItem;
import cn.hexing.fas.model.dlms.DlmsRequest;

import com.hx.dlms.DlmsData;

/**
 * @author: Adam Bao, hbao2k@gmail.com
 *
 */
public interface IDlmsScaleConvert {
	/**
	 * Web request may use string or double type data, but DLMS must use U32.
	 * convert request data type to correct type.
	 * @param DlmsData: reqData
	 * @return
	 * @throws IOException 
	 */
	DlmsData downLinkConvert(DlmsRequest request,DlmsData data,DlmsObisItem item) throws IOException;
	DlmsData upLinkConvert(DlmsRequest request,DlmsData data,DlmsObisItem item) throws IOException;
}
