/**
 * ����Socket���������󹫹��ӿڶ��壬�����첽TCP�������Լ�ͬ��UDP��������
 */
package cn.hexing.fk.common.spi.socket;

import java.nio.ByteBuffer;

import cn.hexing.fk.common.spi.IModStatistics;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.message.IMessage;

/**
 *
 */
public interface ISocketServer extends IModStatistics,IModule{
	/**
	 * �����������˿�
	 * @return
	 */
	int getPort();
	
	/**
	 * Ϊ��֧���ն����ӵ�������ʵ�ʶ�ӦIP��ַ����Ҫ����IP:PORT��
	 * ��socket��������ȡ��ip��ַ����ӳ�䵽�����ĵ�ַ��
	 * @return
	 */
	String getServerAddress();
	
	/**
	 * ��������IO�����������ڴ����յ�������������Ҫ������������
	 * IOHandler�ܹ����յ��������н�������������Ϣ�����߰���Ϣ���������������ͳ�ȥ��
	 * @return
	 */
	IClientIO getIoHandler();
	
	/**
	 * ��������IO�����̳߳ش�С�������ն�TCP������������ÿ100���ն�����1��IO�̡߳�
	 * @return
	 */
	int getIoThreadSize();
	
	/**
	 * ���ͻ��˶Ͽ�����ʱ���ӷ�������ά�����б���ɾ����
	 * @param client
	 */
	void removeClient(IServerSideChannel client);
	
	int getClientSize();	//�������Ѿ����ӵ�socket client������
	/**
	 * ���ر��������������client���顣
	 * @return
	 */
	IServerSideChannel[] getClients();
	
	/**
	 * �������������Լ��ܹ��������Ϣ�����Ա��Լ���IOHandler���ж�д����
	 * @return
	 */
	IMessage createMessage(ByteBuffer buf);
	
	/**
	 * ������������ȱʡsocket���������ȡ�
	 * @return
	 */
	int getBufLength();
	boolean useDirectBuffer();
	String getMonitedIPs();
	

	/**
	 * ����ͨ���������ơ���ֹ���ɶ�ȡ���ݣ�����������Ӧ��
	 * @return
	 */
	int getMaxContinueRead();
	int getWriteFirstCount();
	
	/**
	 * �����������ͳ����Ϣ�Ĵ���
	 */
	void setLastReceiveTime(long lastRecv);
	void setLastSendTime(long lastSend);
	void incRecvMessage();
	void incSendMessage();
}
