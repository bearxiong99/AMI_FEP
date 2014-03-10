/*
 * Created on 2006-2-27
 */
package cn.hexing.fas.model;

import java.io.Serializable;

/**
 * FAAL 通讯请求参数
 */
public class FaalRequestParam implements Serializable {
    
    private static final long serialVersionUID = 8826872062189860703L;
    
    /** 参数名称 */
    private String name;
    /** 参数值 */
    private String value;

    /**
     * 构造一个请求参数
     */
    public FaalRequestParam() {
        super();
    }
    
    /**
     * 构造一个请求参数
     * @param name 参数名称
     * @param value 参数值
     */
    public FaalRequestParam(String name, String value) {
        this.name = name;
        this.value = value;
    }
    
    public String toString() {
        return "DI" + name + "=" + value;
    }
    
    /**
     * @return Returns the name.
     */
    public String getName() {
        return name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        this.name = name;
    }
    /**
     * @return Returns the value.
     */
    public String getValue() {
        return value;
    }
    /**
     * @param value The value to set.
     */
    public void setValue(String value) {
        this.value = value;
    }
}
