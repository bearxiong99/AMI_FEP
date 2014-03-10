package cn.hexing.rmi.client.view;

import java.awt.Component;

import javax.swing.ImageIcon;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;

import cn.hexing.rmi.client.model.LeftTreeNode;

public class TreeRenderer extends DefaultTreeCellRenderer{

	/**
	 * 
	 */
	private static final long serialVersionUID = 5722007954612661849L;
	
	
	ImageIcon root = new ImageIcon("./resource/folder.gif");
	
	ImageIcon root_open = new ImageIcon("./resource/folder-open.gif");
	
	ImageIcon department = new ImageIcon("./resource/folder_go.png");
	
	ImageIcon transformer = new ImageIcon("./resource/byq.gif");
	ImageIcon leafLeaf = new ImageIcon("./resource/leaf.gif");
	
	
	public Component getTreeCellRendererComponent(JTree tree, Object value,  
	        boolean sel, boolean expanded,  boolean leaf, int row, boolean hasFocus)  
	    {  
	        //执行父类默认的节点绘制操作  
	        super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);  
	        DefaultMutableTreeNode node=(DefaultMutableTreeNode) value;
	        LeftTreeNode data=(LeftTreeNode) node.getUserObject();
	        ImageIcon icon =leafLeaf;
	        if(data.getType().equals("root")){
	        	icon = expanded?root_open:root;
	        }else if(data.getType().equals("dw")){
	        	icon = department;
	        }else if(data.getType().equals("tq")){
	        	icon = transformer;
	        }else if(data.getType().equals("xl")){
	        	icon = transformer;
	        }else if(data.getType().equals("byq")){
	        	icon = transformer;
	        }
	        
	        this.setIcon(icon);
	        
	        return this;  
	    }  
}
