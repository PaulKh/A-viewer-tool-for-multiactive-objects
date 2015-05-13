package views;

import enums.OrderingPolicyEnum;
import utils.PreferencesHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class SettingsDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JCheckBox allowAutomaticReorderingCheckBox;
    private JTextField numberOfDialogsTextfield;

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

        numberOfDialogsTextfield.setDocument(new JTextFieldLimit(1));
        numberOfDialogsTextfield.setText(String.valueOf(PreferencesHelper.getNumberOfDialogs(MainWindow.class)));
        numberOfDialogsTextfield.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                if (!Character.isDigit(e.getKeyChar()))
                    e.consume();
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

        setSize(new Dimension(450, 200));
        setLocationRelativeTo(null);
        initValues();
        setVisible(true);
    }

    private void initValues() {
        OrderingPolicyEnum orderingPolicyEnum = PreferencesHelper.getReorderingPolicy(this.getClass());
        allowAutomaticReorderingCheckBox.setSelected(orderingPolicyEnum == OrderingPolicyEnum.ENABLED);
    }

    private void onOK() {
        OrderingPolicyEnum orderingPolicyEnum = allowAutomaticReorderingCheckBox.isSelected() ? OrderingPolicyEnum.ENABLED : OrderingPolicyEnum.DISABLED;
        PreferencesHelper.saveOrderingPolicy(this.getClass(), orderingPolicyEnum);
        PreferencesHelper.saveNumberOfDialogs(this.getClass(), Integer.valueOf(numberOfDialogsTextfield.getText()));
// add your code here
        dispose();
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }
}
