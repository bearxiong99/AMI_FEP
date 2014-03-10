package cn.hexing.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Properties;

/**
 * 基于属性文件的属性列表。从属性文件中加载属性列表，先按绝对路径加载文件，如果文件
 * 不存在，则继续在类路径中查找资源文件。如果最终从资源文件中加载属性列表，则属性列
 * 表为只读，即属性值不能写回到文件中。否则，属性列表为可写的，设置属性值时，将同时
 * 写入属性文件中
 */
public class FileBasedProperties extends Properties {

    private static final long serialVersionUID = 3788399848195322525L;
    
    /** 属性文件 */
    private String filepath;
    /** 是否只读 */
    private boolean readOnly;
    
    /**
     * 构造一个属性列表
     * @param file 属性文件
     */
    public FileBasedProperties(String file) {
        super();
        filepath = getAbsolutePath(file);
        if( null == filepath )
        	throw new RuntimeException("属性文件不存在："+file);
        loadFromFile();
    }
    
    private String getAbsolutePath(String name){
    	File file = new File(name);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	int index = name.lastIndexOf(File.separator);
    	if( index>=0 )
    		name = name.substring(index,name.length());
    	else
    		name = File.separator + name;
    	
    	//从当前工作目录下找config/cfg/configuration目录下的配置文件
    	String workDir = System.getProperty("user.dir");
    	String cfgpath = workDir + File.separator + "config" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = workDir + File.separator + "configuration" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = workDir + File.separator + "cfg" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	
    	//从当前类的根目录搜索
    	String classRoot = PathUtil.getRootPath(FileBasedProperties.class);
    	String path = classRoot + name;
    	file = new File(path);
    	if( file.exists())
    		return file.getAbsolutePath();
    	file = new File(classRoot);
    	
    	//找jar的根目录上级目录
    	classRoot = file.getParentFile().getAbsolutePath();
    	path = classRoot + name;
    	file = new File(path);
    	if( file.exists())
    		return file.getAbsolutePath();
    
    	//上级目录下找配置目录
    	cfgpath = classRoot + File.separator + "config" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = classRoot + File.separator + "configuration" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
    	cfgpath = classRoot + File.separator + "cfg" + name;
    	file = new File(cfgpath);
    	if( file.exists() )
    		return file.getAbsolutePath();
   		return null;
    }

    /**
     * 构造一个属性列表
     * @param file 属性文件
     * @param defaults 属性缺省值
     */
    public FileBasedProperties(String file, Properties defaults) {
        super(defaults);
        filepath = getAbsolutePath(file);
        if( null == filepath )
        	throw new RuntimeException("属性文件不存在："+file);
        loadFromFile();
    }

    /**
     * 判断属性文件是否是只读的
     * @return Returns true - 只读，false - 可写.
     */
    public boolean isReadOnly() {
        return readOnly;
    }

    /**
     * 读取布尔类型属性值
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 布尔类型的属性值。如果未指定属性值，则返回缺省值
     */
    public boolean getBoolean(String key, boolean defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        
        return Boolean.valueOf(value).booleanValue();
    }
    
    /**
     * 读取整形属性值
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 整形的属性值。如果未指定属性值，则返回缺省值
     */
    public int getInt(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        value = value.trim();
        if (value.length() == 0) {
            return defaultValue;
        }
        
        return Integer.parseInt(value);
    }

    /**
     * 读取日期类型的属性值
     * @param key 属性名
     * @return 日期类型的属性值。如果未指定属性值，则返回 null
     */
    public Calendar getDate(String key) {
        String value = getProperty(key);
        return CalendarUtil.parse(value);
    }

    /**
     * 读取日期类型的属性值
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 日期类型的属性值。如果未指定属性值，则返回缺省值
     */
    public Calendar getDate(String key, Calendar defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        
        return CalendarUtil.parse(value, defaultValue);
    }
    
    /**
     * 读取表示文件或缓充区大小的属性。属性值可以不指定单位，也可以指定单位（KB/K、MB/M 或 GB/G）
     * @param key 属性名
     * @param defaultValue 缺省值
     * @return 长整形的属性值。如果未指定属性值，则返回缺省值
     */
    public long getSize(String key, long defaultValue) {
        String value = getProperty(key);
        if (value == null || value.trim().length() == 0) {
            return defaultValue;
        }        
        
        String s = value.trim().toUpperCase();
        if (s.length() == 0) {
            return defaultValue;
        }
        
        long multiplier = 1;
        int index;

        if ((index = s.indexOf("KB")) != -1 || (index = s.indexOf("K")) != -1) {
            multiplier = 1024;
            s = s.substring(0, index);
        }
        else if ((index = s.indexOf("MB")) != -1 || (index = s.indexOf("M")) != -1) {
            multiplier = 1024 * 1024;
            s = s.substring(0, index);
        }
        else if ((index = s.indexOf("GB")) != -1 || (index = s.indexOf("G")) != -1) {
            multiplier = 1024 * 1024 * 1024;
            s = s.substring(0, index);
        }
        
        return Long.parseLong(s) * multiplier;
    }
    
    /* (non-Javadoc)
     * @see java.util.Properties#setProperty(java.lang.String, java.lang.String)
     */
    public synchronized Object setProperty(String key, String value) {
        Object oldValue = super.setProperty(key, value);
        if (!readOnly) {
            OutputStream out = null;
            try {
                File f = new File(filepath);
                if (!f.exists()) {
                    f.createNewFile();
                }
                out = new FileOutputStream(f);
                super.store(out, null);
            }
            catch (FileNotFoundException ex) {
                throw new RuntimeException("Can't store property: " + key, ex);
            }
            catch (IOException ex) {
                throw new RuntimeException("Can't store property: " + key, ex);
            }
            finally {
                if (out != null) {
                    try {
                        out.close();
                    }
                    catch (IOException e) {
                        // 忽略
                    }
                }
            }
        }
        
        return oldValue;
    }

    /**
     * 从属性文件中加载属性列表
     * @param file 属性文件
     */
    public void loadFromFile() {
        File f = new File(filepath);
        InputStream in = null;
        if (f.exists()) {
            readOnly = false;
            try {
                in = new FileInputStream(f);
            }
            catch (FileNotFoundException e) {
                // 忽略
            }
        }
        else {
            in = FileBasedProperties.class.getResourceAsStream(filepath);
            if (in != null) {
                readOnly = true;
            }
        }
        
        if (in != null) {
            try {
                super.load(in);
            }
            catch (IOException ex) {
                throw new RuntimeException("Can't load properties from file: " + filepath, ex);
            }
            finally {
                try {
                    in.close();
                }
                catch (IOException ex) {
                    // 忽略
                }
            }
        }
    }
}
