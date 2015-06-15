package model;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ThreadEvent {
    private static int counter = 0;
    private int localUniqueId;
    //WARNING: sequence number might be not unique in thread
    //Id of calling active object + sequence number
    private long sequenceNumber;
    private long startTime;
    private long finishTime;
    private long derivedTime;
    private String methodName;
    private String senderActiveObjectId;
    private long requestSentTime;
    private int senderThreadId;
    private ActiveObjectThread thread;

    public ThreadEvent(long sequenceNumber, ActiveObjectThread thread) {
        this.sequenceNumber = sequenceNumber;
        this.thread = thread;
        localUniqueId = updateCounter();
    }
    private synchronized int updateCounter(){
        return counter++;
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

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public long getDerivedTime() {
        return derivedTime;
    }

    public void setDerivedTime(long derivedTime) {
        this.derivedTime = derivedTime;
    }

    public long getRequestSentTime() {
        return requestSentTime;
    }

    public void setRequestSentTime(long requestSentTime) {
        this.requestSentTime = requestSentTime;
    }

    public int getSenderThreadId() {
        return senderThreadId;
    }

    public void setSenderThreadId(int senderThreadId) {
        this.senderThreadId = senderThreadId;
    }

    public String getId() {
        return senderActiveObjectId + sequenceNumber;
    }

    public ActiveObjectThread getThread() {
        return thread;
    }

    public String getUniqueMethodName() {
        return methodName + "_" + localUniqueId;
    }
}
