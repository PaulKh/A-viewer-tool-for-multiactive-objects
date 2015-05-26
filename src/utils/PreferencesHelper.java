package utils;

import enums.OrderingPolicyEnum;
import views.MainWindow;

import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class PreferencesHelper {
    private static final String defaultDirectoryKey = "defaultDirectoryKey";
    private static final String reOrderingAllowedKey = "reOrderingAllowedKey";
    private static final String viewRepositioningAllowedKey = "viewRepositioningAllowedKey";
    private static final String numberOfDialogsKey = "numberOfDialogsKey";

    public static OrderingPolicyEnum getReorderingPolicy() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        int value = prefs.getInt(reOrderingAllowedKey, OrderingPolicyEnum.getDefaultValue());
        return OrderingPolicyEnum.getOrderingPolicyByValue(value);
    }

    public static void saveOrderingPolicy(OrderingPolicyEnum policyEnum) {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        prefs.putInt(reOrderingAllowedKey, OrderingPolicyEnum.getValueByOrderingPolicy(policyEnum));
    }
    public static boolean isRepositioningAllowed() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        return prefs.getBoolean(viewRepositioningAllowedKey, true);
    }

    public static void setRepositioningAllowed(Boolean isAllowed) {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        prefs.putBoolean(viewRepositioningAllowedKey, isAllowed);
    }
    public static int getNumberOfDialogs() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        return prefs.getInt(numberOfDialogsKey, 0);
    }

    public static void saveNumberOfDialogs(int numberOfDialogs) {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        prefs.putInt(numberOfDialogsKey, numberOfDialogs);
    }

    public static String getPathToDirectory() {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        return prefs.get(defaultDirectoryKey, null);
    }

    public static void setPathToDirectory(String value) {
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        prefs.put(defaultDirectoryKey, value);
    }
    public static void clearAllPreferences(){
        Preferences prefs = Preferences.userNodeForPackage(MainWindow.class);
        try {
            prefs.clear();
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

}
