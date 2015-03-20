package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ActiveObjectThread {
    private List<ThreadEvent> events = new ArrayList<>();
    private int threadId;

    public ActiveObjectThread(int threadId) {
        this.threadId = threadId;
    }

    public int getThreadId() {
        return threadId;
    }
//    public ThreadEvent addThreadEvent(int id){
//        for (ThreadEvent event:events){
//            if (event.getId() == id)
//                return event;
//        }
//        ThreadEvent event = new ThreadEvent(id);
//        events.add(event);
//        return event;
//    }
    public void addThreadEvent(ThreadEvent event){
        events.add(event);
    }
    public void updateThreadEvent(ThreadEvent event){
        if (events.contains(event)){
            events.set(events.indexOf(event), event);
        }
    }

    public ThreadEvent getThreadEvent(long id){
        for (ThreadEvent event:events)
            if (event.getId() == id)
                return event;
        return null;
    }

    public List<ThreadEvent> getEvents() {
        return events;
    }
}
