package cn.hexing.util;

import java.util.Calendar;

/**
 * 访问 FAS 配置属性的工具类
 */
public class FasProperties {

    /** FAS 配置文件名 */
    private static final String PROPS_FILE = "/fas.properties";
    /** 配置属性名：文件保存路径 */
    private static final String PROP_FILE_STORE_PATH = "fas.file.store.path";
    /** 缺省的文件保存路径 */
    private static final String DEFAULT_FILE_STORE_PATH = "fas";
    
    /** FAS 属性集 */
    private static FileBasedProperties props = null;
    
    /**
     * 返回 FAS 属性集
     * @return 属性集
     */
    public static FileBasedProperties getProperties() {
        if (props == null) {
            synchronized (FasProperties.class) {
                if (props == null) {
                    props = new FileBasedProperties(PROPS_FILE);
                }
            }
        }
        
        return props;
    }
    
    /**
     * 取得用于存储通讯服务产生的文件的绝对路径。如果配置文件中指定了相对路径，则相对于
     * 用户当前目录解释相对路径
     * @return 绝对路径名
     */
    public static String getFileStorePath() {
        String path = getProperties().getProperty(PROP_FILE_STORE_PATH);
        if (path == null || path.trim().length() == 0) {
            path = DEFAULT_FILE_STORE_PATH;
        }
        
        return FileUtil.getAbsolutePath(path);
    }
    
    /**
     * 取得某个属性值
     * @param key 属性的键名
     * @return 属性值。若属性不存在，则返回 null
     */
    public static String getProperty(String key) {
        return getProperties().getProperty(key);
    }
    
    /**
     * 取得某个属性值
     * @param key 属性的键名
     * @param defaultValue 缺省值
     * @return 属性值。若属性不存在，则返回缺省值
     */
    public static String getProperty(String key, String defaultValue) {
        return getProperties().getProperty(key, defaultValue);
    }
    
    /**
     * 读取布尔类型属性值
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 布尔类型的属性值。如果未指定属性值，则返回缺省值
     */
    public static boolean getBoolean(String key, boolean defaultValue) {
        return getProperties().getBoolean(key, defaultValue);
    }
    
    /**
     * 读取整形属性值
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 整形的属性值。如果未指定属性值，则返回缺省值
     */
    public static int getInt(String key, int defaultValue) {
        return getProperties().getInt(key, defaultValue);
    }
    
    /**
     * 读取表示大小的属性。属性值可以不指定单位，也可以指定单位（KB/K、MB/M 或 GB/G）
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 长整形的属性值。如果未指定属性值，则返回缺省值
     */
    public static long getSize(String key, long defaultValue) {
        return getProperties().getSize(key, defaultValue);
    }

    /**
     * 读取日期类型的属性值
     * @param key 属性名
     * @return 日期类型的属性值。如果未指定属性值，则返回 null
     */
    public static Calendar getDate(String key) {
        return getProperties().getDate(key);
    }
}
