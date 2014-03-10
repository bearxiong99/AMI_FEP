package cn.hexing.fk.message.gw;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import cn.hexing.fk.common.spi.socket.IChannel;
import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.MessageConst;
import cn.hexing.fk.message.MessageType;
import cn.hexing.fk.utils.HexDump;
/**
 * ������Լ֡���Ķ���.֡��ʽ���£�
 * 68h+ 2��L(ÿL 2�ֽ�) + 68H + C + A1 + A2 + A3 + data + CS + 16H
 *
 */
public class MessageGw implements IMessage {
	private static final Logger log = Logger.getLogger(MessageGw.class);
	private static final MessageType type = MessageType.MSG_GW_10;
	private IChannel source;
	public ByteBuffer data = null;
	private ByteBuffer aux = null;
	private StringBuffer rawPacket = new StringBuffer(256);
	private byte[] prefix;		//ǰ���ַ���
	private int priority = IMessage.PRIORITY_LOW;	//low priority
	private long ioTime;		//�����յ���Ϣ���߷������ʱ��
	private String peerAddr;	//�Է���IP:PORT��ַ
	private String serverAddress;	//�ն�ʵ�����ӵ�����IP��PORT�����ڽ��յ���Ϣʱ���ն��ʲ���ȶԡ�
	private String txfs="";
	public MessageGwHead head = new MessageGwHead();
	//�����ڲ�����
	private int state = IMessage.STATE_INVALID;
	private byte _cs = 0;		//����ʱ����������У����
	private static final String charset = "ISO-8859-1";
	
	//by yangjie Ϊ��Լ������������
	/**ÿ����Ϣ��Ψһkey */
	public long key = 0;	//key = rtua | fseq<<32
    /** ��֡������״̬ */
    private String status;
    /** �������ݿ�����ID */
    private Long cmdId;
    private int msgCount;
    boolean isTask = false;

    public void setSendable(){
    	 state = IMessage.STATE_INVALID;
    }
	/**
	 * ������Լ֡��λ
	 * @param readBuffer
	 * @return
	 */
	public int locatePacket(ByteBuffer readBuffer)  throws MessageParseException{
		int pos = -1;
		int pos0 = readBuffer.position();
		boolean located = false;
		for( pos=pos0; pos+16 <= readBuffer.limit(); pos++){
			if( 0x68 != readBuffer.get(pos) )
				continue;
			if( 0x68 == readBuffer.get(pos+5) ){
				//ò���ҵ�������Լ֡����Ҫ�Ѹոն�λ��֮ǰ������Ϊprefix����������
				//��68xxxxxxxxxx68֮ǰ������Ϊprefix��������Ƕ�λʧ�ܣ���68xxxxxxxxxx������Ϊ����������68��ʼ�ҡ�

				//��1����������ݳ��ȣ������Ƿ�Ƿ���  ÿ֡���յ��ַ���Ϊ�û����ݳ���L1+8
				byte c1=0;
				short len1=0,len2=0;
				c1 = readBuffer.get(pos+1);
				len1 = (short)(c1 & 0xFF);
				c1 = readBuffer.get(pos+2);
				len1 |= (c1 & 0xFF) << 8;
				//short len1 = readBuffer.getShort(pos+1);
				head.decodeL(len1);
				//short len2 = readBuffer.getShort(pos+3);
				c1 = readBuffer.get(pos+3);
				len2 |= (c1 & 0xFF);
				c1 = readBuffer.get(pos+4);
				len2 |= (c1 & 0xFF) << 8;
				if( len1 != len2 || head.proto_flag != 2 || head.dlen-8<0 ){
					//�϶����󡣶���68֮��4�ֽڡ���ʾcontinue֮���ִ��pos++
					pos += 4;
					continue;
				}
				head.decodeC( readBuffer.get(pos+6) );
				
				//��2�������֡ǰ׺Я������Ϣ
				if( pos>pos0 ){
					prefix = new byte[pos-pos0];
					//�ȶ�λ�����һ��'|'
					int lastDelimiter = -1;
					for(int i=0;i<prefix.length;i++){
						prefix[i] = readBuffer.get();
						if( '|' == prefix[i] )
							lastDelimiter = i;
					}
					//���°��������й�����Լ��Ϣ����ʽ����
					//iotime=xxxx|peeraddr=xxxxx|txfs=xxx|������Լԭʼ֡
					if( prefix.length> 16 ){
						byte[] iot = "iotime=".getBytes();
						boolean isAttr = true;
						for(int j=0; j<iot.length; j++){
							if( iot[j] != prefix[j] ){
								isAttr = false;
								break;
							}
						}
						if( isAttr ){
							String attrs;
							try{
								attrs = new String(prefix,0,lastDelimiter,charset);
							}catch(UnsupportedEncodingException e){
								attrs = new String(prefix);
							}
							StringTokenizer st = new StringTokenizer(attrs,"|");
							String token = st.nextToken().substring(7);
							this.ioTime = Long.parseLong(token);
							this.peerAddr = st.nextToken().substring(9);
							if( st.hasMoreTokens() )
								this.txfs = st.nextToken().substring(5);
							byte[]p = new byte[prefix.length-lastDelimiter-1];
							for(int i=0;i<p.length; i++)
								p[i] = prefix[lastDelimiter+1+i];
							prefix = p;
						}
					}
					rawPacket.append(HexDump.hexDumpCompact(prefix,0, prefix.length));
				}
				located = true;
				break;
			}
		}
		if( !located ){
			//��������ݿ���ȫ�����ǷǷ����ݡ���Ҫ����
			for(; pos<readBuffer.limit(); pos++ ){
				if( 0x68 != readBuffer.get(pos) )
					continue;
			}
			byte[] bts = new byte[pos-pos0];
			readBuffer.get(bts);
			String expInfo = "exp:unmatched GW Protocol,Message will be discard:"+ HexDump.hexDumpCompact(bts, 0, bts.length);//String expInfo = "exp�������㽭��Լ�����ı�������"+ HexDump.hexDumpCompact(bts, 0, bts.length);
			if (expInfo.length()>1000)//�ַ�����������ȫ��ӡ
				expInfo=expInfo.substring(0,1000);
			log.warn(expInfo);
			throw new MessageParseException(expInfo);
		}
		//�Ӻ���ĵط���ʼ�����ģ�����ǰ�����ݡ�
		return pos;
	}

	public boolean read(ByteBuffer readBuffer) throws MessageParseException {
		if( state == IMessage.STATE_INVALID && readBuffer.remaining()<16 ){
			if( log.isDebugEnabled() )
				log.debug("���Ȳ����Զ�ȡ������Լ����ͷ��������ȡ��readBuffer.remaining="+readBuffer.remaining());
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( state == IMessage.STATE_INVALID ){	//��Ϣͷû�ж�ȡ
			//���ǰ���ַ�������λ������Լ֡ͷ
			int pos= locatePacket(readBuffer);
			if( readBuffer.limit()-pos < 16 ){
				if( log.isDebugEnabled() )
					log.debug("����������Լ��λ����˺󣬳��Ȳ����Զ�ȡ����ͷ��readBuffer.remaining="+readBuffer.remaining());
				return false;
			}
			//���ζ�ȡ ����ͷ�������塢����β
			readBuffer.position(pos);
			byte [] bts = new byte[14];
			_cs = 0;
			for( int i = 0; i< 14 ; i++ ){
				bts[i] = readBuffer.get(i+pos);		//����λ�ö���Ӱ��position()
				//�û������������ֽڵİ�λλ��������. �û����������������򡢵�ַ����·�û����ݣ�Ӧ�ò㣩������
				if( i> 5 )  // i>=6
					_cs += bts[i];
			}
			rawPacket.append(HexDump.hexDumpCompact(bts, 0, bts.length));
			
			head.flag1 = readBuffer.get();
			readBuffer.getShort();readBuffer.getShort();	//��ȡ2��L
			head.flag2 = readBuffer.get(); //�ڶ���68
			readBuffer.get(); //������C. ��locate�������Ѿ���ȡ
			byte c1 = readBuffer.get();
			head.rtua |= (c1 & 0xFF) << 16;
			c1 = readBuffer.get();
			head.rtua |= (c1 & 0xFF) << 24;
			c1 = readBuffer.get();
			head.rtua |= (c1 & 0xFF);
			c1 = readBuffer.get();
			head.rtua |= (c1 & 0xFF) << 8;
			head.decodeA3(readBuffer.get());
			//Ӧ�ò��2���ֽڣ������������浽����ͷ����Ϊ����֡���ǹ̶��ġ�
			head.app_func = readBuffer.get();
			head.decodeSEQ(readBuffer.get());
			//set priority
			if( head.c_dir == 0 ){
				//down
				if( afn() == 0x04 || afn() == 0x05 || afn() == 0x06 )
					this.priority = IMessage.PRIORITY_VIP;
				else if( afn() == 0x0d )
					this.priority = IMessage.PRIORITY_LOW;
				else
					this.priority = IMessage.PRIORITY_NORMAL;
			}
			else{
				//up
				if( head.c_prm == 1 ){
					if( afn() == 0x0E )
						this.priority = IMessage.PRIORITY_HIGH;
					else
						this.priority = IMessage.PRIORITY_LOW;
				}
				else{
					if( afn()== 0x0d )
						this.priority = IMessage.PRIORITY_NORMAL;
					else
						this.priority = IMessage.PRIORITY_VIP;
				}
			}
			//ʵ��Ӧ��������,����ҵ��仯��
			state = IMessage.STATE_READ_DATA;
			//head.dlen����= C + A + AFN + SEQ + data + aux
			//�Ѿ���ȡ8�ֽ�
			if( head.dlen-8 > readBuffer.remaining() ){	//��Ҫ�����ȴ�����
				if( log.isDebugEnabled() )
					log.debug("���л��������ݳ���[buflen="+readBuffer.remaining()+"]<������Լ����������["+(head.dlen+2)+"]");
				return false;
			}
			//�ܹ�������ȡ������
			return readDataSection(readBuffer);
		}
		else if( state == IMessage.STATE_READ_DATA || state == IMessage.STATE_READ_TAIL ){
			return readDataSection(readBuffer);
		}
		else{
			//״̬�Ƿ�
			log.error("��Ϣ��ȡ״̬�Ƿ�,state="+state);
		}
		return true;
	}
	
	private boolean readDataSection(ByteBuffer readBuffer) throws MessageParseException{
		if( null == data )
			data = ByteBuffer.allocate(head.dlen-8);
		if( state == IMessage.STATE_READ_DATA ){
			while( data.hasRemaining() ){		//������û�ж�ȡ���
				if( readBuffer.hasRemaining() )
					data.put(readBuffer.get());
				else
				{//������û�������ˣ����Ǳ����廹û�ж�ȡ���
					return false;
				}
			}
			data.flip();		//ready for read.
			byte[] d = data.array();
			for(int i=0; i<d.length; i++){
				_cs += d[i];
			}
			state = IMessage.STATE_READ_TAIL;
			rawPacket.append(HexDump.hexDumpCompact(data));
		}
		if( state == IMessage.STATE_READ_TAIL ){
			if( readBuffer.remaining()>=2 ){
				byte cs0 = readBuffer.get();
				rawPacket.append(HexDump.toHex(cs0));
				byte flag16 = readBuffer.get();
				rawPacket.append(HexDump.toHex(flag16));
				if( cs0 != _cs ){
					//У���벻һ��
					data = null;
					String packet = rawPacket.toString();
					rawPacket.delete(0, packet.length());
					state = IMessage.STATE_INVALID;		//���¿�ʼ����Ϣ��״̬��
					throw new MessageParseException("expУ���벻��ȷ:"+packet);
				}
				if( 0x16 != flag16 ){
					//֡���Դ��ڴ���������Ϊ0x16
					data = null;
					String packet = rawPacket.toString();
					rawPacket.delete(0, packet.length());
					state = IMessage.STATE_INVALID;		//���¿�ʼ����Ϣ��״̬��
					throw new MessageParseException("exp�����16��־����֡��ʽ����packet="+packet);
				}
				
				state = IMessage.STATE_READ_DONE;
				return true;
			}
		}
		//���б���βû�ж�ȡ��
		return false;
	}
	
	public boolean write(ByteBuffer writeBuffer) {
		synchronized(rawPacket){
			return _write(writeBuffer);
		}
	}

	/**
	 * д��Ϣ��Ҳ���뿼�ǲ��ֿ�д���������Ϣ���ܲ���һ���������ͳ�ȥ��
	 */
	private boolean _write(ByteBuffer writeBuffer) {
		int prefixLen = null==prefix ? 0 : prefix.length ;
		if( head.dlen == 0 )
			head.dlen = (short)(8 + (null != data ? data.remaining() : 0 ) + ( null != aux ? aux.remaining() : 0));
		if( (state == IMessage.STATE_INVALID || IMessage.STATE_READ_DONE == state)
				&& writeBuffer.remaining()<16+prefixLen ){
			log.info("д���峤�Ȳ��㣬�Ȼ������д��");
			return false;		//�������Բ��㣬�Ȼ��������ȡ
		}
		if( state == IMessage.STATE_INVALID //����´�������Ϣ
			|| IMessage.STATE_READ_DONE == state //������ʹ�����ͨ����ȡ����Ϣ��
			){	//��Ϣͷû��д
			if( rawPacket.length() >0 ){
				rawPacket.delete(0, rawPacket.length());
			}
			if( null != prefix ){
				writeBuffer.put(prefix);		//ǰ���ַ���
				rawPacket.append(HexDump.hexDumpCompact(prefix, 0, prefix.length));
			}
			//д��Ϣͷ
			int pos0 = writeBuffer.position();	//��Ϣд��֮ǰ��λ��
			byte c = 0;
			_cs = 0;
			writeBuffer.put((byte) 0x68);
			short stemp = head.encodeL();
			writeBuffer.putShort(stemp); writeBuffer.putShort(stemp);
			writeBuffer.put((byte) 0x68);
			c = head.encodeC();
			_cs += c;
			writeBuffer.put(c);
			
			//���A��ַ��
			c = (byte)( (head.rtua>>16) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte)( (head.rtua>>24) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte)( (head.rtua) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = (byte)( (head.rtua>>8) & 0xFF);
			_cs += c;
			writeBuffer.put(c);
			c = head.encodeA3();
			_cs += c;
			writeBuffer.put(c);
			
			//����Ӧ�ò��AFN��ҵ�����룩+ SEQ
			c = head.app_func;
			_cs += c;
			writeBuffer.put(c);
			c = head.encodeSEQ();
			_cs += c;
			writeBuffer.put(c);
			
			int pos1 = writeBuffer.position();
			byte[] bts = new byte[pos1-pos0];
			for(int i=0;i<bts.length;i++){
				bts[i] = writeBuffer.get(pos0+i);
			}
			rawPacket.append(HexDump.hexDumpCompact(bts, 0, bts.length));
			//֡ͷд��ϣ����濪ʼд�����岿��
			state = IMessage.STATE_SEND_DATA;
			return _writeDataSection(writeBuffer);
		}
		else if( IMessage.STATE_SEND_DATA == state || IMessage.STATE_SEND_TAIL == state ){
			return _writeDataSection(writeBuffer);
		}
		else return IMessage.STATE_SEND_DONE == state;
	}
	
	private boolean _writeDataSection(ByteBuffer writeBuffer){
		byte c;
		if( ! writeBuffer.hasRemaining() ){
			log.info("���ͻ���������Ϊ0�����͵���ʧ��");
			return false;
		}
		if( IMessage.STATE_SEND_DATA == state ){
			//����data����
			if( null != data && data.hasRemaining() ){
				while( data.hasRemaining() && writeBuffer.hasRemaining() ){
					c = data.get();
					_cs += c;
					writeBuffer.put(c);
				}
				if( !data.hasRemaining() ){
					data.rewind();
					rawPacket.append(HexDump.hexDumpCompact(data));
					data.position(data.limit());
				}
			}
			if( null != aux && aux.hasRemaining() ){
				while( aux.hasRemaining() && writeBuffer.hasRemaining() ){
					c = aux.get();
					_cs += c;
					writeBuffer.put(c);
				}
				if( !aux.hasRemaining() ){
					aux.rewind();
					rawPacket.append(HexDump.hexDumpCompact(aux));
					aux.position(aux.limit());
				}
			}
			//����Ƿ�ȫ��������ϡ�����ǲ��ַ�����ϣ��򷵻�false
			//�����data����data�������ݣ�������aux����aux�������ݣ���ôû�з������
			boolean notFinished = ( null!=data && data.hasRemaining() ) || (null!=aux && aux.hasRemaining() );
			if( notFinished ){
				if( log.isDebugEnabled() )
					log.debug("������̫�̣�����һ�ΰ������巢�����");
				return false;
			}
			//������������ϣ���Ҫ�ָ�data�Լ�aux
			if( null != data )
				data.rewind();
			if( null != aux )
				aux.rewind();
			state = IMessage.STATE_SEND_TAIL;
		}
		if( IMessage.STATE_SEND_TAIL == state ){
			//���ͱ���β
			if( writeBuffer.remaining()>=2 ){
				writeBuffer.put(_cs);		rawPacket.append(HexDump.toHex(_cs));
				writeBuffer.put((byte) 0x16); rawPacket.append("16");
				state = IMessage.STATE_SEND_DONE;
				return true;
			}
		}
		return false;
	}

    public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}

	public Long getCmdId() {
		return cmdId;
	}

	public int getMsgCount() {
		return msgCount;
	}

	public void setMsgCount(int msgCount) {
		this.msgCount = msgCount;
	}

	public long getIoTime() {
		return ioTime;
	}

	public MessageType getMessageType() {
		return type;
	}

	public String getPeerAddr() {
		return peerAddr;
	}

	public int getPriority() {
		return priority;
	}

	public byte[] getRawPacket() {
		byte[] ret;
		byte[] raw = HexDump.toByteBuffer(getRawPacketString()).array();
		if( ioTime>0 ){
			StringBuffer sb = new StringBuffer(64);
			sb.append("iotime=").append(ioTime);
			sb.append("|peeraddr=").append(peerAddr).append("|txfs=");
			sb.append(txfs).append("|");
			byte[] att = null;
			try{
				att = sb.toString().getBytes(charset);
			}catch(UnsupportedEncodingException e){
				att = sb.toString().getBytes();
			}
			ret = new byte[att.length+raw.length];
			System.arraycopy(att, 0, ret, 0, att.length);
			System.arraycopy(raw, 0, ret, att.length, raw.length);
		}
		else
			ret = raw;
		return ret;
	}

	public String getRawPacketString() {
		if( rawPacket.length()<13 ){
			ByteBuffer buf = ByteBuffer.allocate(1024*5);
			int _state = state;
			write(buf);
			state = _state;
		}
		return rawPacket.toString();
	}

	public String getServerAddress() {
		return serverAddress;
	}

	public IChannel getSource() {
		return source;
	}

	public String getStatus() {
		return status;
	}

	public String getTxfs() {
		return txfs;
	}

	public boolean isHeartbeat() {
		return head.app_func == MessageConst.GW_FUNC_HEART;
	}
	public int getFseq(){
		return head.seq_pseq;
	}
	public void setIoTime(long time) {
		ioTime = time;
	}

	public void setPeerAddr(String peer) {
		peerAddr = peer;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public void setServerAddress(String serverAddress) {
		this.serverAddress = serverAddress;
	}

	public void setSource(IChannel src) {
		source = src;
	}

	public void setTxfs(String fs) {
		txfs = fs;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getRtua(){
		return head.rtua;
	}
	
	public String getLogicalAddress(){
		return HexDump.toHex(head.rtua);
	}
	
	public void setLogicalAddress(String logicAddr){
		if(null != logicAddr && logicAddr.length()>0 ){
			head.rtua = (int)Long.parseLong(logicAddr, 16);
		}
	}

	public ByteBuffer getAux() {
		return aux;
	}

	/**
	 * ���ø�����Ϣ��
	 * @param aux �� ����������Ϣ��
	 * @param hasTp: ָʾ�Ƿ����ʱ���ǩ
	 */
	public void setAux(ByteBuffer aux, boolean hasTp) {
		this.aux = aux;
		this.head.seq_tpv = (byte)(hasTp ? 1 : 0);
	}
	
	public void setSEQ( byte seq ){
		head.seq_pseq = seq;
		if (aux!=null){//�������ʱ���ǩ�������е�֡��������Ҫͬ����ֵ
			aux.rewind();		
			String sAux=HexDump.hexDumpCompact(aux);
			if (sAux.length()==12&&seq<=15){//����֡���0~15
				sAux="0"+Integer.parseInt(""+seq,16)+sAux.substring(2);
				setAux(HexDump.toByteBuffer(sAux), true);
			}
		}
		 
	}
	
	public boolean isNeedConfirm(){
		return this.head.seq_con == 0x01;
	}
	/**
	 * ָ�������Ƿ���Ҫȷ��֡��
	 * @param needConfirm
	 */
	public void needConfirm( boolean needConfirm ){
		this.head.seq_con = (byte)( needConfirm ? 1 : 0);
	}

	public byte afn(){
		return getAFN();
	}
	public void afn(byte afn){
		setAFN(afn);
	}
	public byte getAFN(){
		return head.app_func;
	}
	
	public void setAFN( byte afn ){
		needConfirm(false);
		this.head.app_func = afn;
		byte frameFn = 0;
		switch( afn ){
		case MessageConst.GW_FUNC_REPLY:
			break;
		case MessageConst.GW_FUNC_RESET:
			frameFn = MessageConst.GW_FN_RESET;
			needConfirm(true);
			break;
		case MessageConst.GW_FUNC_HEART:	//����, ��·�ӿڼ��
			frameFn = MessageConst.GW_FN_HEART;
			break;
		case MessageConst.GW_FUNC_SETPARAM: //���ò���
		case MessageConst.GW_FUNC_CONTROL: //��������		
			frameFn = MessageConst.GW_FN_LEVEL1;
			needConfirm(true);
			break;		
		case MessageConst.GW_FUNC_AUTH: //�����֤����ԿЭ��
		case MessageConst.GW_FUNC_RELAY_CTRL: //�м�վ����
		case MessageConst.GW_FUNC_REQ_CASCADE_UP: //���󱻼����ն������ϱ�
		case MessageConst.GW_FUNC_REQ_RTU_CFG: //�����ն�����
		case MessageConst.GW_FUNC_GETPARAM: //��ѯ����
		case MessageConst.GW_FUNC_GET_TASK: //������������
		case MessageConst.GW_FUNC_GET_DATA1: //����1�����ݣ�ʵʱ���ݣ�
		case MessageConst.GW_FUNC_GET_DATA2: //����2�����ݣ���ʷ���ݣ�
		case MessageConst.GW_FUNC_GET_DATA3: //����3�����ݣ��¼����ݣ�
		case MessageConst.GW_FUNC_RELAY_READ: //����ת��(�м̳���)
		case MessageConst.GW_FUNC_FILE: //�ļ�����		
			frameFn = MessageConst.GW_FN_LEVEL2;
			break;			
		}
		this.head.c_func = frameFn;
	}
	
	/**
	 * �Ƿ��ǵ�½֡
	 * @return
	 */
	public boolean isLogin(){
		if(afn()!=MessageConst.GW_FUNC_HEART) return false;
		
		if(data.array().length!=4) return false;
		
		return data.array()[0]==0x00 && data.array()[1]==0x00&&data.array()[2]==0x01&&data.array()[3]==0x00;
	}
	
	public MessageGw createConfirm(){
		MessageGw con = null;
		if( head.seq_con == 1 ){
			con = new MessageGw();
			byte cfunc = 0;
			switch( head.c_func ){
			case 1:
				cfunc = 0;
				break;
			case 9:
				cfunc = 11;
				break;
			default:
				cfunc = 8;
			}
			con.head.c_func = cfunc;
			con.head.c_prm = 0;
			con.head.dlen = 12;
			con.head.rtua = head.rtua;
			con.head.seq_con = 0;
			con.head.seq_pseq = head.seq_pseq;
			byte[] repData = { 0x00, 0x00, 0x01, 0x00 };
			con.data = ByteBuffer.wrap(repData);
		}
		return con;
	}
	
	public int length(){
		return head.dlen + 8;
	}

	public boolean isTask() {
		return isTask;
	}

	public void setTask(boolean isTask) {
		this.isTask = isTask;
	}

	@Override
	public String toString() {
		return this.getRawPacketString();
	}
}
