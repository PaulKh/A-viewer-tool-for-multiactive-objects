package views;

import callbacks.ThreadEventClickedCallback;
import com.sun.deploy.util.StringUtils;
import enums.IssueType;
import exceptions.WrongLogFileFormatException;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.Arrow;
import supportModel.ErrorEntity;
import supportModel.ParsedData;
import utils.DataHelper;
import utils.DataParser;
import utils.PreferencesHelper;
import utils.SizeHelper;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by pkhvoros on 3/16/15.
 */
public class MainWindow extends JFrame implements ThreadEventClickedCallback {
    private DataHelper dataHelper;
    private List<ActiveObject> activeObjects;
    private String directory;
    ActionListener openLogFiles = e -> {
        final JFileChooser fc = new JFileChooser();
        if (directory != null && Files.exists(Paths.get(directory))) {
            fc.setCurrentDirectory(new File(directory));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(MainWindow.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            PreferencesHelper.setPathToDirectory(MainWindow.class, fc.getSelectedFile().toString());
            System.out.println("getSelectedFile() : "
                    + fc.getSelectedFile());
            directory = fc.getSelectedFile().toString();
        }
    };

    //views
    private JButton selectLogFilesButton;
    private JPanel rootPanel;
    private JButton parseButton;
    private JPanel activeObjectsRoot;
    private JScrollPane scrollPane;
    private ScrollRootPanel scrollPaneRoot;
    ActionListener parseLogsAndBuildTree = e -> {
        dataHelper = new DataHelper(directory);
        activeObjects = dataHelper.getActiveObjects();

        showErrorMessage(dataHelper.getErrorEntities());
        buildTree();
    };
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JPanel scrollContainer;
    private ScalePanel scalePanel;
    private List<ThreadFlowPanel> flowPanels = new ArrayList<>();

    public MainWindow(String headTitle) throws HeadlessException {
        super(headTitle);
        directory = PreferencesHelper.getPathToDirectory(MainWindow.class);
        setContentPane(rootPanel);
        setJMenuBar(createMenuBar());
        assignActionsToButtons();
        initSlider();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initSlider() {
        scaleLabel.addMouseListener(new MouseListener() {
            @Override
            public void mouseClicked(MouseEvent e) {
                labelPressed();
            }

            @Override
            public void mousePressed(MouseEvent e) {

            }

            @Override
            public void mouseReleased(MouseEvent e) {

            }

            @Override
            public void mouseEntered(MouseEvent e) {

            }

            @Override
            public void mouseExited(MouseEvent e) {

            }
        });
        scaleSlider.setValue(500);
        scaleSlider.addChangeListener(e -> {
            if (scrollPaneRoot == null)
                return;
            scaleLabel.setText(scaleSlider.getValue() + " pixels/seconds");
            SizeHelper.instance().setScale(scaleSlider.getValue());
            for (ThreadFlowPanel flowPanel : flowPanels) {
                flowPanel.updateSize();
            }
            if (scalePanel != null) {
                scalePanel.updateView();
            }
            scrollPaneRoot.repaint();
            revalidate();
            repaint();
        });
    }

    private void labelPressed() {
        JFrame frame = new JFrame();
        Object result = JOptionPane.showInputDialog(frame, "Scale should be between " + scaleSlider.getMinimum() + " and " + scaleSlider.getMaximum() + " pixels per second");
        if (result.toString().matches("^-?\\d+$")) {
            if (Integer.valueOf(result.toString()) >= scaleSlider.getMinimum() && Integer.valueOf(result.toString()) <= scaleSlider.getMaximum()) {
                scaleSlider.setValue(Integer.valueOf(result.toString()));
                scaleLabel.setText(result.toString() + " pixels/second");
            } else
                labelPressed();
        } else {
            labelPressed();
        }
    }

    private void assignActionsToButtons() {
        if (directory != null)
            parseButton.setEnabled(true);
        selectLogFilesButton.addActionListener(openLogFiles);
        parseButton.addActionListener(parseLogsAndBuildTree);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);
        JMenuItem openAction = new JMenuItem("Open");
        JMenuItem exitAction = new JMenuItem("Exit");
        fileMenu.add(openAction);
        fileMenu.add(exitAction);

        openAction.addActionListener(openLogFiles);
        exitAction.addActionListener(e -> System.exit(0));
        return menuBar;
    }

    private void discoverMinimumAndMaximum() {
        long minimumTime = Long.MAX_VALUE;
        long maximumTime = 0;
        for (ActiveObject activeObject : activeObjects) {
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                for (ThreadEvent threadEvent : thread.getEvents()) {
                    if (threadEvent.getStartTime() < minimumTime) {
                        minimumTime = threadEvent.getStartTime();
                    }
                    if (threadEvent.getFinishTime() > maximumTime) {
                        maximumTime = threadEvent.getFinishTime();
                    }
                }
            }
        }
        SizeHelper.instance().init(minimumTime, maximumTime, scaleSlider.getValue());
    }

    private void buildTree() {
        discoverMinimumAndMaximum();
        GridBagLayout gridBagLayout = new GridBagLayout();
        GridBagConstraints constraints;
        if (scrollPane == null) {
            scrollPaneRoot = new ScrollRootPanel(gridBagLayout);
            scrollPane = new JScrollPane(scrollPaneRoot);
            scrollContainer.add(scrollPane, BorderLayout.CENTER);
        } else {
            scrollPaneRoot.removeAll();
            scrollPaneRoot.setLayout(gridBagLayout);
        }
        for (ActiveObject activeObject : activeObjects) {

            ActiveObjectTitlePanel titlePanel = new ActiveObjectTitlePanel(activeObject.getIdentifier());
            constraints = new GridBagConstraints();
            constraints.weightx = 0.0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridwidth = 1;
            constraints.gridheight = activeObject.getThreads().size() * 2 + 1;
            constraints.fill = GridBagConstraints.VERTICAL;
            gridBagLayout.setConstraints(titlePanel, constraints);
            scrollPaneRoot.add(titlePanel);

            EmptyRow emptyRow1 = new EmptyRow(10);
            constraints = new GridBagConstraints();
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagLayout.setConstraints(emptyRow1, constraints);
            scrollPaneRoot.add(emptyRow1);

            for (ActiveObjectThread thread : activeObject.getThreads()) {

                ThreadTitlePanel threadTitlePanel = new ThreadTitlePanel(thread.getThreadId() + "");
                constraints = new GridBagConstraints();
                constraints.weightx = 0.0;
                constraints.fill = GridBagConstraints.NONE;
                gridBagLayout.setConstraints(threadTitlePanel, constraints);
                scrollPaneRoot.add(threadTitlePanel);

                ThreadFlowPanel flowPanel = new ThreadFlowPanel(thread);
                flowPanels.add(flowPanel);
                flowPanel.setCallback(this);
                constraints = new GridBagConstraints();
                constraints.weightx = 0.0;
                constraints.gridwidth = GridBagConstraints.NONE;
                constraints.fill = GridBagConstraints.BOTH;
                gridBagLayout.setConstraints(flowPanel, constraints);
                scrollPaneRoot.add(flowPanel);

                EmptyRow emptyRow2 = new EmptyRow(10);
                constraints = new GridBagConstraints();
                constraints.weightx = 1.0;
                constraints.gridwidth = GridBagConstraints.REMAINDER;
                gridBagLayout.setConstraints(emptyRow2, constraints);
                scrollPaneRoot.add(emptyRow2);
            }
            EmptyRow emptyRow = new EmptyRow(50);
            constraints = new GridBagConstraints();
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagLayout.setConstraints(emptyRow, constraints);
            scrollPaneRoot.add(emptyRow);
        }
        EmptyRow emptyRow = new EmptyRow(50);
        constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        gridBagLayout.setConstraints(emptyRow, constraints);
        scrollPaneRoot.add(emptyRow);

//        EmptyRow emptyRow1 = new EmptyRow(50);
//        gridBagLayout.setConstraints(emptyRow1, constraints);
//        scrollPaneRoot.add(emptyRow1);

        scalePanel = new ScalePanel();
        constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.NONE;
        constraints.fill = GridBagConstraints.BOTH;
        gridBagLayout.setConstraints(scalePanel, constraints);
        scrollPaneRoot.add(scalePanel);

        EmptyRow emptyRow2 = new EmptyRow(50);
        constraints = new GridBagConstraints();
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.setConstraints(emptyRow2, constraints);
        scrollPaneRoot.add(emptyRow2);
        revalidate();
        scrollPaneRoot.setFlowX((flowPanels.size() != 0) ? flowPanels.get(0).getX() : 200);
        repaint();
    }

    private void showErrorMessage(List<ErrorEntity> errorEntities) {
        if (errorEntities.size() == 0) {
            return;
        }
//        String message = "";
//        for (ErrorEntity errorEntity:errorEntities){
//            message = message + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
//        }
        JDialog dialog = new JDialog(this);
        dialog.setTitle("Issue log");
        dialog.setLocationByPlatform(true);
        StyleContext sc = new StyleContext();
        final DefaultStyledDocument doc = new DefaultStyledDocument(sc);

        final Style errorStyle = sc.addStyle("ErrorStyle", null);
        errorStyle.addAttribute(StyleConstants.Foreground, Color.red);

        JTextPane pane = new JTextPane(doc);
        try {
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Error ||
                        errorEntity.getErrorType().getIssueType() == IssueType.FatalError) {
                    String message = "ERROR: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                    doc.setParagraphAttributes(doc.getLength() - message.length(), 1, errorStyle, false);
                }
            }
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Warning) {
                    String message = "WARNING: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                    doc.setParagraphAttributes(doc.getLength() - message.length(), 1, errorStyle, false);
                }
            }
            for (ErrorEntity errorEntity : errorEntities) {
                if (errorEntity.getErrorType().getIssueType() == IssueType.Info) {
                    String message = "INFO: " + errorEntity.getErrorType().getMessage() + errorEntity.getMessage() + "\n";
                    doc.insertString(doc.getLength(), message, null);
                }
            }
        } catch (BadLocationException e) {
            e.printStackTrace();
        }
        pane.select(0, 0);
        pane.setEditable(false);
        dialog.setPreferredSize(new Dimension(400, 300));
        dialog.add(new JScrollPane(pane));
        dialog.pack();
        dialog.setVisible(true);
    }

    @Override
    public void threadEventClicked(ThreadEvent threadEvent) {
        if (!scrollPaneRoot.removeArrowsIfAdded(threadEvent)) {
            addArrowForThreadEvent(null, threadEvent);
            List<ThreadEvent> threadEvents = dataHelper.getOutgoingThreadEvents(threadEvent);
            for (ThreadEvent threadEvent1 : threadEvents) {
                addArrowForThreadEvent(threadEvent, threadEvent1);
            }
        }
        for (ThreadFlowPanel threadFlowPanel : flowPanels) {
            threadFlowPanel.setHighlightedEvent(scrollPaneRoot.getArrows());
        }
        scrollPaneRoot.repaint();
    }

    private void addArrowForThreadEvent(ThreadEvent sourceThreadEvent, ThreadEvent destinationThreadEvent) {
        ThreadFlowPanel sourcePanel = null, destinationPanel = null;
        for (ThreadFlowPanel threadFlowPanel : flowPanels) {
            if (threadFlowPanel.getActiveObjectThread() == destinationThreadEvent.getThread()) {
                destinationPanel = threadFlowPanel;
            }
            if (threadFlowPanel.getActiveObjectThread().getThreadId() == destinationThreadEvent.getSenderThreadId()) {
                sourcePanel = threadFlowPanel;
                if (sourceThreadEvent == null) {
                    sourceThreadEvent = getSourceEvent(destinationThreadEvent, sourcePanel);
                }
            }
        }
        scrollPaneRoot.addArrow(sourceThreadEvent, destinationThreadEvent, sourcePanel, destinationPanel);
    }

    private ThreadEvent getSourceEvent(ThreadEvent threadEvent, ThreadFlowPanel flowPanel) {
        for (ThreadEvent tempThreadEvent : flowPanel.getActiveObjectThread().getEvents()) {
            if (threadEvent.getRequestSentTime() >= tempThreadEvent.getStartTime() && threadEvent.getRequestSentTime() <= tempThreadEvent.getFinishTime())
                return tempThreadEvent;
        }
        return null;
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
        rootPanel = new JPanel();
        rootPanel.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        selectLogFilesButton = new JButton();
        selectLogFilesButton.setText("Select log folder");
        panel1.add(selectLogFilesButton, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final com.intellij.uiDesigner.core.Spacer spacer1 = new com.intellij.uiDesigner.core.Spacer();
        panel1.add(spacer1, new com.intellij.uiDesigner.core.GridConstraints(0, 1, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, 1, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        parseButton = new JButton();
        parseButton.setEnabled(false);
        parseButton.setText("Parse logs and build execution tree");
        panel1.add(parseButton, new com.intellij.uiDesigner.core.GridConstraints(0, 2, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_NORTH, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        activeObjectsRoot = new JPanel();
        activeObjectsRoot.setLayout(new com.intellij.uiDesigner.core.GridLayoutManager(3, 1, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(activeObjectsRoot, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(500, 500), null, 0, false));
        scrollContainer = new JPanel();
        scrollContainer.setLayout(new BorderLayout(0, 0));
        activeObjectsRoot.add(scrollContainer, new com.intellij.uiDesigner.core.GridConstraints(0, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_BOTH, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_SHRINK | com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaleLabel = new JLabel();
        scaleLabel.setText("500 pixels/second");
        activeObjectsRoot.add(scaleLabel, new com.intellij.uiDesigner.core.GridConstraints(1, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_CENTER, com.intellij.uiDesigner.core.GridConstraints.FILL_NONE, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaleSlider = new JSlider();
        scaleSlider.setMaximum(10000);
        scaleSlider.setMinimum(1);
        scaleSlider.setPaintTicks(false);
        scaleSlider.setValue(100);
        activeObjectsRoot.add(scaleSlider, new com.intellij.uiDesigner.core.GridConstraints(2, 0, 1, 1, com.intellij.uiDesigner.core.GridConstraints.ANCHOR_WEST, com.intellij.uiDesigner.core.GridConstraints.FILL_HORIZONTAL, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_WANT_GROW, com.intellij.uiDesigner.core.GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
