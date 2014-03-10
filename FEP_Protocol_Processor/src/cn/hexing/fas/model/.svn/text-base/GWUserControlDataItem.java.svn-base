package cn.hexing.fas.model;

	import java.text.SimpleDateFormat;
	import java.util.Calendar;
	import java.util.Date;
/**
 * 国网中继继电器操作dataItem
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
		 * 构建中继报文数据区
		 */
		public String constructor(String password,String userNo,
									String command_tpye1,String command_tpye2,Date date,int delay){
			String endData=null;
			endData="02"+password+userNo+command_tpye1+command_tpye2+EffectiveTime(date,delay);
			return endData;
		}
		private String EffectiveTime(Date date,int delay){
			String stime=null;
			Calendar calendar = Calendar.getInstance(); //得到日历
			calendar.setTime(date);//把当前时间赋给日历
			calendar.add(Calendar.MINUTE,delay);  //加上delay
			SimpleDateFormat sdf=new SimpleDateFormat("ssmmHHddMMyy");
			stime=sdf.format(calendar.getTime());
			return stime;
		}

		/**
		 * 获取表计密码
		 * @return pw
		 */
		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}
		/**
		 * 获取用户编号
		 * @return 
		 */
		public String getUserNo() {
			return userNo;
		}

		public void setUserNo(String userNo) {
			this.userNo = userNo;
		}

		/**
		 * 获取操作命令类型1
		 * @return 
		 */
		public String getCommand_tpye1() {
			return command_tpye1;
		}

		public void setCommand_tpye1(String command_tpye1) {
			this.command_tpye1 = command_tpye1;
		}

		/**
		 * 获取操作命令类型1
		 * @return 
		 */
		public String getCommand_tpye2() {
			return command_tpye2;
		}

		public void setCommand_tpye2(String command_tpye2) {
			this.command_tpye2 = command_tpye2;
		}
		
		/**
		 * 获取操作命令时间
		 * @return 
		 */
		public Date getDate() {
			return date;
		}

		public void setDate(Date date) {
			this.date = date;
		}
		
		/**
		 * 获取操作命令超时时间
		 * @return 
		 */
		public int getDelay() {
			return delay;
		}

		public void setDelay(int delay) {
			this.delay = delay;
		}
		
	}
