package study.acodexm;


public class NativePanorama {
    public native static void processPanorama(long[] imageAddressArray, long outputAddress, String[] stringArray);

    public native static void cropPanorama(long imageAddress, long outputAddress);

    public native static int getProgress();
}
