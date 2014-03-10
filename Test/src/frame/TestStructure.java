package frame;

import java.io.IOException;
import java.nio.ByteBuffer;

import cn.hexing.fk.bp.dlms.protocol.DlmsScaleItem;
import cn.hexing.fk.utils.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.DlmsData;

public class TestStructure {
	public static void main(String[] args) throws IOException {
		DlmsScaleItem sdi = new DlmsScaleItem();
		sdi.callingDataType = 10;
		sdi.arrayStructItems="8.0.0.1.0.0.255.2;3.1.0.31.7.0.255.2;3.1.0.51.7.0.255.2;3.1.0.71.7.0.255.2";
		String frame = "01060204090C056F060AFF0B0000008000011200001200001200000204090C056F060AFF0B0F00008000011200001200001200000204090C056F060AFF0B1E00008000011200001200001200000204090C056F060AFF0B2D00008000011200001200001200000204090C056F060AFF0C0000008000011200001200001200000204090C056F060AFF0C0F0000800001120000120000120000";
		ByteBuffer buffer = ByteBuffer.wrap(HexDump.toArray(frame));
		DlmsData resultData = new DlmsData();
		resultData.decode(new DecodeStream(buffer));
		DlmsData dd = sdi.upLinkConvert(null, resultData, null);
		System.out.println(dd.getStringValue());
	}
}
/**
public void test(){
DlmsScaleItem[] dsis = new DlmsScaleItem[4];
for(int i=0;i<dsis.length;i++){
	dsis[i] = new DlmsScaleItem();
}
dsis[0].classId = 8;
dsis[0].obis = "0.0.1.0.0.255";
dsis[0].attrId = 2;
dsis[0].callingDataType = 25;

dsis[1].classId = 3;
dsis[1].obis = "1.0.31.7.0.255";
dsis[1].attrId = 2;
dsis[1].callingDataType = 18;

dsis[2].classId = 3;
dsis[2].obis = "1.0.51.7.0.255";
dsis[2].attrId = 2;
dsis[2].callingDataType = 18;

dsis[3].classId = 3;
dsis[3].obis = "1.0.71.7.0.255";
dsis[3].attrId = 2;
dsis[3].callingDataType = 18;
putScaleItem(dsis[0]);
putScaleItem(dsis[1]);
putScaleItem(dsis[2]);
putScaleItem(dsis[3]);
}*/