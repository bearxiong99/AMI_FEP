package cn.hexing.fk.sockclient.async.simulator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Timer;
import java.util.TimerTask;

import cn.hexing.fk.message.IMessage;
import cn.hexing.fk.sockclient.async.JAsyncSocket;

public class SimulatorManager {
	private static ArrayList<IRtuSimulator> simulators = new ArrayList<IRtuSimulator>();
	public static int rtua = 92010001;
	public static int heartInterval = 5;
	public static int taskInterval = 15;
	private static Timer timerHeart = null, timerTask = null;
	public static int totalSend = 0,totalRecv = 0;
	private static LinkedList<IMessage> sendMsg = new LinkedList<IMessage>();
	private static LinkedList<IMessage> recvMsg = new LinkedList<IMessage>();
	private static Object lockRecv = new Object(),lockSend = new Object();

	public static void startHeart(){
		stopHeart();
		timerHeart = new Timer("simul.heart");
		timerHeart.schedule(new TimerTask(){
			@Override
			public void run() {
				synchronized( simulators){
					for(IRtuSimulator simu : simulators){
						simu.sendHeart();
					}
				}
			}
		}, 0, heartInterval*1000);
	}
	
	public static void stopHeart(){
		if( null != timerHeart ){
			timerHeart.cancel();
			timerHeart = null;
		}
	}
	
	public static boolean isHeartRunning(){
		return timerHeart != null;
	}
	
	public static void startTask(){
		stopTask();
		timerTask = new Timer("simul.task");
		timerTask.schedule(new TimerTask(){
			@Override
			public void run() {
				synchronized( simulators){
					for(IRtuSimulator simu : simulators){
						simu.sendTask();
					}
				}
			}
		}, 0, taskInterval*1000);
	}
	
	public static void stopTask(){
		if( null != timerTask ){
			timerTask.cancel();
			timerTask = null;
		}
	}

	public static boolean isTaskRunning(){
		return null != timerTask;
	}
	
	public static void onChannelConnected(JAsyncSocket channel){
		IRtuSimulator simulator = (IRtuSimulator)channel.attachment();
		if( 0 == simulator.getRtua() ){
			synchronized(simulators){
				simulator.setRtua(rtua++);
				simulators.add(simulator);
			}
		}
		simulator.onConnect(channel);
	}

	public static void onChannelClosed(JAsyncSocket channel){
		IRtuSimulator simulator = (IRtuSimulator)channel.attachment();
		simulator.onClose(channel);
	}

	public static void onChannelReceive(JAsyncSocket channel,IMessage message){
		IRtuSimulator simulator = (IRtuSimulator)channel.attachment();
		simulator.onReceive(channel, message);
		synchronized(lockRecv){
			totalRecv++;
			if( recvMsg.size()> 5000 )
				recvMsg.removeFirst();
			recvMsg.add(message);
		}
	}

	public static void onChannelSend(JAsyncSocket channel,IMessage message){
		IRtuSimulator simulator = (IRtuSimulator)channel.attachment();
		simulator.onSend(channel,message);
		synchronized(lockSend){
			totalSend++;
			if( sendMsg.size()>5000 )
				sendMsg.removeFirst();
			sendMsg.add(message);
		}
	}

	public static ArrayList<IRtuSimulator> getSimulators() {
		synchronized(simulators){
			return simulators;
		}
	}

	public static IMessage[] getSendMsg() {
		synchronized(lockSend){
			return sendMsg.toArray(new IMessage[0]);
		}
	}

	public static IMessage[] getRecvMsg() {
		synchronized(lockRecv){
			return recvMsg.toArray(new IMessage[0]);
		}
	}

}
