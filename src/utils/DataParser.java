package utils;

import enums.Error;
import enums.TypeOfRequest;
import exceptions.WreckedFileException;
import exceptions.WrongLogFileFormatException;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.Group;
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
import java.util.HashMap;
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
        List<WrappedCompatibilityInformation> wrappedCompatibilityInformationList = new ArrayList<>();
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
                                } else if (filePath.getFileName().toString().startsWith("Compatibility")) {
                                    WrappedCompatibilityInformation compatibilityInformation = readComapatibilityFile(filePath);
                                    wrappedCompatibilityInformationList.add(compatibilityInformation);
                                    errorEntities.addAll(compatibilityInformation.getErrorEntities());
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
        enrichActiveObjectWithCompatibility(wrappedCompatibilityInformationList, activeObjects);
        parsedData.setActiveObjects(activeObjects);
        parsedData.addAllErrorEntities(errorEntities);
        return parsedData;
    }

    private WrappedCompatibilityInformation readComapatibilityFile(Path path) {
        WrappedCompatibilityInformation compatibilityInformation = new WrappedCompatibilityInformation();
        CompatibilityStatusParsing statusParsing = CompatibilityStatusParsing.NONE;


        try {
            try (BufferedReader br = new BufferedReader(new FileReader(path.toString()))) {
                String line;
                String firstLine = br.readLine();
                if (firstLine != null)
                    compatibilityInformation.setActiveObjectId(locateOrGenerateIdentifierFromKey(firstLine));
                else
                    return null;
                List<WrappedMethod> wrappedMethods = new ArrayList<>();
                List<WrappedGroup> wrappedGroups = new ArrayList<>();
                while ((line = br.readLine()) != null) {
                    if (line.equals("compatibility")) {
                        statusParsing = CompatibilityStatusParsing.GROUP_COMPATIBILITY;
                        continue;
                    } else if (line.equals("methods")) {
                        statusParsing = CompatibilityStatusParsing.METHOD_OWNED;
                        continue;
                    } else {
                        try {
                            switch (statusParsing) {
                                case METHOD_OWNED:
                                    wrappedMethods.add(parseMethodsLine(line));
                                    break;
                                case GROUP_COMPATIBILITY:
                                    wrappedGroups.add(parseCompatibilityGroup(line));
                                    break;
                                default:
                                    break;
                            }
                        } catch (WreckedFileException ex) {
                            compatibilityInformation.addErrorEntity(generateWreckedWrongFileErrorEntity(path.toString(), Error.WrongFileFormat));
                        }
                    }
                }
                compatibilityInformation.setGroups(mergeCompatibilityParseInformation(wrappedGroups, wrappedMethods));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return compatibilityInformation;
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
                String senderId = locateOrGenerateIdentifierFromKey(deserializedLoggedData.getSender());
//                if (oldAndNewAOIdsKeyValuePairs.containsKey(deserializedLoggedData.getSender()))
                event.setSenderActiveObjectId(senderId);
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

    private void enrichActiveObjectWithCompatibility(List<WrappedCompatibilityInformation> wrappedCompatibilityInformationList, List<ActiveObject> activeObjects) {
        for (ActiveObject activeObject : activeObjects) {
            for (WrappedCompatibilityInformation compatibilityInformation : wrappedCompatibilityInformationList) {
                if (activeObject.getIdentifier().equals(compatibilityInformation.getActiveObjectId())) {
                    activeObject.setGroups(compatibilityInformation.getGroups());
                    break;
                }
            }
        }
    }

    private WrappedGroup parseCompatibilityGroup(String line) throws WreckedFileException {
        String delims = "[ ]";
        String[] properties = line.split(delims);
        Group group = new Group(properties[0]);
        WrappedGroup wrapper = new WrappedGroup(group);
        for (int i = 1; i < properties.length; i++) {
            wrapper.addNewGroupName(properties[i]);
        }
        return wrapper;
    }

    private WrappedMethod parseMethodsLine(String line) throws WreckedFileException {
        String delims = "[ ]";
        String[] properties = line.split(delims);
        if (properties.length != 2)
            throw new WreckedFileException();
        String groupName = properties[1];
        int indexOfBraces = properties[0].indexOf('(');
        if (indexOfBraces == -1)
            throw new WreckedFileException();
        return new WrappedMethod(properties[1], properties[0].substring(0, indexOfBraces));

    }

    private DeserializedRequestEntity parseDeliverRequests(String line, TypeOfRequest typeOfRequest) throws WreckedFileException {
        DeserializedRequestEntity requestData = DeserializedRequestEntity.buildWithRequestType(typeOfRequest);
        String delims = "[ ]";
        String[] properties = line.split(delims);
        if (properties.length < 6)
            throw new WreckedFileException();
        requestData.setReceiverIdentifier(locateOrGenerateIdentifierFromKey(properties[1]));
        requestData.setThreadId(Integer.parseInt(properties[2]));
        requestData.setMethodName(properties[3]);
        requestData.setSequenceNumber(Long.valueOf(properties[4]));
        requestData.setTimeStamp(Long.valueOf(properties[5]));
        requestData.setSenderIdentifier(locateOrGenerateIdentifierFromKey(properties[6]));
        return requestData;
    }

    private List<Group> mergeCompatibilityParseInformation(List<WrappedGroup> wrappedGroups, List<WrappedMethod> wrappedMethods) {
        List<Group> finalGroups = new ArrayList<>();
        Map<String, Group> groups = new HashMap<>();
        for (WrappedGroup group : wrappedGroups) {
            groups.put(group.getGroup().getName(), group.getGroup());
        }
        for (WrappedMethod method : wrappedMethods) {
            Group group = groups.get(method.getGroupName());
//            if (group != null)
            group.addMethodName(method.getMethodName());
        }
        for (WrappedGroup group : wrappedGroups) {
            for (String groupName : group.getCompatibleGroupNames()) {
                Group compatibleGroup = groups.get(groupName);
                group.getGroup().addCompatibleGroup(compatibleGroup);
            }
            finalGroups.add(group.getGroup());
        }
        return finalGroups;
    }

    private synchronized String locateOrGenerateIdentifierFromKey(String key) {
        String newIdentifier;
        if (!oldAndNewAOIdsKeyValuePairs.containsKey(key)) {
            newIdentifier = AOIdentifierGenerator.generateUniqueAOIdentifier(key);
            oldAndNewAOIdsKeyValuePairs.put(key, newIdentifier);
        } else {
            newIdentifier = oldAndNewAOIdsKeyValuePairs.get(key);
        }
        return newIdentifier;
    }

    private enum CompatibilityStatusParsing {
        GROUP_COMPATIBILITY,
        METHOD_OWNED,
        NONE
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

    private class WrappedCompatibilityInformation {
        List<ErrorEntity> errorEntities = new CopyOnWriteArrayList<>();
        private String activeObjectId;
        private List<Group> groups = new ArrayList<>();

        public String getActiveObjectId() {
            return activeObjectId;
        }

        public void setActiveObjectId(String activeObjectId) {
            this.activeObjectId = activeObjectId;
        }

        public void addGroup(Group group) {
            this.groups.add(group);
        }

        public List<Group> getGroups() {
            return groups;
        }

        public void setGroups(List<Group> groups) {
            this.groups = groups;
        }

        public List<ErrorEntity> getErrorEntities() {
            return errorEntities;
        }

        public void setErrorEntities(List<ErrorEntity> errorEntities) {
            this.errorEntities = errorEntities;
        }

        public void addErrorEntity(ErrorEntity errorEntity) {
            errorEntities.add(errorEntity);
        }
    }

    private class WrappedGroup {
        private Group group;
        private List<String> compatibleGroupNames = new ArrayList<>();

        public WrappedGroup(Group group) {
            this.group = group;
        }

        public void addNewGroupName(String name) {
            this.compatibleGroupNames.add(name);
        }

        public Group getGroup() {
            return group;
        }

        public List<String> getCompatibleGroupNames() {
            return compatibleGroupNames;
        }
    }

    private class WrappedMethod {
        private String groupName;
        private String methodName;

        public WrappedMethod(String groupName, String methodName) {
            this.groupName = groupName;
            this.methodName = methodName;
        }

        public String getGroupName() {
            return groupName;
        }

        public String getMethodName() {
            return methodName;
        }
    }
}

