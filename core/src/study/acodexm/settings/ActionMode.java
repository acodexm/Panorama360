package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum ActionMode {
    FullAuto, Manual, Test;

    public static ActionMode stringToEnum(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            LOG.e("ActionMode", "string casting failed", e);
            return FullAuto;
        }
    }
}
