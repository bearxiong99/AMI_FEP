package cn.hexing.fas.protocol.gw.codec;

import cn.hexing.exception.MessageEncodeException;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataValue;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.util.EsamUtil;

/**
 * 集中器更新非对称密钥数据解析
 * @author Administrator
 *
 */
public class C06MessageDecoder extends AbstractMessageDecoder {

	/**更新主站公钥*/
	private static final String UPDATE_MASTER_PUBKEY = "06F020";
	
	/**更新集中器公钥*/
	private static final String UPDATE_TERMINAL_PUBKEY = "06F021";
	
	@Override
	public Object decode(IMessage message) {
		HostCommand hc = new HostCommand();
		try{
			String data=ParseTool.getMsgData(message);
			MessageGw msg = (MessageGw) message;
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			
			BizRtu rtu = RtuManage.getInstance().getBizRtuInCache(message.getLogicalAddress());
			
			String[] codes = DataItemParser.dataCodeParser(data.substring(4,8), "06");
			
			data=data.substring(8); //消去单元数据
			if(codes.length>0){
				if(UPDATE_MASTER_PUBKEY.equals(codes[0])){
					//先将标志位取出来,0表示取随机数，1表示更新结果
					DataValue value = DataItemParser.parser(data, "HTB1", false);
					data=data.substring(2);
					String sValue="";
					if(value.getValue().equals("0")){//取随机数
						sValue = data.substring(0,32);
					}else if(value.getValue().equals("1")){
						data = data.substring(8);
						sValue =data.substring(0, 4);
						sValue = EsamUtil.getInstance().decript(rtu, sValue, msg.getFseq(),0,"");
						//更新成功或失败，通知主站
						HostCommandResult hcr=new HostCommandResult();
						hcr.setCode(codes[0]);
						hcr.setTn("0");
						hcr.setValue("01");
						if("6108".equals(sValue)){//更新主站公钥成功
							hcr.setValue("00");
						}
						hc.setStatus(HostCommand.STATUS_SUCCESS);
						//解密更新成功或失败
						hc.addResult(hcr);
						return hc;
					}
					return sValue;
				}else if(UPDATE_TERMINAL_PUBKEY.equals(codes[0])){
					//format: 是否首次FLAG+SEQ+48字节公钥[+48字节公钥TAG]
					//解密更新成功或失败
					boolean updateSuccess = false;
					String sValue = "";
					boolean isFirstTime=Integer.parseInt(data.substring(0, 2))==0?true:false;
					data = data.substring(2+8);//将FLAG和SEQ去掉
					if(isFirstTime){//只有48个字节的公钥
						sValue = data.substring(0,48*2);
						//解密集中器公钥
						sValue=EsamUtil.getInstance().decript(rtu, sValue, msg.getFseq(),0,"");
						updateSuccess=true;
					}else{
						sValue = data.substring(0,48*2);
						data=data.substring(48*2);
						String signature=data.substring(0, 48*2);
						data=data.substring(48*2);
						//验签
						updateSuccess=EsamUtil.getInstance().pubKeyVerify(rtu,sValue, signature,sValue);
					}
					
					//解密，验签
					HostCommandResult hcr = new HostCommandResult();
					hcr.setValue("01");
					hc.setStatus(HostCommand.STATUS_RTU_FAILED);
					if(updateSuccess){
						hcr.setValue(sValue);	
						hc.setStatus(HostCommand.STATUS_SUCCESS);
					}
					hcr.setCode(codes[0]);
					hcr.setTn("0");
					hc.addResult(hcr);

				}
			}
		}catch(MessageEncodeException e){
			throw e;
		}
		catch(Exception e){
			throw new MessageEncodeException(e);
		}
		
		return hc;
	}

//	private String getSystitle(String logicalAddress, int fseq) {
//		ByteBuffer buf = ByteBuffer.allocate(12);
//		buf.put((byte)0x48).put((byte)0x58).put((byte)0x45).put((byte)0x11);
//		byte[] bytes = HexDump.toArray(logicalAddress);
//		bytes[0] = (byte) (bytes[0] & 0x0F);
//		buf.put(bytes);
//		buf.putInt( fseq );
//		buf.flip();
//		return HexDump.toHex(buf.array());
//	}

}
