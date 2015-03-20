package views;

import model.ActiveObjectThread;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class ThreadViewController {
    private ActiveObjectThread activeObjectThread;
    private JPanel threadContainer;
    private JLabel threadId;
    private JPanel flowContainer;

    public ThreadViewController(ActiveObjectThread activeObjectThread) {
        this.activeObjectThread = activeObjectThread;

    }
    public JPanel getRootPanel(){
        return threadContainer;
    }
    public void drawThread(SizeHelper sizeHelper){
        this.threadId.setText("Thread id = " + activeObjectThread.getThreadId());
        threadContainer.setMinimumSize(new Dimension(2000, 50));
        return;
    }
}
