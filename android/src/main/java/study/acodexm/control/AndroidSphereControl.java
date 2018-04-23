package study.acodexm.control;

import java.util.ArrayList;
import java.util.List;

import study.acodexm.SphereControl;

public class AndroidSphereControl implements SphereControl {
    private byte[] mPicture;
    private List<Integer> ids;
    private CameraControl mCameraControl;
    private int position;
    private PicturePosition mPosition;
    private List<Integer> takenPicturesIds;
    private List<PicturePosition> takenPictures;

    public AndroidSphereControl(CameraControl cameraControl) {
        mCameraControl = cameraControl;
        takenPicturesIds = new ArrayList<>();
        takenPictures = new ArrayList<>();
    }

    @Override
    public void autoTakePicture(int id) {
        mCameraControl.takePicture(id);
        this.takenPicturesIds.add(id);
    }

    @Override
    public void autoTakePicture2(String position) {
        PicturePosition picturePosition = new PicturePosition(position);
        mCameraControl.takePicture2(picturePosition);
        this.takenPictures.add(picturePosition);
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
    public void setSphereDimensions(int x, int y) {

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
