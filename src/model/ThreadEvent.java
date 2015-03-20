package model;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ThreadEvent {
    private long id;
    private long startTime;
    private long finishTime;
    private String methodName;
    private String senderActiveObjectId;

    public ThreadEvent(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public long getFinishTime() {
        return finishTime;
    }

    public void setFinishTime(long finishTime) {
        this.finishTime = finishTime;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public String getSenderActiveObjectId() {
        return senderActiveObjectId;
    }

    public void setSenderActiveObjectId(String senderActiveObjectId) {
        this.senderActiveObjectId = senderActiveObjectId;
    }

    public long getId() {
        return id;
    }
}
