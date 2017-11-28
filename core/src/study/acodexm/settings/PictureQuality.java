package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum PictureQuality {
    LOW, HIGH;

    public static PictureQuality stringToEnum(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            LOG.e("PictureQuality", "string casting failed", e);
            return LOW;
        }
    }
}
