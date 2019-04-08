package study.acodexm.settings;


public interface SettingsControl {
    PictureMode getPictureMode();

    void setPictureMode(PictureMode pictureMode);

    ActionMode getActionMode();

    void setActionMode(ActionMode actionMode);

    PictureQuality getPictureQuality();

    void setPictureQuality(PictureQuality pictureQuality);

    GridSize getGridSize();

    void setGridSize(GridSize gridSize);
}
