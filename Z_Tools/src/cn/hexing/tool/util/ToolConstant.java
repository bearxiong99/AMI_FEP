package cn.hexing.tool.util;

import javax.swing.JOptionPane;

public class ToolConstant {

	
	public static void showMsg(String msg) {
		JOptionPane.showMessageDialog(null, msg);
	}

	public static boolean checkStringIsNotNull(String value) {
		if (value == null || "".equals(value.trim()))
			return false;
		return true;
	}
}
