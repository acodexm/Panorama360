package acodexm.panorama;


public class NativePanorama {
    public native static void processPanorama(long[] imageAddressArray,
                                              long outputAddress);
}
