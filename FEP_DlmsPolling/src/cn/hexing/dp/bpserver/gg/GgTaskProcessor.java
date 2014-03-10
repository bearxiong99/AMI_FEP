package cn.hexing.dp.bpserver.gg;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import cn.hexing.dp.bpserver.TPConstant;
import cn.hexing.dp.bpserver.dlms.DlmsProcessor;
import cn.hexing.dp.dao.LoadDatasDao;
import cn.hexing.dp.model.RtuTask;
import cn.hexing.dp.queue.GdgyRequestQueue;
import cn.hexing.fas.model.FaalGGKZM12Request;
import cn.hexing.fas.model.FaalRequestRtuParam;
import cn.hexing.fk.clientmod.ClusterClientModule;
import cn.hexing.fk.common.spi.IModule;
import cn.hexing.fk.common.spi.abstra.BaseModule;
import cn.hexing.fk.model.BizRtu;
import cn.hexing.fk.model.RtuManage;
import cn.hexing.fk.model.TaskTemplate;
import cn.hexing.fk.tracelog.TraceLog;
import cn.hexing.fk.utils.State;

public class GgTaskProcessor extends BaseModule{
	private static final Logger log = Logger.getLogger(DlmsProcessor.class);
	private static final TraceLog tracer = TraceLog.getTracer(DlmsProcessor.class);
	//����������
	private String name = "GgTask";	
	//�����ڲ�״̬
	private volatile State state = new State();	
	private WorkThread work=null;
	private Timer timer;			
	private ClusterClientModule com=null;
	
	
	private LoadDatasDao loadGgDatasDao;

	public void setCom(ClusterClientModule com) {
		this.com = com;
	}
	public String getName() {
		return name;
	}
	
	public boolean isActive() {
		return state.isActive();
	}

	public boolean start() {
		if( !state.isStopped() )
			return false;
		state = State.STARTING;
		work=new WorkThread();
		work.start();
		GdgyRequestQueue.getInstance().initTaskData();
		timer=new Timer();
		TimerTask task=new TimerTask(){
			@Override
			public void run(){
				onTimer();
			}
		};
		timer.schedule(task,0, 2*1000);		
	
		state = State.RUNNING;
		if( log.isInfoEnabled())
			log.info("thread("+name+") start...");
		
		return true;
	}
	private void onTimer(){
		
	}

	private List<FaalGGKZM12Request> getGgRequest(RtuTask req) {
		return terminalTask(req);
	}
	
	/**
	 * �����ն�����
	 */
	private List<FaalGGKZM12Request> terminalTask(RtuTask req) {
		BizRtu meterRtu = RtuManage.getInstance().getBizRtuInCache(req.getRtuId());
		if(meterRtu==null) return null;
		TaskTemplate taskTemplate = meterRtu.getTaskTemplate(req.getTaskNo());
		if(taskTemplate==null) return null;
		req.setTaskProperty(taskTemplate.getTaskProperty());
		return createTerminalTaskRequest(req,taskTemplate);
	}
	/**
	 *  ����request�ͼ��ص�����������������request
	 * @param req
	 * @param taskTemplate 
	 * @param dataCodes
	 * @return
	 */
	private List<FaalGGKZM12Request> createTerminalTaskRequest(RtuTask req, TaskTemplate taskTemplate) {
		List<FaalGGKZM12Request> requests=new ArrayList<FaalGGKZM12Request>();
		List<String> codes = taskTemplate.getDataCodes();
		if(codes ==null || codes.size() ==0) return null;
		else{
			for(String code:codes){
				FaalGGKZM12Request request = new FaalGGKZM12Request();
				log.info("create FaalGGKZM12Request,meterId:"+req.getRtuId()+",taskNo:"+req.getTaskNo()+",taskDate"+req.getTaskDate());
				if("42".equals(req.getTaskProperty())){//�ն�������  ����taskProperty���ж��������ͣ������Ѿ��ȶ��İ汾��ȥ�޸ģ������³��Ŀ�����������޸�һ��
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(getDateBefore(req.getTaskDate()));
					request.setEndTime(req.getTaskDate());
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //�û�������ʱ��д����
					request.setType(18); // 0X12=18
				    requests.add(request);
				}
				else if("43".equals(req.getTaskProperty())){//�¶�������
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(getDateBeforeMonth(req.getTaskDate()));
					request.setEndTime(req.getTaskDate());
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //�û�������ʱ��д����
					request.setType(18); // 0X12=18
					requests.add(request);
				}else {//��������   else if("44".equals(req.getTaskProperty()))
					request.setProtocol("04");
					request.setTaskNo(req.getTaskNo());
					request.setStartTime(req.getTaskDate());
					//��������Ҫ��ȡ10:00�����ݣ���ô��Ҫ����ʱ��Ϊ10��00-10��01��1���ӣ����ɼ�������ʱ����һ��ǰ�պ󿪵�����
					//�����������ʱ�����30��
					request.setEndTime(getDateAfterNow(req.getTaskDate()));
					FaalRequestRtuParam rtuparam =new FaalRequestRtuParam();
					rtuparam.setCmdId((long)40754);
					rtuparam.setRtuId(req.getRtuId());
					rtuparam.addParam(code, null);
					request.addRtuParam(rtuparam);
//					request.setYhlx("");  //�û�������ʱ��д����
					request.setType(18); // 0X12=18
					requests.add(request);
				}
			}
		}
		return requests;
	}
	
	
	public void stop() {
		state = State.STOPPING;
		work.interrupt();
		if( log.isInfoEnabled())
			log.info("thread("+name+") stop...");
		state = State.STOPPED;
	}
	
	public String getModuleType() {
		return IModule.MODULE_TYPE_BP;
	}
	
	private class WorkThread extends Thread{
		public WorkThread(){
		}
		public void run() {
			log.info("work running:"+this.getName());
			while( !GgTaskProcessor.this.state.isStopping() && !GgTaskProcessor.this.state.isStopped() ){
				try{
					ArrayList<RtuTask> list=GdgyRequestQueue.getInstance().getRtuTaskRequestList(System.currentTimeMillis());
					if (list!=null&&list.size()>0){
						long sleepWhenOverMaxSendTime = TPConstant.getInstance().getSleepWhenOverMaxSendTime();
						int maxSendOneTime = TPConstant.getInstance().getMaxSendOneTime();
						int sendSize=0;
						//��request,������
						for(RtuTask rt:list){
							List<FaalGGKZM12Request> ggRequestList=getGgRequest(rt);
							if(ggRequestList==null) continue;
							if(sendSize++>maxSendOneTime){
								//������͵ĸ�������һ�η���������,˯��1��
								Thread.sleep(sleepWhenOverMaxSendTime);
								sendSize=0;
							}
							for(FaalGGKZM12Request ggRequest:ggRequestList){
								com.sendRequest(null, null, ggRequest);
								try{
									Thread.sleep(500);//��Ϣһ�������һ֡
								}catch(Exception e){
									e.printStackTrace();
								}
							}
						}																		
					}
					else{
						Thread.sleep(5000);
					}				
				}catch(Exception exp){
					log.error("��������������д����������̴߳������", exp);
					continue;
				}
			}
		}				
	}

	public final void setLoadGgDatasDao(LoadDatasDao loadGgDatasDao) {
		this.loadGgDatasDao = loadGgDatasDao;
	}
	public Date getDateBefore(Date date){
		Calendar calendar = Calendar.getInstance(); //�õ�����
		calendar.setTime(date);//�ѵ�ǰʱ�丳������
		calendar.add(Calendar.DAY_OF_MONTH, -1);  //����Ϊǰһ��
		return  calendar.getTime();   //�õ�ǰһ���ʱ��
	}
	public Date getDateBeforeMonth(Date date){
		Calendar calendar = Calendar.getInstance(); //�õ�����
		calendar.setTime(date);//�ѵ�ǰʱ�丳������
		calendar.add(Calendar.MONTH, -1);  //����Ϊ��ǰ����һ����
		return  calendar.getTime();   //�õ�ǰһ���ʱ��
	}
	public Date getDateAfterNow(Date date){
		Calendar calendar = Calendar.getInstance(); //�õ�����
		calendar.setTime(date);//�ѵ�ǰʱ�丳������
		calendar.add(Calendar.MINUTE, +1);  //����Ϊ��ǰ����һ����
		return  calendar.getTime();   //�õ�ǰһ���ʱ��
	}
}

