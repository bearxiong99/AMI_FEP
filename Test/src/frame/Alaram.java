package frame;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import cn.hexing.fk.utils.DateConvert;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;
import com.hx.dlms.DlmsDataType;
import com.hx.dlms.DlmsDateTime;
import com.hx.dlms.applayer.eventnotification.DlmsAlarmItem;

public class Alaram {
	public static void main(String[] args) throws IOException {
		String frame="01090202090C056F0514FF0F23360080000111FF0202090C056F051BFF120E3B00800001110F0202090C056F051BFF120F2F00800001110F0202090C056F051BFF12111400800001110F0202090C056F051BFF12113A00800001110F0202090C056F051BFF1218240080000111070202090C056F051BFF1232310080000111070202090C056F051BFF1332140080000111040202090C056F060FFF0F0D25008000011105";
		DlmsData resultData =null;
		ByteBuffer buffer = ByteBuffer.wrap(HexDump.toArray(frame));
		resultData = new DlmsData();
		resultData.decode(new DecodeStream(buffer));
		short evtCode = 0x0100;
		DlmsData[] ar =resultData.getArray();
		DlmsAlarmItem[] result = new DlmsAlarmItem[ar.length];
		for(int i=0;i<ar.length;i++){

			int structSize = ar[i].getStructureSize();
			if( structSize< 2 ){
				System.out.println("....");
			}
			int offset = 0;
			int subCode = 0;
			DlmsData member = ar[i].getStructureItem(offset++);
			if( member.type() == DlmsDataType.UNSIGNED_LONG ){  //U16
				int count = member.getUnsignedLong();
				member = ar[i].getStructureItem(offset++);
				if( member.type() == DlmsDataType.UNSIGNED )
					subCode = member.getUnsigned();
				else if( member.type() == DlmsDataType.UNSIGNED_LONG )
					subCode = member.getUnsignedLong() & 0xFF ;
				result[i] = new DlmsAlarmItem( (short)(evtCode | subCode) );
				result[i].setRelatedData("count",Integer.toString(count));
			}
			else{
				String occurTime = null;
				try{
					DlmsDateTime dlmsTime = member.getDateTime();
					if( null != dlmsTime )
						occurTime = dlmsTime.toString();
				}catch(Exception e){}
				member = ar[i].getStructureItem(offset++);
				if( member.type() == DlmsDataType.UNSIGNED )
					subCode = member.getUnsigned();
				else if( member.type() == DlmsDataType.UNSIGNED_LONG )
					subCode = member.getUnsignedLong() & 0xFF ;
				result[i] = new DlmsAlarmItem( (short)( evtCode + subCode), occurTime);
			}
		
		}
		
		System.out.println(result);
		
	for(DlmsAlarmItem alarm : result){
			


			String occurTime = alarm.getTime();
			//TODO: iran time to gregorian???
			occurTime = DateConvert.iranToGregorian(occurTime);
			
			
			String alarmCode = alarm.getAlarmCode();//¸æ¾¯±àÂë
			StringBuilder sb = new StringBuilder();
			String params = "";
			if(alarm.getRelatedData() !=null){
				
				HashMap<String, String> relatedData=alarm.getRelatedData();
				
				for(String str:relatedData.keySet()){
					sb.append(str+"="+relatedData.get(str)+";");
				}
				
				sb.deleteCharAt(sb.length()-1);
			}
			System.out.println(sb);
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = null;
			try {
				date = sdf.parse(occurTime);
			} catch (ParseException e) {}
			System.out.println("occurTime:"+sdf.format(date));
			System.out.println("code:"+alarmCode);
	}
	}
}
