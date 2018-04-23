package study.acodexm.control;


import java.util.List;

import study.acodexm.CameraSurface;
import study.acodexm.SphereControl;

public interface CameraControl {
    void takePicture(int id);

    void takePicture2(PicturePosition position);

    void startPreview();

    void stopPreview();

    CameraSurface getSurface();

    SphereControl getSphereControl();

    List<Integer> getIdsTable();

}
