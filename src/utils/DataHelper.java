package utils;

import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.*;
import supportModel.deserializedData.DeserializedRequestEntity;
import supportModel.deserializedData.DeserializedRequestSent;
import supportModel.deserializedData.DeserializedRequestsDelivered;

import java.util.*;

/**
 * Created by pkhvoros on 4/2/15.
 */
//This is the class which parses data from the given directory
public class DataHelper {
    private DataParser dataParser;
    private List<ErrorEntity> errorEntities;
    private List<ActiveObject> activeObjects;
    private long maximumTime = 0, minimumTime = Long.MAX_VALUE;

    public DataHelper(String directory) {
        long time = System.currentTimeMillis();
        dataParser = new DataParser();
        ParsedData parsedData = dataParser.parseData(directory);
        errorEntities = parsedData.getErrorEntities();
        long timeForParsing = System.currentTimeMillis() - time;
        this.activeObjects = parsedData.getActiveObjects();
        saturateActiveObjectsWithRequests(parsedData);
        collectStatistics();
        System.out.println("time for parsing = " + timeForParsing + "\ntime for merging = "
                + (System.currentTimeMillis() - timeForParsing - time) + "\ntotal time = " + (System.currentTimeMillis() - time)
                + "\nnumber of delivery " + parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().size()
                + "\nnumber of sendings = " + parsedData.getDeserializedRequestData().getDeserializedSendRequestData().size());
    }

    private void saturateActiveObjectsWithRequests(ParsedData parsedData) {
        enrichThreadEvent(parsedData);
    }

//The method updates the values of when request has been delivered, sent and who was the sender
//In other words it is the merging of two different types of logs. One about the activeobject and the other is about request delivery info.
    private void enrichThreadEvent(ParsedData parsedData) {
        for (ActiveObject activeObject : activeObjects) {
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                for (ThreadEvent threadEvent : thread.getEvents()) {
                    setMaxMinForThreadEvent(threadEvent);
                    DeserializedRequestsDelivered delivered = parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().get(threadEvent.getId());
                    DeserializedRequestSent sent = parsedData.getDeserializedRequestData().getDeserializedSendRequestData().get(threadEvent.getId());
                    if (delivered != null) {
                        threadEvent.setDerivedTime(delivered.getTimeStamp());
                    }
                    if (sent != null) {
                        threadEvent.setRequestSentTime(sent.getTimeStamp());
                        threadEvent.setSenderThreadId(sent.getThreadId());
                        addArrows(sent, threadEvent);
                        parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().remove(threadEvent.getId());
                        parsedData.getDeserializedRequestData().getDeserializedSendRequestData().remove(threadEvent.getId());
                    }
                }
            }
        }
        addNotCompletedArrows(parsedData);
        setMaxForNotCompletedEvents(parsedData);
    }
    private void addNotCompletedArrows(ParsedData parsedData){
        for (Map.Entry<String, DeserializedRequestSent> requestSentTuple: parsedData.getDeserializedRequestData().getDeserializedSendRequestData().entrySet()){
            String key = requestSentTuple.getKey();
            DeserializedRequestsDelivered deliveredRequest = parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().get(key);
            if (deliveredRequest == null)
                continue;
            DeserializedRequestSent requestSent = requestSentTuple.getValue();
            ActiveObject senderActiveObject = findActiveObjectWithId(requestSent.getSenderIdentifier());
            if(senderActiveObject != null){
                ActiveObjectThread senderThread = findThreadById(senderActiveObject, requestSent.getThreadId());
                ThreadEvent callerEvent = senderThread.findThreadEventByTime(requestSent.getTimeStamp());
                callerEvent.addArrow(new NotCompleteArrow(callerEvent, requestSent.getTimeStamp(), deliveredRequest.getTimeStamp(), findActiveObjectWithId(deliveredRequest.getReceiverIdentifier())));
            }
        }
    }
    private void addArrows(DeserializedRequestSent sent, ThreadEvent threadEvent){
        ActiveObject senderActiveObject = findActiveObjectWithId(sent.getSenderIdentifier());
        if (senderActiveObject == null)
            return;
        ActiveObjectThread senderThread = findThreadById(senderActiveObject, sent.getThreadId());
        ThreadEvent callerEvent = senderThread.findThreadEventByTime(sent.getTimeStamp());
        Arrow arrow = new CompleteArrow(callerEvent, threadEvent);
        threadEvent.addArrow(arrow);
        callerEvent.addArrow(arrow);
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
    private void setMaxForNotCompletedEvents(ParsedData parsedData){
        for (DeserializedRequestSent sent:parsedData.getDeserializedRequestData().getDeserializedSendRequestData().values()){
            if (sent.getTimeStamp() > maximumTime)
                maximumTime = sent.getTimeStamp();
        }
        for (DeserializedRequestsDelivered delivered:parsedData.getDeserializedRequestData().getDeserializedDeliveryRequestData().values()){
            if (delivered.getTimeStamp() > maximumTime)
                maximumTime = delivered.getTimeStamp();
        }
    }
    private void setMaxMinForThreadEvent(ThreadEvent threadEvent){
        if (threadEvent.getStartTime() < minimumTime) {
            minimumTime = threadEvent.getStartTime();
        }
        if (threadEvent.getFinishTime() > maximumTime) {
            maximumTime = threadEvent.getFinishTime();
        }
        if (threadEvent.getStartTime() + 50 > maximumTime) {
            maximumTime = threadEvent.getStartTime() + 50;
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

    private ActiveObject findActiveObjectWithId(String activeObjectId){
        for (ActiveObject innerActiveObject: activeObjects){
            if (innerActiveObject.getIdentifier().equals(activeObjectId)){
                return innerActiveObject;
            }
        }
        return null;
    }

    private ActiveObjectThread findThreadById(ActiveObject activeObject, int threadId){
        for (ActiveObjectThread innerThread:activeObject.getThreads()){
            if (innerThread.getThreadId() == threadId){
                return innerThread;
            }
        }
        return null;
    }

    public long getMaximumTime() {
        return maximumTime;
    }

    public long getMinimumTime() {
        return minimumTime;
    }

    public StatisticsInformation collectStatistics(){
        StatisticsInformation statisticsInformation = new StatisticsInformation();
        long maximumWatingInQueueTime = Long.MIN_VALUE;
        long maximumRequestExecutionTime = Long.MIN_VALUE;
        long minimumWatingInQueueTime = Long.MAX_VALUE;
        long minimumRequestExecutionTime = Long.MAX_VALUE;
        ThreadEvent maximumWaitingInQueueRequest = null;
        ThreadEvent minimumWaitingInQueueRequest = null;
        ThreadEvent maximumExecutionRequest = null;
        ThreadEvent minimumExecutionRequest = null;
        int counterRequests = 0;
        long totalQueueTime = 0;
        Set<Arrow> arrows = new HashSet<>();
        for (ActiveObject activeObject : activeObjects) {
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                counterRequests += thread.getEvents().size();
                for (ThreadEvent event:thread.getEvents()){
                    long executionTime = event.getFinishTime() - event.getStartTime();
                    long waitingInQueueTime = event.getStartTime() - event.getDerivedTime();
                    arrows.addAll(event.getArrows());
                    if (event.getDerivedTime() != 0) {
                        totalQueueTime += waitingInQueueTime;
                    }
                    if (executionTime > maximumRequestExecutionTime){
                        maximumRequestExecutionTime = executionTime;
                        maximumExecutionRequest = event;
                    }
                    if (executionTime < minimumRequestExecutionTime){
                        minimumRequestExecutionTime = executionTime;
                        minimumExecutionRequest = event;
                    }
                    if (waitingInQueueTime > maximumWatingInQueueTime){
                        maximumWatingInQueueTime = waitingInQueueTime;
                        maximumWaitingInQueueRequest = event;
                    }
                    if (waitingInQueueTime < minimumWatingInQueueTime){
                        minimumWatingInQueueTime = waitingInQueueTime;
                        minimumWaitingInQueueRequest = event;
                    }
                }
            }
        }

        statisticsInformation.setLeastWaitedRequest(minimumWaitingInQueueRequest);
        statisticsInformation.setMostWaitedRequest(maximumWaitingInQueueRequest);
        statisticsInformation.setLongestExecutedRequest(maximumExecutionRequest);
        statisticsInformation.setShortestExecutedRequest(minimumExecutionRequest);

        statisticsInformation.setAverageQueueTime(totalQueueTime/counterRequests);
        statisticsInformation.setNumberOfActiveObjects(activeObjects.size());
        statisticsInformation.setTotalNumberOfRequests(counterRequests);
        statisticsInformation.setTotalNumberOfArrows(arrows.size());
        return statisticsInformation;
    }
}
