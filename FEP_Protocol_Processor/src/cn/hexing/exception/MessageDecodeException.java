package cn.hexing.exception;

/**
 * 消息解码异常
 */
public class MessageDecodeException extends RuntimeException {

    private static final long serialVersionUID = 3337569199562775364L;

    public MessageDecodeException() {
        super();
    }

    /**
     * @param message
     */
    public MessageDecodeException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public MessageDecodeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public MessageDecodeException(String message, Throwable cause) {
        super(message, cause);
    }

}
