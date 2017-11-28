package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum PictureMode {
    auto,
    panorama,
    widePicture,
    picture360;

    public static PictureMode stringToEnum(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            LOG.e("PictureMode", "string casting failed", e);
            return auto;
        }
    }
}
