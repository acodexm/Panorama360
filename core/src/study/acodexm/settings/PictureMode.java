package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum PictureMode {
    auto,
    multithreaded,
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

    public static PictureMode intToEnum(int i) {
        switch (i) {
            case 0: {
                return auto;
            }
            case 1: {
                return multithreaded;
            }
            case 2: {
                return panorama;
            }
            case 3: {
                return widePicture;
            }
            case 4: {
                return picture360;
            }
            default: {
                return auto;
            }
        }
    }
}
