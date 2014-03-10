/**
 * 浙江电力负控系统－通讯系统启动模块
 */
package cn.hexing.fas.startup;

import cn.hexing.fas.model.FaalGWNoParamRequest;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fas.protocol.handler.ProtocolHandler;
import cn.hexing.fas.protocol.handler.ProtocolHandlerFactory;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.gw.MessageGw;


/**
 *
 */
public class Application {

	/**
	 * @param args
	 */
	
	public static void main(String[] args) {
		initialize();
		IMessage msg = new MessageGw();
		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();        
        ProtocolHandler handler = factory.getProtocolHandler(MessageGw.class);
        //IMessage[] responses = handler.process(msg);  
    	FaalGWNoParamRequest request= new FaalGWNoParamRequest();
        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
        int[] tn={0};
//        request.setTpSendTime("02 16:25:00");
        request.setTpTimeout(10);
        rtuParam.setTn(tn);
        rtuParam.setRtuId("33334444");
        rtuParam.addParam("04F010", "1#2#0#0#0#0#0#0#0#0#0");
        request.setType(1);
        request.setProtocol("02");
        request.addRtuParam(rtuParam);
        IMessage[] messages = handler.createMessage(request);
        System.out.println(messages[0]);
		
//		ProtocolHandlerFactory factory = ProtocolHandlerFactory.getInstance();        
//        ProtocolHandler handler = factory.getProtocolHandler(MessageZj.class);
//        //IMessage[] responses = handler.process(msg);  
//        FaalReadCurrentDataRequest request= new FaalReadCurrentDataRequest();
//        FaalRequestRtuParam rtuParam=new FaalRequestRtuParam();
//        int[] tn={1};
//        rtuParam.setRtuId("A0T0013040");
//        rtuParam.setCmdId(new Long(0));
//        rtuParam.setTn(tn);
//        rtuParam.addParam("0100100000","");
//        request.addRtuParam(rtuParam);
	}
	
	public static void initialize(){
		ClassLoaderUtil.initializeClassPath();
	}
	
}
