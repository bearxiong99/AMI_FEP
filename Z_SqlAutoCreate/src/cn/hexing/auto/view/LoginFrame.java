package cn.hexing.auto.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.sql.DataSource;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import net.miginfocom.swing.MigLayout;

import org.logicalcobwebs.proxool.ProxoolDataSource;

import cn.hexing.auto.control.CodeCreatorFactory;
import cn.hexing.auto.control.ICodeCreator;
import cn.hexing.auto.db.SqlCreator;
import cn.hexing.auto.model.Constant;

/**
 * 
 * @author gaoll
 *
 * @time 2013-9-16 下午03:11:26
 *
 * @info 功能介绍:
 * 提供一个数据库和一个数据表，可以产生一个java类和一段sql
 * 
 * 
 *
 * 
 * 
 * 
 * 
 * 
 */
public class LoginFrame extends JFrame{

	/**
	 * 
	 */
	private static final long serialVersionUID = 8742186477374988364L;

	private JPanel contentPane ;

	
	public LoginFrame(){
		
		
		createMainPane();
		this.pack();
		int s_height=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int s_width=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int width =400;
		int height=230;
		int y=(s_height-height)/2;
		int x=(s_width-width)/2;
		
		this.setBounds(x, y, width, height);
		this.setContentPane(contentPane);
	
		this.setVisible(true);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
	}
	
	private JComboBox dbType;
	private JTextField tf_dbName;
	private JTextField tf_dbUser;
	private JTextField tf_dbPassword;
	private JButton    jb_creator;
	private JTextField tf_tableName;
	private JTextField tf_dbIP;
	private JTextField tf_dbPort;
	
	private void createMainPane() {

		contentPane=new JPanel(new MigLayout());
		
		this.contentPane.add(FrameConstant.createLabel("数据库类型:"));
		dbType = createComboBox();
		this.contentPane.add(dbType,"wrap");
		
		
		this.contentPane.add(FrameConstant.createLabel("数据库名称:"));
		tf_dbName=FrameConstant.createTextField("ami",20,true);
		this.contentPane.add(tf_dbName,"wrap");
		
		this.contentPane.add(FrameConstant.createLabel("数据库IP:"));
		tf_dbIP=FrameConstant.createTextField("192.168.2.176",20,true);
		this.contentPane.add(tf_dbIP,"split");
		this.contentPane.add(FrameConstant.createLabel("数据库端口:"));
		tf_dbPort=FrameConstant.createTextField("1521",10,true);
		this.contentPane.add(tf_dbPort,"wrap");
		
		this.contentPane.add(FrameConstant.createLabel("数据库用户名:"));
		tf_dbUser=FrameConstant.createTextField("AMI3",20,true);
		this.contentPane.add(tf_dbUser,"wrap");
		
		this.contentPane.add(FrameConstant.createLabel("数据库密码:"));
		tf_dbPassword=FrameConstant.createTextField("AMI3",20,true);
		this.contentPane.add(tf_dbPassword,"wrap");
		
		this.contentPane.add(FrameConstant.createLabel("表名称:"));
		tf_tableName = FrameConstant.createTextField("SB_FHSJ",20,true);
		this.contentPane.add(tf_tableName,"wrap");
		
		jb_creator=FrameConstant.createButton("生成", false);
		this.contentPane.add(jb_creator,"skip");
		
		
		addActionListener();
		
	}

	private void addActionListener() {
		
		jb_creator.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				JFileChooser choser = new JFileChooser();
				choser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int result = choser.showOpenDialog(null);
				if(result == JFileChooser.APPROVE_OPTION){
					String sTabName = (String) tf_tableName.getText();
					DataSource dataSource = buildDataSource();
					String sDbType=(String) dbType.getSelectedItem();

					ICodeCreator codeCreator = CodeCreatorFactory.getInstance().getCodeCreator(sDbType, dataSource);
					File file = choser.getSelectedFile();
					int count=codeCreator.create(file.getPath(), sTabName);
					JOptionPane.showMessageDialog(null, "成功生成"+count+"个文件");
				}
			}
		});
		
	}
	
	private DataSource buildDataSource(){
		
		String sDbType=(String) dbType.getSelectedItem();
		String sDbName=(String) tf_dbName.getText();
		String sDbUser=(String) tf_dbUser.getText();
		String sDbPassword=(String) tf_dbPassword.getText();
		String sDbIP = (String) tf_dbIP.getText();
		String sDbPort = (String) tf_dbPort.getText();
		
		ProxoolDataSource dataSource = new ProxoolDataSource("AutoCreatorDb");
		dataSource.setUser(sDbUser);
		dataSource.setPassword(sDbPassword);
		dataSource.setDriver(SqlCreator.getDbDriver(sDbType));
		dataSource.setDriverUrl(SqlCreator.getDbDriverUrl(sDbType,sDbIP,sDbPort,sDbName));
		dataSource.setHouseKeepingTestSql("select sysdate from dual");
		dataSource.setVerbose(true);
		dataSource.setTrace(true);
		dataSource.setMaximumConnectionCount(1);
		return dataSource;
	}

	private JComboBox createComboBox() {
		JComboBox cm = new JComboBox();
		cm.addItem(Constant.ORACLE);
		cm.addItem(Constant.MYSQL);
		cm.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				JComboBox cm=(JComboBox) e.getSource();
				String selectItem = (String) cm.getSelectedItem();
				initText(selectItem);
			}
		});
		return cm;
	}
	private void initText(String dbType) {
		
		if(dbType.equalsIgnoreCase(Constant.ORACLE)){
			tf_dbPort.setText("1521");
			tf_dbUser.setText("AMI3");
			tf_dbPassword.setText("AMI3");
			tf_dbName.setText("ami");
			tf_dbIP.setText("192.168.2.176");
		}else if(dbType.equalsIgnoreCase(Constant.MYSQL)){
			tf_dbPort.setText("3306");
			tf_dbUser.setText("root");
			tf_dbPassword.setText("root");
			tf_dbName.setText("ami");
			tf_dbIP.setText("192.168.2.170");
		}
		
	}
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				new LoginFrame();
			}
		});
	}
	
	
	
}
