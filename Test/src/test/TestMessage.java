package test;

import java.nio.ByteBuffer;

import com.hx.dlms.message.DlmsMessage;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.bengal.BengalMessage;
import cn.hexing.fk.message.gate.MessageGate;
import cn.hexing.fk.message.gw.MessageGw;
import cn.hexing.fk.message.zj.MessageZj;
import cn.hexing.fk.utils.HexDump;

public class TestMessage {
	private static final byte[] gateFlag = "JBBS".getBytes();
	public static void main(String[] args) {
		ByteBuffer bb = HexDump.toByteBuffer("696F74696D653D313334383930323931313338387C70656572616464723D3231312E3134302E352E3132313A36343236303A547C747866733D30327C0001000100100025CC233000000EFCCED1EC45E4D1A7AEB01BDE8C94D75E3BD7EA01C381BE5CE1E2FD5C771373");
		System.out.println(getMessage(bb));
	}
	
	public static IMessage getMessage(ByteBuffer buf){
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
		int last68 = -1;
		for( int pos = buf.position(); pos+10 <= buf.limit(); pos++){
			//�ȶ�λ��һ��68H
			if( 0x68 != buf.get(pos) )
				continue;
			last68 = pos;
			//�ȶ�λ������Լ
			if( 0x68 == buf.get(pos+5) ){
				//�����ǹ�����Լ��68H��L(2)+L(2)+68H
				short len1 = buf.getShort(pos+1);
				short len2 = buf.getShort(pos+3);
				if( len1 == len2 )
					return new MessageGw();
			}
			//�ٶ�λ�㽭��Լ.������㽭��Լ����ôpos+5λ�þ��Բ�������68H
			if( 0x68 == buf.get(pos+7) ){
				return new MessageZj();
			}
		}
		buf.position(startPos);
		for(int pos=buf.position(); pos<buf.limit()-1; pos++ ){
			byte c = buf.get(pos);
			if( Character.isLetterOrDigit(c) || c == '|' ){
				continue;
			}
			if( c == 0x00 ){
				if( buf.get(pos+1) == 0x01 ){
					return new DlmsMessage();
				}
			}
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
			System.out.println(("���Լʶ����������Ч���ݣ�"+HexDump.hexDumpCompact(dump, 0, dump.length)));
			return null;
	}
}
