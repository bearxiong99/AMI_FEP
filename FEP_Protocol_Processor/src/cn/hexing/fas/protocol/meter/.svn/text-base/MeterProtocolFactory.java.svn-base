package cn.hexing.fas.protocol.meter;

import java.util.Hashtable;

import org.apache.log4j.Logger;

import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fk.utils.StringUtil;
import cn.hexing.util.CastorUtil;

public class MeterProtocolFactory {
	private static Hashtable datamappings;
	private static Object lock=new Object();
	private static final Logger log = Logger.getLogger(MeterProtocolFactory.class);

	public static MeterProtocolDataSet createMeterProtocolDataSet(String key){
		synchronized(lock){
			if(datamappings==null){
				datamappings=new Hashtable();
			}
			if(!datamappings.containsKey(key)){
				datamappings.put(key,createDataSet(key));
			}
			return (MeterProtocolDataSet)datamappings.get(key);
		}
	}
	
	private static MeterProtocolDataSet createDataSet(String key){
		MeterProtocolDataSet dataset=null;
		try{
			if (key.equals("ZJMeter")) {
				dataset = (MeterProtocolDataSet) CastorUtil.unmarshal("cn/hexing/fas/protocol/meter/conf/protocol-meter-zj-mapping.xml","cn/hexing/fas/protocol/meter/conf/protocol-meter-zj-dataset.xml");
			} else if (key.equals("BBMeter")) {
				dataset = (MeterProtocolDataSet) CastorUtil.unmarshal("cn/hexing/fas/protocol/meter/conf/protocol-meter-zj-mapping.xml","cn/hexing/fas/protocol/meter/conf/protocol-meter-bb-dataset.xml");
			} else if (key.equals("HX645Meter")) {
				dataset = (MeterProtocolDataSet) CastorUtil.unmarshal("cn/hexing/fas/protocol/meter/conf/protocol-meter-hx645-mapping.xml","cn/hexing/fas/protocol/meter/conf/protocol-meter-hx645-dataset.xml");
			} else if (key.equals("Modbus")) {
				dataset = (MeterProtocolDataSet) CastorUtil.unmarshal("cn/hexing/fas/protocol/meter/conf/protocol-meter-modbus-mapping.xml","cn/hexing/fas/protocol/meter/conf/protocol-meter-modbus-dataset.xml");
			}
			if(dataset!=null){
				dataset.packup();				
			}
		}catch(Exception e){
			log.error(StringUtil.getExceptionDetailInfo(e));
		}
		return dataset;
	}
}
