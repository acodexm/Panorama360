package study.acodexm;


import java.util.List;

public interface SphereControl {
    void autoTakePicture(int id);

    void autoTakePicture2(String position);

    byte[] getPicture();

    void setPicture(byte[] picture);

    int getLastPosition();

    String getLastPosition2();

    void setLastPosition(int position);

    void setLastPosition2(String position);

    void setSphereDimensions(int x, int y);

    List<Integer> getTakenPicturesIds();

    List<Integer> getTakenPicturesIds();

}
