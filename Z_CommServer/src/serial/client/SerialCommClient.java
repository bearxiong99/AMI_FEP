package serial.client;

import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TooManyListenersException;

import cn.hexing.fk.exception.MessageParseException;
import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.message.msgbytes.MessageBytes;
import cn.hexing.fk.message.msgbytes.MessageBytesCreator;
import cn.hexing.fk.utils.HexDump;

/**
 * 
 * @author gaoll
 *
 * @time 2013-4-10 ����03:29:47
 *
 * @info ���ڷ�����,����ĳ����,�����ڷ��������������͸����ء�
 *       �����ݾ������ݣ���ʱ�����ط���������
 * 		������������Ϣ(У��λ,������).
 * 	             ���ض˿���Ϣ(�������ӵ�����).
 * 	            �������server�Ķ����ն�,����Ҫ���ն˺�(��������ʹ��),���ݲ�ͬ��Լ���Ͳ�ͬ����,�����CSDģʽ�أ�����Ҫ��������.
 * 	            ���ط���������Ϣ�������ȷ����Ϣ,Ӧ�ò�������,������Ϣ��͸��ת��.
 */
public class SerialCommClient implements SerialPortEventListener,Runnable{

	protected SerialTcpClient channelClient;
	ByteBuffer buffer = ByteBuffer.allocateDirect(1000);

	protected InputStream input = null;
	protected OutputStream output = null;
	protected SerialPort port = null;
	protected String portName;
	CommPortIdentifier cpi = null;
	public enum Setting {PARAMS_TIMEOUT,PARAMS_PORT,PARAMS_DATABITS,PARAMS_STOPBITS,PARAMS_PARITY,PARAMS_RATE,GATE_PORT,GATE_IP};
	
	Map<Setting,String> params;
	
	List<IMessage> messageLists = new ArrayList<IMessage>();
	
	private boolean active = false;
	
	@Override
	public void serialEvent(SerialPortEvent event) {
		onReceive(event);
	}
	
	public SerialCommClient(Map<Setting,String> params){
		this.params = params;
	}
	
	private void init() {
		int timeOut = Integer.parseInt(params.get(Setting.PARAMS_TIMEOUT));
		String portName=params.get(Setting.PARAMS_PORT).toUpperCase();
		int dataBit = Integer.parseInt(params.get(Setting.PARAMS_DATABITS));
		int stopBit = Integer.parseInt(params.get(Setting.PARAMS_STOPBITS));
		int parity = Integer.parseInt(params.get(Setting.PARAMS_PARITY));
		int rate=Integer.parseInt(params.get(Setting.PARAMS_RATE));
		
		String gateIp = params.get(Setting.GATE_IP);
		int gatePort = Integer.parseInt(params.get(Setting.GATE_PORT));
		
		try {
			cpi = CommPortIdentifier.getPortIdentifier(portName);
			port = (SerialPort) cpi.open(this.toString(), timeOut);
			input = port.getInputStream();
			output = port.getOutputStream();
			port.addEventListener(this);
			port.notifyOnDataAvailable(true);
			port.setSerialPortParams(rate, dataBit, stopBit, parity);
			//����client
			channelClient = new SerialTcpClient();
			channelClient.setHostIp(gateIp);
			channelClient.setCommClient(this);
			channelClient.setMessageCreator(new MessageBytesCreator());
			channelClient.setHostPort(gatePort);
			channelClient.start();
			active=true;
			new WriteThread().run();
			
		} catch (NoSuchPortException e) {
			e.printStackTrace();
		} catch (PortInUseException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TooManyListenersException e) {
			e.printStackTrace();
		} catch (UnsupportedCommOperationException e) {
			e.printStackTrace();
		}
	}

	public boolean connect(){
		init();
		return true;
	}
	
	@Override
	public void run() {
		connect();
	}
	
	public boolean sendMessage(String content){
		return sendMessage(content.getBytes());
	}
	
	public boolean sendMessage(byte[] content){
		try {
			System.out.println("send msg:"+HexDump.toHex(content));
			output.write(content);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}
	
	class WriteThread extends Thread{
		
		@Override
		public void run(){
			
			while(active){
				synchronized (messageLists) {
					while(messageLists.size()==0){
						try {
							messageLists.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					if(!active) return;
					IMessage message = messageLists.remove(0);
					sendMessage(HexDump.toArray(message.toString()));
				}
			}
		}
	}
	
	public void offerDownMessage(IMessage message){
		synchronized (messageLists) {
			messageLists.add(message);
			messageLists.notifyAll();
		}
	}
	
	public void onReceive(SerialPortEvent event){
		switch(event.getEventType()){
		case SerialPortEvent.BI:
		case SerialPortEvent.OE:
		case SerialPortEvent.FE:
		case SerialPortEvent.PE:
		case SerialPortEvent.CD:
		case SerialPortEvent.CTS:
		case SerialPortEvent.DSR:
		case SerialPortEvent.RI:
		case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			break;
		case SerialPortEvent.DATA_AVAILABLE:
			byte[] readBuffer = new byte[20];
			try {
				buffer.clear();
				while (input.available() > 0){
					int num=input.read(readBuffer);
					buffer.put(readBuffer, 0, num);
				}
				buffer.flip();
				MessageBytes mb = new MessageBytes();
				mb.read( buffer);
				channelClient.sendMessage(mb);
			} catch (IOException e) {
//				e.printStackTrace();
				stop();
			} catch (MessageParseException e) {
				e.printStackTrace();
			}
			break;
		}
	}
	

	private void stop() {
		active = false;
		channelClient.stop();
		port.close();
		synchronized (messageLists) {
			messageLists.notifyAll();
		}
	
	}

	public static void main(String[] args) {
		HashMap<Setting,String> params = new HashMap<Setting, String>();
		params.put(Setting.PARAMS_TIMEOUT, "1000");
		params.put(Setting.PARAMS_RATE, "9600");
		params.put(Setting.PARAMS_DATABITS, "8");
		params.put(Setting.PARAMS_STOPBITS, "1");
		params.put(Setting.PARAMS_PARITY, "0");
		params.put(Setting.PARAMS_PORT, "COM3");
		params.put(Setting.GATE_PORT, "8002");
		params.put(Setting.GATE_IP, "172.16.241.43");
		SerialCommClient su = new SerialCommClient(params);
		new Thread(su).start();
	}
}