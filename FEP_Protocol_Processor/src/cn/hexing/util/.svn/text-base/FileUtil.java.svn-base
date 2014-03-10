package cn.hexing.util;

import java.io.File;
import java.io.IOException;

/**
 * 文件操作工具类
 */
public class FileUtil {

    /**
     * 创建目录。如果父目录不存在，将创建所有父目录
     * @param path 路径名
     * @return 目录对象
     */
    public static File mkdirs(String path) {
        File dir = new File(path);
        if (dir.isFile()) {
            throw new IllegalArgumentException(path + " is not a directory");
        }
        
        if (!dir.exists()) {
            dir.mkdirs();
        }
        
        return dir;
    }
    
    /**
     * 打开文件。如果文件不存在，则创建之
     * @param path 文件所在目录
     * @param fileName 文件名
     * @return 文件对象
     */
    public static File openFile(String path, String fileName) {
        File dir = mkdirs(path);
        File file = new File(dir, fileName);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }
            catch (IOException ex) {
                throw new RuntimeException("Error to open file: " + fileName, ex);
            }
        }
        
        return file;
    }
    
    /**
     * 删除文件
     * @param path 文件所在路径
     * @param fileName 文件名
     */
    public static void deleteFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
     * 取得目录的绝对路径名。如果传入的路径是相对路径，则把用户的当前目录作为其父目录 
     * @param path 路径名。可能是绝对路径或相对路径
     * @return 绝对路径名
     */
    public static String getAbsolutePath(String path) {
        File f = new File(path);
        return f.getAbsolutePath();
    }
    
    /**
     * 取得文件的绝对路径名
     * @param path 文件的存放路径
     * @param fileName 文件名
     * @return 文件的绝对路径名
     */
    public static String getAbsolutePath(String path, String fileName) {
        File dir = mkdirs(getAbsolutePath(path));
        File file = new File(dir, fileName);
        return file.getAbsolutePath();
    }
}
