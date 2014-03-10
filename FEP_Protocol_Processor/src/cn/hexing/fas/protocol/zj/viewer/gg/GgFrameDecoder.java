package cn.hexing.fas.protocol.zj.viewer.gg;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.hexing.fas.framework.message.MessageGg;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.conf.ProtocolDataConfig;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.zj.codec.AbstractMessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C00MessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C04MessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C07MessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C15MessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C19MessageDecoder;
import cn.hexing.fas.protocol.zj.codec.C30MessageDecoder;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.util.CastorUtil;

public class GgFrameDecoder extends AbstractMessageDecoder{
	private static final String DATACONFIG_XML = "cn/hexing/fas/protocol/gg/conf/protocol-data-config.xml";
	private static final String MAPPING_XML = "cn/hexing/fas/protocol/gg/conf/protocol-data-config-mapping.xml";
	public static void main(String[] args) throws MessageParseException {
		GgFrameDecoder.getInstance().decode("6812070298010068810B001080110CA524EE73AAAA023D16");
	}
	
	
	public Object decode(String message) throws MessageParseException{
		MessageGg gg = new MessageGg();
		gg.read(HexDump.toByteBuffer(message));
		return getInstance().decode(gg);
	}
	private static GgFrameDecoder instance = new GgFrameDecoder();
	private GgFrameDecoder(){init();}
	
	Map<Byte,AbstractMessageDecoder> decoders = new HashMap<Byte, AbstractMessageDecoder>();
	private void init(){
		decoders.put( MessageConst.GG_FUNC_READ_TASK2, new C12MessageDecoder());
		decoders.put( MessageConst.ZJ_FUNC_RELAY, new C00MessageDecoder());
		decoders.put( MessageConst.ZJ_FUNC_READ_CUR, new C01MessageDecoder());
		decoders.put( MessageConst.ZJ_FUNC_READ_PROG, new C04MessageDecoder());
		decoders.put( MessageConst.ZJ_FUNC_WRITE_ROBJ, new C07MessageDecoder());
		decoders.put( MessageConst.ZJ_FUNC_WRITE_OBJ, new C08MessageDecoder());
		decoders.put( MessageConst.GG_FUNC_Action, new C14MessageDecoder());	
		decoders.put( MessageConst.GG_FUNC_AutoRegistered, new C15MessageDecoder());
		decoders.put( MessageConst.GG_FUNC_Event, new C19MessageDecoder());
		decoders.put( MessageConst.GG_UPGRADE, new C30MessageDecoder());
		decoders.put( MessageConst.GG_FUNC_READ_TASK1, new C11MessageDecoder());

		
		dataConfig = (ProtocolDataConfig) CastorUtil.unmarshal(MAPPING_XML, DATACONFIG_XML);			
		Collection<AbstractMessageDecoder> values = decoders.values();
		for(AbstractMessageDecoder decoder:values){
			decoder.setDataConfig(dataConfig);
		}
	}
	
	public static GgFrameDecoder getInstance(){
		if(instance ==null){
			instance = new GgFrameDecoder();
		}
		return instance;
	}
	@Override
	public Object decode(IMessage message) {
		if(message instanceof MessageGg){
			MessageGg gg = (MessageGg) message;
			String logicAddr = HexDump.toHex(gg.head.rtua);
			
			byte dir = gg.head.c_dir;
			byte func = (byte) (gg.head.c_func&0xff);
			if(dir == MessageConst.DIR_UP){
				if(decoders.containsKey(func)){
					Object result = decoders.get(func).decode(message);
					if(result instanceof HostCommand){
						((HostCommand)result).setRtuId(logicAddr);
					}
					return result;
				}
			}else{
				HostCommand hc = new HostCommand();
				byte[] data = ParseTool.getData(message);
				String sData = HexDump.toHex(data);

				switch (func) {
				case MessageConst.ZJ_FUNC_READ_CUR:
				{
					while(sData.length()>=4){
						HostCommandResult hcr = new HostCommandResult();
						String key = sData.substring(0,4);
						key=DataSwitch.ReverseStringByByte(key);
						sData = sData.substring(4);
						hcr.setCode(key);
						hc.addResult(hcr);
					}
					return hc;
				}
				case MessageConst.ZJ_FUNC_WRITE_OBJ:
				{
					sData=sData.substring(10);
					hc=(HostCommand) decoders.get(MessageConst.ZJ_FUNC_READ_CUR).decode(message);
					return hc;
				}
				default:
					break;
				}
			}
			
		}
		
		return null;
	}

}
