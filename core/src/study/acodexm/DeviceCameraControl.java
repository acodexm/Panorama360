package study.acodexm;

public interface DeviceCameraControl {

    // Synchronous interface
    void prepareCamera();

    void startPreview();

    void stopPreview();

    void takePicture();

    byte[] getPictureData();

    // Asynchronous interface - need when called from a non platform thread (GDXOpenGl thread)
    void startPreviewAsync();

    void stopPreviewAsync();

    void saveAsJpeg();

    boolean isReady();

    void prepareCameraAsync();

    float getChangeX();

    float getChangeY();

    float getChangeZ();
}