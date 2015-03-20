package views;

import model.ActiveObject;
import model.ActiveObjectThread;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by pkhvoros on 3/18/15.
 */
public class ActiveObjectViewController {
    private ActiveObject activeObject;
    private JLabel activeObjectId;
    private JPanel aoPanel;
    private JPanel threadsContainer;
    private List<ThreadViewController> threadViewControllers = new ArrayList<>();
    public ActiveObjectViewController(ActiveObject activeObject) {
        this.activeObject = activeObject;
        activeObjectId.setText(activeObject.getIdentifier());
        for (ActiveObjectThread activeObjectThread:activeObject.getThreads()){
            ThreadViewController controller = new ThreadViewController(activeObjectThread);
            threadViewControllers.add(controller);
            threadsContainer.setSize(3000,100);
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.gridwidth = GridBagConstraints.REMAINDER;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            threadsContainer.add(controller.getRootPanel(), gbc);
        }
    }
    public JPanel getPanel(){
        return aoPanel;
    }
    public void update(SizeHelper sizeHelper){
        for (ThreadViewController threadViewController:threadViewControllers){
            threadViewController.drawThread(sizeHelper);
        }
    }
}
