package supportModel;

import javax.swing.*;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 7/24/15.
 */
public class WrappedQueueCompatibilityData extends JDialog {
    private List<Integer> compatibilityIdentifiersSelected = new ArrayList<>();
    private int compatibilityIdentifierSelected = -1;


    public WrappedQueueCompatibilityData(Frame owner) {
        super(owner);
    }

    public void setCompatibilityIdentifierSelected(int compatibilityIdentifierSelected) {
        this.compatibilityIdentifierSelected = compatibilityIdentifierSelected;
    }

    public int getCompatibilityIdentifierSelected() {
        return compatibilityIdentifierSelected;
    }
}
