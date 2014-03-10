package cn.hexing.fk.gate.event.autoreply;

import cn.hexing.fk.message.zj.MessageZj;

public class AutoReplyMessageZj {
	public static final MessageZj reply(MessageZj msg){
		//自动应答规则需注意，可能厂家理解不一样，目前按大多数厂家理解做的
		int func = msg.head.c_func & 0xFF;
		//终端登录、退出、心跳自动成功应答
		if( msg.head.c_expflag != 0 )
			return null;
		MessageZj rep = null;
		if( 0x21 == func ){
			rep = new MessageZj();
			rep.head.c_func = (byte)func;
			rep.head.fseq = msg.head.fseq;
			rep.head.rtua = msg.head.rtua;
			rep.head.c_dir = 0;
			rep.data = null;
		}
		else if( 0x22 == func ){
			rep = new MessageZj();
			rep.head.c_func = (byte)func;
			rep.head.fseq = msg.head.fseq;
			rep.head.rtua = msg.head.rtua;
			rep.head.c_dir = msg.head.c_dir;	//特殊性
			rep.data = null;
		}
		else if( 0x24 == func ){
			rep = new MessageZj();
			rep.head.c_func = (byte)func;
			rep.head.fseq = msg.head.fseq;
			rep.head.rtua = msg.head.rtua;
			rep.head.c_dir = (byte)(~(msg.head.c_dir | 0xFE));	//A4--24 24--A4
			rep.data = null;
		}
		return rep;
	}
}
