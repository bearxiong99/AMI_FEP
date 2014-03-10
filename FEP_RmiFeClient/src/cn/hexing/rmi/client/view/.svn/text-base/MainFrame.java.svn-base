package cn.hexing.rmi.client.view;

import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;

import net.miginfocom.swing.MigLayout;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import cn.hexing.fk.rmi.fe.model.RtuCommunicationInfo;
import cn.hexing.fk.rmi.fe.model.RtuCurrentMessage;
import cn.hexing.fk.rmi.fe.model.RtuCurrentWorkState;
import cn.hexing.fk.utils.ApplicationContextUtil;
import cn.hexing.fk.utils.HexDump;
import cn.hexing.rmi.client.control.RmiClientDbService;
import cn.hexing.rmi.client.model.LeftTreeNode;
import cn.hexing.rmi.client.model.RtuInfoClient;

/**
 * 
 * @author gaoll
 *
 * @time 2013-9-14 下午01:22:58
 *
 * @info
 * 
 * FE客户端界面显示
 * 
 * 整体界面包含两个部分(左边panel,右边panel)
 * 1.左边panel
 * 		包含一个tabPanel,tabPanel包含两个tab页
 * 		一个设备树的tab页面，整体是一棵树，最下面是叶子节点，如果叶子节点是设备的话，点击节点，可查询信息
 * 		一个查询tab页面,可提供查询终端，将查询结果显示到list容器内,点击list容器内数据，可进行查询信息
 * 2.右边panel
 * 		可供查看终端信息，包含一个查询按钮，可在text里手动输入终端进行查询信息，当终端的最近一次通讯时间不超过十分钟，
 * 		会显示绿色图标，表示在线，否则显示红色图标，表示不在线。
 * 		下面包含最近收到GPRS通讯时间，以及心跳时间。
 * 		接着是一个报文信息查看的textArea,当服务端的FE收到报文，界面会实时显示。
 * 		左边有两个按钮，终止显示按钮用来是否实时显示报文，清除报文信息，可以将textArea清空。
 */
public class MainFrame extends JFrame{

	//ten miniute
	private static final int ONLINE_TIME = 10*60*1000;


	private static final String RESOURCE_GREEN_PNG = "./resource/green.png";


	private static final String RESOURCE_RED_PNG = "./resource/red.png";


	/**
	 * 
	 */
	private static final long serialVersionUID = -347003037896182059L;


	private JPanel contentPane = new JPanel(new MigLayout("","[grow,25%][grow]","[grow]"));
	
	private boolean isDisplay = true;
	
	public MainFrame(){
		
		init();

		this.setContentPane(contentPane);
		this.pack();
		int s_height=(int)Toolkit.getDefaultToolkit().getScreenSize().getHeight();
		int s_width=(int)Toolkit.getDefaultToolkit().getScreenSize().getWidth();
		int width = 800;
		int height=500;
		int y=(s_height-height)/2;
		int x=(s_width-width)/2;
		
		this.setBounds(x, y, width, height);
		this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		this.addWindowListener(new WindowAdapter() {
			public void windowClosed(WindowEvent e) {logout();}
		});
		this.setVisible(true);
	}
	
	private void init(){
		
		//leftPanel
		createLeftPanel();
		contentPane.add(leftTabPanel, "spany,grow");
		
		//rightPanel
		createRightPanel();
		contentPane.add(rightPanel,"spany,grow");
		addActionListener();

	}

	private JPanel rightPanel;
	
	private JTextField tf_LogicAddr;
	private JTextField tf_LastHeartBeat;
	private JTextField tf_LastGprs;
	private JButton b_displaySwitch;
	private JButton b_clearInfos;
	private JTextArea ta_frameArea;
	private JScrollPane frameScrollPane;
	private JButton b_queryRtuInfo;
	
	private JLabel light;
	
	private void createRightPanel() {
		
		rightPanel = new JPanel(new MigLayout("wrap","[][][grow][]","[][][grow][][]"));
		
		rightPanel.add(FrameConstant.createLabel("终端逻辑地址:"),"r");
		
		tf_LogicAddr=FrameConstant.createTextField("",20,true);
		rightPanel.add(tf_LogicAddr,"split");
		
		b_queryRtuInfo = FrameConstant.createButton("查询", false);
		rightPanel.add(b_queryRtuInfo,"split");
		
		light = new JLabel();
		rightPanel.add(light,"wrap");
		
		rightPanel.add(FrameConstant.createLabel("最近一次心跳时间:"),"l");
		
		tf_LastHeartBeat=FrameConstant.createTextField("",20,false);
		rightPanel.add(tf_LastHeartBeat,"split");
		
		rightPanel.add(FrameConstant.createLabel("最近一次GPRS时间:"),"l");
		
		tf_LastGprs=FrameConstant.createTextField("",20,false);
		rightPanel.add(tf_LastGprs,"wrap");
		
		rightPanel.add(FrameConstant.createLabel("报文信息:"),"r,t");
		frameScrollPane = FrameConstant.createTextAreaScroll("", 20, 60, true);
		ta_frameArea = (JTextArea) frameScrollPane.getViewport().getView();
		rightPanel.add(frameScrollPane,"span 3 3,grow");
		
		
		b_displaySwitch=FrameConstant.createButton("终止显示", false);
		rightPanel.add(b_displaySwitch,"c");
		b_clearInfos=FrameConstant.createButton("清除报文", false);
		rightPanel.add(b_clearInfos,"t,c");
		
	}

	private void addActionListener() {
		
		
		//终止显示按钮控制器
		b_displaySwitch.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				isDisplay=!isDisplay;
				String buttonText;
				buttonText=isDisplay?"终止显示":"开始显示";
				b_displaySwitch.setText(buttonText);
				
			}
		});
		
		//清除报文按钮控制器
		b_clearInfos.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				ta_frameArea.setText("");
			}
		});
		
		
		//右边Panle用来查看终端信息
		b_queryRtuInfo.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				String logicAddr=tf_LogicAddr.getText();
				
				getRtuState(logicAddr);
			}
		});
		
		//左边查询按钮
		b_leftQuery.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				//将查询结果存放在list里
				String logicAddr=tf_leftLogicAddr.getText();
				
				if(logicAddr!=null && !logicAddr.trim().equals("")){
					//从数据库获得数据
					List<String> lists = RmiClientDbService.getInstance().queryTerminal(logicAddr);
					leftQueryResultList.setListData(lists.toArray());
				}
			}
		});
		
		//查询结果list,点击可进行查询信息
		leftQueryResultList.addMouseListener(new MouseAdapter() {

			@Override
			public void mouseClicked(MouseEvent e) {
				
				String logicAddr=(String) leftQueryResultList.getSelectedValue();
				
				if(e.getClickCount()!=1) return;
				
				getRtuState(logicAddr);
			}
			
		});
		
		//左边树监听器，可用来加载树，查询信息
		leftTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				//处理鼠标双击事件
				if(leftTree.getSelectionModel()==null) return;
				
				if(leftTree.getSelectionModel().getSelectionPath()==null) return;
				
				DefaultMutableTreeNode treeNode=(DefaultMutableTreeNode) leftTree.getSelectionModel().getSelectionPath().getLastPathComponent();
				LeftTreeNode node=(LeftTreeNode) treeNode.getUserObject();
				String id = node.getId();  //当前树节点的id
				String type = node.getType(); //节点类型
				//通过id获得，当前ID下的下属设备
				if(type.equals("root")) return;
				
				addChild(type,id,treeNode);
				
				if(!type.equals("sb")) return;
				
				if(e.getClickCount()!=1) return;
				
				String logicAddr=node.getId();
				getRtuState(logicAddr);
				
			}
		});
		
	}

	protected void clearAll() {
		ta_frameArea.setText("");
		tf_LastGprs.setText("");
		tf_LastHeartBeat.setText("");
	}

	
	//-------------------------------------------------------------------------------------
	private JTree leftTree;
	private JScrollPane leftTreePanel;
	private JTabbedPane leftTabPanel;
	private JPanel leftQueryPane;
	private void createLeftPanel() {
		
		leftTabPanel = new JTabbedPane(JTabbedPane.LEFT);
		createLeftTreePanel();
		createLeftQueryPane();
		
		leftTabPanel.addTab("<html>\n<br>设<br>\t<br>备<br>\n<br></html>", leftTreePanel);
		leftTabPanel.addTab("<html>\n<br>查<br>\t<br>寻<br>\n<br></html>", leftQueryPane);

	}
	
	//-------------------------------------------------------------------------------------

	
	//-------------------------------------------------------------------------------------
	private JTextField tf_leftLogicAddr;
	private JButton b_leftQuery;
	private JScrollPane leftQueryResultPanel;
	private JList leftQueryResultList;
	private void createLeftQueryPane() {
		

		leftQueryPane = new JPanel(new MigLayout("","[][grow]","[][][][grow]"));
		leftQueryPane.add(FrameConstant.createLabel("设备号:"),"split");
		tf_leftLogicAddr = FrameConstant.createTextField(20);
		leftQueryPane.add(tf_leftLogicAddr,"wrap");
		
		b_leftQuery=FrameConstant.createButton("查询", false);
		leftQueryPane.add(b_leftQuery,"wrap");
		
		leftQueryPane.add(FrameConstant.createLabel("查询结果:"),"split,wrap");
		leftQueryResultList = new JList();
		
		leftQueryResultPanel = new JScrollPane(leftQueryResultList);
		leftQueryPane.add(leftQueryResultPanel, "growy,w :200");
		
		
	}

	private void createLeftTreePanel(){
		leftTree=createLeftTree();
		leftTreePanel  = new JScrollPane(leftTree);
	}

	private JTree createLeftTree() {

		//1.get root
		List<LeftTreeNode> nodes = RmiClientDbService.getInstance().getLeftTreeNodes("root", null);
		LeftTreeNode node = nodes.get(0);
		DefaultMutableTreeNode root = new DefaultMutableTreeNode(node);
		JTree tree = new JTree(root);
		tree.setCellRenderer(new TreeRenderer());
		//2.get secondLevel
		addChild("dw",node.getId(),root);
		return tree;
	}
	//-------------------------------------------------------------------------------------
	
	
	
	public void addChild(String type,String pid,DefaultMutableTreeNode parent){
		LeftTreeNode leftTreeNode = (LeftTreeNode) parent.getUserObject();
		if(leftTreeNode.isDone()) return;
		leftTreeNode.setDone();
		List<LeftTreeNode> nodes = RmiClientDbService.getInstance().getLeftTreeNodes(type, pid);
		for(LeftTreeNode n : nodes){
			parent.add(new DefaultMutableTreeNode(n));
		}

	}
	
	
	public static void main(String[] args) {
		ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext.xml");
		ApplicationContextUtil.setContext(context);
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				try {
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				MainFrame mf = new MainFrame();
				RtuInfoClient.getInstance().setMainFrame(mf);
			}
		});
	}
	
	public void logout(){
		RtuInfoClient.getInstance().logout();
		System.exit(0);
	}

	public void rtuStateChange(RtuCommunicationInfo info) {
		if(isNull(info)) return;
		
		RtuCurrentWorkState infoState = info.getState();
		
		if(isNull(infoState)) return;
		
		if(!isDisplay) return;
		
		Date lastGprsRecvTime = infoState.getLastGprsRecvTime();
		Date lastHeartBeatTime=infoState.getLastHeartBeatTime();
		
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if(!isNull(lastGprsRecvTime)){
			String time=sdf.format(lastGprsRecvTime);
			tf_LastGprs.setText(time);
		}
		
		if(!isNull(lastHeartBeatTime)){
			String time=sdf.format(lastHeartBeatTime);
			tf_LastHeartBeat.setText(time);
		}
		
		displayOnlineStatus(info);
	}

	public void newMsgComing(RtuCommunicationInfo info) {
		
		if(isNull(info)) return;

		RtuCurrentMessage curMsg = info.getMsg();
		
		if(isNull(curMsg)) return;
		
		if(!isDisplay) return;
		StringBuilder sb = new StringBuilder();
		
		String dir = curMsg.getDir()==0?"下行":"上行";
		sb.append(dir).append("\t");
		
		if(curMsg.getTime() != 0){
			Date msgTime = new Date(curMsg.getTime());
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			
			String time=sdf.format(msgTime);
			
			sb.append(time).append("\t");
		}

		String peerAddr = curMsg.getPeerAddr();
		if(peerAddr!=null && !peerAddr.trim().equals("")){
			sb.append(peerAddr);
		}
		String content=curMsg.getContent();
		
		content=HexDump.hexDump(HexDump.toByteBuffer(content));
		
		sb.append("\n").append(content).append("\n\n");
		
		ta_frameArea.append(sb.toString());
		//滚动条滚到最下面
		ta_frameArea.setCaretPosition(ta_frameArea.getText().length());
		
		displayOnlineStatus(info);
	}
	
	public boolean isNull(Object o){
		return o==null;
	}

	private void getRtuState(String logicAddr) {
		
		if(logicAddr==null || logicAddr.trim().equals("")) return;
		
		RtuCommunicationInfo info = RtuInfoClient.getInstance().getRtuState(logicAddr);
		clearAll();
		tf_LogicAddr.setText(logicAddr);
		rtuStateChange(info);
		
		displayOnlineStatus(info);
	}

	private void displayOnlineStatus(RtuCommunicationInfo info) {
		if(info==null){
			light.setIcon(new ImageIcon(RESOURCE_RED_PNG));
			light.setToolTipText("not online");
		}else{
			RtuCurrentWorkState state = info.getState();
			if(state!=null){
				Date gprsTime=state.getLastGprsRecvTime();
				Date heartTime=state.getLastHeartBeatTime();
				long bigTime =0;
				if(gprsTime!=null && heartTime!=null){
					long lGprsTime = gprsTime.getTime();
					long lHeartTime = gprsTime.getTime();
					bigTime = lGprsTime>lHeartTime?lGprsTime:lHeartTime;
				}else{
					bigTime=gprsTime==null?heartTime.getTime():gprsTime.getTime();
				}
				long curTime=info.getCurrentTime();
				
				if(curTime-bigTime>ONLINE_TIME){
					light.setIcon(new ImageIcon(RESOURCE_RED_PNG));
					light.setToolTipText("not online");
				}else{
					light.setIcon(new ImageIcon(RESOURCE_GREEN_PNG));
					light.setToolTipText("online");
				}
			}
		}
	}
	
	
}
