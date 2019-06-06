package study.acodexm.control;

import study.acodexm.SphereControl;

public class AndroidSphereControl implements SphereControl {
    private byte[] mPicture;
    private CameraControl mCameraControl;

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
