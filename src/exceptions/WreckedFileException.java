package exceptions;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class WreckedFileException extends Exception {
    public WreckedFileException() {
    }

    public WreckedFileException(String path) {
        super(path);
    }

    public WreckedFileException(String path, Throwable cause) {
        super(path, cause);
    }

    public WreckedFileException(Throwable cause) {
        super(cause);
    }

    public WreckedFileException(String path, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(path, cause, enableSuppression, writableStackTrace);
    }
}
