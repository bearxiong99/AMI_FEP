package cn.hexing.fas.protocol.pos;

import java.util.HashMap;
import java.util.Map;

public class PosItemConfig {
	public static Map<String,String> itemMap = new HashMap<String, String>();
	
	public static Map<String,String> itemSubMap=new HashMap<String, String>();
	
	public static Map<String,String> itemBlockMap = new HashMap<String, String>();
	
	/**��������*/
	public static final byte FUNC_1  = 1;
	public static final byte FUNC_F  = 0x0F;
	public static final byte FUNC_2  = 0x02;
	public static final byte FUNC_3  = 0x03;
	/**������Ϣ*/
	public static final byte FUNC_4  = 0x04;
	/**�����½*/
	public static final byte FUNC_5  = 0x05;
	/**��½��Ӧ*/
	public static final byte FUNC_6  = 0x06;
	/**����������*/
	public static final byte FUNC_7  = 0x07;
	/**����ע����½*/
	public static final byte FUNC_9  = 0x09;
	/**�����·�token������*/
	public static final byte FUNC_B  = 0x0B;
	/**�����ط�token*/
	public static final byte FUNC_D  = 0x0D;
	/**���󽻰�СƱ*/
	public static final byte FUNC_11  = 0x11;
	/**��ӡ���Ӱ�СƱ��Ӧ֡*/
	public static final byte FUNC_12  = 0x12;
	/**���ͻ�ID��������*/
	public static final byte FUNC_13 = 0x13;
	/**���ͻ�ID������Ӧ*/
	public static final byte FUNC_14=0x14;
	/**�첽������ѯ��ȡ��*/
	public static final byte FUNC_15=0x15;
	/**�첽���������Ӧ*/
	public static final byte FUNC_16=0x16;
	/**�µ�Э��ͷ��ʶ*/
	public static final byte FUNC_BC=(byte) 0xBC;
	/**��Э���½*/
	public static final short FUNC_BC_0001=(short)0x0001;
	/**��Э���½����*/
	public static final short FUNC_BC_0002=(short)0x0002;
	/**��Э�鹺������*/
	public static final short FUNC_BC_0003=(short)0x0003;
	/**��Э�鹺��������Ӧ*/
	public static final short FUNC_BC_0004=(short)0x0004;
	/**��ѵ�������*/
	public static final short FUNC_BC_0005=(short)0x0005;
	/**��ѵ�����Ӧ*/
	public static final short FUNC_BC_0006=(short)0x0006;
	/**�µĲ���TOKEN����*/
	public static final short FUNC_BC_0007=(short)0x0007;
	/**�µĲ���TOKEN��Ӧ*/
	public static final short FUNC_BC_0008=(short)0x0008;
	/**ȷ�ϱ����Ϣ����*/
	public static final short FUNC_BC_0009=(short)0x0009;
	/**ȷ�ϱ����Ϣ��Ӧ*/
	public static final short FUNC_BC_000A=(short)0x000A;
	/**Trail Vend request*/
	public static final short FUNC_BC_000B=(short)0x000B;
	/**Trail Vend response*/
	public static final short FUNC_BC_000C=(short)0x000C;
	/**Update master key request*/
	public static final short FUNC_BC_000D=(short)0x000D;
	/**Update master key response*/
	public static final short FUNC_BC_000E=(short)0x000E;
	/**Customer Fault Report request*/
	public static final short FUNC_BC_000F=(short)0x000F;
	/**Customer Fault Report response*/
	public static final short FUNC_BC_0010=(short)0x0010;
	/**��ȡ�ְ�����*/
	public static final short FUNC_BC_0011=(short)0x0011;
	public static final short FUNC_BC_0013=(short)0x0013;

	/**����*/
	public static final byte FUNC_FF  = (byte) 255;
	
	
	static{
		itemMap.put("1", "BCD6#HTB4#HTB1#HTB1#BCD6#HTB4#HTB4#HTB1#HTB1#HTB2#HTB4#BCD10#HTB1#BCD10#HTB1#BCD10#HTB1#HTB4");
		itemMap.put("15", "BCD6#HTB4#HTB1#HTB1#BCD8#HTB1#BCD6#HTB4#HTB4#HTB1#HTB1#HTB2#HTB4#BCD10#HTB1#BCD10#HTB1#BCD10#HTB1#HTB4");
		itemMap.put("2", "BCD6#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4");
		itemMap.put("3", "BCD6#HTB4#HTB4#HTB4#HTB4#BCD1#BCD8#HTB4");
		itemMap.put("4", "BCD8#BCD6#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#BCD6#BCD10#BCD10#BCD10#HTB1");
		itemMap.put("5", "ASC21#ASC33#ASC16");
		itemMap.put("6", "HTB4#BCD6");
		itemMap.put("7", "BCD8#BCD6#ASC21#ASC33#HTB4");
		itemMap.put("9", "HTB4#ASC16");
		itemMap.put("11", "BCD8#BCD6#HTB4");
		itemMap.put("13", "BCD6#HTB4");
		itemMap.put("17", "BCD6#HTB4");
		itemMap.put("18", "HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#BCD6#BCD6#HTB4");
		itemMap.put("19", "ASC18#HTB1#HTB4#HTB1#ASC1#BCD8#HTB4");
		itemMap.put("20", "BCD8#BCD6#ASC18#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#HTB4#BCD6#BCD10#BCD10#BCD10#HTB1");
		itemMap.put("21", "HTB4#HTB1");
		itemMap.put("22","HTB4#HTB4");
		
		itemMap.put("188", "HTB4#HTB4#CB_HTB1_0_0");
		
		itemMap.put("255", "HTB4#ASC90");
		
		//CB_HTB1_0_0:��ʾѡ���_ѡ������_subClass_index
		
		itemSubMap.put("0", "HTB4#HTB4#CB_HTB1_0_0");
		itemSubMap.put("1", "ASC21#ASC33#HTB1#ASC16");
		itemSubMap.put("2","ASC13#ASC13#ASC80#ASC10#HTB4#HTB4");
		itemSubMap.put("3","CB_HTB1_3_1#HTB1#HTB4#CB_HTB1_3_2#HTB4");
		itemSubMap.put("4","ASC40#ASC40#ASC40#ASC80#BCD7#ASC15#ASC13#ASC13#ASC20#ASC13#HTB1#HTB1#BCD3#HTB1#HTB1#N1#CB_HTB1_4_1");

		itemSubMap.put("5", "CB_HTB1_5_1#HTB1#HTB4#HTB1#HTB4");
		itemSubMap.put("6", itemSubMap.get("4"));
		
		itemSubMap.put("7", "CB_HTB1_7_1#HTB4");
		itemSubMap.put("8", "N1!"+itemSubMap.get("4"));
		
		itemSubMap.put("9","CB_HTB1_5_1#HTB4");
		itemSubMap.put("10","ASC13#BCD3#HTB1#HTB1#HTB1#HTB1");
		
		itemSubMap.put("11", itemSubMap.get("3"));
		itemSubMap.put("12", itemSubMap.get("4"));
		
		itemSubMap.put("13",itemSubMap.get("9"));
		itemSubMap.put("14", "ASC40#ASC40#ASC40#ASC80#BCD7#ASC13#ASC13#ASC160#ASC160#ASC13#BCD3#HTB1#HTB1#HTB1#HTB1#BCD10#BCD10#BCD3#HTB1#HTB1#ASC160#HTB4#BCD10");
		
		itemSubMap.put("15","CB_HTB1_5_1#HTB1#ASC160#ASC40#ASC80#ASC12#HTB4");
		itemSubMap.put("16", "ASC40#ASC13#ASC13#BCD7#ASC40#ASC12#ASC80#ASC13#BCD3#HTB1#HTB1#HTB1#HTB1#ASC160#ASC20#ASC30#ASC160#ASC30");
		itemSubMap.put("17", "HTB4#HTB4");
		itemSubMap.put("19", "HTB4#HTB4");

		/*
		 * ��ʽ˵����
		 * key��'_'�ָ� 
		 * ��һ��ֵ��ʾ����ID���ڶ���ֵ��ʾ��ǰ����ID�ĵڼ�������,������ֵ��ʾ��ID
		 * 
		 * */
		itemBlockMap.put("0_0_1","HTB1#HTB1#HTB1#HTB4");
		
		
		
		itemBlockMap.put("3_1_0", "ASC13");
		itemBlockMap.put("3_1_1","ASC37");
		itemBlockMap.put("3_1_2","ASC13#BCD3#HTB1#HTB1#HTB1#HTB1");
		
		itemBlockMap.put("3_2_1", "HTB4"); 
		
		itemBlockMap.put("4_1_0", "ASC15#ASC160#HTB4#HTB4#BCD10#ASC160#ASC160#N1,HTB1,HTB4,HTB4,ASC160");
		itemBlockMap.put("4_1_1", "ASC20#ASC160#ASC160#ASC160#ASC160#HTB4#N1,HTB1,HTB4,HTB4,ASC160");
		itemBlockMap.put("4_1_2", itemBlockMap.get("4_1_0"));
		itemBlockMap.put("5_1_0", "ASC13");
		itemBlockMap.put("5_1_1", "ASC37");
		itemBlockMap.put("5_1_2", "ASC13#BCD3#HTB1#HTB1#HTB1#HTB1");
		itemBlockMap.put("7_1_0", "ASC13");
		itemBlockMap.put("7_1_1", "ASC37");
		itemBlockMap.put("7_1_3", "ASC10");
		
		
	}
	
	private static long frameCounter=0;
	
	public static long getNextFrameCounter(){
		frameCounter++;
		if((frameCounter > 0x7FFFFFFF) ){
			frameCounter=0;
		}
		return frameCounter;
	}
	
	public static Map<Long,byte[]> subFrameMap = new HashMap<Long, byte[]>();
	
	public static void main(String[] args) {
		System.out.println(0x7FFFFFFF);
		for(int i=0;i<10;i++){
			System.out.println(getNextFrameCounter());
		}
	}
}
