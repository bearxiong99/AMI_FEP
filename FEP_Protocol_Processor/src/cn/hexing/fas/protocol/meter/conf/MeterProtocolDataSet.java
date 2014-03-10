package cn.hexing.fas.protocol.meter.conf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


/**
 * 表规约数据集
 *
 */
public class MeterProtocolDataSet {
	private static final String PROTOCOL_NAME="ZJMETER";
	private String name;
	private Hashtable dataset;
	private Map<String,String> codeConvertMap1=new HashMap<String,String>();//内部编码和外部电表规约1编码的转换表
	private Map<String,String> codeConvertMap2=new HashMap<String,String>();//内部编码和外部电表规约2编码的转换表
	private List dataarray=new ArrayList();
	
	
	/**
	 * default constructor
	 *
	 */
	public MeterProtocolDataSet(){
		this(PROTOCOL_NAME,new Hashtable());
	}
	
	/**
	 * constructor
	 * @param dataset
	 */
	public MeterProtocolDataSet(String name,Hashtable dataset){		
		this.name=name;
		this.dataset=dataset;
	}
	
	/**
	 * 获取表规约数据项配置
	 * @param code
	 * @return
	 */
	public MeterProtocolDataItem getDataItem(String code){
		return (MeterProtocolDataItem)dataset.get(code);
	}
	
	/**
	 * 获取表规约数据集
	 * @return Returns the dataset.
	 */
	public Hashtable getDataset() {
		return dataset;
	}

	/**
	 * @param dataset The dataset to set.
	 */
	public void setDataset(Hashtable dataset) {
		this.dataset = dataset;
	}
		
	/**
	 * @return Returns the dataarray.
	 */
	public List getDataarray() {
		return dataarray;
	}

	/**
	 * @param dataarray The dataarray to set.
	 */
	public void setDataarray(List dataarray) {
		this.dataarray = dataarray;		
	}

	/**
	 * 获取表规约名称
	 * @return Returns the name.
	 */
	public String getName() {
		return name;
	}
	
	public void setName(String name){
		this.name=name;
	}
	
	public void packup(){
		arrayToMap(dataarray);
	}
	
	private void arrayToMap(List datas){
		if(datas!=null){
			for(Iterator iter=datas.iterator();iter.hasNext();){
				MeterProtocolDataItem child=((MeterProtocolDataItem)iter.next());				
				addChild(child);				
			}
		}
	}
	
	/**
	 * 增加子项(家族的父辈要先于子辈加载，否则出错)
	 * @param item
	 */
	private void addChild(MeterProtocolDataItem item){		
		dataset.put(item.getCode(),item);	//暴露在map中
		if (item.getParentCode1()!=null&&item.getParentCode1().length()>0)
			codeConvertMap1.put(item.getParentCode1(), item.getCode());
		if (item.getParentCode2()!=null&&item.getParentCode2().length()>0)
			codeConvertMap2.put(item.getParentCode2(), item.getCode());
		List cnodes=item.getChildarray();	//处理子节点
		arrayToMap(cnodes);
	}
	public String getConvertCode(String code){
		if(codeConvertMap1.get(code)!=null)
			return codeConvertMap1.get(code);
		else
			return codeConvertMap2.get(code);
	}

}
