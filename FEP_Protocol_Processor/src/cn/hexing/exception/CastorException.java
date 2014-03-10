package cn.hexing.exception;

/**
 * Castor øÚº‹“Ï≥£
 */
public class CastorException extends RuntimeException {

    private static final long serialVersionUID = -4800973432327233301L;

    public CastorException() {
        super();
    }

    /**
     * @param message
     * @param cause
     */
    public CastorException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * @param message
     */
    public CastorException(String message) {
        super(message);
    }

    /**
     * @param cause
     */
    public CastorException(Throwable cause) {
        super(cause);
    }

}
