package utils;


import model.ActiveObject;
import org.apache.commons.collections4.queue.CircularFifoQueue;
import supportModel.OrderStateOfActiveObjects;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;

/**
 * Created by Paul on 07/05/15.
 */
public class SwapActiveObjectsQueue {
    SizedStack<OrderStateOfActiveObjects> queue = new SizedStack<>(40);
    public void addNewState(List<ActiveObject> activeObjects){
        List<String> ids = new ArrayList<>();
        for (ActiveObject activeObject:activeObjects){
            ids.add(activeObject.getIdentifier());
        }
        queue.add(new OrderStateOfActiveObjects(ids));
    }
    public OrderStateOfActiveObjects generateStateByActiveObjects(List<ActiveObject> activeObjects){
        List<String> ids = new ArrayList<>();
        for (ActiveObject activeObject:activeObjects){
            ids.add(activeObject.getIdentifier());
        }
        return new OrderStateOfActiveObjects(ids);
    }
    public void addState(OrderStateOfActiveObjects state){
        queue.add(state);
    }
    //Return false if undo queue is empty and there is nothing to undo
    public List<ActiveObject> undo(List<ActiveObject> activeObjects){
        List<ActiveObject> activeObjectList = new ArrayList<>();
        for (String id:queue.peek().getActiveObjectsIdentifiers()){
            for (ActiveObject activeObject: activeObjects){
                if (activeObject.getIdentifier().equals(id)){
                    activeObjectList.add(activeObject);
                    continue;
                }
            }
        }
        queue.pop();
        return activeObjectList;
    }
    public boolean isQueueEmpty(){
        return queue.isEmpty();
    }
}
