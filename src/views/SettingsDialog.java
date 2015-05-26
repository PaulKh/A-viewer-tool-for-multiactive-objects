package views;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import enums.OrderingPolicyEnum;
import utils.PreferencesHelper;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox allowAutomaticReorderingCheckBox;
    private JTextField numberOfDialogsTextfield;
    private JCheckBox allowViewMovingAfterCheckBox;
    private JCheckBox enableDialogCheckBox;
    private JLabel numberOfDialogsLabel;

    public SettingsDialog() {
        setContentPane(contentPane);
        setModal(true);
        setModalityType(ModalityType.APPLICATION_MODAL);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        int numberOfDialogs = PreferencesHelper.getNumberOfDialogs();
        if (numberOfDialogs != 0) {
            numberOfDialogsTextfield.setVisible(true);
            numberOfDialogsLabel.setVisible(true);
            enableDialogCheckBox.setSelected(true);
        } else enableDialogCheckBox.setSelected(false);
        numberOfDialogsTextfield.setDocument(new JTextFieldLimit(1));
        numberOfDialogsTextfield.setText(String.valueOf(numberOfDialogs));
        numberOfDialogsTextfield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()))
                    e.consume();
            }
        });
        enableDialogCheckBox.addActionListener(e -> {
            if (!enableDialogCheckBox.isSelected()) {
                if (numberOfDialogs == 0) {
                    numberOfDialogsTextfield.setText(3 + "");
                }
                numberOfDialogsTextfield.setVisible(false);
                numberOfDialogsLabel.setVisible(false);
            } else {
                numberOfDialogsTextfield.setVisible(true);
                numberOfDialogsLabel.setVisible(true);
            }
        });
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
//        contentPane.registerKeyboardAction(new ActionListener() {
//            public void actionPerformed(ActionEvent e) {
//                onCancel();
//            }
//        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        setSize(new Dimension(500, 200));
        setLocationRelativeTo(null);
        initValues();
        setVisible(true);
    }

    private void initValues() {
        OrderingPolicyEnum orderingPolicyEnum = PreferencesHelper.getReorderingPolicy();
        allowAutomaticReorderingCheckBox.setSelected(orderingPolicyEnum == OrderingPolicyEnum.ENABLED);
        boolean movingViewEnabled = PreferencesHelper.isRepositioningAllowed();
        allowViewMovingAfterCheckBox.setSelected(movingViewEnabled);
        enableDialogCheckBox.setSelected(PreferencesHelper.getNumberOfDialogs() != 0);
    }

    private void onOK() {
        OrderingPolicyEnum orderingPolicyEnum = allowAutomaticReorderingCheckBox.isSelected() ? OrderingPolicyEnum.ENABLED : OrderingPolicyEnum.DISABLED;
        PreferencesHelper.saveOrderingPolicy(orderingPolicyEnum);
        if (enableDialogCheckBox.isSelected())
            PreferencesHelper.saveNumberOfDialogs(Integer.valueOf(numberOfDialogsTextfield.getText()));
        else
            PreferencesHelper.saveNumberOfDialogs(0);
        PreferencesHelper.setRepositioningAllowed(allowViewMovingAfterCheckBox.isSelected());
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$() {
        contentPane = new JPanel();
        contentPane.setLayout(new GridLayoutManager(2, 1, new Insets(10, 10, 10, 10), -1, -1));
        contentPane.setMinimumSize(new Dimension(500, 200));
        contentPane.setOpaque(true);
        contentPane.setPreferredSize(new Dimension(500, 200));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel1, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
        final JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayoutManager(1, 2, new Insets(0, 0, 0, 0), -1, -1, true, false));
        panel1.add(panel2, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        buttonOK = new JButton();
        buttonOK.setEnabled(true);
        buttonOK.setText("OK");
        panel2.add(buttonOK, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        buttonCancel = new JButton();
        buttonCancel.setText("Cancel");
        panel2.add(buttonCancel, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final JPanel panel3 = new JPanel();
        panel3.setLayout(new GridLayoutManager(4, 3, new Insets(0, 0, 0, 0), -1, -1));
        contentPane.add(panel3, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        allowAutomaticReorderingCheckBox = new JCheckBox();
        allowAutomaticReorderingCheckBox.setText("Allow automatic reordering");
        panel3.add(allowAutomaticReorderingCheckBox, new GridConstraints(1, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfDialogsLabel = new JLabel();
        numberOfDialogsLabel.setText("The maximum number of the dialogs");
        numberOfDialogsLabel.setVisible(false);
        panel3.add(numberOfDialogsLabel, new GridConstraints(2, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        numberOfDialogsTextfield = new JTextField();
        numberOfDialogsTextfield.setText("");
        numberOfDialogsTextfield.setVisible(false);
        panel3.add(numberOfDialogsTextfield, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_EAST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(30, -1), new Dimension(30, -1), 0, false));
        final Spacer spacer2 = new Spacer();
        panel3.add(spacer2, new GridConstraints(3, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        allowViewMovingAfterCheckBox = new JCheckBox();
        allowViewMovingAfterCheckBox.setText("Allow view moving after event selection");
        panel3.add(allowViewMovingAfterCheckBox, new GridConstraints(0, 0, 1, 3, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        enableDialogCheckBox = new JCheckBox();
        enableDialogCheckBox.setText("Enable dialog");
        panel3.add(enableDialogCheckBox, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return contentPane;
    }
}
