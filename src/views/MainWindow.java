package views;

import callbacks.LockerButtonCallback;
import callbacks.SwapButtonPressedListener;
import callbacks.ThreadEventClickedCallback;
import callbacks.UpButtonPressedCallback;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import enums.OrderingPolicyEnum;
import enums.ViewPositionPolicyEnum;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.ErrorEntity;
import supportModel.OrderStateOfActiveObjects;
import utils.*;
import views.builders.ErrorDialogBuilder;
import views.builders.QueuesDialogBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


/**
 * Created by pkhvoros on 3/16/15.
 */

//This is the main frame of the application
public class MainWindow extends JFrame implements ThreadEventClickedCallback, SwapButtonPressedListener, UpButtonPressedCallback, LockerButtonCallback {
    private DataHelper dataHelper;
    private List<ActiveObject> activeObjects;
    private String directory;
    private SwapActiveObjectsQueue undoQueue = new SwapActiveObjectsQueue();
    private List<ActiveObject> lockedObjects = new ArrayList<>();
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
            directory = fc.getSelectedFile().toString();
        }
    };

    //views
    private JButton selectLogFilesButton;
    private JPanel rootPanel;
    private JButton parseButton;
    private JPanel activeObjectsRoot;
    private JScrollPane mainScrollPane;
    private ScrollRootPanel scrollPaneRoot;
    private List<SwapActiveObjectsButton> swapActiveObjectsButtons = new ArrayList<>();
    ActionListener parseLogsAndBuildTree = e -> {
        ArrowHandler.instance().clearAll();
        dataHelper = new DataHelper(directory);
        activeObjects = dataHelper.getActiveObjects();

        showErrorMessage(dataHelper.getErrorEntities());

        discoverMinimumAndMaximum();
        buildMainView();
    };
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JPanel container;
    private JButton undoReorderingButton;
    private ScalePanel scalePanel;
    private List<FlowPanel> flowPanels = new ArrayList<>();

    public MainWindow(String headTitle) throws HeadlessException {
        super(headTitle);
        directory = PreferencesHelper.getPathToDirectory(MainWindow.class);
        setContentPane(rootPanel);
        setJMenuBar(createMenuBar());
        assignActionsToButtons();
        initUndoAction();
        initSlider();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    private void initUndoAction() {
        undoReorderingButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                activeObjects = undoQueue.undo(activeObjects);
                undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
                updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
                highlighThreadEvents();
            }
        });
    }

    private void initSlider() {
        scaleLabel.setBorder(new EmptyBorder(0, 0, 0, 15));
        scaleLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                labelPressed();
            }
        });
        scaleSlider.setValue(500);
        scaleSlider.addChangeListener(e -> {
            if (container == null)
                return;
            scaleLabel.setText(scaleSlider.getValue() + " pixels/seconds");
            SizeHelper.instance().setScale(scaleSlider.getValue());
            for (FlowPanel flowPanel : flowPanels) {
                flowPanel.updateSize();
            }
            if (scalePanel != null) {
                scalePanel.updateView();
            }
            container.repaint();
            revalidate();
            repaint();
        });
    }

    private void labelPressed() {
        JFrame frame = new JFrame();
        Object result = JOptionPane.showInputDialog(frame, "Scale should be between " + scaleSlider.getMinimum() + " and " + scaleSlider.getMaximum() + " pixels per second");
        if (result != null && result.toString().matches("^-?\\d+$")) {
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
        JMenuItem preferencesAction = new JMenuItem("Preferences");
        JMenuItem exitAction = new JMenuItem("Exit");
        fileMenu.add(openAction);
        fileMenu.add(preferencesAction);
        fileMenu.add(exitAction);

        openAction.addActionListener(openLogFiles);
        exitAction.addActionListener(e -> System.exit(0));
        preferencesAction.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                SettingsDialog settingsDialog = new SettingsDialog();

            }
        });

//        JMenu editMenu = new JMenu("Edit");
//        menuBar.add(editMenu);
//        JMenuItem scaleAction = new JMenuItem("Change scale max/min values");
//        editMenu.add(scaleAction);
//
//        scaleAction.addActionListener(e -> {
//            JTextField minimumValue = new JTextField();
//            JTextField maximumValue = new JTextField();
//            final JComponent[] inputs = new JComponent[]{
//                    new JLabel("Minimum value"),
//                    minimumValue,
//                    new JLabel("Maximum value"),
//                    maximumValue,
//            };
//            JOptionPane.showMessageDialog(null, inputs, "My custom dialog", JOptionPane.PLAIN_MESSAGE);
//
//        });

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

    private void buildMainView() {
        container.removeAll();

        BorderLayout gridBagLayout = new BorderLayout();
        container.setLayout(gridBagLayout);

        GridBagLayout titleGridBagLayout = new GridBagLayout();
        JPanel titlesPanel = new JPanel(titleGridBagLayout);
        JScrollPane titleScrollPane = new JScrollPane(titlesPanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

        GridBagLayout mainGridBagLayout = new GridBagLayout();
        scrollPaneRoot = new ScrollRootPanel(mainGridBagLayout);
        mainScrollPane = new JScrollPane(scrollPaneRoot, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        titleScrollPane.getVerticalScrollBar().setModel(mainScrollPane.getVerticalScrollBar().getModel());

        scalePanel = new ScalePanel();
        JScrollPane scaleScrollPane = new JScrollPane(scalePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        scaleScrollPane.getHorizontalScrollBar().setModel(mainScrollPane.getHorizontalScrollBar().getModel());

        initScalePanel();
        initFlowsPanel(scrollPaneRoot, mainGridBagLayout);
        initTitlesView(titlesPanel, titleGridBagLayout);

        JPanel titleScrollContainer = new JPanel();
        titleScrollContainer.add(titleScrollPane);
        container.add(titleScrollPane, BorderLayout.WEST);

        JPanel mainScrollContainer = new JPanel();
        mainScrollContainer.add(mainScrollPane);
        container.add(mainScrollPane, BorderLayout.CENTER);

        container.add(scaleScrollPane, BorderLayout.SOUTH);

        revalidate();
        repaint();
    }

    private void initScalePanel() {
        GridBagLayout scaleGridBagLayout = new GridBagLayout();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.gridwidth = GridBagConstraints.NONE;
        constraints.fill = GridBagConstraints.BOTH;
        scaleGridBagLayout.setConstraints(scalePanel, constraints);
    }

    private void initFlowsPanel(JPanel titlesPanel, GridBagLayout gridBagLayout) {
        GridBagConstraints constraints = new GridBagConstraints();
        boolean firstLineSkipped = false;
        for (ActiveObject activeObject : activeObjects) {
            if (!firstLineSkipped) {
                titlesPanel.add(buildEmptyRow(gridBagLayout, 10));
                firstLineSkipped = true;
            } else {
                titlesPanel.add(buildEmptyRow(gridBagLayout, 40));
            }
            if (lockedObjects.contains(activeObject)) {
                LockedAOFlowPanel flowPanel = new LockedAOFlowPanel(activeObject);
                flowPanels.add(flowPanel);
                constraints = new GridBagConstraints();
                constraints.weightx = 0.0;
                constraints.gridwidth = GridBagConstraints.NONE;
                constraints.fill = GridBagConstraints.BOTH;
                gridBagLayout.setConstraints(flowPanel, constraints);
                titlesPanel.add(flowPanel);
                titlesPanel.add(buildEmptyRow(gridBagLayout, 10));
                continue;
            }
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                ThreadFlowPanel flowPanel = new ThreadFlowPanel(thread);
                flowPanels.add(flowPanel);
                flowPanel.setCallback(this);
                constraints = new GridBagConstraints();
                constraints.weightx = 0.0;
                constraints.gridwidth = GridBagConstraints.NONE;
                constraints.fill = GridBagConstraints.BOTH;
                gridBagLayout.setConstraints(flowPanel, constraints);
                titlesPanel.add(flowPanel);
                titlesPanel.add(buildEmptyRow(gridBagLayout, 10));
            }
        }
        titlesPanel.add(buildEmptyRow(gridBagLayout, 30));
    }

    private void initTitlesView(JPanel titlesPanel, GridBagLayout gridBagLayout) {
        GridBagConstraints constraints;
        boolean firstLineSkipped = false;
        ActiveObject previousActiveObject = null;
        for (int counter = 0; counter < activeObjects.size(); counter++) {
            ActiveObject activeObject = activeObjects.get(counter);
            if (!firstLineSkipped) {
                this.swapActiveObjectsButtons.clear();
                previousActiveObject = activeObject;
                firstLineSkipped = true;
                titlesPanel.add(buildEmptyRow(gridBagLayout, 10));
            } else {
                try {
                    SwapActiveObjectsButton button = new SwapActiveObjectsButton(previousActiveObject, activeObject, this);
                    this.swapActiveObjectsButtons.add(button);
                    previousActiveObject = activeObject;
                    button.setOpaque(false);
                    button.setBorderPainted(false);
                    button.setContentAreaFilled(false);
                    button.setFocusPainted(false);
                    Image img = ImageIO.read(getClass().getResource("/refresh-icon.png"));
                    button.setIcon(new ImageIcon(img));
                    button.setMargin(new Insets(0, 0, 0, 0));
                    constraints = new GridBagConstraints();
                    constraints.gridwidth = GridBagConstraints.REMAINDER;
                    gridBagLayout.setConstraints(button, constraints);
                    titlesPanel.add(button);
                    titlesPanel.add(buildEmptyRow(gridBagLayout, 10));
                } catch (IOException ex) {

                }
            }
            ActiveObjectTitlePanel titlePanel;
            UpButtonPressedCallback upButtonPressedCallback = (counter == 0) ? null : this;
            boolean isLocked = lockedObjects.contains(activeObject);

            titlePanel = new ActiveObjectTitlePanel(activeObject, upButtonPressedCallback, this, isLocked);
            constraints = new GridBagConstraints();
            constraints.weightx = 0.0;
            constraints.gridwidth = 1;
            constraints.gridheight = isLocked ? 2 : activeObject.getThreads().size() * 2;

            constraints.fill = GridBagConstraints.VERTICAL;
            gridBagLayout.setConstraints(titlePanel, constraints);
            titlesPanel.add(titlePanel);
            if (isLocked) {
                String threadTitleIds = " ";
                for (ActiveObjectThread thread : activeObject.getThreads()) {
                    threadTitleIds += thread.getThreadId() + " ";
                }
                createThreadTitlePanel(gridBagLayout, titlesPanel, threadTitleIds);
                continue;
            }
            for (ActiveObjectThread thread : activeObject.getThreads()) {
                createThreadTitlePanel(gridBagLayout, titlesPanel, thread.getThreadId() + "");
            }
        }
        titlesPanel.add(buildEmptyRow(gridBagLayout, 30));

    }

    private void createThreadTitlePanel(GridBagLayout gridBagLayout, JPanel containerPanel, String title) {
        ThreadTitlePanel threadTitlePanel = new ThreadTitlePanel(title);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 0.0;
        constraints.fill = GridBagConstraints.NONE;
        gridBagLayout.setConstraints(threadTitlePanel, constraints);
        containerPanel.add(threadTitlePanel);
        for (int j = 0; j < 2; j++) {
            containerPanel.add(buildEmptyRow(gridBagLayout, 10));
        }
    }

    private EmptyRow buildEmptyRow(GridBagLayout gridBagLayout, int height) {
        EmptyRow emptyRow2 = new EmptyRow(height);
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.weightx = 1.0;
        constraints.gridwidth = GridBagConstraints.REMAINDER;
        gridBagLayout.setConstraints(emptyRow2, constraints);
        return emptyRow2;
    }

    private void showErrorMessage(List<ErrorEntity> errorEntities) {
        ErrorDialogBuilder.buildErrorDialog(errorEntities, this);
    }

    @Override
    public void threadEventClicked(ThreadEvent threadEvent) {
        List<ActiveObject> activeObjectList = ArrowHandler.instance().addArrowsForEvent(threadEvent, flowPanels, dataHelper);
        if (PreferencesHelper.getReorderingPolicy(this.getClass()) == OrderingPolicyEnum.ENABLED) {
            OrderStateOfActiveObjects state = undoQueue.generateStateByActiveObjects(activeObjects);
            if (reorderActiveObjectsDependingOnSelected(activeObjectList)) {
                updateView(ViewPositionPolicyEnum.TO_THE_START);
                undoQueue.addState(state);
                undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
            }
        }
        highlighThreadEvents();
        scrollPaneRoot.repaint();
    }

    @Override
    public void threadClicked(ActiveObject activeObject, long timeClicked) {
        QueuesDialogBuilder.buildQueueDialog(this, activeObject, timeClicked);
    }

    @Override
    public void swapButtonPressed(SwapActiveObjectsButton button) {
        undoQueue.addNewState(activeObjects);
        undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
        Collections.swap(activeObjects, activeObjects.indexOf(button.getActiveObject1()), activeObjects.indexOf(button.getActiveObject2()));
        updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
        highlighThreadEvents();
    }

    private void updateView(ViewPositionPolicyEnum positionPolicyEnum) {
        Point position = null;
        if (positionPolicyEnum == ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE) {
            position = new Point(mainScrollPane.getHorizontalScrollBar().getValue(), mainScrollPane.getVerticalScrollBar().getValue());
        }
        buildMainView();
        if (positionPolicyEnum == ViewPositionPolicyEnum.TO_THE_START) {
            position = ArrowHandler.instance().updateArrows(flowPanels);
            position = new Point(position.x - 50, position.y - 50);
        } else {
            ArrowHandler.instance().updateArrows(flowPanels);
        }

        final Point finalPosition = position;
        SwingUtilities.invokeLater(() -> {
            mainScrollPane.getVerticalScrollBar().setValue(finalPosition.y);
            mainScrollPane.getHorizontalScrollBar().setValue(finalPosition.x);
        });
    }

    // highlights events selected by user and all dependent
    private void highlighThreadEvents() {
        for (FlowPanel threadFlowPanel : flowPanels) {
            if (threadFlowPanel instanceof ThreadFlowPanel)
                ((ThreadFlowPanel) threadFlowPanel).setHighlightedEvent();
        }
    }

    // reorder active objects in the way that dependent objects are situated nearby.
    // Returns true if reordering made, so the caller know that
    // view must be repainted
    private boolean reorderActiveObjectsDependingOnSelected(List<ActiveObject> activeObjectList) {
        int currentPosition = -1;
        boolean swapped = false;
        for (int i = 0; i < activeObjects.size(); i++) {
            ActiveObject activeObject = activeObjects.get(i);
            boolean contains = activeObjectList.contains(activeObject);
            if (!contains)
                continue;
            else if (currentPosition == -1) {
                currentPosition = i;
            } else if (currentPosition + 1 == i) {
                currentPosition++;
            } else {
                currentPosition++;

                this.activeObjects.remove(activeObject);
                this.activeObjects.add(currentPosition, activeObject);
//                Collections.swap(activeObjects, currentPosition, i);
                swapped = true;
            }
        }
        return swapped;
    }

    @Override
    public void upButtonPressed(ActiveObject activeObject) {
        undoQueue.addNewState(activeObjects);
        undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
        this.activeObjects.remove(activeObject);
        this.activeObjects.add(0, activeObject);
        updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
        highlighThreadEvents();
    }

    @Override
    public void lockerButtonPressed(ActiveObject activeObject) {
        if (lockedObjects.contains(activeObject)) {
            lockedObjects.remove(activeObject);
        } else lockedObjects.add(activeObject);
        updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
        highlighThreadEvents();
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
        rootPanel.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
        final JPanel panel1 = new JPanel();
        panel1.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, 1, null, null, null, 0, false));
        selectLogFilesButton = new JButton();
        selectLogFilesButton.setText("Select log folder");
        panel1.add(selectLogFilesButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        parseButton = new JButton();
        parseButton.setEnabled(false);
        parseButton.setText("Parse logs and build execution tree");
        panel1.add(parseButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        activeObjectsRoot = new JPanel();
        activeObjectsRoot.setLayout(new GridLayoutManager(3, 2, new Insets(0, 0, 0, 0), -1, -1));
        rootPanel.add(activeObjectsRoot, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, new Dimension(500, 500), null, 0, false));
        container = new JPanel();
        container.setLayout(new GridLayoutManager(1, 1, new Insets(0, 0, 0, 0), -1, -1));
        activeObjectsRoot.add(container, new GridConstraints(0, 0, 1, 2, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
        scaleSlider = new JSlider();
        scaleSlider.setMaximum(10000);
        scaleSlider.setMinimum(1);
        scaleSlider.setPaintTicks(false);
        scaleSlider.setValue(100);
        activeObjectsRoot.add(scaleSlider, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        scaleLabel = new JLabel();
        scaleLabel.setText("500 pixels/second");
        activeObjectsRoot.add(scaleLabel, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        undoReorderingButton = new JButton();
        undoReorderingButton.setEnabled(false);
        undoReorderingButton.setText("Undo reordering");
        activeObjectsRoot.add(undoReorderingButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
