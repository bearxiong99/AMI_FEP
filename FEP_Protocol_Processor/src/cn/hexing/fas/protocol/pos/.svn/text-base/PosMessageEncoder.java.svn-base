package cn.hexing.fas.protocol.pos;

import java.nio.ByteBuffer;

import cn.hexing.fas.model.pos.PosCommandRequest;
import cn.hexing.fas.protocol.pos.parse.DataItemCoder;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.utils.HexDump;

public class PosMessageEncoder extends AbstractMessageEncoder{
	
	private static PosMessageEncoder instance = new PosMessageEncoder();

	private PosMessageEncoder() {
	}

	public static PosMessageEncoder getInstance() {
		return instance;
	}
	
	public static void main(String[] args) {
		PosCommandRequest request = new PosCommandRequest();
		request.setFun_c((byte) 0xBC); //BCD8#BCD6#HTB4
		request.setSubFun_c((short) 4);
		request.setParam("Utility Vend#ling5#01#12#20130609083422#000000000005002#1122334455667##zd#014101480144#02#07#999910#3#48#1#2#000000000005002#FBECredTokenIssue#11500#30000#17138876959598521850###0");
//		request.setParam("abc#def#ghi#jkl#0123#mno#pq#rst#uvw#xyz#1#1#4567#1#1#2#0#abc#defc#123#321#123#ASC#ASED#2#1#2#3#avc,1#3#4#AVC;0#abc#defc#123#321#123#ASC#ASED#2#1#2#3#avc,1#3#4#avc");
		request.setSeq("00000012");
		PosMessageEncoder pos = new PosMessageEncoder();
		IMessage[] s = pos.encode(request);
		System.out.println(s);
	}

	@Override
	public IMessage[] encode(Object obj) {
		if(!(obj instanceof PosCommandRequest)) return null;
		
		//如果是下一帧，取出来帧序号
		
		PosCommandRequest request = (PosCommandRequest) obj;
		String result = null;
		if(request.getSubFun_c()!=-1){
			 result= DataItemCoder.coder(request.getParam(), PosItemConfig.itemSubMap.get(""+Integer.parseInt(HexDump.toHex(request.getSubFun_c()),16)));
		}else{
			 result = DataItemCoder.coder(request.getParam(), PosItemConfig.itemMap.get(""+Integer.parseInt(HexDump.toHex(request.getFun_c()),16)));
		}
		
		if(request.getFun_c()==(byte)0xBC){
			
			boolean isZip = false;
			boolean isSubFrame=false;
			int maxLen = 1024 * 6;
			if(result.length()/2>maxLen){ //需要分帧
				//要压缩吗？
				isZip = false;
				//要分帧吗?
				isSubFrame=false;
			}
			boolean paramAppendx = isZip||isSubFrame;
			String param =request.getSubFun_c()+"#"+request.getClientVersion()+"#"+(paramAppendx?1:0)+"#";
			
			
			byte[] allFrame = HexDump.toArray(result);
			//将下面的代码移到这里来
			//数据压缩
			//boolean isZip?
			//数据分帧
			//boolean isSubF?
			//如果没有超过最大字节数
			if(paramAppendx){
				long frameCounter=PosItemConfig.getNextFrameCounter();
				PosItemConfig.subFrameMap.put(PosItemConfig.getNextFrameCounter(),allFrame);
				byte[] subFrame = splitFrame(allFrame, maxLen, 0);
				result = HexDump.toHex(subFrame);
				param+=(allFrame.length/maxLen+1)+"#0#"+(isZip?1:0)+"#"+frameCounter;
			}
			String headFormat = PosItemConfig.itemSubMap.get("0");
			String head=DataItemCoder.coder(param, headFormat);
			result=head+result;
		}
		
		
		
		byte c = encodeC(request.getFun_c(),result);
		int L=6+result.length()/2+((request.getFun_c()==(byte)0xBC)?1:0);//最新的协议需要加帧尾，一个字节0x16
		ByteBuffer byteBuffer = ByteBuffer.allocate(L+3);
		byteBuffer.put((byte) 0x68);
		byteBuffer.putShort((short) L);
		byteBuffer.put(HexDump.toArray(HexDump.toHex(Integer.parseInt(request.getSeq()))));
		byteBuffer.put(c);
		byteBuffer.put(request.getFun_c());
		byteBuffer.put(HexDump.toArray(result));
		if(request.getFun_c()==(byte)0xBC){//如果是扩展的命令,需要加上0x16
			byteBuffer.put((byte) 0x16);
		}
		byteBuffer.flip();
		MessageBytes msg = new MessageBytes();
		msg.data = byteBuffer.array();
		//如果分帧了 ，将第一帧数据返回给调用方
		return new IMessage[]{msg};
	}
	
	/**
	 * 将帧数据分离
	 * @param content
	 * @param i
	 * @return
	 */
	private byte[] splitFrame(byte[] content, int maxLen,int frameNo) {
		
		int length =content.length;
		int size = length/maxLen;
		int destSize= maxLen;
		if(size == frameNo){//最后一帧，可能不足i_maxSize
			destSize = maxLen-((frameNo+1)*maxLen-length);
		}
		byte[] dest = new byte[destSize];
		System.arraycopy(content, frameNo*maxLen, dest, 0, destSize);
		return dest;
	}

	private byte encodeC(int fun_c, String data) {
		byte _cs = 0;
		_cs+=fun_c;
		byte[] b_data = HexDump.toArray(data);
		for(byte b:b_data){
			_cs+=b;
		}
		return _cs;
	}


}
