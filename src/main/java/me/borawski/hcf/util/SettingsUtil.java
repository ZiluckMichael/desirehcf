package me.borawski.hcf.util;

import me.borawski.hcf.session.Session;

public class SettingsUtil {

    /**
     * Only for use of true/false settings only!!!
     *
     * @param s
     * @param setting
     */
    public static void toggleSetting(Session s, String setting) {
        if (s.getSettings().get(setting).equals("true")) {
            s.getSettings().put(setting, "false");
        } else if (s.getSettings().get(setting).equals("false")) {
            s.getSettings().put(setting, "true");
        }
    }

}
