package utils;

import enums.OrderingPolicyEnum;

import java.util.prefs.Preferences;

/**
 * Created by pkhvoros on 3/16/15.
 */
public class PreferencesHelper {
    private static final String defaultDirectoryKey = "defaultDirectoryKey";
    private static final String reOrderingAllowedKey = "reOrderingAllowedKey";
    private static final String numberOfDialogsKey = "numberOfDialogsKey";

    public static OrderingPolicyEnum getReorderingPolicy(Class senderClass) {
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        int value = prefs.getInt(reOrderingAllowedKey, OrderingPolicyEnum.getDefaultValue());
        return OrderingPolicyEnum.getOrderingPolicyByValue(value);
    }
    public static void saveOrderingPolicy(Class senderClass, OrderingPolicyEnum policyEnum){
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        prefs.putInt(reOrderingAllowedKey, OrderingPolicyEnum.getValueByOrderingPolicy(policyEnum));
    }
    public static int getNumberOfDialogs(Class senderClass){
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        return prefs.getInt(numberOfDialogsKey, 3);
    }
    public static void saveNumberOfDialogs(Class senderClass, int numberOfDialogs){
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        prefs.putInt(numberOfDialogsKey, numberOfDialogs);
    }
    public static String getPathToDirectory(Class senderClass) {
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        return prefs.get(defaultDirectoryKey, null);
    }

    public static void setPathToDirectory(Class senderClass, String value) {
        Preferences prefs = Preferences.userNodeForPackage(senderClass);
        prefs.put(defaultDirectoryKey, value);
    }

}
