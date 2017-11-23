package study.acodexm;


import java.util.List;
import java.util.Map;

public interface SphereControl {
    void autoTakePicture(int id);

    Map<Integer, byte[]> getPictures();

    void setPictures(Map<Integer, byte[]> pictures);

    List<Integer> getIdTable();

    void setIdTable(List<Integer> ids);

}
