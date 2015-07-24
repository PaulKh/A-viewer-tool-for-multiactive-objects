package supportModel;

import model.ActiveObject;
import supportModel.deserializedData.DeserializedRequestData;
import supportModel.deserializedData.DeserializedRequestEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/25/15.
 */
//This class represents all the information parsed from the logs
public class ParsedData {

    //request send, delivered information
    DeserializedRequestData deserializedRequestData = new DeserializedRequestData();
    //information about active objects
    List<ActiveObject> activeObjects;
    //errors occured during the parsing
    List<ErrorEntity> errorEntities = new ArrayList<>();

    public List<ErrorEntity> getErrorEntities() {
        return errorEntities;
    }

    public void addErrorEntity(ErrorEntity errorEntity) {
        errorEntities.add(errorEntity);
    }


    public List<ActiveObject> getActiveObjects() {
        return activeObjects;
    }

    public void setActiveObjects(List<ActiveObject> activeObjects) {
        this.activeObjects = activeObjects;
    }

    public void addAllErrorEntities(List<ErrorEntity> errorEntities) {
        this.errorEntities.addAll(errorEntities);
        return;
    }

    public void addDeserializedRequestEvent(DeserializedRequestEntity entity) {
        deserializedRequestData.addDeserializedDeliveryEntity(entity);
    }

    public void addRequestEntities(List<DeserializedRequestEntity> entities) {
        for (DeserializedRequestEntity entity : entities) {
            addDeserializedRequestEvent(entity);
        }
    }

    public DeserializedRequestData getDeserializedRequestData() {
        return deserializedRequestData;
    }
}
