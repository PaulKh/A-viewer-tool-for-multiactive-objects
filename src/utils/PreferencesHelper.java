package utils;

import java.util.prefs.Preferences;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class PreferencesHelper {
    private static final String defaultDirectoryKey = "defaultDirectoryKey";

    public static String getPathToDirectory(Class senderClass) {
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        return prefs.get(defaultDirectoryKey, null);
    }

    public static void setPathToDirectory(Class senderClass, String value) {
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        prefs.put(defaultDirectoryKey, value);
    }

}
