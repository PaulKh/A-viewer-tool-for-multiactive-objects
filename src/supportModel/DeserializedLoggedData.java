package supportModel;

import enums.TypeOfRequest;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class DeserializedLoggedData {
    private static int idCounter = 1;
    private TypeOfRequest typeOfRequest;
    private String activeObjectIdentifier;
    private int threadId;
    private long timeStamp;
    private String methodName;
    private String sender;
    private long sequenceNumber;

    public DeserializedLoggedData(TypeOfRequest typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }

    public TypeOfRequest getTypeOfRequest() {
        return typeOfRequest;
    }

    public void setTypeOfRequest(TypeOfRequest typeOfRequest) {
        this.typeOfRequest = typeOfRequest;
    }

    public String getActiveObjectIdentifier() {
        return activeObjectIdentifier;
    }

    public void setActiveObjectIdentifier(String activeObjectIdentifier) {
        this.activeObjectIdentifier = activeObjectIdentifier;
    }

    public int getThreadId() {
        return threadId;
    }

    public void setThreadId(int threadId) {
        this.threadId = threadId;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public String getMethodName() {
        return methodName;
    }


    public String getSender() {
        return sender;
    }

    public long getSequenceNumber() {
        return sequenceNumber;
    }

    public void parseRunnableRequest(String line) {
        String delims = "[,]";
        String[] equations = line.split(delims);
        delims = "[=]";
        String[] leftAndRightSide = equations[0].split(delims);
        this.methodName = leftAndRightSide[leftAndRightSide.length - 1];
        leftAndRightSide = equations[1].split(delims);
        this.sender = leftAndRightSide[leftAndRightSide.length - 1];
        leftAndRightSide = equations[2].split(delims);
        this.sequenceNumber = Long.valueOf(leftAndRightSide[leftAndRightSide.length - 1]);
    }

    public String generateIdentifier(String identifier) {
        String delims = "[.]";
        String[] temp = identifier.split(delims);
        delims = "[_]";
        this.activeObjectIdentifier = temp[temp.length - 1].split(delims)[0] + idCounter;
        idCounter++;
        return this.activeObjectIdentifier;
    }
}
