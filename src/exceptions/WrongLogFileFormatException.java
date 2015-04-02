package exceptions;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class WrongLogFileFormatException extends Exception {
    public WrongLogFileFormatException() {
        super();
    }

    public WrongLogFileFormatException(String message) {
        super(message);
    }

    public WrongLogFileFormatException(String message, Throwable cause) {
        super(message, cause);
    }

    public WrongLogFileFormatException(Throwable cause) {
        super(cause);
    }
}
