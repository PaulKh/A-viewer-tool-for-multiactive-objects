package callbacks;

import enums.MenuItemType;
import model.ActiveObject;
import model.ThreadEvent;

/**
 * Created by pkhvoros on 4/7/15.
 */
public interface ThreadEventClickedCallback {
    public void threadEventClicked(MenuItemType menuItemType, ThreadEvent threadEvent);

    public void threadClicked(ActiveObject activeObject, long timeClicked);

    public void removeCompatibilityClicked(ActiveObject activeObject);
}
