package cn.hexing.fas.protocol.conf;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import cn.hexing.fas.protocol.data.DataItem;

/**
 * 协议数据结构定义
 */
public class ProtocolDataConfig implements IDataSets{
	private static final Log log=LogFactory.getLog(ProtocolDataConfig.class);
	
    /** 数据项定义列表 */
    private List dataItems;
    /** 数据项代码与数据项定义对照表 */
    private Map dataset;
    /** 标准数据项与本地数据项对照表 */
    private HashMap sdataset;	
    /** 解码器对照表 */
    private HashMap parsers;
    /** 内部编码和规约编码的转换表*/
    private Map<String,String> codeConvertMap=new HashMap<String,String>();//内部编码和规约编码的转换表
    /**
     * 查找数据项定义
     * @param code 数据标识代码
     * @return 数据项定义
     */
    public ProtocolDataItemConfig getDataItemConfig(String code) {
        if (dataset == null) {
            synchronized (this) {
                if (dataset == null) {
                    dataset = new HashMap();
                    sdataset=new HashMap();
                    parsers=new HashMap();
                    addToItemMap(dataItems);
                }
            }
        }
        ProtocolDataItemConfig pdc=(ProtocolDataItemConfig) dataset.get(code);
        if (pdc==null)//规约数据标识需要通过数据标识转换才能找到内部数据项标识信息
        	pdc=(ProtocolDataItemConfig) dataset.get(getConvertCode(code));
    	return pdc;
    }
    
    public synchronized void fillMap(){
    	dataset = new HashMap();
        sdataset=new HashMap();
        parsers=new HashMap();
    	addToItemMap(dataItems);
    }
    
    /**
     * 将数据项定义添加到数据项代码与数据项定义对照表
     * @param items 数据项定义列表
     */
    private void addToItemMap(List items) {
        if (items == null || items.isEmpty()) {
            return;
        }
        
    	for (int i = 0; i < items.size(); i++) {
            ProtocolDataItemConfig item = (ProtocolDataItemConfig) items.get(i);
            dataset.put(item.getCode(), item);
            if (item.getParentCode()!=null){
            	codeConvertMap.put(item.getParentCode(), item.getCode());
            }
            if(item.getBean()!=null){
            	loadParser(item.getCode(),item.getBean());
            }
            if(item.getItems()!=null){            	
            	List sitems=item.getItems();
            	for(Iterator iter=sitems.iterator();iter.hasNext();){
            		String si=(String)iter.next();
            		String keychar=item.getKeychar();
            		if(keychar!=null){
            			int index=keychar.indexOf(":");
            			if(index>0){//多个key
            				String[] keys=keychar.split(":");
            				for(int j=0;j<keys.length;j++){
            					String key=si+"|"+keys[j];
                        		sdataset.put(key, item.getCode());
            				}
            			}else{
            				String key=si+"|"+item.getKeychar();
                    		sdataset.put(key, item.getCode());
            			}
            		}            		
            	}
            }
            addToItemMap(item.getChildItems());
        }
    }
    
    private void loadParser(String key,String name){
    	if(name!=null){
    		try{
    			if(!parsers.containsKey(name)){
    				Class clazz = Class.forName(name);
    				parsers.put(key, clazz.newInstance());
    			}
    		}catch(Exception e){
    			log.error("load parser",e);
    		}
    	}
    }
    
    /**
     * @return Returns the dataItems.
     */
    public List getDataItems() {
        return dataItems;
    }
    /**
     * @param dataItems The dataItems to set.
     */
    public void setDataItems(List dataItems) {
        this.dataItems = dataItems;
    }

	public String getLocal(String key, Object para) {
		String local=null;
		if(para instanceof DataItem){
			DataItem di=(DataItem)para;
			String pt=(String)di.getProperty("point");
			if(pt!=null){
				String skey=key+"|"+pt;
				local=(String)sdataset.get(skey);
			}			
		}else if(para instanceof String){
			String skey=key+"|"+(String)para;
			local=(String)sdataset.get(skey);
		}
		return local;
	}

	public IItemParser getParser(String key) {
		IItemParser parser=null;
		if(key!=null){
			parser=(IItemParser)parsers.get(key);
		}
		return parser;
	}
	public List getMConf(String key){
		List conf = null;
		if(key!=null){
			
		}
		return conf;
	}
	public String getConvertCode(String code){
		return codeConvertMap.get(code);
	}
}
