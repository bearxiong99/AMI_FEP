/**
 * Scale management. Used to convert REQUEST into DLMS messages
 * and DLMS message to REQUEST from WEB caller.
 */
package cn.hexing.fk.bp.dlms.protocol;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import cn.hexing.fk.bp.dlms.convert.DlmsAbstractConvert;
import cn.hexing.fk.bp.dlms.persisit.JdbcDlmsDao;
import cn.hexing.fk.bp.model.DlmsMultiScale;

/**
 * @author: Adam Bao, hbao2k@gmail.com
 *
 */
public class DlmsScaleManager {
	private static final Logger log = Logger.getLogger(DlmsScaleManager.class);
	private static final DlmsScaleManager instance = new DlmsScaleManager();

	private JdbcDlmsDao scaleDao;
	private DlmsScaleManager(){ }
	
	public static final DlmsScaleManager getInstance(){ return instance; }
	
	private String baseProtocol = "0";//base protocol is 0
	
	private final HashMap<String,DlmsScaleItem> scaleMap = new HashMap<String,DlmsScaleItem>();
	private final HashMap<String,IDlmsScaleConvert> exHandlers = new HashMap<String,IDlmsScaleConvert>();
	
	public void init(){
		//Load file dlms-scale-config.txt
		//Load config from DB.
		List<DlmsScaleItem> list = scaleDao.loadDlmsScaleItem();
		
		Map<String, List<DlmsMultiScale>> scaleMap = scaleDao.loadDlmsMultiScale();
		for(DlmsScaleItem dsi : list){
			String itemKey=dsi.id;
			List<DlmsMultiScale> scaleModel = scaleMap.get(itemKey);
			//判断当前itemKey是否包含多量纲
			if(scaleModel!=null){
				for(DlmsMultiScale model:scaleModel){
					dsi.multiScale.put(new String(model.getMeterType()).toUpperCase(), model.getScale());
				}
			}
			putScaleItem(dsi);
		}
	}
	
	protected void putScaleItem(DlmsScaleItem item){
		//First search and create extended scale-converter
		if( null != item.customizeClass ){
			IDlmsScaleConvert converter = exHandlers.get(item.customizeClass);
			if( null != converter ){
				item.customizer = converter;
			}
			else{
				try {
					Class<?> clsConvert = Class.forName(item.customizeClass);
					Object objConvert = clsConvert.newInstance();
					if( objConvert instanceof IDlmsScaleConvert ){
						item.customizer = (IDlmsScaleConvert)objConvert;
						exHandlers.put(item.customizeClass, item.customizer);
					}
					else
						log.warn("ScaleConvert class is not IDlmsScaleConvert:"+item.customizeClass);
				} catch (Exception exp) {
					log.warn("ScaleConvert class not found:"+item.customizeClass);
				}
			}
		}
		
		//Second: put item into map.
		DlmsScaleItem it = scaleMap.get(item.itemKey());
		if( it != null ){
			if( item.subProtocol.equals(baseProtocol) ){
				item.subHandlers = new ArrayList<DlmsScaleItem>();
				item.subHandlers.add(it);
				if( null != it.subHandlers )
				item.subHandlers.addAll(it.subHandlers);
				scaleMap.put(item.itemKey(), item);
			}
			else{
				if( null == it.subHandlers )
					it.subHandlers = new ArrayList<DlmsScaleItem>();
				it.subHandlers.add(item);
			}
		}
		else{
			scaleMap.put(item.itemKey(), item);
		}
	}
	
	public IDlmsScaleConvert getConvert(String itemKey){
		DlmsScaleItem converter = scaleMap.get(itemKey);
		String subProtocol = null;
		if( null == converter ){
			int index = itemKey.indexOf('.');
			if( index < 0 ){
				log.warn("no converter is defined: key="+itemKey);
				return null;
			}
			subProtocol = itemKey.substring(0, index);
			itemKey = itemKey.substring(index+1);
		}
		converter = scaleMap.get(itemKey);
		if( null == converter ){
			log.warn("no converter is defined: key="+itemKey);
			return null;
		}
		if( null==subProtocol || subProtocol.equals(baseProtocol) )
			return null != converter.customizer ? converter.customizer : converter;
		for(int i=0; null != converter&& null!=converter.subHandlers&& i<converter.subHandlers.size(); i++){
			DlmsScaleItem it = converter.subHandlers.get(i);
			if( it.subProtocol.equals(subProtocol) )
				return null != it.customizer ? it.customizer : it;
		}
		return null != converter.customizer ? converter.customizer : converter;
	}
	
	public IDlmsScaleConvert getConvert(String subProtocol,int classId,String obis,int attrId){
		String key = classId + "." + obis + "." + attrId;
		DlmsScaleItem converter = scaleMap.get(key);
		if( null == converter ){
			log.warn("no converter is defined: key="+key);
			return null;
		}
		if( null==subProtocol || subProtocol.equals(baseProtocol) ){
			if(null!=converter.customizer){
				if(converter.customizer instanceof DlmsAbstractConvert){
					buildSubConvertSth(converter);
					return converter.customizer;
				}
			}else{
				return converter;
			}
			return null != converter.customizer ? converter.customizer : converter;
		}
		for(int i=0; null != converter&& null!=converter.subHandlers&& i<converter.subHandlers.size(); i++){
			DlmsScaleItem it = converter.subHandlers.get(i);
			if( it.subProtocol.equals(subProtocol) )
				return null != it.customizer ? it.customizer : it;
		}
		if(null!=converter.customizer){
			if(converter.customizer instanceof DlmsAbstractConvert){
				buildSubConvertSth(converter);
				return converter.customizer;
			}
		}else{
			return converter;
		}
		return null != converter.customizer ? converter.customizer : converter;
	}

	/**
	 * 给converter里的子convert设置一些参数
	 * @param converter
	 */
	private void buildSubConvertSth(DlmsScaleItem converter) {
		((DlmsAbstractConvert)converter.customizer).setArrayStructItems(converter.getArrayStructItems());
		((DlmsAbstractConvert)converter.customizer).setScale(converter.getScale());
		((DlmsAbstractConvert)converter.customizer).multiScale=converter.multiScale;
	}

	public JdbcDlmsDao getScaleDao() {
		return scaleDao;
	}

	public void setScaleDao(JdbcDlmsDao scaleDao) {
		this.scaleDao = scaleDao;
	}
}
