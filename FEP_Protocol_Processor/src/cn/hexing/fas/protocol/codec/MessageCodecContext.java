package cn.hexing.fas.protocol.codec;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息编码/解码器上下文。用来设置不能解析的任务号或错误编码
 */
public class MessageCodecContext {

    /** 用来不能解析的任务号的线程局部变量 */
    private static final ThreadLocal task = new ThreadLocal();
    /** 用来不能解析的告警编码列表的线程局部变量 */
    private static final ThreadLocal codes = new ThreadLocal();
    
    /**
     * 设置不能解析的任务号
     * @param taskNum 任务号
     */
    public static void setTaskNum(String taskNum) {
        task.set(taskNum);
    }
    
    /**
     * 取得不能解析的任务号
     * @return 任务号。如果未设置，则返回 null
     */
    public static String getTaskNum() {
        return (String) task.get();
    }
    
    /**
     * 取得不能解析的任务号，然后清除楚上下文中保存的任务号
     * @return 任务号。如果未设置，则返回 null
     */
    public static String pollTaskNum() {
        String taskNum = (String) task.get();
        if (taskNum != null) {
            task.set(null);
        }
        
        return taskNum;
    }
    
    /**
     * 添加不能完全解析的告警编码
     * @param alertCode 告警编码
     */
    public static void addAlertCode(int alertCode) {
        List alertCodes = (List) codes.get();
        if (alertCodes == null) {
            alertCodes = new ArrayList(1);
            codes.set(alertCodes);
        }
        alertCodes.add(new Integer(alertCode));
    }
    
    /**
     * 取得不能完全解析的告警编码列表
     * @return 告警编码列表[Integer]。如果未设置，则返回 null
     */
    public static List getAlertCodes() {
        return (List) codes.get();
    }
    
    /**
     * 取得不能完全解析的告警编码列表，然后清除楚上下文中保存的告警编码列表
     * @return 告警编码列表[Integer]。如果未设置，则返回 null
     */
    public static List pollAlertCodes() {
        List alertCodes = (List) codes.get();
        if (alertCodes != null) {
            codes.set(null);
        }
        
        return alertCodes;
    }
}
