package cn.hexing.fas.protocol.zj;

import cn.hexing.fas.model.HostCommand;

/**
 * 浙江规约错误编码
 */
public abstract class ErrorCode {

    /** 正确，无错误 */
    public static final byte CMD_OK = 0x00;
    /** 中继命令没有返回 */
    public static final byte FWD_CMD_NO_RESPONSE = 0x01;
    /** 设置内容非法 */
    public static final byte ILLEGAL_DATA = 0x02;
    /** 密码权限不足 */
    public static final byte INVALID_PASSWORD = 0x03;
    /** 无此项数据 */
    public static final byte NO_DATA = 0x04;
    /** 命令时间失效 */
    public static final byte CMD_TIMEOUT = 0x05;
    /** 目标地址不存在 */
    public static final byte TARGET_NOT_EXISTS = 0x11;
    /** 发送失败 */
    public static final byte CMD_SEND_FAILURE = 0x12;
    /** 短消息帧太长 */
    public static final byte FRAME_TOO_LONG = 0x13;
    
    // 自定义错误码
    /** 终端响应超时 */
    public static final byte RESPONSE_TIMEOUT = (byte) 0xF1;
    /** 主站发送失败，没有有效的下行通道 */
    public static final byte MST_SEND_FAILURE = (byte) 0xF2;
    /** 终端断开连接 */
    public static final byte RTU_DISCONNECT = (byte) 0xF3;
    /** 非法报文 */
    public static final byte ILLEGAL_PACKET = (byte) 0xFF;
    
    /**
     * 将错误代码转化为主站操作命令的状态代码
     * @param errorCode 浙规错误代码
     * @return 主站操作命令的状态代码
     */
    public static String toHostCommandStatus(byte errorCode) {
        switch (errorCode) {
            case CMD_OK:
                return HostCommand.STATUS_SUCCESS;                
            case FWD_CMD_NO_RESPONSE:
            	return HostCommand.STATUS_FWD_CMD_NO_RESPONSE;
            case ILLEGAL_DATA:
            	return HostCommand.STATUS_PARA_INVALID;
            case INVALID_PASSWORD:
            	return HostCommand.STATUS_PERMISSION_DENIDE;
            case NO_DATA:
            	return HostCommand.STATUS_ITEM_INVALID;
            case CMD_TIMEOUT:
                return HostCommand.STATUS_TIME_OVER;                
            case TARGET_NOT_EXISTS:
            	return HostCommand.STATUS_TARGET_UNREACHABLE;
            case CMD_SEND_FAILURE:
            	return HostCommand.STATUS_SEND_FAILURE;
            case FRAME_TOO_LONG:
            	return HostCommand.STATUS_SMS_OVERFLOW;
            case MST_SEND_FAILURE:
                return HostCommand.STATUS_COMM_FAILED;
            case ILLEGAL_PACKET:
            	return HostCommand.STATUS_PARSE_ERROR;
            case RESPONSE_TIMEOUT:
                return HostCommand.STATUS_TIMEOUT;                
            default:
                return HostCommand.STATUS_COMM_FAILED;
        }
    }
}
