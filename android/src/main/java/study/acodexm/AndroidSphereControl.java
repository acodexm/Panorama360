package study.acodexm;

import java.util.List;
import java.util.Map;

public class AndroidSphereControl implements SphereControl {
    private Map<Integer, byte[]> mPictures;
    private List<Integer> ids;
    private CameraControl mCameraControl;

    public AndroidSphereControl(CameraControl cameraControl) {
        mCameraControl = cameraControl;
    }

    @Override
    public void autoTakePicture(int id) {
        mCameraControl.takePicture(id);
    }

    @Override
    public Map<Integer, byte[]> getPictures() {
        return mPictures;
    }

    @Override
    public void setPictures(Map<Integer, byte[]> pictures) {
        this.mPictures = pictures;
    }

    @Override
    public List<Integer> getIdTable() {
        return ids;
    }

    @Override
    public void setIdTable(List<Integer> ids) {
        this.ids = ids;
    }

}
