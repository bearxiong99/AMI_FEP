package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


/**
 * FAAL 通讯响应,携带测量点号
 */
public class FaalRequestResponseNew implements Serializable{
	private static final long serialVersionUID = 3L;
	
    /** 主站操作请求对应的命令ID */
    private Long cmdId;
    /** 终端局号ID */
    private String rtuId;
    /** 终端业务类型:01专变；02集抄*/
    private String rtuType;
    /** FAAL 通讯请求类型 */
    private String cmdStatus;
    /** FAAL 通讯请求返回参数结果 */
    private List<DataItem> params;
    
    public void addDataItem(DataItem item) {
        if (params == null) {
        	params = new ArrayList<DataItem>();
        }
        params.add(item);
    }
    public void addDataItem(String tn,String code,String value) {
        if (params == null) {
        	params = new ArrayList<DataItem>();
        }
        DataItem item=new DataItem();
        item.setTn(tn);        
        item.setCode(code);
        item.setValue(value);
        params.add(item);
    }
	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public String getCmdStatus() {
		return cmdStatus;
	}
	public void setCmdStatus(String cmdStatus) {
		this.cmdStatus = cmdStatus;
	}
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	
	public String getRtuType() {
		return rtuType;
	}
	public void setRtuType(String rtuType) {
		this.rtuType = rtuType;
	}
	public List<DataItem> getParams() {
		return params;
	}

       
}
