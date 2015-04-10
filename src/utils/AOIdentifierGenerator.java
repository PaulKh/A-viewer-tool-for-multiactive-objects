package utils;

/**
 * Created by pkhvoros on 3/31/15.
 */
public class AOIdentifierGenerator {
    private static int activeObjectIdentifierCounter = 0;
    public static String generateUniqueAOIdentifier(String identifier) {
        String delims = "[.]";
        String[] temp = identifier.split(delims);
        delims = "[_]";
        activeObjectIdentifierCounter++;
        return temp[temp.length - 1].split(delims)[0] + activeObjectIdentifierCounter;
    }
}
