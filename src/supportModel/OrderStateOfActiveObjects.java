package supportModel;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Paul on 07/05/15.
 */
public class OrderStateOfActiveObjects {
    private List<String> activeObjectsIdentifiers = new ArrayList<>();

    public OrderStateOfActiveObjects(List<String> activeObjectsIdentifiers) {
        this.activeObjectsIdentifiers = activeObjectsIdentifiers;
    }

    public List<String> getActiveObjectsIdentifiers() {
        return activeObjectsIdentifiers;
    }
}
