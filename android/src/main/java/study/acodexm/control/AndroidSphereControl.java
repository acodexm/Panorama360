package study.acodexm.control;

import java.util.List;

import study.acodexm.SphereControl;

public class AndroidSphereControl implements SphereControl {
    private byte[] mPicture;
    private List<Integer> ids;
    private CameraControl mCameraControl;
    private int position;

    public AndroidSphereControl(CameraControl cameraControl) {
        mCameraControl = cameraControl;
    }

    @Override
    public void autoTakePicture() {
        mCameraControl.takePicture();
    }


    @Override
    public byte[] getPicture() {
        return mPicture;
    }

    @Override
    public void setPicture(byte[] picture) {
        this.mPicture = picture;
    }

}
