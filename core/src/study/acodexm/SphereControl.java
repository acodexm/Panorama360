package study.acodexm;


import java.util.List;

public interface SphereControl {
    void autoTakePicture(int id);

    byte[] getPicture();

    void setPicture(byte[] picture);

    int getLastPosition();

    void setLastPosition(int position);

    List<Integer> getIdTable();

    void setIdTable(List<Integer> ids);

    List<Integer> getTakenPicturesIds();

}
