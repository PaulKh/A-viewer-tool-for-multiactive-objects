package views;

import exceptions.WrongLogFileFormat;
import model.ActiveObject;
import model.ActiveObjectThread;
import model.ThreadEvent;
import utils.DataParser;
import utils.PreferencesHelper;
import utils.SizeHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by pkhvoros on 3/16/15.
 */
public class MainWindow extends JFrame{
    private List<ActiveObject> activeObjects;
    private String directory;
    private SizeHelper sizeHelper;

    //views
    private JButton selectLogFilesButton;
    private JPanel rootPanel;
    private JButton parseButton;
    private JPanel activeObjectsRoot;
    private JScrollPane scrollPane;
    private JPanel scrollPaneRoot;
    private JSlider scaleSlider;
    private JLabel scaleLabel;
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
        scaleSlider.setValue(500);
        scaleSlider.addChangeListener(e -> {
            scaleLabel.setText(scaleSlider.getValue() + " pixels/seconds");

            for (ThreadFlowPanel flowPanel:flowPanels){
                sizeHelper = new SizeHelper(scaleSlider.getValue());
                flowPanel.updateSize(sizeHelper);
            }
            revalidate();
            repaint();
        });
    }
    private void assignActionsToButtons(){
        if (directory != null)
            parseButton.setEnabled(true);
        selectLogFilesButton.addActionListener(openLogFiles);
        parseButton.addActionListener(parseLogsAndBuildTree);
    }
    private JMenuBar createMenuBar(){
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
    ActionListener openLogFiles = e -> {
        final JFileChooser fc = new JFileChooser();
        if (directory != null && Files.exists(Paths.get(directory))) {
            fc.setCurrentDirectory(new java.io.File(directory));
        }
        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fc.setAcceptAllFileFilterUsed(false);
        int returnVal = fc.showOpenDialog(MainWindow.this);
        if (returnVal == JFileChooser.APPROVE_OPTION) {
            PreferencesHelper.setPathToDirectory(MainWindow.class, fc.getSelectedFile().toString());
            System.out.println("getSelectedFile() : "
                    +  fc.getSelectedFile());
            directory = fc.getSelectedFile().toString();
        }
    };
    ActionListener parseLogsAndBuildTree = e -> {
        try {
            activeObjects = DataParser.parseData(directory);
            buildTree();
        }
        catch (WrongLogFileFormat wff){
            JOptionPane.showMessageDialog(null, wff.getMessage());
        }
    };
    private void discoverMinimumAndMaximum(){
        long minimumTime = Long.MAX_VALUE;
        long maximumTime = 0;
        for (ActiveObject activeObject:activeObjects){
            for (ActiveObjectThread thread:activeObject.getThreads()){
                for (ThreadEvent threadEvent:thread.getEvents()){
                    if (threadEvent.getStartTime() < minimumTime){
                        minimumTime = threadEvent.getStartTime();
                    }
                    if (threadEvent.getFinishTime() > maximumTime){
                        maximumTime = threadEvent.getFinishTime();
                    }
                }
            }
        }
        sizeHelper = new SizeHelper(minimumTime, maximumTime, scaleSlider.getValue());
    }
    private void buildTree(){
        discoverMinimumAndMaximum();


        GridBagLayout gridBagLayout = new GridBagLayout();
        scrollPaneRoot.setLayout(gridBagLayout);
        GridBagConstraints constraints;
        for (ActiveObject activeObject:activeObjects){

            ActiveObjectTitlePanel titlePanel = new ActiveObjectTitlePanel(activeObject.getIdentifier());
            constraints = new GridBagConstraints();
            constraints.weightx = 0.0;
            constraints.fill = GridBagConstraints.NONE;
            constraints.gridwidth = 1;
            constraints.gridheight = activeObject.getThreads().size() * 2 + 1;
            gridBagLayout.setConstraints(titlePanel, constraints);
            scrollPaneRoot.add(titlePanel);

            EmptyRow emptyRow1 = new EmptyRow(10);
            constraints = new GridBagConstraints();
            constraints.gridwidth = GridBagConstraints.REMAINDER;
            gridBagLayout.setConstraints(emptyRow1, constraints);
            scrollPaneRoot.add(emptyRow1);

            for (ActiveObjectThread thread:activeObject.getThreads()){

                ThreadTitlePanel threadTitlePanel = new ThreadTitlePanel(thread.getThreadId() + "");
                constraints = new GridBagConstraints();
                constraints.weightx = 0.0;
                constraints.fill = GridBagConstraints.NONE;
                gridBagLayout.setConstraints(threadTitlePanel, constraints);
                scrollPaneRoot.add(threadTitlePanel);

                ThreadFlowPanel flowPanel = new ThreadFlowPanel(sizeHelper);
                flowPanels.add(flowPanel);
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

        revalidate();
        repaint();
    }
}
