package cn.hexing.fk.message.gw;

public class MessageGwHead {
	public byte flag1=0x68; //֡��ʼ�� 68h
	public byte flag2=0x68;
	public short dlen = 0;	//����L���������򡢵�ַ����·�û����ݣ���2�ֽ���� d2-d15
	public byte proto_flag = 2;//����L��d0d1��λ 
	public int rtua = 0;	//a1a2 ����������+�ն˵�ַ
	public byte a3_d0=0; 	//�ն����ַ��־��D0=0��ʾ�ն˵�ַA2Ϊ����ַ��D0=1��ʾ�ն˵�ַA2Ϊ���ַ
	public byte a3_msa=1; 	//A3��D1��D7���0��127����վ��ַMSA
	public byte c_dir=0; 	//���䷽��λ,C�ֽڵ�d7λ. 0 ��ʾ���� 1��ʾ�ն����С�
	public byte c_prm=1;	//������־λ, d6  PRM =1����ʾ��֡������������վ��PRM =0����ʾ��֡�������ԴӶ�վ
	public byte c_acd=0;	//D5λ��ACDλ����������Ӧ�����С�ACD=1��ʾ�ն�����Ҫ�¼��ȴ�����
	public byte c_fcv=0;	//D4.  FCV=1����ʾFCBλ��Ч��FCV=0����ʾFCBλ��Ч��
	protected byte c_func=0;	//D0-D3 �����롣
	
	protected byte app_func=0;	//Ӧ�ò㹦����
	public byte seq_tpv=0;	//֡�������ֽ�D7 0����ʾ�ڸ�����Ϣ������ʱ���ǩTp
	public byte seq_fir=1;	//D6 �á�1�������ĵĵ�һ֡
	public byte seq_fin=1;	//D5 �á�1�������ĵ����һ֡
	protected byte seq_con=1;	//D4 λ�á�1������ʾ��Ҫ�Ը�֡���Ľ���ȷ��
	public byte seq_pseq=0;	//d0-d3
	
	//��Լ��L�ֶ�
	public void decodeL(short L){
		proto_flag = (byte)(L & 0x3) ;		// 2 ��ʾQ��GDW 376.1-2009�������û��õ���Ϣ�ɼ�ϵͳͨ��Э�飺��վ��ɼ��ն�ͨ��Э�顷
		dlen = (short)(L >>> 2) ;
	}
	
	public short encodeL(){
		int len=(((dlen & 0xFFFF)<<2) | proto_flag );
		len= (len<<8)& 0x00FF00 | ((len>>> 8) & 0x00FF);
		return (short)len;
		//return (short)(((dlen & 0xFFFF)<<2) | proto_flag );
	}
	
	//��ȡ������C
	public void decodeC(byte C){
		c_dir = (byte)( ( 0x80 & C ) >>> 7);
		c_prm = (byte)( ( 0x40 & C ) >>> 6);
		c_acd = (byte)( ( 0x20 & C ) >>> 5);
		c_fcv = (byte)( ( 0x10 & C ) >>> 4);
		c_func =(byte)( 0x0F & C );
	}
	
	public byte encodeC(){
		int c = c_dir << 7;
		c |= c_prm << 6;
		c |= c_acd << 5;
		c |= c_fcv << 4;
		c |= c_func;
		return (byte)c;
	}
	
	public void decodeA3(byte A3){
		a3_d0 = (byte)( 0x01 & A3);
		a3_msa = (byte)( A3>>>1 );
	}
	
	public byte encodeA3(){
		return (byte)((a3_msa << 1) | a3_d0);
	}
	
	public void decodeSEQ(byte SEQ){
		seq_tpv = (byte)( ( 0x80 & SEQ ) >>> 7);
		seq_fir = (byte)( ( 0x40 & SEQ ) >>> 6);
		seq_fin = (byte)( ( 0x20 & SEQ ) >>> 5);
		seq_con = (byte)( ( 0x10 & SEQ ) >>> 4);
		seq_pseq =(byte)( 0x0F & SEQ );
	}
	
	public byte encodeSEQ(){
		int c = seq_tpv << 7;
		c |= seq_fir << 6;
		c |= seq_fin << 5;
		c |= seq_con << 4;
		c |= (seq_pseq & 0x0F);
		return (byte)c;
	}
}
