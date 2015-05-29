package utils;

import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.deserializedData.DeserializedRequestEntity;
import supportModel.ErrorEntity;
import supportModel.ParsedData;
import supportModel.deserializedData.DeserializedRequestSent;
import supportModel.deserializedData.DeserializedRequestsDelivered;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/2/15.
 */
//This is the class which parses data from the given directory
public class DataHelper {
    private DataParser dataParser;
    private List<ErrorEntity> errorEntities;
    private List<ActiveObject> activeObjects;

    public DataHelper(String directory) {
        long time = System.currentTimeMillis();
        dataParser = new DataParser();
        ParsedData parsedData = dataParser.parseData(directory);
        errorEntities = parsedData.getErrorEntities();
        long timeForParsing = System.currentTimeMillis() - time;
        saturateActiveObjectsWithRequests(parsedData);
        int counter = 0;
        for (ActiveObject activeObject:activeObjects){
            for (ActiveObjectThread thread:activeObject.getThreads())
                counter += thread.getEvents().size();
        }
        System.out.println("time for parsing = "  + timeForParsing + "\ntime for merging = "
                + (System.currentTimeMillis() - timeForParsing) + "\ntotal time = " + (System.currentTimeMillis() - time)
                + "\nnumber of delivery " + parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().size()
                + "\nnumber of sendings = " + parsedData.getDeserializedRequestData().getDeserializedSendRequestData().size()
                + "\nevents count = " + counter + "\n");

    }

    private void saturateActiveObjectsWithRequests(ParsedData parsedData) {
//        int counter = 0, counter1 = 0;
        this.activeObjects = parsedData.getActiveObjects();
        enrichThreadEvent(parsedData);
    }
//The method updates the values of when request has been delivered, sent and who was the sender
//In other words it is the merging of two different types of logs. One about the activeobject and the other is about request delivery info.
    private void enrichThreadEvent(ParsedData parsedData) {
        for (ActiveObject activeObject : activeObjects) {
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                for (ThreadEvent threadEvent : thread.getEvents()) {

                    DeserializedRequestsDelivered delivered = parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().get(threadEvent.getId());
                    if (delivered != null)
                        setDerivedTime(threadEvent, delivered);
                    DeserializedRequestSent sent = parsedData.getDeserializedRequestData().getDeserializedSendRequestData().get(threadEvent.getId());
                    if (sent != null)
                        setRequestSent(threadEvent, sent);
                }
            }
        }
    }

    private void setRequestSent(ThreadEvent threadEvent, DeserializedRequestEntity requestData) {
        threadEvent.setRequestSentTime(requestData.getTimeStamp());
        threadEvent.setSenderThreadId(requestData.getThreadId());
    }

    private void setDerivedTime(ThreadEvent threadEvent, DeserializedRequestsDelivered requestData) {
        threadEvent.setDerivedTime(requestData.getTimeStamp());
    }

    public List<ErrorEntity> getErrorEntities() {
        return errorEntities;
    }

    public List<ActiveObject> getActiveObjects() {
        return activeObjects;
    }

//This method identifies threadEvents which were called by the given thread event
    public List<ThreadEvent> getOutgoingThreadEvents(ThreadEvent threadEvent) {
        List<ThreadEvent> threadEvents = new ArrayList<>();
        for (ActiveObject activeObject : activeObjects) {
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                for (ThreadEvent innerThreadEvent : thread.getEvents()) {
                    if (innerThreadEvent.getSenderActiveObjectId() == null)
                        break;
                    if (innerThreadEvent.getSenderActiveObjectId().equals(threadEvent.getThread().getActiveObject().getIdentifier()))
                        if (innerThreadEvent.getSenderThreadId() == threadEvent.getThread().getThreadId())
                            if (innerThreadEvent.getRequestSentTime() >= threadEvent.getStartTime() && innerThreadEvent.getRequestSentTime() <= threadEvent.getFinishTime())
                                threadEvents.add(innerThreadEvent);
                }
            }
        }
        return threadEvents;
    }
}
