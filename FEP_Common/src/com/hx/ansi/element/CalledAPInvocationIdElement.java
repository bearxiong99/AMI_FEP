package com.hx.ansi.element;

import cn.hexing.fk.utils.HexDump;

/** 
 * @Description A4--CalledAPInvocationIdElement 
 * @author  Rolinbor
 * @Copyright 2013 hexing Inc. All rights reserved
 * @time��2013-3-20 ����06:18:14
 * @version 1.0 
 */

public class CalledAPInvocationIdElement implements Element {
	//A4 06 FF 04 00 03 00 02
	// called-AP-invocation-id-element,0xFFΪ֡����ӱ�ʶ��ĳ���������n֡����ô��һ�δ���ʱ֡���к�Ϊn-1���ڶ��δ���ʱ���к�Ϊn-2�����һ��Ϊ0��ֻ��Ҫһ�δ��͵���������ֽ�Ϊ0��0003Ϊ��֡����00 02��ʾ��ǰ֡���
	private int seq;
	private int  calledTotalMessage;	
	private  int calledAPInvocationId;
	private String calledAPInvocationIdElement;



	@Override
	public void encode() {
		String sCalledAPInvocationId=HexDump.toHex(calledAPInvocationId);
		calledAPInvocationIdElement="A4030201"+"00".substring(sCalledAPInvocationId.length())+sCalledAPInvocationId;
	}

	@Override
	public void decode() {
		this.seq=Integer.parseInt(this.calledAPInvocationIdElement.substring(0, 2), 16);
		this.calledTotalMessage=Integer.parseInt(this.calledAPInvocationIdElement.substring(4, 8), 16);
		this.calledAPInvocationId=Integer.parseInt(this.calledAPInvocationIdElement.substring(8, 12), 16);
	}
	
	
	
	public int getCalledAPInvocationId() {
		return calledAPInvocationId;
	}

	public void setCalledAPInvocationId(int calledAPInvocationId) {
		this.calledAPInvocationId = calledAPInvocationId;
	}

	public String getCalledAPInvocationIdElement() {
		return calledAPInvocationIdElement;
	}

	public void setCalledAPInvocationIdElement(String calledAPInvocationIdElement) {
		this.calledAPInvocationIdElement = calledAPInvocationIdElement;
	}

	public int getcalledTotalMessage() {
		return calledTotalMessage;
	}

	public void setcalledTotalMessage(int calledTotalMessage) {
		this.calledTotalMessage = calledTotalMessage;
	}

	public int getSeq() {
		return seq;
	}

	public void setSeq(int seq) {
		this.seq = seq;
	}
	
	
	
	
}
