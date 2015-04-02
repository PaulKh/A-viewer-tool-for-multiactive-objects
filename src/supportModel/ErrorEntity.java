package supportModel;

import enums.Error;

/**
 * Created by pkhvoros on 3/25/15.
 */
public class ErrorEntity {
    private Error errorType;
    private String message;

    public ErrorEntity(Error errorType) {
        this.errorType = errorType;
    }

    public Error getErrorType() {
        return errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
