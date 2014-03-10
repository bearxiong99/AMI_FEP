package cn.hexing.fk.message;

import java.math.BigInteger;
import java.nio.ByteBuffer;

import org.apache.log4j.Logger;

import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.utils.HexDump;

import com.hx.ansi.message.AnsiMessage;
import com.hx.dlms.message.DlmsHDLCMessage;
import com.hx.dlms.message.DlmsMessage;

public class MultiProtoRecognizer implements IMessageCreator{
	private static final Logger log = Logger.getLogger(MultiProtoRecognizer.class);
	private static final byte[] gateFlag = "JBBS".getBytes();
	/**
	 * ���Լʶ����.
	 * ������Լ����16�ֽڡ��㽭��Լ����13�ֽڡ�
	 * ���������<13����ô����ʶ����Ϣ����
	 * @param buf
	 * @return
	 */
	public static IMessage recognize(ByteBuffer buf) {
		if( buf.remaining()<9 )//dlms��������ȷ��9���ֽ�
			return null;	//���������Ȳ�����ʶ��������
		int startPos = buf.position();
		//0. �ж��Ƿ��ַ���
		byte ch = 0;
		int p = buf.position();
		while(buf.hasRemaining() && (ch = buf.get(p)) == 0 ){
			buf.get();
			p++;
		}
		if( ch == '<' || ch == '\n' || ch == '\r' ){
			int len = Math.min(buf.remaining(), 60);
			byte[] xmlBytes = new byte[len];
			int posSave = buf.position();
			buf.get(xmlBytes);
			buf.position(posSave);
			String xmlFrag = new String(xmlBytes);
			if( xmlFrag.indexOf("xml")>0 && xmlFrag.indexOf("ProtocolHead")>0 )
				return new BengalMessage();
		}
		
		
		//1. ��ʶ�����ع�Լ
		int flen = gateFlag.length;
		boolean matched = true;
		for( int pos = buf.position(); pos+flen < buf.limit(); pos++ ){
			matched = true;
			for( int i=0;i<flen; i++){
				if( buf.get(pos+i) != gateFlag[i] ){
					matched = false;
					break;
				}
			}
			if( matched ){
				buf.position(pos);
				break;
			}
		}
		if( matched )
			return new MessageGate();
		//2. ʶ���㽭��Լ���߹�����Լ
		IMessage gwOrZjMsg = null;
		int gwOrZjLoc=0;
		int last68 = -1;
		for( int pos = buf.position(); pos+10 <= buf.limit(); pos++){
			//�ȶ�λ��һ��68H
			gwOrZjLoc++;
			if( 0x68 != buf.get(pos) )
				continue;
			last68 = pos;
			//�ȶ�λ������Լ
			if( 0x68 == buf.get(pos+5) ){
				//�����ǹ�����Լ��68H��L(2)+L(2)+68H
				short len1 = buf.getShort(pos+1);
				short len2 = buf.getShort(pos+3);
				if( len1 == len2 ){
					gwOrZjMsg = new MessageGw();					
					break;
				}
				
			}
			//�ٶ�λ�㽭��Լ.������㽭��Լ����ôpos+5λ�þ��Բ�������68H
			if (0x68 == buf.get(pos + 7)) {// �㽭��Լ�ж�̫�򵥣���Ҫ���������ж�
				// 68 xx xx xx xx xx xx 68 C LL HL xx..xx CS 0x16
				try {
					byte ll = buf.get(pos + 9); // ���ȵ��ֽ�
					byte hl = buf.get(pos + 10);// ���ȸ��ֽ�
					byte[] len = new byte[] { hl, ll };
					BigInteger bi = new BigInteger(len);
					int iLen = bi.intValue();
					if (buf.limit() < pos + iLen + 12 && pos + iLen + 12 > 0)
						continue;

					if (0x16 == buf.get(pos + iLen + 12)) {// �����ټ��ϼ���У����
						gwOrZjMsg = new MessageZj();
						break;
					}
				} catch (Exception e) {
					break;
				}
			}
		}
	
		//Dlms��·��֡
		buf.position(startPos);
		for(int pos=buf.position();pos<buf.limit()-2;pos++){
			
			if(gwOrZjMsg!=null) break;
			
			byte c = buf.get(pos);
			if( Character.isLetterOrDigit(c) || c == '|' ){
				continue;
			}
			if(c==(byte)0x7E){
				if(buf.get(pos+1)==(byte)0xA0){//�����жϲ�̫��ȫ�����ܻ���Ҫ��������ж�
					
					int len=buf.get(pos+2)&0xFF;
					try {
						if(0x7E==buf.get(len+1))
							return new DlmsHDLCMessage();
					} catch (Exception e) {
						break;
					}
				}
			}
		}
		
		IMessage dlmsOrAnsiMsg=null;
		int dlmsOrAnsiMsgLoc=0;
		/*
		 *Dlms֡ &Ansi֡
		 */
		buf.position(startPos);
		for(int pos=buf.position(); pos<buf.limit()-2; pos++ ){
			byte c = buf.get(pos);
			dlmsOrAnsiMsgLoc++;
			if( Character.isLetterOrDigit(c) || c == '|' ){
				continue;
			}
			
			//�����һ�ν���ѭ��,������������,ֱ����Ϊ��DLMS֡
			if( c == 0x00 ){
				if( buf.get(pos+1) == 0x01 ){
					//���׼ȷ��λ��DLMS֡�����Ҳ�Ӱ������֡��λ
					dlmsOrAnsiMsg=new DlmsMessage();
					break;
				}
			}
			//Ʃ��֡��������60xxA2...0001 ....���֡��ansi(��������жϣ��϶�����ΪDLMS)
			//���֡������:0001...60xxA200001���֡��DLMS(������ж�ansi����ô�϶�����Ϊ��ansi)
			if(c == 0x60 && 
			(buf.get(pos+2) == (byte)0xA2 || 
			 buf.get(pos+2) == (byte)0xA4 ||  
			 buf.get(pos+2) == (byte)0xA8 ||  
			 buf.get(pos+2) == (byte)0xBE)){
				dlmsOrAnsiMsg = new  AnsiMessage();
				break;
			}
		}
		buf.position(startPos);
		if(dlmsOrAnsiMsg!=null || gwOrZjMsg!=null){
			//ͬʱ��λ����������,����֡��ʼ��λ�ã������ʼλ��С�����Ǹ�����֡�Ŀ����Դ�
			return gwOrZjLoc>dlmsOrAnsiMsgLoc?dlmsOrAnsiMsg:gwOrZjMsg;
		}
		
		//3. ���л���������ʶ����Ҫ��68֮ǰ������Ϣ������
		byte[] dump = null;
		if( last68 == -1 ){
			dump = new byte[buf.remaining()>200 ? 200 : buf.remaining() ];
			for(int i=0; i<dump.length; i++)
				dump[i] = buf.get(buf.position()+i);
			//δ����68��ȫ������
			buf.position(0); buf.limit(0);
		}
		else{
			//�������һ��68֮ǰ���ݡ�
			int len = buf.limit() - last68;
			if( len == 0 ){
				//��ӡ��������������
				dump = new byte[ buf.remaining() > 200 ? 200 : buf.remaining() ];
				for(int i=0; i<dump.length; i++)
					dump[i] = buf.get(buf.position()+i);
				//��ջ�����
				buf.position(0); buf.limit(0);
			}
			else{
				//��ӡ��������������
				int rem = last68-buf.position();
				dump = new byte[rem>200 ? 200 : rem ];
				for(int i=0; i<dump.length; i++ )
					dump[i] = buf.get(buf.position()+i);
				for(int i=0; i<len; i++ )
					buf.put(i, buf.get(last68+i));
				buf.position(0);
				buf.limit(len);
			}
		}
		if( log.isDebugEnabled() ){
			log.debug("���Լʶ����������Ч���ݣ�"+HexDump.hexDumpCompact(dump, 0, dump.length));
		}
		return null;
	}
	
	public IMessage create() {
		return null;
	}
	public IMessage createHeartBeat(int reqNum) {
		return null;
	}

}
