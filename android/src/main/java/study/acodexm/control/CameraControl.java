package study.acodexm.control;


import study.acodexm.CameraSurface;
import study.acodexm.SphereControl;

public interface CameraControl {

    void takePicture();

    void startPreview();

    void stopPreview();

    CameraSurface getSurface();

    SphereControl getSphereControl();


}
