package xrate;

/**
 * A custom exception class that we'll throw if an attempt to read a API access
 * key from the system environment fails.
 * 
 * This basically just has all the "standard" exception constructors, calling
 * "super()" to ensure that the relevant constructor in RuntimeException is also
 * called.
 */
public class MissingAccessKeyException extends RuntimeException {

    public MissingAccessKeyException() {
    }

    public MissingAccessKeyException(String message) {
        super(message);
    }

    public MissingAccessKeyException(Throwable cause) {
        super(cause);
    }

    public MissingAccessKeyException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingAccessKeyException(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

}
