package cn.hexing.fas.protocol.meter;

import cn.hexing.fas.model.FaalHX645RelayRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.data.DataItem;
import cn.hexing.fas.protocol.gw.parse.DataItemCoder;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataItem;
import cn.hexing.fas.protocol.meter.conf.MeterProtocolDataSet;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.util.HexDump;

public class HX645MeterParser implements IMeterParser{
	
	private MeterProtocolDataSet dataset;
	public HX645MeterParser(){
		try{
			dataset=MeterProtocolFactory.createMeterProtocolDataSet("HX645Meter");
		}catch(Exception e){
		}	
	}
	
	public static void main(String[] args) {

		FaalHX645RelayRequest faal = new FaalHX645RelayRequest();
		FaalRequestRtuParam frrp = new FaalRequestRtuParam();
		frrp.setCmdId((long)0);
		frrp.setRtuId("66554433");
		frrp.addParam("10F001", "");
		faal.addRtuParam(frrp);
		faal.setId("EA20");
		faal.setDataArea("2013-01-01 00:01:02");
		faal.setOp("04");
		faal.setType(16);
		faal.setFixAddre("19860811");
		faal.setPort(31); //端口号
		frrp.setTn(new int[]{3});
		faal.setProtocol("02");
		HX645MeterParser h = new HX645MeterParser();
		System.out.println(HexDump.toHex(h.constructor(faal)));
	
	}
	public  byte[] constructor(FaalHX645RelayRequest request){
		if("04".equals(request.getOp())){
			MeterProtocolDataItem item = dataset.getDataItem(request.getId());
			int length = item.getLength();
			byte[] frame = new byte[19+length];
			frame[0]=0x68;
			if(item.getOperationTo()==1){  //直接对表操作表号补零
				ParseTool.HexsToBytesAA(frame,1,request.getFixAddre(),6,(byte)0x00);
			}else{//对采集器操作表号补FF
				ParseTool.HexsToBytesAA(frame,1,request.getFixAddre(),6,(byte)0xFF);				
			}
			frame[7]=0x68;
			frame[8]=0x00;
			frame[9]=0x04;
			frame[10]=(byte)(2+length+4);
			byte[] id=HexDump.toArray(request.getId());
			for(int i=0;i<id.length;i++){
				frame[11+i]=(byte) (id[id.length-1-i]+0x33);
			}
			byte[] password=HexDump.toArray("00000000");
			for(int i=0;i<password.length;i++){
				frame[11+id.length+i]=(byte)(password[i]+0x33);
			}
			if(length!=0){
				byte[] dataArea = null;
				if("EA20".equals(request.getId())){
					//日期这里特殊处理下，避免对以前的冲突
					dataArea =HexDump.toArray( DataItemCoder.constructor(request.getDataArea(), item.getFormat()));
					String str = DataSwitch.ReverseStringByByte(HexDump.toHex(dataArea));
					dataArea = HexDump.toArray(str);
				}else{
					dataArea = HexDump.toArray(request.getDataArea());					
				}
				for(int i=0;i<length;i++){
					frame[11+id.length+password.length+(length-1)-i]=(byte)((dataArea[i]&0xFF)+0x33);
				}
			}
			frame[frame.length-2]=ParseTool.calculateCS(frame, 0, frame.length-2);
			frame[frame.length-1]=0x16;
			return frame;
			
		}else if("01".equals(request.getOp())){
			byte[] frame = new byte[15];
			frame[0]=0x68;
			MeterProtocolDataItem item = dataset.getDataItem(request.getId());
			if(item.getOperationTo()==1){  //直接对表操作表号补零
				ParseTool.HexsToBytesAA(frame,1,request.getFixAddre(),6,(byte)0x00);
			}else{//对采集器操作表号补FF
				ParseTool.HexsToBytesAA(frame,1,request.getFixAddre(),6,(byte)0xFF);				
			}
			frame[7]=0x68;
			frame[8]=0x00;
			frame[9]=0x01;
			frame[10]=0x02;
			byte[] id=HexDump.toArray(request.getId());
			for(int i=0;i<id.length;i++){
				frame[11+i]=(byte) (id[id.length-1-i]+0x33);
			}
			frame[13]=ParseTool.calculateCS(frame, 0, 13);
			frame[14]=0x16;
			return frame;
		}
		
		return null;
		
	}

	@Override
	public String[] convertDataKey(String[] datakey) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public byte[] constructor(String[] datakey, DataItem para) {
		return null;
	}

	@Override
	public Object[] parser(byte[] data, int loc, int len) {return  null;}

	@Override
	public Object[] parser(String key, String data, String meteraddr) {
		return null;
	}

	@Override
	public String[] getMeter1Code(String[] codes) {
		return null;
	}

	@Override
	public String[] getMeter2Code(String[] codes) {
		return null;
	}

	@Override
	public Object[] parser(byte[] data, int loc, int len, BizRtu rtu) {
		// TODO Auto-generated method stub
		return null;
	}

	public Object[] parser(byte[] data, int loc, int length, boolean setFlag) {
		byte[] id = new byte[2];
		id[0] = data[1];id[1] = data[0];
		MeterProtocolDataItem s = this.dataset.getDataItem(HexDump.toHex(id));
		byte[] value = new byte[data.length-id.length];
		System.arraycopy(data, id.length, value, 0, value.length);
		String format = "";
		if(s.getCode().equals("EA20") && setFlag){
			//这里由于当初没有要求设置时间，先处理的是TOKEN下发，由于TOKEN下发有返回值，所以导致这里要特殊处理，以后需要将TOKEN下发作为特殊处理。
			return new String[]{HexDump.toHex(value)};
		}else{
			if(s.getChildarray().size()>0){
				for(int i=0;i<s.getChildarray().size();i++){
					MeterProtocolDataItem item = (MeterProtocolDataItem) s.getChildarray().get(i);
					format+="#"+item.getFormat();
				}
				format=format.substring(1);
			}else{
				format = s.getFormat();
			}
			
			DataValue result = DataItemParser.parser(HexDump.toHex(value), format, false);
			
			return new String[]{result.getValue()};
		}
	
	}
}
