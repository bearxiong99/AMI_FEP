package cn.hexing.fk.message;

public class MessageConst {
	//�㽭��Լ��Ϣ��������
	/**
	 * �м�
	 */
	public static final byte ZJ_FUNC_RELAY = 0x00;
	/**
	 * ����ǰ����
	 */
	public static final byte ZJ_FUNC_READ_CUR = 0x01;
	/**
	 * ����������
	 */
	public static final byte ZJ_FUNC_READ_TASK = 0x02;
	/**
	 * �������־
	 */
	public static final byte ZJ_FUNC_READ_PROG	= 0x04;
	/**
	 * ʵʱд�������
	 */
	public static final byte ZJ_FUNC_WRITE_ROBJ = 0x07;	
	/**
	 * д�������
	 */
	public static final byte ZJ_FUNC_WRITE_OBJ = 0x08;
	/**
	 * �쳣�澯
	 */
	public static final byte ZJ_FUNC_EXP_ALARM	= 0x09;
	/**
	 * �澯ȷ��
	 */
	public static final byte ZJ_FUNC_ALARM_CONFIRM = 0x0A;
	/**
	 * ���㳭��������
	 */
	public static final byte GG_FUNC_READ_TASK1 = 0x11;
	/**
	 * ��漯����������
	 */
	public static final byte GG_FUNC_READ_TASK2 = 0x12;
	/**
	 * ��漯��������������
	 */
	public static final byte GG_FUNC_Action = 0x14;
	/**
	 * ��漯��������ע����Ϣ����
	 */
	public static final byte GG_FUNC_AutoRegistered = 0x15;
	/**
	 * ��漯���¼��澯����
	 */
	public static final byte GG_FUNC_Event = 0x19;
	/**
	 * ���Ԥ������Ϣ����
	 */
	public static final byte GG_Pay_token = 0x33;
	
	/**
	 * ���Զ������
	 */
	public static final byte GG_UPGRADE=0x30;
	

	/**
	 * �û��Զ�������
	 */
	public static final byte ZJ_FUNC_USER_DEFINE = 0x0F;
	public static final byte ZJ_FUNC_LOGIN = 0x21;			//��¼
	public static final byte ZJ_FUNC_LOGOUT = 0x22;		//��¼�˳�
	public static final byte ZJ_FUNC_HEART = 0x24;			//��������
	public static final byte ZJ_FUNC_REQ_SMS = 0x28;		//�����Ͷ���
	public static final byte ZJ_FUNC_RECV_SMS = 0x29;		//�յ������ϱ�
	
	
	//��Ϣ������
	public static final byte DIR_DOWN = 0x00;			//����վ����������֡
	public static final byte DIR_UP = 0x01;				//���ն˷�����Ӧ��֡
	
	//����Ӧ�ò㹦���붨��  16���ƶ���
	public static final byte GW_FUNC_REPLY = 0x00;		//ȷ�ϨM����
	public static final byte GW_FUNC_RESET = 0x01;		//��λ
	public static final byte GW_FUNC_HEART = 0x02;		//����, ��·�ӿڼ��
	public static final byte GW_FUNC_RELAY_CTRL = 0X03;		//�м�վ����
	public static final byte GW_FUNC_SETPARAM = 0x04;	//���ò���
	public static final byte GW_FUNC_CONTROL = 0x05;	//��������
	public static final byte GW_FUNC_AUTH = 0x06;		//�����֤����ԿЭ��
	public static final byte GW_FUNC_BAK1 = 0x07;		//����
	public static final byte GW_FUNC_REQ_CASCADE_UP=0x08;	//���󱻼����ն������ϱ�
	public static final byte GW_FUNC_REQ_RTU_CFG = 0x09;	//�����ն�����
	public static final byte GW_FUNC_GETPARAM = 0x0A;		//��ѯ����
	public static final byte GW_FUNC_GET_TASK = 0x0B;		//������������
	public static final byte GW_FUNC_GET_DATA1 = 0x0C;		//����1�����ݣ�ʵʱ���ݣ�
	public static final byte GW_FUNC_GET_DATA2 = 0x0D;		//����2�����ݣ���ʷ���ݣ�
	public static final byte GW_FUNC_GET_DATA3 = 0x0E;		//����3�����ݣ��¼����ݣ�
	public static final byte GW_FUNC_FILE = 0x0F;			//�ļ����� �����Զ�������
	public static final byte GW_FUNC_RELAY_READ = 0x10;		//����ת�����м̳���
	public static final byte GW_FUNC_BAK2 = 0x11;			//11H��FFH,����
	
	//������Լ֡���湦����	10���ƶ��� ��վ����
	public static final byte GW_FN_RESET = 1;				//��λ����
	public static final byte GW_FN_USER = 4;				//�û�����
	public static final byte GW_FN_HEART = 9;				//���� ���߽� ��·����
	public static final byte GW_FN_LEVEL1 = 10;				//����1������ Ӧ�ò�����ȷ�ϣ�CON=1��
	public static final byte GW_FN_LEVEL2 = 11;				//����2������

	//dlms��Լ������
	public static final short DLMS_FUNC_HEART = 0xDD;			//��������
	
	public static final String DLMS_RELAY_FLAG = "dlms-relay";
}

