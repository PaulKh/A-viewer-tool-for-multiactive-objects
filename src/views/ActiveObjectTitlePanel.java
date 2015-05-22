package views;

import callbacks.LockerButtonCallback;
import callbacks.UpButtonPressedCallback;
import model.ActiveObject;
import utils.SizeHelper;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * Created by pkhvoros on 3/20/15.
 */
public class ActiveObjectTitlePanel extends JPanel {
    private ActiveObject activeObject;
    private UpButtonPressedCallback upButtonCallback;
    private LockerButtonCallback lockerButtonCallback;

    public ActiveObjectTitlePanel(ActiveObject activeObject, UpButtonPressedCallback callback, LockerButtonCallback lockerButtonCallback, boolean isLocked) {
        this.lockerButtonCallback = lockerButtonCallback;
        this.upButtonCallback = callback;
        this.activeObject = activeObject;
        GridBagLayout gridBagLayout = new GridBagLayout();
        this.setLayout(gridBagLayout);
        GridBagConstraints constraints = new GridBagConstraints();
        try {
            JButton button = new JButton();
            button.setBorderPainted(false);
            button.setContentAreaFilled(false);
            button.setFocusPainted(false);
            button.setOpaque(false);
            button.setMargin(new Insets(0, 0, 0, 0));
            button.addActionListener(e -> lockerButtonPressed());
            String pathToImage = isLocked ? "/locked.png" : "/unlocked.png";
            Image img = ImageIO.read(getClass().getResource(pathToImage));
            button.setIcon(new ImageIcon(img));
            this.add(button);
        } catch (IOException ex) {
        }
        if (callback != null) {
            try {
                JButton button = new JButton();
                button.setBorderPainted(false);
                button.setContentAreaFilled(false);
                button.setFocusPainted(false);
                button.setOpaque(false);
                button.setMargin(new Insets(0, 0, 0, 0));
                button.addActionListener(e -> upButtonPressed());
                Image img = ImageIO.read(getClass().getResource("/arrow-up.png"));
                button.setIcon(new ImageIcon(img));
//                constraints.gridwidth = GridBagConstraints.REMAINDER;
//                gridBagLayout.setConstraints(button, constraints);
                this.add(button);
            } catch (IOException ex) {
            }
        }

        JTextArea label = new JTextArea("Active Object: " + activeObject.getIdentifier());
        label.setLineWrap(true);
        label.setWrapStyleWord(true);
        label.setBackground(null);
        constraints.fill = GridBagConstraints.BOTH;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.setConstraints(label, constraints);
        this.add(label);

//        try {
//            JButton button = new JButton();
//            Image img = ImageIO.read(new FileInputStream("res/arrow_down.png"));
//            button.setIcon(new ImageIcon(img));
//            constraints.gridwidth = GridBagConstraints.REMAINDER;
//            gridBagLayout.setConstraints(button, constraints);
//            this.add(button);
//        }
//        catch (IOException ex) {
//        }
    }

    private void upButtonPressed() {
        if (upButtonCallback != null) {
            upButtonCallback.upButtonPressed(activeObject);
        }
    }
    private void lockerButtonPressed(){
        if (lockerButtonCallback != null)
            lockerButtonCallback.lockerButtonPressed(activeObject);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(SizeHelper.activeObjectTitleWidth, 0);
    }

    public ActiveObject getActiveObject() {
        return activeObject;
    }
//    @Override
//    protected void paintComponent(Graphics g) {
//        super.paintComponent(g);
//        g.setColor(Color.black);
//        g.fillRect(0, 0 , 100, 100);
//    }
}
