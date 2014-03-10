package cn.hexing.fas.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * FAAL 通讯请求终端参数，应用于参数读取和查询、一二类数据查询
 */
public class FaalRequestRtuParam implements Serializable {
    
    private static final long serialVersionUID = 8826872062189860710L;
    
    /** 终端局号 */
    private String rtuId;
    /** 命令ID */
    private Long cmdId;
    /** 测量点号数组 */
    private int[] tn;
    /** 请求参数列表 */
    private List<FaalRequestParam> params;
    /**
     * 构造一个请求参数
     */
    public FaalRequestRtuParam() {
    }
    /**
     * 添加请求参数
     * @param name 参数名称
     * @param value 参数值
     */
    public void addParam(String name, String value) {
        addParam(new FaalRequestParam(name, value));
    }
    /**
     * 添加浙规请求参数
     * @param param 请求参数
     */
    public void addParam(FaalRequestParam param) {
        if (params == null) {
            params = new ArrayList<FaalRequestParam>();
        }
        params.add(param);
    }
	public String getRtuId() {
		return rtuId;
	}
	public void setRtuId(String rtuId) {
		this.rtuId = rtuId;
	}
	public Long getCmdId() {
		return cmdId;
	}
	public void setCmdId(Long cmdId) {
		this.cmdId = cmdId;
	}
	public int[] getTn() {
		return tn;
	}
	public void setTn(int[] tn) {
		this.tn = tn;
	}
	public List<FaalRequestParam> getParams() {
		return params;
	}
	public void setParams(List<FaalRequestParam> params) {
		this.params = params;
	}
    
}
  
