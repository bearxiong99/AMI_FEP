package cn.hexing.fas.protocol.zj;

/**
 * 浙江规约功能码
 */
public abstract class FunctionCode {

    // 浙规功能码
    /** 功能码：读中继 */
    public static final int READ_FORWARD_DATA = 0x00;
    /** 功能码：读当前数据 */
    public static final int READ_CURRENT_DATA = 0x01;
    /** 功能码：读任务数据 */
    public static final int READ_TASK_DATA = 0x02;
    /** 功能码：读编程日志 */
    public static final int READ_PROGRAM_LOG = 0x04;
    /** 功能码：实时写对象参数 */
    public static final int REALTIME_WRITE_PARAMS = 0x07;
    /** 功能码：写对象参数 */
    public static final int WRITE_PARAMS = 0x08;
    /** 功能码：读告警数据 */
    public static final int READ_ALERT = 0x09;
    /** 功能码：告警确认 */
    public static final int CONFIRM_ALERT = 0x0A;
    
    /** 功能码：读日冻结数据 */
    public static final int READ_HISTORY_DATA11 = 0x11;
    /** 功能码：批量日冻结数据 */
    public static final int READ_HISTORY_DATA12 = 0x12;
    /** 功能码：控制操作类 */
    public static final int Action = 0x14;
    /** 功能码：自动注册 */
    public static final int AutoRegistered = 0x15;
    /** 功能码：事件告警 */
    public static final int Event = 0x19;
    /** 功能码：远程升级 */
    public static final int RemoteUpgrade = 0x30;
    /** 功能码：网络预付费信息类 */
    public static final int pay_token = 0x33;
    /** 功能码：发送短信 */
    public static final int SEND_SMS = 0x28;
   
    // 自定义功能码，用于内部特殊用途，如将消息解码成原始报文或非法报文
    /** 功能码：刷新通讯服务缓存 */
    public static final int REFRESH_CACHE = 0xFE;
    /** 功能码：其它自定义操作 */
    public static final int OTHER = 0xFF;

    
}