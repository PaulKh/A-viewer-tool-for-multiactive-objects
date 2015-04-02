package supportModel;

import enums.TypeOfRequest;

/**
 * Created by pkhvoros on 3/30/15.
 */
public class DeserializedRequestData {
    private TypeOfRequest typeOfRequest;
    private String senderIdentifier;
    private String receiverIdentifier;
    private String methodName;
    private long sequenceNumber;
    private long timeStamp;

    public DeserializedRequestData(TypeOfRequest typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }

    public TypeOfRequest getTypeOfRequest() {
        return typeOfRequest;
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
}
