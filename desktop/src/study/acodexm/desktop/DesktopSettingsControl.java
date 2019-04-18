package study.acodexm.desktop;


import study.acodexm.settings.ActionMode;
import study.acodexm.settings.GridSize;
import study.acodexm.settings.PictureMode;
import study.acodexm.settings.PictureQuality;
import study.acodexm.settings.SettingsControl;

public class DesktopSettingsControl implements SettingsControl {
    private PictureQuality mPictureQuality;
    private ActionMode mActionMode;
    private PictureMode mPictureMode;
    private GridSize mGridSize = new GridSize(7, 5);

    @Override
    public PictureMode getPictureMode() {
        return mPictureMode;
    }

    @Override
    public void setPictureMode(PictureMode pictureMode) {
        mPictureMode = pictureMode;
    }

    @Override
    public ActionMode getActionMode() {
        return mActionMode;
    }

    @Override
    public void setActionMode(ActionMode actionMode) {
        mActionMode = actionMode;
    }

    @Override
    public PictureQuality getPictureQuality() {
        return mPictureQuality;
    }

    @Override
    public void setPictureQuality(PictureQuality pictureQuality) {
        mPictureQuality = pictureQuality;
    }

    @Override
    public GridSize getGridSize() {
        return mGridSize;
    }

    @Override
    public void setGridSize(GridSize gridSize) {
        mGridSize = gridSize;
    }
}

