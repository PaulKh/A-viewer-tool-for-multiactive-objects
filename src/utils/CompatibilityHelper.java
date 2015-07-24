package utils;

import model.ActiveObject;
import model.ThreadEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by pkhvoros on 7/22/15.
 */
public class CompatibilityHelper {
    private static CompatibilityHelper compatibilityHelper;
    private Map<ActiveObject, ThreadEvent> compatibilityEvents = new HashMap<>();
    public synchronized static CompatibilityHelper instance(){
        if (compatibilityHelper == null){
            compatibilityHelper = new CompatibilityHelper();
        }
        return compatibilityHelper;
    }



//    public Map<ActiveObject, ThreadEvent> getCompatibilityEvents() {
//        return compatibilityEvents;
//    }
    public void addTuple(ActiveObject activeObject, ThreadEvent threadEvent){
        compatibilityEvents.put(activeObject, threadEvent);
    }
    public ThreadEvent getEventForKey(ActiveObject activeObject){
        return compatibilityEvents.get(activeObject);
    }
    public void removeTuple(ActiveObject key){
        compatibilityEvents.remove(key);
    }
}
