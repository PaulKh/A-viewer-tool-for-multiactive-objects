package views;

import callbacks.SwapButtonPressedListener;
import model.ActiveObject;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by pkhvoros on 4/24/15.
 */
public class SwapActiveObjectsButton extends JButton{
    private ActiveObject activeObject1;
    private ActiveObject activeObject2;

    public SwapActiveObjectsButton(ActiveObject activeObject1, ActiveObject activeObject2, SwapButtonPressedListener listener) {
        addActionListener(e -> listener.swapButtonPressed(SwapActiveObjectsButton.this));
        this.activeObject1 = activeObject1;
        this.activeObject2 = activeObject2;
    }

    public ActiveObject getActiveObject1() {
        return activeObject1;
    }

    public ActiveObject getActiveObject2() {
        return activeObject2;
    }
}
