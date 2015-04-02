package enums;

/**
 * Created by pkhvoros on 3/16/15.
 */
public enum TypeOfRequest {
    None(0),
    ServeStarted(4),
    RequestDelivered(0),
    RequestSent(0),
    ServeStopped(4);
    private int numberOfLinesInLog;

    TypeOfRequest(int numberOfLinesInLog) {
        this.numberOfLinesInLog = numberOfLinesInLog;
    }

    public int getNumberOfLinesInLog() {
        return numberOfLinesInLog;
    }
}
