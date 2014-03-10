package cn.hexing.exception;

/**
 * œ˚œ¢±‡¬Î“Ï≥£
 */
public class MessageEncodeException extends RuntimeException {
	private int errcode;
	private String code;
	
    private static final long serialVersionUID = -2488563605733447133L;

    public MessageEncodeException() {
        super();
    }

    /**
     * @param message
     */
    public MessageEncodeException(String code,String message) {    	
        super(message);
        this.code=code;
    }
    /**
     * @param message
    */ 
    public MessageEncodeException(String message) {
        super(message);
    }
    /**
     * @param cause
     */
    public MessageEncodeException(Throwable cause) {
        super(cause);
    }

    /**
     * @param message
     * @param cause
     */
    public MessageEncodeException(String message, Throwable cause) {
        super(message, cause);
    }

	public int getErrcode() {
		return errcode;
	}

	public void setErrcode(int errcode) {
		this.errcode = errcode;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
    
}
