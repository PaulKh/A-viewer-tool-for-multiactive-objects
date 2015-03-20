package exceptions;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class InconsistentDataSourceException extends Exception {
    public InconsistentDataSourceException() {
    }

    public InconsistentDataSourceException(String path) {
        super(path);
    }

    public InconsistentDataSourceException(String path, Throwable cause) {
        super(path, cause);
    }

    public InconsistentDataSourceException(Throwable cause) {
        super(cause);
    }

    public InconsistentDataSourceException(String path, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(path, cause, enableSuppression, writableStackTrace);
    }
}
