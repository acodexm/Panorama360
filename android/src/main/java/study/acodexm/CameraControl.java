package study.acodexm;


public interface CameraControl {
    void takePicture(int id);

    void startPreview();

    void stopPreview();

    CameraSurface getSurface();

    SphereControl getSphereControl();

}
