package study.acodexm.settings;


import study.acodexm.Utils.LOG;

public enum PictureMode {
    auto,
    multithreaded,
    panorama,
    widePicture,
    picture360,
    test;

    public static PictureMode stringToEnum(String s) {
        try {
            return valueOf(s);
        } catch (Exception e) {
            LOG.e("PictureMode", "string casting failed", e);
            return auto;
        }
    }

    public static String[] getValues() {
        return new String[]{auto.name(), multithreaded.name(), panorama.name(), widePicture.name(), picture360.name(), test.name()};
    }

    public static PictureMode intToEnum(int i) {
        switch (i) {
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
            case 5: {
                return test;
            }
            default: {
                return auto;
            }
        }
    }

    public static int enumToInt(PictureMode i) {
        switch (i) {
            case multithreaded: {
                return 1;
            }
            case panorama: {
                return 2;
            }
            case widePicture: {
                return 3;
            }
            case picture360: {
                return 4;
            }
            case test: {
                return 5;
            }
            default: {
                return 0;
            }
        }
    }
}
