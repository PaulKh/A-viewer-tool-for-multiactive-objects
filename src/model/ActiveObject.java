package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/13/15.
 */
public class ActiveObject {
    private static int idCounter=1;
    private String identifier;
    private List<ActiveObjectThread> threads = new ArrayList<ActiveObjectThread>();

    public String getIdentifier() {
        return identifier;
    }

    public String setIdentifier(String identifier) {
        String delims = "[.]";
        String[] temp = identifier.split(delims);
        delims = "[_]";
        this.identifier = temp[temp.length - 1].split(delims)[0] + idCounter;
        idCounter++;
        return identifier;
    }
}
