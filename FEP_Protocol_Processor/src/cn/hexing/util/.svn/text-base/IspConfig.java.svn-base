package cn.hexing.util;

import java.util.HashMap;
import java.util.Map;

/**
 * 通讯服务提供商配置
 */
public class IspConfig {

    /** ISP 编码：中国联通 */
    public static final String ISP_CODE_UNICOM = "01";
    /** ISP 编码：中国移动 */
    public static final String ISP_CODE_MOBILE = "02";
    
    /** ISP 配置属性名的前缀 */
    private static final String PROP_PFEFIX = "fas.isp.";
    /** 属性名：允许的最大短信条数 */
    private static final String PROP_MAX_SMSCOUNT = ".maxSmsCount";
    /** 属性名：允许的最大流量 */
    private static final String PROP_MAX_THROUGHPUT = ".maxThroughput";
    
    /** 单例 */
    private static IspConfig instance;
    /** 通讯限制集合[ispCode - CommLimit] */
    private Map limits = new HashMap(4);
    
    /**
     * 构造一个通讯服务提供商配置
     */
    private IspConfig() {
        CommLimit limit = readCommLimit(ISP_CODE_UNICOM);
        if (limit != null) {
            limits.put(ISP_CODE_UNICOM, limit);
        }
        
        limit = readCommLimit(ISP_CODE_MOBILE);
        if (limit != null) {
            limits.put(ISP_CODE_MOBILE, limit);
        }
    }
    
    /**
     * 取得通讯服务提供商配置实例
     * @return 配置实例
     */
    public static IspConfig getInstance() {
        synchronized (IspConfig.class) {
            if (instance == null) {
                instance = new IspConfig();
            }
        }
        return instance;
    }
    
    /**
     * 取得某个通讯提供商允许的最大短信条数
     * @param ispCode 通讯提供商编码
     * @return 允许的最大短信条数
     */
    public long getMaxSmsCount(String ispCode) {
        CommLimit limit = (CommLimit) limits.get(ispCode);
        return (limit == null ? 0L : limit.getMaxSmsCount());
    }
    
    /**
     * 取得某个通讯提供商允许的最大流量
     * @param ispCode 通讯提供商编码
     * @return 允许的最大流量
     */
    public long getMaxThroughput(String ispCode) {
        CommLimit limit = (CommLimit) limits.get(ispCode);
        return (limit == null ? 0L : limit.getMaxThroughput());
    }
    
    /**
     * 从配置文件中读取某个通讯提供商的流量限制
     * @param ispCode 通讯提供商编码
     * @return 流量限制
     */
    private CommLimit readCommLimit(String ispCode) {
        CommLimit limit = new CommLimit();
        long l = FasProperties.getSize(PROP_PFEFIX + ispCode + PROP_MAX_SMSCOUNT, 0);
        limit.setMaxSmsCount(l);
        l = FasProperties.getSize(PROP_PFEFIX + ispCode + PROP_MAX_THROUGHPUT, 0);
        limit.setMaxThroughput(l);
        
        return limit;
    }
}

/**
 * 通讯限制
 * @author 张文亮
 */
class CommLimit {
    
    /** 允许的最大短信条数 */
    private long maxSmsCount;
    /** 允许的最大流量（Byte） */
    private long maxThroughput;
    
    /**
     * @return Returns the maxSmsCount.
     */
    public long getMaxSmsCount() {
        return maxSmsCount;
    }
    /**
     * @param maxSmsCount The maxSmsCount to set.
     */
    public void setMaxSmsCount(long maxSmsCount) {
        this.maxSmsCount = maxSmsCount;
    }
    /**
     * @return Returns the maxThroughput.
     */
    public long getMaxThroughput() {
        return maxThroughput;
    }
    /**
     * @param maxThroughput The maxThroughput to set.
     */
    public void setMaxThroughput(long maxThroughput) {
        this.maxThroughput = maxThroughput;
    }
}
