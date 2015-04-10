package supportModel;

import model.ActiveObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/25/15.
 */
public class ParsedData {
    List<DeserializedRequestData> deserializedRequestDataList;
    List<ActiveObject> activeObjects;
    List<ErrorEntity> errorEntities = new ArrayList<>();

    public List<ErrorEntity> getErrorEntities() {
        return errorEntities;
    }

    public void addErrorEntity(ErrorEntity errorEntity){
        errorEntities.add(errorEntity);
    }

    public List<ActiveObject> getActiveObjects() {
        return activeObjects;
    }

    public void setActiveObjects(List<ActiveObject> activeObjects) {
        this.activeObjects = activeObjects;
    }

    public void addAllErrorEntities(List<ErrorEntity> errorEntities){
        this.errorEntities.addAll(errorEntities);
        return;
    }

    public List<DeserializedRequestData> getDeserializedRequestDataList() {
        return deserializedRequestDataList;
    }

    public void setDeserializedRequestDataList(List<DeserializedRequestData> deserializedRequestDataList) {
        this.deserializedRequestDataList = deserializedRequestDataList;
    }
}
