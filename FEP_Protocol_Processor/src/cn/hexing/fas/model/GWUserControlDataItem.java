package cn.hexing.fas.model;

	import java.text.SimpleDateFormat;
	import java.util.Calendar;
	import java.util.Date;
/**
 * �����м̵̼�������dataItem
 * @author Administrator
 *
 */
	
	
	public class GWUserControlDataItem {
		private String password;
		private String userNo="00000000";
		private String command_tpye1;
		private String command_tpye2="00";
		private Date date;
		private int delay=10;
		
		/**
		 * �����м̱���������
		 */
		public String constructor(String password,String userNo,
									String command_tpye1,String command_tpye2,Date date,int delay){
			String endData=null;
			endData="02"+password+userNo+command_tpye1+command_tpye2+EffectiveTime(date,delay);
			return endData;
		}
		private String EffectiveTime(Date date,int delay){
			String stime=null;
			Calendar calendar = Calendar.getInstance(); //�õ�����
			calendar.setTime(date);//�ѵ�ǰʱ�丳������
			calendar.add(Calendar.MINUTE,delay);  //����delay
			SimpleDateFormat sdf=new SimpleDateFormat("ssmmHHddMMyy");
			stime=sdf.format(calendar.getTime());
			return stime;
		}

		/**
		 * ��ȡ�������
		 * @return pw
		 */
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		/**
		 * ��ȡ�û����
		 * @return 
		 */
		public String getUserNo() {
			return userNo;
		}

		public void setUserNo(String userNo) {
			this.userNo = userNo;
		}

		/**
		 * ��ȡ������������1
		 * @return 
		 */
		public String getCommand_tpye1() {
			return command_tpye1;
		}

		public void setCommand_tpye1(String command_tpye1) {
			this.command_tpye1 = command_tpye1;
		}

		/**
		 * ��ȡ������������1
		 * @return 
		 */
		public String getCommand_tpye2() {
			return command_tpye2;
		}

		public void setCommand_tpye2(String command_tpye2) {
			this.command_tpye2 = command_tpye2;
		}
		
		/**
		 * ��ȡ��������ʱ��
		 * @return 
		 */
		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
		
		/**
		 * ��ȡ�������ʱʱ��
		 * @return 
		 */
		public int getDelay() {
			return delay;
		}

		public void setDelay(int delay) {
			this.delay = delay;
		}
		
	}
