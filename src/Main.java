import org.apache.commons.io.FileUtils;
import views.MainWindow;

import java.io.File;
import java.io.IOException;

/**
 * Created by pkhvoros on 3/13/15.
 */

public class Main {
    private static final String windowTitle = "Viewer tool for ProActive";
    public static void main(String[] args){
        MainWindow mainWindow = new MainWindow(windowTitle);
    }
    private static void cleanDirectory(String directory){
        try {
            FileUtils.cleanDirectory(new File(directory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
