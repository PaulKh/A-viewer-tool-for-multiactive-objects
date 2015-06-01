package supportModel.deserializedData;

import enums.TypeOfRequest;

/**
 * Created by pkhvoros on 3/30/15.
 */
public abstract class DeserializedRequestEntity {
    private String senderIdentifier;
    private String receiverIdentifier;
    private String methodName;
    private int threadId;
    private long sequenceNumber;
    private long timeStamp;

    public static DeserializedRequestEntity buildWithRequestType(TypeOfRequest typeOfRequest) {
        switch (typeOfRequest) {
            case RequestDelivered:
                return new DeserializedRequestsDelivered();
            default:
                return new DeserializedRequestSent();
        }
    }

    public String getSenderIdentifier() {
        return senderIdentifier;
    }

    public void setSenderIdentifier(String senderIdentifier) {
        this.senderIdentifier = senderIdentifier;
    }

    public String getReceiverIdentifier() {
        return receiverIdentifier;
    }

    public void setReceiverIdentifier(String receiverIdentifier) {
        this.receiverIdentifier = receiverIdentifier;
    }

    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void setSequenceNumber(long sequenceNumber) {
        this.sequenceNumber = sequenceNumber;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public String getId() {
        return senderIdentifier + sequenceNumber;
    }
}
