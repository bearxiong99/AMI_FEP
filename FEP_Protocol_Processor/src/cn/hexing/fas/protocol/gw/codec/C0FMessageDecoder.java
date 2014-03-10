package cn.hexing.fas.protocol.gw.codec;

import java.util.ArrayList;
import java.util.List;

import cn.hexing.exception.MessageDecodeException;
import cn.hexing.fas.model.FaalGWAFN0FRequest;
import cn.hexing.fas.model.HostCommand;
import cn.hexing.fas.model.HostCommandResult;
import cn.hexing.fas.protocol.gw.parse.DataItemParser;
import cn.hexing.fas.protocol.gw.parse.DataSwitch;
import cn.hexing.fas.protocol.gw.parse.ParseTool;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;

/**
 * 文件传输（AFN=0FH） 上行消息解码器
 * @author Administrator
 *
 */


public class C0FMessageDecoder extends AbstractMessageDecoder {	
	
    public Object decode(IMessage message) {    
		HostCommand hc=new HostCommand();
		List<HostCommandResult> value=new ArrayList<HostCommandResult>();
		try{			
			String data=ParseTool.getMsgData(message);	
			MessageGw msg = (MessageGw) message;
			BizRtu rtu=(RtuManage.getInstance().getBizRtuInCache(msg.getRtua()));
			if (msg.head.seq_tpv==1)//TpV位置1表示附加域有时间标签
				data=data.substring(0,data.length()-12);//消去6个字节的时间标签
			if (msg.head.c_acd==1)//ACD位置1表示附加域有事件计数器
				data=data.substring(0,data.length()-4);//消去2个字节的事件计数器
			String sAFN=Integer.toString(msg.getAFN()& 0xff,16).toUpperCase();
			sAFN=DataSwitch.StrStuff("0", 2, sAFN, "left");				
			while(data.length()>=8){
				int[] tn=DataItemParser.measuredPointParser(data.substring(0,4));
				String[] codes= DataItemParser.dataCodeParser(data.substring(4,8), sAFN);
				data=data.substring(8);//消去数据单元标识
				data=DataSwitch.ReverseStringByByte(data);
				int idata=Integer.parseInt(data,16);
				rtu.removeParamFromFileMap(idata);
				rtu.addParamToCurrentMessageCountMap(1, idata);
				FaalGWAFN0FRequest request = (FaalGWAFN0FRequest) rtu.getUpgradeParams().get("request");
				rtu.removeParamFromMap(msg.head.seq_pseq);
				data=data.substring(8);
		        HostCommandResult hcr=new HostCommandResult();
		        hcr.setSoftUpgradeID(request.getSoftUpgradeID());
		        hcr.setLogicAddr(rtu.getLogicAddress());
		        hcr.setMessageCount(rtu.getParamFromMessageCountMap(1));
	    		hcr.setCode(codes[0]);
	    		hcr.setValue("0");
	    		hcr.setCurrentMessage(idata+1);//计数从0开始的，所以加1.
	    		if((idata+1)==(rtu.getParamFromMessageCountMap(1))){
	    			hcr.setStatus(11);
	    		}else{
	    			hcr.setStatus(2);
	    		}
	    		hcr.setTn(String.valueOf(tn[0]));
	    		hcr.setCommandId(hc.getId());
	    		value.add(hcr);
			}
	        hc.setStatus(HostCommand.STATUS_SUCCESS);
	        hc.setResults(value);
		}catch(Exception e){
			throw new MessageDecodeException(e);
		}
		return hc;	
    }
}
    
    