package cn.hexing.fas.protocol.zj.codec;

import java.nio.ByteBuffer;

import cn.hexing.fas.model.FaalGGKZM30Request;
import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fas.protocol.zj.parse.Parser03;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.message.zj.MessageZjHead;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;

/**
 * 
 * @author gaoll
 *
 * @time 2013-8-19 上午09:27:13
 *
 * @info 软件升级组帧
 */
public class C30MessageEncoder extends AbstractMessageEncoder{

	
	public static void main(String[] args) {
		C30MessageEncoder coder = new C30MessageEncoder();
		FaalGGKZM30Request req = new FaalGGKZM30Request();
		req.setFileName("test.txt");
		req.setFileType(1);
		req.setCurrentContent(new byte[]{0x01,0x02,0x03,0x04});
		req.setContentNum(3);
		req.setLogicAddress("22114433");
		coder.encode(req);
	}
	
	@Override
	public IMessage[] encode(Object obj) {
		if(!(obj instanceof FaalGGKZM30Request)) return null;
		
		FaalGGKZM30Request request = (FaalGGKZM30Request) obj;
		
		String filename=request.getFileName();
		byte[] fileContent=request.getCurrentContent();
		int currentNum=request.getContentNum();
		
    	BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(request.getLogicAddress()));
    	if(rtu == null)
    		return null;
    	
    	
    	int fileNameLength = request.getFileName().length();
		int contentLength = request.getCurrentContent().length;
		int length = 1+3+1+1+fileNameLength+2+contentLength;
    	
    	byte[] dataArea = new byte[length];
    	rtu.setHiAuthPassword("111111");
    	if(null == rtu.getHiAuthPassword())
    		return null;
    	
    	int loc = 0;
    	//permissions
    	dataArea[loc]=0x11;
    	loc+=1;    	
    	
    	//pwd
    	ParseTool.HexsToBytesAA(dataArea,loc,rtu.getHiAuthPassword(),3,(byte)0xAA);	
    	loc+=3;
    	
    	//fileNameLength
    	dataArea[loc] = (byte) fileNameLength;
    	loc+=1;
    	
    	//fileName
    	System.arraycopy(filename.getBytes(), 0, dataArea, loc, fileNameLength);
    	loc+=fileNameLength;
    	
    	//fileType
    	dataArea[loc]=(byte) request.getFileType();
    	loc+=1;
    	
    	//contentNum
    	Parser03.constructor(dataArea, ""+currentNum, loc, 2, 0);
    	loc+=2;
    	
    	//fileContent
    	System.arraycopy(fileContent, 0, dataArea, loc, contentLength);
    	loc+=contentLength;
    	
	
		MessageZj zjMsg = new MessageZj();
		zjMsg.head = createHead(rtu);
		zjMsg.data = ByteBuffer.wrap(dataArea);
		
		
		return new IMessage[]{zjMsg};
	}

	private MessageZjHead createHead(BizRtu rtu) {
		// 帧头数据
		MessageZjHead head = new MessageZjHead();
		head.c_dir = 0; // 主站下发
		head.c_expflag = 0; // 异常码
		head.c_func = (byte) 0x30; // 功能码
		head.rtua = rtu.getRtua();
		return head;
	}
}
