package exceptions;

/**
 * Created by pkhvoros on 3/26/15.
 */
public class RequestNotStop extends Exception {
    public RequestNotStop() {
    }

    public RequestNotStop(String message) {
        super(message);
    }

    public RequestNotStop(String message, Throwable cause) {
        super(message, cause);
    }

    public RequestNotStop(Throwable cause) {
        super(cause);
    }

    public RequestNotStop(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
