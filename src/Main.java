import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;

/**
 * Created by pkhvoros on 3/13/15.
 */

public class Main {
    public static void main(String[] args){
        if(args != null || args.length > 0){
            DataParser.parseData(args[0]);
//            cleanDirectory(args[0]);
        }
    }
    private static void cleanDirectory(String directory){
        try {
            FileUtils.cleanDirectory(new File(directory));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
