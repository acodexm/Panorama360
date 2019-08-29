package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum PictureMode {
    OPEN_CV_DEFAULT,
    AUTO,
    MULTITHREADED,
    PANORAMA,
    WIDE_PICTURE,
    PICTURE_360,
    TEST;

    public static PictureMode stringToEnum(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            LOG.e("PictureMode", "string casting failed", e);
            return AUTO;
        }
    }

    public static String[] getValues() {
        return new String[]{AUTO.name(), MULTITHREADED.name(), PANORAMA.name(), WIDE_PICTURE.name(), PICTURE_360.name(), TEST.name(), OPEN_CV_DEFAULT.name()};
    }

    public static PictureMode intToEnum(int i) {
        switch (i) {
            case 1: {
                return MULTITHREADED;
            }
            case 2: {
                return PANORAMA;
            }
            case 3: {
                return WIDE_PICTURE;
            }
            case 4: {
                return PICTURE_360;
            }
            case 5: {
                return TEST;
            }
            case 6: {
                return OPEN_CV_DEFAULT;
            }
            default: {
                return AUTO;
            }
        }
    }

    public static int enumToInt(PictureMode i) {
        switch (i) {
            case MULTITHREADED: {
                return 1;
            }
            case PANORAMA: {
                return 2;
            }
            case WIDE_PICTURE: {
                return 3;
            }
            case PICTURE_360: {
                return 4;
            }
            case TEST: {
                return 5;
            }
            case OPEN_CV_DEFAULT: {
                return 6;
            }
            default: {
                return 0;
            }
        }
    }
}
