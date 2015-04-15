package supportModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 4/14/15.
 */
public class DeserializedActiveObject {
    private List<DeserializedThreadEvent> deserializedThreadEvents = new ArrayList<>();
    private List<ErrorEntity> errorEntities = new ArrayList<>();

    public List<DeserializedThreadEvent> getDeserializedThreadEvents() {
        return deserializedThreadEvents;
    }

    public void setDeserializedThreadEvents(List<DeserializedThreadEvent> deserializedThreadEvents) {
        this.deserializedThreadEvents = deserializedThreadEvents;
    }

    public List<ErrorEntity> getErrorEntities() {
        return errorEntities;
    }

    public void setErrorEntities(List<ErrorEntity> errorEntities) {
        this.errorEntities = errorEntities;
    }
}
