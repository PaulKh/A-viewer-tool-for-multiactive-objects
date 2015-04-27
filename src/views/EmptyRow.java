package views;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/20/15.
 */
public class EmptyRow extends JPanel {
    private int height;
    public EmptyRow(int height) {
        this.height = height;
        setPreferredSize(new Dimension(0, height));
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(0, height);
    }
}
