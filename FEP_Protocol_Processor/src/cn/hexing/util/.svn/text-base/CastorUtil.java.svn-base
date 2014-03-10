package cn.hexing.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;

import org.exolab.castor.mapping.Mapping;
import org.exolab.castor.xml.Unmarshaller;
import org.xml.sax.InputSource;

import cn.hexing.exception.CastorException;

/**
 * Castor 辅助工具
 */
public class CastorUtil {
    
    /**
     * 从 xml 文件中解析一个对象
     * @param mappingResource 映射文件
     * @param dataResource 数据文件
     * @return Java 对象
     */
    public static Object unmarshal(String mappingResource, String dataResource) {
        if (!mappingResource.startsWith("/")) {
            mappingResource = "/" + mappingResource;
        }
        if (!dataResource.startsWith("/")) {
            dataResource = "/" + dataResource;
        }
        
        try {
            Mapping mapping = new Mapping();
            InputSource in = new InputSource(CastorUtil.class.getResourceAsStream(mappingResource));
            try {
                mapping.loadMapping(in);
            }
            finally {
                in.getByteStream().close();
            }
            
            Unmarshaller unmarshaller = new Unmarshaller(mapping);
            Reader reader = new BufferedReader(new InputStreamReader(CastorUtil.class.getResourceAsStream(dataResource),"UTF-8"));
            try {
                return unmarshaller.unmarshal(reader);
            }
            finally {
                reader.close();
            }
        }
        catch (Exception ex) {
            String msg = "Error to unmarshal from xml ["
                + "mappingResource: " + mappingResource
                + ", dataResource: " + dataResource + "]";
            throw new CastorException(msg, ex);
        }
    }
}
