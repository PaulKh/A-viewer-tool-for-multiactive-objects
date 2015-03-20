package exceptions;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class WrongLogFileFormat extends Exception {
    public WrongLogFileFormat() { super(); }
    public WrongLogFileFormat(String message) { super("Error: wrong file format. " + "File name=" + message); }
    public WrongLogFileFormat(String message, Throwable cause) { super(message, cause); }
    public WrongLogFileFormat(Throwable cause) { super(cause); }
}
