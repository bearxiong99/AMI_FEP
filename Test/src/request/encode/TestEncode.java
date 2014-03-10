package request.encode;

import java.io.IOException;

import cn.hexing.util.HexDump;

import com.hx.dlms.DecodeStream;
import com.hx.dlms.applayer.get.GetResponse;
import com.hx.dlms.applayer.set.SetResponse;



public class TestEncode 
{
	public static void main(String[] args) throws IOException {
		SetResponse set = new SetResponse();
		set.decode(new DecodeStream(HexDump.toArray("C501040103")));
		System.out.println(set);

		GetResponse get = new GetResponse();
		get.decode(new DecodeStream(HexDump.toArray("C401050103")));
		System.out.println(get);
	}
}
