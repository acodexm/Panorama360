package study.acodexm.control;


import study.acodexm.settings.ActionMode;
import study.acodexm.settings.PictureMode;
import study.acodexm.settings.PictureQuality;
import study.acodexm.settings.SettingsControl;

public class AndroidSettingsControl implements SettingsControl {
    private PictureQuality mPictureQuality;
    private ActionMode mActionMode;
    private PictureMode mPictureMode;

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
}
