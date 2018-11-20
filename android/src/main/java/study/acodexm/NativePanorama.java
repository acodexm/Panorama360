package study.acodexm;


public class NativePanorama {
    public native static void processPanorama(long[] imageAddressArray, long outputAddress, boolean isCropped);
}
