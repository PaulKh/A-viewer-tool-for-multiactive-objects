package utils;

import enums.Error;
import enums.TypeOfRequest;
import exceptions.WreckedFileException;
import exceptions.WrongLogFileFormatException;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.ErrorEntity;
import supportModel.ParsedData;
import supportModel.deserializedData.DeserializedActiveObject;
import supportModel.deserializedData.DeserializedRequestEntity;
import supportModel.deserializedData.DeserializedThreadEvent;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class DataParser {
    //this is the mapping between activeObject ids from the program and new human readable activeObject ids
    private static Map<String, String> oldAndNewAOIdsKeyValuePairs = new ConcurrentHashMap<>();

    private static ErrorEntity generateWreckedWrongFileErrorEntity(String path, Error error) {
        ErrorEntity errorEntity = new ErrorEntity(error);
        errorEntity.setMessage("File name=" + path);
        return errorEntity;
    }

    public ParsedData parseData(String sourceDirectory) {
        ParsedData parsedData = new ParsedData();
        List<DeserializedActiveObject> deserializedAOs = new CopyOnWriteArrayList<DeserializedActiveObject>();
        List<ErrorEntity> errorEntities = new CopyOnWriteArrayList<>();
        List<Thread> threads = new ArrayList<>();
        try {
            Files.walk(Paths.get(sourceDirectory)).forEach(filePath -> {
                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        if (!Files.isDirectory(filePath)) {
                            try {
                                if (filePath.getFileName().toString().startsWith("ActiveObject")) {
                                    DeserializedActiveObject activeObject = readActiveObjectFile(filePath);
                                    deserializedAOs.add(readActiveObjectFile(filePath));
                                    errorEntities.addAll(activeObject.getErrorEntities());
                                } else if (filePath.getFileName().toString().startsWith("Request")) {
                                    WrappedRequestWithError wrappedRequestWithError = readRequestFile(filePath);
                                    errorEntities.addAll(wrappedRequestWithError.getErrorEntities());
                                    parsedData.addRequestEntities(wrappedRequestWithError.getRequestData());
                                } else {
                                    errorEntities.add(generateWreckedWrongFileErrorEntity(filePath.toString(), Error.WrongFileFormat));
                                }
                            } catch (WrongLogFileFormatException exception) {
                                errorEntities.add(generateWreckedWrongFileErrorEntity(exception.getMessage(), Error.WrongFileFormat));
                            }
                        }
                    }
                });
                thread.start();
                threads.add(thread);

            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        List<ActiveObject> activeObjects = new ArrayList<>();
        for (DeserializedActiveObject activeObject : deserializedAOs) {
            WrappedActiveObjectWithError activeObjectWithError = getActiveObjectFromDeserializedData(activeObject.getDeserializedThreadEvents());
            activeObjects.add(activeObjectWithError.getActiveObject());
            errorEntities.addAll(activeObjectWithError.getErrorEntities());
        }
        parsedData.setActiveObjects(activeObjects);
        parsedData.addAllErrorEntities(errorEntities);
        return parsedData;
    }

    private WrappedRequestWithError readRequestFile(Path path) {
        List<DeserializedRequestEntity> deserializedRequestDataList = new ArrayList<>();
        List<String> lines = null;
        List<ErrorEntity> errorEntities = new ArrayList<>();
        try {
            try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    try {
                        if (line.startsWith("deliverrequest")) {
                            deserializedRequestDataList.add(parseDeliverRequests(line, TypeOfRequest.RequestDelivered));
                        } else if (line.startsWith("beforerequestsent")) {
                            deserializedRequestDataList.add(parseDeliverRequests(line, TypeOfRequest.RequestSent));
                        }
                    } catch (WreckedFileException e) {
                        errorEntities.add(generateWreckedWrongFileErrorEntity(path.toString(), Error.WreckedFile));
                        break;
                    }
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

    private DeserializedActiveObject readActiveObjectFile(Path path) throws WrongLogFileFormatException {
        List<DeserializedThreadEvent> deserializedLoggedDataList = new ArrayList<>();
        List<ErrorEntity> errorEntities = new ArrayList<>();
        DeserializedThreadEvent currentRequest = null;
        int numberOfLinesForRequest = 0;
        try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
            String line;
            while ((line = br.readLine()) != null) {
                switch (numberOfLinesForRequest) {
                    case 0:
                        if (currentRequest != null) {
                            deserializedLoggedDataList.add(currentRequest);
                        }
                        if (line.startsWith("ServeStarted")) {
                            currentRequest = new DeserializedThreadEvent(TypeOfRequest.ServeStarted);
                            numberOfLinesForRequest = TypeOfRequest.ServeStarted.getNumberOfLinesInLog();
                        } else if (line.startsWith("ServeStopped")) {
                            currentRequest = new DeserializedThreadEvent(TypeOfRequest.ServeStopped);
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
        DeserializedActiveObject deserializedActiveObject = new DeserializedActiveObject();
        if (deserializedLoggedDataList.size() != 0)
            deserializedActiveObject.setDeserializedThreadEvents(deserializedLoggedDataList);
//            activeObjectWithError = getActiveObjectFromDeserializedData(deserializedLoggedDataList);
        else
            throw new WrongLogFileFormatException(path.toString());
        if (numberOfLinesForRequest != 0) {
            errorEntities.add(generateWreckedWrongFileErrorEntity(path.toString(), Error.WreckedFile));
        }
        deserializedActiveObject.setErrorEntities(errorEntities);
        return deserializedActiveObject;
    }

    public WrappedActiveObjectWithError getActiveObjectFromDeserializedData(List<DeserializedThreadEvent> dataList) {
        ActiveObject activeObject;
        if (dataList.size() != 0)
            activeObject = new ActiveObject(dataList.get(0).getActiveObjectIdentifier());
        else
            return null;
        List<StartedButNotFinishedEvent> startedButNotFinishedEvents = new ArrayList<>();
        for (DeserializedThreadEvent deserializedLoggedData : dataList) {
            ActiveObjectThread thread = activeObject.addThreadWithId(deserializedLoggedData.getThreadId());
            ThreadEvent event = new ThreadEvent(deserializedLoggedData.getSequenceNumber(), thread);
            if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStarted) {
                event.setStartTime(deserializedLoggedData.getTimeStamp());
                event.setMethodName(deserializedLoggedData.getMethodName());
                if (oldAndNewAOIdsKeyValuePairs.containsKey(deserializedLoggedData.getSender()))
                    event.setSenderActiveObjectId(oldAndNewAOIdsKeyValuePairs.get(deserializedLoggedData.getSender()));
                startedButNotFinishedEvents.add(new StartedButNotFinishedEvent(thread, event));
                thread.addThreadEvent(event);
            } else if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStopped) {
                for (StartedButNotFinishedEvent tempEvent : startedButNotFinishedEvents) {
                    if (tempEvent.getActiveObjectThread().getThreadId() == thread.getThreadId()
                            && tempEvent.getEvent().getSequenceNumber() == event.getSequenceNumber()) {
                        tempEvent.getEvent().setFinishTime(deserializedLoggedData.getTimeStamp());
                        thread.updateThreadEvent(tempEvent.getEvent());
                        startedButNotFinishedEvents.remove(tempEvent);
                        break;
                    }
                }
            }
        }
        WrappedActiveObjectWithError wrappedActiveObjectWithError = new WrappedActiveObjectWithError(activeObject);
        for (StartedButNotFinishedEvent event : startedButNotFinishedEvents) {
            ErrorEntity errorEntity = new ErrorEntity(Error.RequestNeverEnds);
            errorEntity.setMessage("Active object id: " + activeObject.getIdentifier() + " thread id = " + event.getActiveObjectThread().getThreadId() + " thread event sequence number = " + event.getEvent().getSequenceNumber());
            wrappedActiveObjectWithError.addErrorEntity(errorEntity);
        }
        return wrappedActiveObjectWithError;
    }

    private DeserializedRequestEntity parseDeliverRequests(String line, TypeOfRequest typeOfRequest) throws WreckedFileException {
        DeserializedRequestEntity requestData = DeserializedRequestEntity.buildWithRequestType(typeOfRequest);
        String delims = "[ ]";
        String[] properties = line.split(delims);
        requestData.setReceiverIdentifier(locateOrGenerateIdentifierFromKey(properties[1]));
        requestData.setThreadId(Integer.parseInt(properties[2]));
        requestData.setMethodName(properties[3]);
        requestData.setSequenceNumber(Long.valueOf(properties[4]));
        requestData.setTimeStamp(Long.valueOf(properties[5]));
        requestData.setSenderIdentifier(locateOrGenerateIdentifierFromKey(properties[6]));
        return requestData;
    }

    private String locateOrGenerateIdentifierFromKey(String key) {
        String newIdentifier;
        if (!oldAndNewAOIdsKeyValuePairs.containsKey(key)) {
            newIdentifier = AOIdentifierGenerator.generateUniqueAOIdentifier(key);
            oldAndNewAOIdsKeyValuePairs.put(key, newIdentifier);
        } else {
            newIdentifier = oldAndNewAOIdsKeyValuePairs.get(key);
        }
        return newIdentifier;
    }

    private class StartedButNotFinishedEvent {
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

    private class WrappedActiveObjectWithError {
        List<ErrorEntity> errorEntities = new ArrayList<>();
        private ActiveObject activeObject;

        public WrappedActiveObjectWithError(ActiveObject activeObject) {
            this.activeObject = activeObject;
        }

        public List<ErrorEntity> getErrorEntities() {
            return errorEntities;
        }

        public void addErrorEntity(ErrorEntity errorEntity) {
            errorEntities.add(errorEntity);
        }


        public ActiveObject getActiveObject() {
            return activeObject;
        }
    }

    private class WrappedRequestWithError {
        List<ErrorEntity> errorEntities = new ArrayList<>();
        private List<DeserializedRequestEntity> requestData = new ArrayList<>();

        public List<ErrorEntity> getErrorEntities() {
            return errorEntities;
        }

        public void setErrorEntities(List<ErrorEntity> errorEntities) {
            this.errorEntities = errorEntities;
        }

        public void addErrorEntity(ErrorEntity errorEntity) {
            errorEntities.add(errorEntity);
        }

        public void addRequestData(DeserializedRequestEntity requestData) {
            this.requestData.add(requestData);
        }

        public List<DeserializedRequestEntity> getRequestData() {
            return requestData;
        }

        public void setRequestData(List<DeserializedRequestEntity> requestData) {
            this.requestData = requestData;
        }
    }
}

