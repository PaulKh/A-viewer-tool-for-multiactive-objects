package utils;

import enums.TypeOfRequest;
import exceptions.WrongLogFileFormatException;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.DeserializedRequestData;
import supportModel.ErrorEntity;
import supportModel.ParsedData;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/2/15.
 */
public class DataHelper {
    private DataParser dataParser;
    private List<ErrorEntity> errorEntities;
    private List<ActiveObject> activeObjects;
    public DataHelper(String directory){
        dataParser = new DataParser();
        ParsedData parsedData = dataParser.parseData(directory);
        errorEntities = parsedData.getErrorEntities();
        saturateActiveObjectsWithRequests(parsedData);
    }
    private void saturateActiveObjectsWithRequests(ParsedData parsedData){
//        int counter = 0, counter1 = 0;
        for (DeserializedRequestData requestData:parsedData.getDeserializedRequestDataList()){
//            if (requestData.getTypeOfRequest() == TypeOfRequest.RequestDelivered)
//                counter++;
            enrichThreadEvent(parsedData.getActiveObjects(), requestData);
        }
//        for (ActiveObject activeObject: parsedData.getActiveObjects())
//            for (ActiveObjectThread thread: activeObject.getThreads())
//                for (ThreadEvent threadEvent:thread.getEvents())
//                    counter1++;

        this.activeObjects = parsedData.getActiveObjects();
    }
    private void enrichThreadEvent(List<ActiveObject> activeObjects, DeserializedRequestData requestData){
        for (ActiveObject activeObject: activeObjects){
            for (ActiveObjectThread thread: activeObject.getThreads()){
                for (ThreadEvent threadEvent:thread.getEvents()){
                    if (threadEvent.getId().equals(requestData.getId())){
                        switch (requestData.getTypeOfRequest()){
                            case RequestDelivered:
                                setDerivedTime(threadEvent, requestData);
                                break;
                            case RequestSent:
                                setRequestSent(threadEvent, requestData);
                                break;
                        }
                        return;
                    }
                }
            }
        }
    }
    private void setRequestSent(ThreadEvent threadEvent, DeserializedRequestData requestData){
        threadEvent.setRequestSentTime(requestData.getTimeStamp());
        threadEvent.setSenderThreadId(requestData.getThreadId());
    }
    private void setDerivedTime(ThreadEvent threadEvent, DeserializedRequestData requestData){
        threadEvent.setDerivedTime(requestData.getTimeStamp());
    }
    public List<ErrorEntity> getErrorEntities() {
        return errorEntities;
    }

    public List<ActiveObject> getActiveObjects() {
        return activeObjects;
    }

    public List<ThreadEvent> getOutgoingThreadEvents(ThreadEvent threadEvent){
        List<ThreadEvent> threadEvents = new ArrayList<>();
        for (ActiveObject activeObject: activeObjects){
            for (ActiveObjectThread thread: activeObject.getThreads()){
                for (ThreadEvent innerThreadEvent:thread.getEvents()){
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
