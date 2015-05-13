package utils;

/**
 * Created by Paul on 05/05/15.
 */
public class SettingsHelper {

    private static SettingsHelper settingsHelper;

    public static SettingsHelper instance() {
        if (settingsHelper == null) {
            settingsHelper = new SettingsHelper();
        }
        return settingsHelper;
    }
}
