package cn.hexing.fas.protocol.meter;

import org.apache.log4j.Logger;

import cn.hexing.fas.protocol.Protocol;
import cn.hexing.fk.utils.StringUtil;

/**
 * @filename	MeterParserFactory.java
 * TODO
 */
public class MeterParserFactory {
	
	private static final Logger log = Logger.getLogger(MeterParserFactory.class);

	
	public static IMeterParser getMeterParser(String type){
		IMeterParser rt=null;
		try{
			if(type.equals(Protocol.ZJMeter)){
				rt=new ZjMeterParser();
			}else if(type.equals(Protocol.BBMeter97)||type.equals(Protocol.BBMeter07)){
				rt=new BbMeterParser();
			}else if(type.equals(Protocol.HX645)){
				rt = new HX645MeterParser();
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return rt;
	}
}
