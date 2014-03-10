package cn.hexing.fas.startup;

import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fas.model.FaalDlmsRequest;
import cn.hexing.fas.protocol.gw.codec.C02MessageDecoder;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.util.HexDump;

public class ProtocolDecoder {
	private static final Logger log = Logger.getLogger(ProtocolDecoder.class);
	
	public Object zjMessageDecoder(String input) throws MessageParseException{
		Object value = "";
		MessageZj msg= new MessageZj();
		msg.read(HexDump.toByteBuffer(input.trim()));
		
		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();        
	    ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
	    boolean dirUp = msg.head.c_dir == MessageConst.DIR_UP;
	    if (dirUp)
	    	value = handler.process(msg); 
		return value;
	}
	
	
	public Object messageDecoder(String input){
		Object value="";
		try{
			MessageGw msg = new MessageGw();
			msg.read(HexDump.toByteBuffer(input.trim()));
			ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();        
		    ProtocolHandler handler = factory.getProtocolHandler(MessageGw.class);  
		    boolean dirUp = msg.head.c_dir == MessageConst.DIR_UP;
		    if (dirUp)
		    	value = handler.process(msg); 
		    else{//下行报文
		    	String sAFN=Integer.toString(msg.getAFN()& 0xff,16);
				sAFN=DataSwitch.StrStuff("0", 2, sAFN, "left");		
				if (sAFN.equals("04")||sAFN.equals("05")){
					value = handler.process(msg);	
				}else{				    
				    C02MessageDecoder cd=new C02MessageDecoder();
				    value=cd.decode(msg);
				}
		    }
		    
		    
		}catch(Exception ex){
			log.error("input is error:input="+input+","+ex);
		}		
        return value;
	}
	
	public static void main(String[] args) throws MessageParseException {
		FaalDlmsRequest fr=new FaalDlmsRequest();
    	String data="1234567890";  	
    	fr.setData(HexDump.toByteBuffer(data));	
    	ByteBuffer buffer=FaalDlmsRequest.encode(fr);
    	System.out.println(HexDump.hexDumpCompact(buffer));
    	fr=(FaalDlmsRequest)FaalDlmsRequest.decode(buffer);
		
		
    	
		/*String str="C96167882600027A00000400";
		int ps=0;
		for(int i=0;i<str.length()/2;i++){
			ps=ps+Integer.parseInt(str.substring(i*2,i*2+2),16);
		}
		ps=ps % 256;
		String ss=Integer.toHexString(ps);*/
		ProtocolDecoder pd=new ProtocolDecoder();
		pd.messageDecoder("68AA01AA0168C411220100000D6002010101270214002227021404007007000000000000000075010000002004000000730100008004000000000000090100005402000015010000230200000000000024000000710100002700000057020000000000008500000083000000880000005416");
	
		
		Object o =pd.zjMessageDecoder("6812148620801268890900010012112714253D800116");
		System.out.println(o);
	
	}
	
}
