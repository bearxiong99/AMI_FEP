package cn.hexing.fk.utils;

import java.io.Closeable;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * �ļ�����������
 */
public class FileUtil {

	private static Logger log = Logger.getLogger(FileUtil.class);
	
    /**
     * ����Ŀ¼�������Ŀ¼�����ڣ����������и�Ŀ¼
     * @param path ·����
     * @return Ŀ¼����
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
     * ���ļ�������ļ������ڣ��򴴽�֮
     * @param path �ļ�����Ŀ¼
     * @param fileName �ļ���
     * @return �ļ�����
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
     * ɾ���ļ�
     * @param path �ļ�����·��
     * @param fileName �ļ���
     */
    public static void deleteFile(String path, String fileName) {
        File file = new File(path, fileName);
        if (file.exists()) {
            file.delete();
        }
    }
    
    /**
     * ȡ��Ŀ¼�ľ���·��������������·�������·��������û��ĵ�ǰĿ¼��Ϊ�丸Ŀ¼ 
     * @param path ·�����������Ǿ���·�������·��
     * @return ����·����
     */
    public static String getAbsolutePath(String path) {
        File f = new File(path);
        return f.getAbsolutePath();
    }
    
    /**
     * ȡ���ļ��ľ���·����
     * @param path �ļ��Ĵ��·��
     * @param fileName �ļ���
     * @return �ļ��ľ���·����
     */
    public static String getAbsolutePath(String path, String fileName) {
        File dir = mkdirs(getAbsolutePath(path));
        File file = new File(dir, fileName);
        return file.getAbsolutePath();
    }
    
    /**
     * ������д��ָ���ļ�
     * @param pojo
     * @param file
     * @param isAppend,��׷�ӻ��Ǹ���
     */
    public static void writeObjectToFile(Object pojo,File file,boolean isAppend){
    	ObjectOutputStream oos = null;
      	FileOutputStream  fos = null;
    	try {
    		fos = new FileOutputStream(file,isAppend);
    		if(file.length()>1){
    			//����ļ�����1�������ļ���дheader
    			oos = new AppendableObjectOutputStream(fos);
    		}else{
    			oos = new ObjectOutputStream(fos);
    		}
			oos.writeObject(pojo);
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (IOException e) {
			log.error(e.getMessage());
		}finally{
			close(oos);
			close(fos);
		}
    }
    
    public static List<Object> readObjectFromFile(File file){
    	
    	List<Object> result = null;
    	ObjectInputStream ois = null;
    	try {
			ois= new ObjectInputStream(new FileInputStream(file));
			result = new ArrayList<Object>();
			while(true){
				result.add(ois.readObject());
			}
			
		} catch (FileNotFoundException e) {
			log.error(e.getMessage());
		} catch (EOFException e) {
			//�����ļ�β��ʲôҲ������
		} catch (IOException e) {
			log.error(e.getMessage());
		} catch (ClassNotFoundException e) {
			log.error(e.getMessage());
		} catch (Exception e ){
			log.error(e.getMessage());
		}finally{
			close(ois);
		}
		return result;
    }
    
    /**
     * ���ļ���ס�����������̴߳�
     * @param lockFile
     * @return
     */
    public static FileLock tryLockFile(File lockFile){
    	try {
    		
    		if(!lockFile.exists()) return null;
    		
    		//����ļ������ڣ�����Ĵ���Ϊ����һ���ļ�����
            RandomAccessFile randomAccessFile = new RandomAccessFile(
            		lockFile, "rw");

            FileChannel fileChannel = randomAccessFile.getChannel();

            return fileChannel.tryLock();
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    /**
     * �����ļ�
     * @param fileLock
     */
    public static void unlockFile(FileLock fileLock) {
        try {
            fileLock.release();

            FileChannel fileChannel = fileLock.channel();

            close(fileChannel);
        }
        catch (IOException ioe) {
            throw new RuntimeException(ioe);
        }
    }
    
    public static void nioTransferCopy(File source, File target) {
        FileChannel in = null;
        FileChannel out = null;
        FileInputStream inStream = null;
        FileOutputStream outStream = null;
        try {
            inStream = new FileInputStream(source);
            outStream = new FileOutputStream(target);
            in = inStream.getChannel();
            out = outStream.getChannel();
            in.transferTo(0, in.size(), out);
        } catch (IOException e) {
        	log.error(e.getMessage());
        } finally {
        	close(inStream);
        	close(outStream);
        	close(in);
        	close(out);
        }
    }
    
    public static void trashferCopy(String srcPath,String srcFileName,String destPath,String destFileName){
    	File srcDir = new File(srcPath);
    	if(!srcDir.exists())
    		throw new RuntimeException(srcPath+" can't found");
    	File srcFile = new File(srcPath+File.separator+srcFileName);
    	if(!srcFile.exists())
    		throw new RuntimeException(srcFile.getAbsolutePath()+" can't found");
    	mkdirs(destPath);
    	File destFile = new File(destPath+File.separator+destFileName);
    	nioTransferCopy(srcFile, destFile);
    }
    
    public static void close(Closeable c){
    	if(null == c) return;
    	try {
			c.close();
		} catch (Exception e) {
			log.error(e.getMessage());
		}
    }
    
    
    
}
