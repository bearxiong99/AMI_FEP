package cn.hexing.fas.protocol.meter.conf;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

/**
 * ���Լ�������
 *
 */
public class MeterProtocolDataItem {
	private String code;			/*�ڲ�ͳһ���ݱ�ʶ*/
	private String description;		/*��������*/
	private String parentCode1;		/*��Ӧ��Լ1���ݱ�ʶ*/
	private String parentCode2;		/*��Ӧ��Լ2���ݱ�ʶ*/
	private int length;				/*���ݳ���*/
	private int length2;			/*07����97�����Լ��ʽ��һ��ʱ�ı������ݳ���*/
	private int type;				/*���ݽ�������*/
	private int fraction;			/*С��λ��*/
	private int fraction2;			/*07����97�����Լ��ʽ��һ��ʱ�ı���С��λ��*/
	private String familycode;		/*�������ʶ*/
	private Hashtable children;
	private int operationTo;  //���������ʶ�����͸��ɼ��������Ǳ�
	private String format;
	private List childarray=new ArrayList();
	
	private int sumLength;
	private String endPos;
	private String startPos;
	
	
	public MeterProtocolDataItem(){
		this("","","","",0,-1,0,0,-1,"");
	}
	
	public MeterProtocolDataItem(String code,String parentCode1,String parentCode2,String description,int len,int len2,int type,int fraction,int fraction2,String familycode){
		this.code=code;
		this.parentCode1=parentCode1;
		this.parentCode2=parentCode2;
		this.length=len;
		this.length2=len2;
		this.type=type;
		this.fraction=fraction;
		this.fraction2=fraction2;
		this.description=description;
		this.familycode=familycode;
		children=new Hashtable();
	}
	
	
	/**
	 * @return Returns the code.
	 */
	public String getCode() {
		return code;
	}

	/**
	 * @param code The code to set.
	 */
	public void setCode(String code) {
		this.code = code;
	}

	/**
	 * @return Returns the fraction.
	 */
	public int getFraction() {
		return fraction;
	}

	/**
	 * @param fraction The fraction to set.
	 */
	public void setFraction(int fraction) {
		this.fraction = fraction;
	}

	/**
	 * @return Returns the len.
	 */
	public int getLength() {
		return length;
	}

	/**
	 * @param len The len to set.
	 */
	public void setLength(int len) {
		this.length = len;
	}

	/**
	 * @return Returns the type.
	 */
	public int getType() {
		return type;
	}

	/**
	 * @param type The type to set.
	 */
	public void setType(int type) {
		this.type = type;
	}

	
	/**
	 * @return Returns the description.
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description The description to set.
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return Returns the familycode.
	 */
	public String getFamilycode() {
		return familycode;
	}

	/**
	 * @param familycode The familycode to set.
	 */
	public void setFamilycode(String familycode) {
		this.familycode = familycode;
	}

	/**
	 * @return Returns the children.
	 */
	public Hashtable getChildren() {
		return children;
	}

	/**
	 * @param children The children to set.
	 */
	public void setChildren(Hashtable children) {
		this.children = children;
	}

	/**
	 * @return Returns the childarray.
	 */
	public List getChildarray() {
		return childarray;
	}

	/**
	 * @param childarray The childarray to set.
	 */
	public void setChildarray(ArrayList childarray) {
		this.childarray = childarray;		
	}

	public String getParentCode1() {
		return parentCode1;
	}

	public void setParentCode1(String parentCode1) {
		this.parentCode1 = parentCode1;
	}

	public String getParentCode2() {
		return parentCode2;
	}

	public void setParentCode2(String parentCode2) {
		this.parentCode2 = parentCode2;
	}

	public int getFraction2() {
		return fraction2;
	}

	public void setFraction2(int fraction2) {
		this.fraction2 = fraction2;
	}

	public int getLength2() {
		return length2;
	}

	public void setLength2(int length2) {
		this.length2 = length2;
	}

	public int getOperationTo() {
		return operationTo;
	}

	public void setOperationTo(int operationTo) {
		this.operationTo = operationTo;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}


	public int getSumLength() {
		return sumLength;
	}

	public void setSumLength(int sumLength) {
		this.sumLength = sumLength;
	}

	public String getEndPos() {
		return endPos;
	}

	public void setEndPos(String endPos) {
		this.endPos = endPos;
	}

	public String getStartPos() {
		return startPos;
	}

	public void setStartPos(String startPos) {
		this.startPos = startPos;
	}

	
}
