package utils;

import enums.TypeOfRequest;
import exceptions.WrongLogFileFormat;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.DeserializedLoggedData;

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

    public static List<ActiveObject> parseData(String sourceDirectory) throws WrongLogFileFormat {
        List<ActiveObject> activeObjects = new ArrayList<ActiveObject>();
        try {
            Files.walk(Paths.get(sourceDirectory)).forEachOrdered(filePath -> {
                try {
                    if (!Files.isDirectory(filePath)) {
                        ActiveObject activeObject = readFile(filePath);
                        activeObjects.add(activeObject);
                    }
                } catch (WrongLogFileFormat wff) {
                    wff.printStackTrace();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activeObjects;
    }

    private static ActiveObject readFile(Path path) throws WrongLogFileFormat {
        List<DeserializedLoggedData> deserializedLoggedDataList = new ArrayList<>();
        DeserializedLoggedData currentRequest = null;
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
                            currentRequest = new DeserializedLoggedData(TypeOfRequest.ServeStarted);
                            numberOfLinesForRequest = TypeOfRequest.ServeStarted.getNumberOfLinesInLog();
                        } else if (line.startsWith("ServeStopped")) {
                            currentRequest = new DeserializedLoggedData(TypeOfRequest.ServeStopped);
                            numberOfLinesForRequest = TypeOfRequest.ServeStopped.getNumberOfLinesInLog();
                        } else {
                            throw new WrongLogFileFormat(path.toString());
                        }
                        break;
                    case 1:
                        currentRequest.setTimeStamp(Long.parseLong(line));
                        numberOfLinesForRequest--;
                        break;
                    case 2:
                        currentRequest.parseRunnableRequest(line);
                        numberOfLinesForRequest--;
                        break;
                    case 3:
                        currentRequest.setThreadId(Integer.parseInt(line));
                        numberOfLinesForRequest--;
                        break;
                    case 4:
                        numberOfLinesForRequest--;
                        String newIdentifier;
                        if (!oldAndNewAOIdsKeyValuePairs.containsKey(line)) {
                            newIdentifier = currentRequest.generateIdentifier(line);
                            oldAndNewAOIdsKeyValuePairs.put(line, newIdentifier);
                        } else {
                            newIdentifier = oldAndNewAOIdsKeyValuePairs.get(line);
                        }
                        currentRequest.setActiveObjectIdentifier(newIdentifier);
                        break;
                    default: {
                        System.out.println("It shouldn\'t happened");
                        throw new WrongLogFileFormat(path.toString());
                    }
                }
            }
            if (currentRequest != null) {
                deserializedLoggedDataList.add(currentRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (deserializedLoggedDataList.size() == 0)
            throw new WrongLogFileFormat(path.toString());
        return getActiveObjectFromDeserializedData(deserializedLoggedDataList);
    }

    public static ActiveObject getActiveObjectFromDeserializedData(List<DeserializedLoggedData> dataList) {
        ActiveObject activeObject = new ActiveObject();
        for (DeserializedLoggedData deserializedLoggedData : dataList) {
            activeObject.setIdentifier(deserializedLoggedData.getActiveObjectIdentifier());
            ActiveObjectThread thread = activeObject.addThreadWithId(deserializedLoggedData.getThreadId());
            if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStarted) {
                ThreadEvent event = new ThreadEvent(deserializedLoggedData.getSequenceNumber());
                event.setStartTime(deserializedLoggedData.getTimeStamp());
                event.setMethodName(deserializedLoggedData.getMethodName());
                if (oldAndNewAOIdsKeyValuePairs.containsKey(deserializedLoggedData.getSender()))
                    event.setSenderActiveObjectId(oldAndNewAOIdsKeyValuePairs.get(deserializedLoggedData.getSender()));
                thread.addThreadEvent(event);

            } else if (deserializedLoggedData.getTypeOfRequest() == TypeOfRequest.ServeStopped) {
                ThreadEvent event = thread.getThreadEvent(deserializedLoggedData.getSequenceNumber());
                event.setFinishTime(deserializedLoggedData.getTimeStamp());
                thread.updateThreadEvent(event);
            }
        }

//        for (ActiveObjectThread thread:activeObject.getThreads()){
//            System.out.print("threadId = " + thread.getThreadId() + " " + thread.getEvents().size() + " ");
//        }
//        System.out.println();
        return activeObject;
    }
}

