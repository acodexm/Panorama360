package study.acodexm.control;

import java.util.ArrayList;
import java.util.List;

import study.acodexm.SphereControl;

public class AndroidSphereControl implements SphereControl {
    private byte[] mPicture;
    private List<Integer> ids;
    private CameraControl mCameraControl;
    private int position;
    private List<Integer> takenPicturesIds;

    public AndroidSphereControl(CameraControl cameraControl) {
        mCameraControl = cameraControl;
        takenPicturesIds = new ArrayList<>();
    }

    @Override
    public void autoTakePicture(int id) {
        mCameraControl.takePicture(id);
        this.takenPicturesIds.add(id);
    }

    @Override
    public byte[] getPicture() {
        return mPicture;
    }

    @Override
    public void setPicture(byte[] picture) {
        this.mPicture = picture;
    }

    @Override
    public int getLastPosition() {
        return position;
    }

    @Override
    public void setLastPosition(int position) {
        this.position = position;

    }

    @Override
    public List<Integer> getIdTable() {
        return ids;
    }

    @Override
    public void setIdTable(List<Integer> ids) {
        this.ids = ids;
    }

    @Override
    public List<Integer> getTakenPicturesIds() {
        return takenPicturesIds;
    }

}
