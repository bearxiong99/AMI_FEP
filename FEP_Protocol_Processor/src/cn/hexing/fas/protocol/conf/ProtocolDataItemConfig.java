package cn.hexing.fas.protocol.conf;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import cn.hexing.fas.protocol.zj.parse.ParseTool;
import cn.hexing.fk.utils.StringUtil;

/**
 * Э���������
 */
public class ProtocolDataItemConfig implements IDataItem{   
	
	private static final Logger log = Logger.getLogger(ProtocolDataItemConfig.class);

    /** ���ݱ�ʶ���� */
    private String code;
    /** �������ݿ���� */
    private String parentCode;
    /** �������ݳ��� */
    private int length;
    /** ���� ����λparseno+����λfraction*/
    private String type;
    /** ���ݸ�ʽ�� Ŀǰ��Ź������ݸ�ʽ����Ҫ�ǹ����������ø�ʽ���Ǹ����ͣ��޷������ͳһ����*/
    private String format;
    /** ��Լ�������ͣ���Ӧ�ڽ�������� */
    private int parserno=0;
    /** ���ܰ�����С��λ�� */
    private int fraction=0;
    
    private String keychar;
    
    private String bean;
    /** ��Ӧ Bean �������� */
    private String property;
    /** ��������� */
    private List childItems;
    /** ���ݱ�ʶ*/
    private int datakey;
    /** ��Ӧ�ı�׼���ݱ�ʶ*/
    private List items;
    
    private int dkey;
    
    /**
     * �������ݱ�ʶ������ֵ
     * @return ���ݱ�ʶ������ֵ
     */
    public int getDataKey(){
    	return datakey;
    }
    
    public int getDatakey(){
    	return datakey;
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
        this.datakey=ParseTool.HexToDecimal(code);
    }
    /**
     * @return Returns the length.
     */
    public int getLength() {        
        if (length == 0 && childItems != null) {
            for (int i = 0; i < childItems.size(); i++) {
                length += ((ProtocolDataItemConfig) childItems.get(i)).getLength();
            }
        }
        return length;
    }
    /**
     * @param length The length to set.
     */
    public void setLength(int length) {
        this.length = length;
    }
    /**
     * @return Returns the type.
     */
    public String getType() {
        return type;
    }
    /**
     * @param type The type to set.
     */
    public void setType(String type) {
        this.type = type;
        try{
        	if(type!=null){
        		if(type.length()>3){
        			this.parserno=Integer.parseInt(type.substring(0,2));
        			this.fraction=Integer.parseInt(type.substring(2,4));
        		}
        	}
        }catch(Exception e){
        	log.error(StringUtil.getExceptionDetailInfo(e));
        }
    }
    /**
     * @return Returns the bean.
     */
    public String getBean() {
        return bean;
    }

    /**
     * @return Returns the property.
     */
    public String getProperty() {
        return property;
    }
    /**
     * @param property The property to set.
     */
    public void setProperty(String property) {
        this.property = property;
    }
    /**
     * @return Returns the childItems.
     */
    public List getChildItems() {
        return childItems;
    }
    /**
     * @param childItems The childItems to set.
     */
    public void setChildItems(List childItems) {
        this.childItems = childItems;
        length = 0;
    }

	/**
	 * @return Returns the fraction.
	 */
	public int getFraction() {
		return fraction;
	}

	/**
	 * @return Returns the parserno.
	 */
	public int getParserno() {
		return parserno;
	}

	public String getSdRobot() {		
		return null;
	}

	public List getStandardDatas() {		
		return items;
	}

	public boolean isMe(String dataid) {		
		boolean rt=false;
		if(items!=null){
			for(Iterator iter=items.iterator();iter.hasNext();){
				String dk=(String)iter.next();
				if(dk.equalsIgnoreCase(dataid)){
					rt=true;
					break;
				}
			}
		}
		return rt;
	}

	public List getItems() {
		return items;
	}

	public void setItems(List items) {
		this.items = items;
	}

	public String getKeychar() {
		return keychar;
	}

	public void setKeychar(String keychar) {
		this.keychar = keychar;
	}

	public int getDkey() {
		return dkey;
	}

	public void setDkey(int dkey) {
		this.dkey = dkey;
	}

	public String getFormat() {
		return format;
	}

	public void setFormat(String format) {
		this.format = format;
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}
    
    
}
