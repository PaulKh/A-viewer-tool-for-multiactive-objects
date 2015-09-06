package views.helpers;

import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;

/**
 * Created by Paul on 06/09/15.
 */
public class RootPanelComponentlistener implements ComponentListener {
    private RootViewResizedCallback rootViewResizedCallback;
    public RootPanelComponentlistener(RootViewResizedCallback rootViewResizedCallback) {
        this.rootViewResizedCallback = rootViewResizedCallback;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        if (rootViewResizedCallback != null){
            rootViewResizedCallback.rootViewResized();
        }
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }
}
