package utils;

import enums.Error;
import enums.TypeOfRequest;
import exceptions.WreckedFileException;
import exceptions.WrongLogFileFormatException;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.DeserializedActiveObjectData;
import supportModel.DeserializedRequestData;
import supportModel.ErrorEntity;
import supportModel.ParsedData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class DataParser {

    private static Map<String, String> oldAndNewAOIdsKeyValuePairs = new HashMap<>();

    public static ParsedData parseData(String sourceDirectory) throws WrongLogFileFormatException {
        List<ActiveObject> activeObjects = new ArrayList<ActiveObject>();
        List<ErrorEntity> errorEntities = new ArrayList<>();
        List<DeserializedRequestData> deserializedRequestDataList = new ArrayList<>();

        try {
            Files.walk(Paths.get(sourceDirectory)).forEachOrdered(filePath -> {
                    if (!Files.isDirectory(filePath)) {
                        WrappedActiveObjectWithError activeObject = null;
                        try {
                            if (filePath.getFileName().toString().startsWith("ActiveObject")){
                                activeObject = readActiveObjectFile(filePath);
                                activeObjects.add(activeObject.getActiveObject());
                                errorEntities.addAll(activeObject.getErrorEntities());
                            }
                            else if(filePath.getFileName().toString().startsWith("Request")){
                                WrappedRequestWithError wrappedRequestWithError = readRequestFile(filePath);
                                errorEntities.addAll(wrappedRequestWithError.getErrorEntities());
                                deserializedRequestDataList.addAll(wrappedRequestWithError.getRequestData());
                            }
                            else{
                                errorEntities.add(generateWreckedWrongFileErrorEntity(filePath.toString(), Error.WrongFileFormat));
                            }
                        } catch (WrongLogFileFormatException exception) {
                            errorEntities.add(generateWreckedWrongFileErrorEntity(exception.getMessage(), Error.WrongFileFormat));
                        }

                    }

            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        ParsedData parsedData = new ParsedData();
        parsedData.setActiveObjects(activeObjects);
        parsedData.addAllErrorEntities(errorEntities);
        return parsedData;
    }
    private static WrappedRequestWithError readRequestFile(Path path){
        List<DeserializedRequestData> deserializedRequestDataList = new ArrayList<>();
        List<String> lines = null;
        List<ErrorEntity> errorEntities = new ArrayList<>();
        try {
            lines = Files.readAllLines(path);
            for (int i = 0; lines.size() > i; i++) {
                String line = lines.get(i);
                try {
                    if(line.startsWith("deliverrequest")){
                        deserializedRequestDataList.add(parseDeliverRequests(line, TypeOfRequest.RequestDelivered));
                    }
                    else if (line.startsWith("requestsent")){
                        deserializedRequestDataList.add(parseDeliverRequests(line, TypeOfRequest.RequestSent));
                    }
                } catch (WreckedFileException e) {
                    errorEntities.add(generateWreckedWrongFileErrorEntity(path.toString(), Error.WreckedFile));
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WrappedRequestWithError requestWithError = new WrappedRequestWithError();
        requestWithError.setRequestData(deserializedRequestDataList);
        requestWithError.setErrorEntities(errorEntities);
        return requestWithError;

    }
    private static WrappedActiveObjectWithError readActiveObjectFile(Path path) throws WrongLogFileFormatException{
        List<DeserializedActiveObjectData> deserializedLoggedDataList = new ArrayList<>();
        List<ErrorEntity> errorEntities = new ArrayList<>();
        DeserializedActiveObjectData currentRequest = null;
        int numberOfLinesForRequest = 0;
        try {
            List<String> lines = Files.readAllLines(path);
            for (int i = 0; lines.size() > i; i++) {
                String line = lines.get(i);
                switch (numberOfLinesForRequest) {
                    case 0:
                        if (currentRequest != null) {
                            deserializedLoggedDataList.add(currentRequest);
                        }
                        if (line.startsWith("ServeStarted")) {
                            currentRequest = new DeserializedActiveObjectData(TypeOfRequest.ServeStarted);
                            numberOfLinesForRequest = TypeOfRequest.ServeStarted.getNumberOfLinesInLog();
                        } else if (line.startsWith("ServeStopped")) {
                            currentRequest = new DeserializedActiveObjectData(TypeOfRequest.ServeStopped);
                            numberOfLinesForRequest = TypeOfRequest.ServeStopped.getNumberOfLinesInLog();
                        } else {
                            throw new WrongLogFileFormatException(path.toString());
                        }
                        break;
                    case 1:
                        currentRequest.setTimeStamp(Long.parseLong(line));
                        numberOfLinesForRequest--;
                        break;
                    case 2:
                        try {
                            currentRequest.parseRunnableRequest(line);
                        } catch (WreckedFileException e) {
                            errorEntities.add(generateWreckedWrongFileErrorEntity(path.toString(), Error.WreckedFile));
                        }
                        numberOfLinesForRequest--;
                        break;
                    case 3:
                        currentRequest.setThreadId(Integer.parseInt(line));
                        numberOfLinesForRequest--;
                        break;
                    case 4:
                        numberOfLinesForRequest--;
                        currentRequest.setActiveObjectIdentifier(locateOrGenerateIdentifierFromKey(line));
                        break;
                    default: {
                        System.out.println("It shouldn\'t happened");
                        throw new WrongLogFileFormatException(path.toString());
                    }
                }
            }
            if (currentRequest != null && numberOfLinesForRequest == 0) {
                deserializedLoggedDataList.add(currentRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        WrappedActiveObjectWithError activeObjectWithError;
        if (deserializedLoggedDataList.size() != 0)
            activeObjectWithError = getActiveObjectFromDeserializedData(deserializedLoggedDataList);
        else
            throw new WrongLogFileFormatException(path.toString());
        if (numberOfLinesForRequest != 0) {
            activeObjectWithError.addErrorEntity(generateWreckedWrongFileErrorEntity(path.toString(), Error.WreckedFile));
        }
        return activeObjectWithError;
    }
    private static ErrorEntity generateWreckedWrongFileErrorEntity(String path, Error error){
        ErrorEntity errorEntity = new ErrorEntity(error);
        errorEntity.setMessage("File name=" + path);
        return errorEntity;
    }
    public static WrappedActiveObjectWithError getActiveObjectFromDeserializedData(List<DeserializedActiveObjectData> dataList) {
        ActiveObject activeObject = new ActiveObject();
        List<StartedButNotFinishedEvent> startedButNotFinishedEvents = new ArrayList<>();
        for (DeserializedActiveObjectData deserializedLoggedData : dataList) {
            activeObject.setIdentifier(deserializedLoggedData.getActiveObjectIdentifier());
            ActiveObjectThread thread = activeObject.addThreadWithId(deserializedLoggedData.getThreadId());
            ThreadEvent event = new ThreadEvent(deserializedLoggedData.getSequenceNumber());
            if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStarted) {
                event.setStartTime(deserializedLoggedData.getTimeStamp());
                event.setMethodName(deserializedLoggedData.getMethodName());
                if (oldAndNewAOIdsKeyValuePairs.containsKey(deserializedLoggedData.getSender()))
                    event.setSenderActiveObjectId(oldAndNewAOIdsKeyValuePairs.get(deserializedLoggedData.getSender()));
                startedButNotFinishedEvents.add(new StartedButNotFinishedEvent(thread, event));
                thread.addThreadEvent(event);
            } else if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStopped) {
                for (StartedButNotFinishedEvent tempEvent:startedButNotFinishedEvents){
                    if(tempEvent.getActiveObjectThread().getThreadId() == thread.getThreadId()
                            && tempEvent.getEvent().getSequenceNumber() == event.getSequenceNumber()){
                        tempEvent.getEvent().setFinishTime(deserializedLoggedData.getTimeStamp());
                        thread.updateThreadEvent(tempEvent.getEvent());
                        startedButNotFinishedEvents.remove(tempEvent);
                        break;
                    }
                }
            }
        }
        WrappedActiveObjectWithError wrappedActiveObjectWithError = new WrappedActiveObjectWithError(activeObject);
        for (StartedButNotFinishedEvent event: startedButNotFinishedEvents){
            ErrorEntity errorEntity = new ErrorEntity(Error.RequestNeverEnds);
            errorEntity.setMessage("Active object id: " + activeObject.getIdentifier() + " thread id = " + event.getActiveObjectThread().getThreadId() + " thread event sequence number = " + event.getEvent().getSequenceNumber());
            wrappedActiveObjectWithError.addErrorEntity(errorEntity);
        }
        return wrappedActiveObjectWithError;
    }

    private static class StartedButNotFinishedEvent{
        private ActiveObjectThread activeObjectThread;
        private ThreadEvent event;

        public StartedButNotFinishedEvent(ActiveObjectThread activeObjectThread, ThreadEvent event) {
            this.activeObjectThread = activeObjectThread;
            this.event = event;
        }

        public ActiveObjectThread getActiveObjectThread() {
            return activeObjectThread;
        }

        public ThreadEvent getEvent() {
            return event;
        }

        public void setEvent(ThreadEvent event) {
            this.event = event;
        }
    }

    private static class WrappedActiveObjectWithError{
        private ActiveObject activeObject;
        List<ErrorEntity> errorEntities = new ArrayList<>();

        public WrappedActiveObjectWithError(ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public List<ErrorEntity> getErrorEntities() {
            return errorEntities;
        }

        public void addErrorEntity(ErrorEntity errorEntity){
            errorEntities.add(errorEntity);
        }


        public ActiveObject getActiveObject() {
            return activeObject;
        }
    }
    private static class WrappedRequestWithError{
        private List<DeserializedRequestData> requestData = new ArrayList<>();
        List<ErrorEntity> errorEntities = new ArrayList<>();

        public List<ErrorEntity> getErrorEntities() {
            return errorEntities;
        }

        public void addErrorEntity(ErrorEntity errorEntity){
            errorEntities.add(errorEntity);
        }
        public void addRequestData(DeserializedRequestData requestData){
            this.requestData.add(requestData);
        }
        public List<DeserializedRequestData> getRequestData() {
            return requestData;
        }

        public void setRequestData(List<DeserializedRequestData> requestData) {
            this.requestData = requestData;
        }

        public void setErrorEntities(List<ErrorEntity> errorEntities) {
            this.errorEntities = errorEntities;
        }
    }
    private static DeserializedRequestData parseDeliverRequests(String line, TypeOfRequest typeOfRequest) throws WreckedFileException{
        DeserializedRequestData requestData = new DeserializedRequestData(typeOfRequest);
        String delims = "[ ]";
        String[] properties = line.split(delims);
        requestData.setReceiverIdentifier(locateOrGenerateIdentifierFromKey(properties[1]));
        requestData.setMethodName(properties[2]);
        requestData.setSequenceNumber(Long.valueOf(properties[3]));
        requestData.setTimeStamp(Long.valueOf(properties[4]));
        requestData.setReceiverIdentifier(locateOrGenerateIdentifierFromKey(properties[5]));
        return requestData;
    }
    private static String locateOrGenerateIdentifierFromKey(String key){
        String newIdentifier;
        if (!oldAndNewAOIdsKeyValuePairs.containsKey(key)) {
            newIdentifier = DataParserHelper.generateUniqueAOIdentifier(key);
            oldAndNewAOIdsKeyValuePairs.put(key, newIdentifier);
        } else {
            newIdentifier = oldAndNewAOIdsKeyValuePairs.get(key);
        }
        return newIdentifier;
    }
}

