package supportModel.deserializedData;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by pkhvoros on 5/29/15.
 */
public class DeserializedRequestData {
    //request information with delivery time
    Map<String, DeserializedRequestsDelivered> deserializedDeliveryRequestData = new ConcurrentHashMap<>();
    //request information with sending time
    Map<String, DeserializedRequestSent> deserializedSendRequestData = new ConcurrentHashMap<>();

    public Map<String, DeserializedRequestsDelivered> getDeserializedDeliveryRequestData() {
        return deserializedDeliveryRequestData;
    }

    public Map<String, DeserializedRequestSent> getDeserializedSendRequestData() {
        return deserializedSendRequestData;
    }
    public void addDeserializedDeliveryEntity(DeserializedRequestEntity entity){
        if (entity instanceof DeserializedRequestsDelivered){
            deserializedDeliveryRequestData.put(entity.getId(), (DeserializedRequestsDelivered) entity);
        }
        else if(entity instanceof DeserializedRequestSent){
            deserializedSendRequestData.put(entity.getId(), (DeserializedRequestSent) entity);
        }
    }
}
