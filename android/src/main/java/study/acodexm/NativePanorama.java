package study.acodexm;


public class NativePanorama {
    public native static void processPanorama(long[] imageAddressArray, long outputAddress, String[] stringArray);

    public native static int getProgress();
}
