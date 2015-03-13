import exceptions.WrongLogFileFormat;
import model.ActiveObject;

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

    public static List<ActiveObject> parseData(String sourceDirectory){
//        File f = new File(sourceDirectory);
//        Path path = FileSystems.getDefault().getPath(sourceDirectory);
        List<ActiveObject> activeObjects = new ArrayList<ActiveObject>();
        try {
            Files.walk(Paths.get(sourceDirectory)).forEachOrdered(filePath -> {
                try {
                    activeObjects.add(readFile(filePath));
                }
                catch (WrongLogFileFormat wff){
                    System.out.print("Error wrong file format");
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activeObjects;
    }

    private static ActiveObject readFile(Path path) throws WrongLogFileFormat {
        ActiveObject activeObject = new ActiveObject();

        //Type of the request which is handled at the moment, contains number of lines left until we are ready to move to another request
        TypeOfRequest typeOfRequest = TypeOfRequest.None;
        try {
            List<String> lines = Files.readAllLines(path);
            for(int i = 0; lines.size() > i; i++){
                String line = lines.get(i);
                switch (typeOfRequest.getNumberOfLinesLeft()) {
                    case 0:
                        if (line.startsWith("ServeStarted")) {
                            typeOfRequest = TypeOfRequest.ServeStarted;
                        }
                        else if (line.startsWith("ServeStopped")){
                            typeOfRequest = TypeOfRequest.ServeStopped;
                        }
                        else {
                            throw new WrongLogFileFormat();
                        }
                        break;
                    case 1:
                        typeOfRequest.nextLine();
                        break;
                    case 2:
                        typeOfRequest.nextLine();
                        break;
                    case 3:
                        typeOfRequest.nextLine();
                        String newIdentifier;
                        if(!oldAndNewAOIdsKeyValuePairs.containsKey(line)){
                            newIdentifier = activeObject.setIdentifier(line);
                            oldAndNewAOIdsKeyValuePairs.put(line, newIdentifier);
                            activeObject.setIdentifier(newIdentifier);
                        }
                        else{
                            newIdentifier = oldAndNewAOIdsKeyValuePairs.get(line);
                            activeObject.setIdentifier(newIdentifier);
                        }
                        break;
                    default:
                        System.out.println("It shouldn\'t happened");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return activeObject;
    }
}
enum TypeOfRequest{
    None(0),
    ServeStarted(3),
    ServeStopped(3);
    private int numberOfLinesInLog;

    TypeOfRequest(int numberOfLinesInLog) {
        this.numberOfLinesInLog = numberOfLinesInLog;
    }
    public int getNumberOfLinesLeft(){
        return numberOfLinesInLog;
    }
    public void nextLine(){
        numberOfLinesInLog--;
    }
}
