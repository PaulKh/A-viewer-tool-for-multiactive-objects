package views;

import callbacks.LockerButtonCallback;
import callbacks.SwapButtonPressedListener;
import callbacks.ThreadEventClickedCallback;
import callbacks.UpButtonPressedCallback;
import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import enums.MenuItemType;
import enums.OrderingPolicyEnum;
import enums.ViewPositionPolicyEnum;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import supportModel.ArrowWithPosition;
import supportModel.ErrorEntity;
import supportModel.OrderStateOfActiveObjects;
import utils.*;
import views.builders.ErrorDialogBuilder;
import views.builders.QueuesDialogBuilder;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
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
    MouseAdapter openLogFiles = new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            super.mouseClicked(e);
            openLogFiles();
        }
    };
    private DataHelper dataHelper;
    private List<ActiveObject> activeObjects;
    private String directory;
    ActionListener parseLogsAndBuildTree = e -> {
        ArrowHandler.instance().clearAll();
        dataHelper = new DataHelper(directory);
        activeObjects = dataHelper.getActiveObjects();
        showErrorMessage(dataHelper.getErrorEntities());
        discoverMinimumAndMaximum(dataHelper);
        buildMainView();
    };
    private SwapActiveObjectsQueue undoQueue = new SwapActiveObjectsQueue();
    private List<ActiveObject> lockedObjects = new ArrayList<>();
    //views
    private JTextField selectLogFilesTextField;
    private JPanel rootPanel;
    private JButton parseButton;
    private JPanel activeObjectsRoot;
    private JScrollPane mainScrollPane;
    private ScrollRootPanel scrollPaneRoot;
    private List<SwapActiveObjectsButton> swapActiveObjectsButtons = new ArrayList<>();
    private JSlider scaleSlider;
    private JLabel scaleLabel;
    private JPanel container;
    private JButton undoReorderingButton;
    private JButton clearButton;
    private ScalePanel scalePanel;
    private List<FlowPanel> flowPanels;

    public MainWindow(String headTitle) throws HeadlessException {
        super(headTitle);
        setDirectory(PreferencesHelper.getPathToDirectory());
        setContentPane(rootPanel);
        setJMenuBar(createMenuBar());
        assignActionsToButtons();
        initUndoAction();
        initSlider();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    //open log file pressed
    private void openLogFiles() {
        final JFileChooser fc = new JFileChooser();
        if (directory != null && Files.exists(Paths.get(directory))) {
            fc.setCurrentDirectory(new File(directory));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(MainWindow.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            setDirectory(fc.getSelectedFile().toString());
            PreferencesHelper.setPathToDirectory(directory);
        }
    }

    private void setDirectory(String directory) {
        this.directory = directory;
        selectLogFilesTextField.setText(directory);
        parseButton.setEnabled(true);
    }

    private void initUndoAction() {
        undoReorderingButton.addActionListener(e -> {
            activeObjects = undoQueue.undo(activeObjects);
            undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
            updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
            highlightDependencyThreadEvents();
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
        if (result != null) {
            if (result.toString().matches("^-?\\d+$")) {
                if (Integer.valueOf(result.toString()) >= scaleSlider.getMinimum() && Integer.valueOf(result.toString()) <= scaleSlider.getMaximum()) {
                    scaleSlider.setValue(Integer.valueOf(result.toString()));
                    scaleLabel.setText(result.toString() + " pixels/second");
                }
            } else
                labelPressed();
        }
    }

    private void assignActionsToButtons() {
        if (directory != null)
            parseButton.setEnabled(true);
        selectLogFilesTextField.addMouseListener(openLogFiles);
        parseButton.addActionListener(parseLogsAndBuildTree);
        clearButton.addActionListener(e -> {
            ArrowHandler.instance().clearAll();
            for (FlowPanel flowPanel : flowPanels) {
                if (flowPanel instanceof ThreadFlowPanel)
                    flowPanel.deHighlightAllTheRectangles();
            }
            clearButton.setEnabled(false);
            repaint();
        });
    }

    private void updateClearButton() {
        if (ArrowHandler.instance().getArrows().size() == 0) {
            clearButton.setEnabled(false);
        } else clearButton.setEnabled(true);
    }

    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        setJMenuBar(menuBar);
        JMenu fileMenu = new JMenu("File");
        JMenu helpMenu = new JMenu("?");
        menuBar.add(fileMenu);
        menuBar.add(helpMenu);

        JMenuItem openAction = new JMenuItem("Open");
        JMenuItem preferencesAction = new JMenuItem("Preferences");
        JMenuItem exitAction = new JMenuItem("Exit");
        JMenuItem helpAction = new JMenuItem("Help");
        fileMenu.add(openAction);
        fileMenu.add(preferencesAction);
        fileMenu.add(exitAction);
        helpMenu.add(helpAction);

        openAction.addActionListener(e -> {
            openLogFiles();
        });
        exitAction.addActionListener(e -> System.exit(0));
        preferencesAction.addActionListener(e -> {
            SettingsDialog settingsDialog = new SettingsDialog();
        });
        helpAction.addActionListener(e -> showHelpMenu());
        return menuBar;
    }

    private void showHelpMenu() {
        JEditorPane editorPane = null;
        try {
            editorPane = new JEditorPane(getClass().getResource("/documentation.html"));
            editorPane.setContentType("text/html");
            editorPane.setEditable(false);
            editorPane.setPreferredSize(new Dimension(600, 300));
            JScrollPane scrollPane = new JScrollPane(editorPane);
            JOptionPane.showMessageDialog(this, scrollPane);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void discoverMinimumAndMaximum(DataHelper dataHelper) {
        SizeHelper.instance().setMaxMinScale(dataHelper.getMaximumTime(), dataHelper.getMinimumTime(), scaleSlider.getValue());
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
        mainScrollPane = new JScrollPane(scrollPaneRoot, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
        titleScrollPane.getVerticalScrollBar().setModel(mainScrollPane.getVerticalScrollBar().getModel());

        scalePanel = new ScalePanel();
        JScrollPane scaleScrollPane = new JScrollPane(scalePanel, JScrollPane.VERTICAL_SCROLLBAR_NEVER, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
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
        flowPanels = new ArrayList<>();
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
    public void threadEventClicked(MenuItemType menuItemType, ThreadEvent threadEvent) {
        switch (menuItemType) {
            case DEPENDENCIES:
                List<ArrowWithPosition> arrowsAdded = ArrowHandler.instance().addArrowsForEvent(threadEvent, flowPanels);
                List<ActiveObject> activeObjectsInvolved = ArrowHandler.instance().getAllActiveObjectsFromArrows(arrowsAdded);
                if (PreferencesHelper.getReorderingPolicy() == OrderingPolicyEnum.ENABLED && arrowsAdded.size() != 0) {
                    OrderStateOfActiveObjects state = undoQueue.generateStateByActiveObjects(activeObjects);
                    if (reorderActiveObjectsDependingOnSelected(activeObjectsInvolved)) {
                        updateView(ViewPositionPolicyEnum.TO_THE_START, arrowsAdded);
                        undoQueue.addState(state);
                        undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
                    } else {
                        if (PreferencesHelper.isRepositioningAllowed() && arrowsAdded.size() != 0)
                            moveViewToTheStart(arrowsAdded);
                    }
                } else {
                    if (PreferencesHelper.isRepositioningAllowed() && arrowsAdded.size() != 0)
                        moveViewToTheStart(arrowsAdded);
                }
                highlightDependencyThreadEvents();
                break;
            case COMPATIBILITY:
                for (FlowPanel flowPanel : flowPanels) {
                    if (flowPanel instanceof ThreadFlowPanel) {
                        if (flowPanel.getActiveObject().equals(threadEvent.getThread().getActiveObject())) {
                            ((ThreadFlowPanel) flowPanel).highlightCompatibility(threadEvent);
                        }
                    }
                }
                CompatibilityHelper.instance().addTuple(threadEvent.getThread().getActiveObject(), threadEvent);
                break;
        }

        updateClearButton();
        scrollPaneRoot.revalidate();
        scrollPaneRoot.repaint();
    }

    @Override
    public void threadClicked(ActiveObject activeObject, long timeClicked) {
        new QueuesDialogBuilder().buildQueueDialog(this, activeObject, timeClicked);
    }

    @Override
    public void removeCompatibilityClicked(ActiveObject activeObject) {
        for (FlowPanel flowPanel : flowPanels) {
            if (flowPanel instanceof ThreadFlowPanel) {
                if (flowPanel.getActiveObject().equals(activeObject)) {
                    ((ThreadFlowPanel) flowPanel).removeCompatibilityHighlight();
                }
            }
        }
        CompatibilityHelper.instance().removeTuple(activeObject);
        scrollPaneRoot.revalidate();
        scrollPaneRoot.repaint();
    }

    @Override
    public void swapButtonPressed(SwapActiveObjectsButton button) {
        undoQueue.addNewState(activeObjects);
        undoReorderingButton.setEnabled(!undoQueue.isQueueEmpty());
        Collections.swap(activeObjects, activeObjects.indexOf(button.getActiveObject1()), activeObjects.indexOf(button.getActiveObject2()));
        updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
        highlightDependencyThreadEvents();
    }

    private void updateView(ViewPositionPolicyEnum positionPolicyEnum) {
        updateView(positionPolicyEnum, null);
    }

    private void updateView(ViewPositionPolicyEnum positionPolicyEnum, List<ArrowWithPosition> arrowsAdded) {
        Point position = new Point(mainScrollPane.getHorizontalScrollBar().getValue(), mainScrollPane.getVerticalScrollBar().getValue());
        buildMainView();
        ArrowHandler.instance().updateArrows(flowPanels);
        if (positionPolicyEnum == ViewPositionPolicyEnum.TO_THE_START && arrowsAdded != null && PreferencesHelper.isRepositioningAllowed()) {
            moveViewToTheStart(arrowsAdded);
        } else
            moveViewToPosition(position);
    }

    //Moves the view to the most left top position of the arrows added after clicking the event
    private void moveViewToTheStart(List<ArrowWithPosition> arrowsAdded) {
        Point position = ArrowHandler.instance().getMostLeftAndTopPositionForArrows(arrowsAdded);
        position = new Point(position.x - 50, position.y - 50);
        moveViewToPosition(position);
    }

    private void moveViewToPosition(Point position) {
        SwingUtilities.invokeLater(() -> {
            mainScrollPane.getVerticalScrollBar().setValue(position.y);
            mainScrollPane.getHorizontalScrollBar().setValue(position.x);
        });
    }

    // highlights events selected by user and all dependent
    private void highlightDependencyThreadEvents() {
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
        highlightDependencyThreadEvents();
    }

    @Override
    public void lockerButtonPressed(ActiveObject activeObject) {
        if (lockedObjects.contains(activeObject)) {
            lockedObjects.remove(activeObject);
        } else lockedObjects.add(activeObject);
        updateView(ViewPositionPolicyEnum.KEEP_ON_THE_CURRENT_PLACE);
        highlightDependencyThreadEvents();
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
        final Spacer spacer1 = new Spacer();
        panel1.add(spacer1, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_NONE, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
        parseButton = new JButton();
        parseButton.setEnabled(false);
        parseButton.setText("Parse logs and build execution tree");
        panel1.add(parseButton, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_NORTH, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
        selectLogFilesTextField = new JTextField();
        selectLogFilesTextField.setEditable(false);
        selectLogFilesTextField.setEnabled(true);
        selectLogFilesTextField.setMargin(new Insets(0, 0, 0, 0));
        selectLogFilesTextField.setText("Select log files...");
        panel1.add(selectLogFilesTextField, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
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
        clearButton = new JButton();
        clearButton.setEnabled(false);
        clearButton.setText("Clear");
        activeObjectsRoot.add(clearButton, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$() {
        return rootPanel;
    }
}
