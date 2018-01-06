package study.acodexm;


interface SphereManualControl {
    int canTakePicture();

    boolean isCameraSteady();

    void startRendering();

    void stopRendering();

    void updateRender();
}
