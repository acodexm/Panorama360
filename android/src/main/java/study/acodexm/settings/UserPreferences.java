package study.acodexm.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;

import java.io.File;


public class UserPreferences {
    private static final String PREF_ACTION_MODE = "action_mode";
    private static final String PREF_PICTURE_MODE = "picture_mode";
    private static final String PREF_PICTURE_QUALITY = "picture_quality";
    private static final String PREF_GRID_LAT = "grid_lat";
    private static final String PREF_GRID_LON = "grid_lon";
    private static final String PREF_SAVE_DIR = "save_dir";
    private static final String APP_PREF = "panorama_application";
    private final SharedPreferences mPreferences;

    public UserPreferences(Context context) {
        this.mPreferences = context.getSharedPreferences(APP_PREF, Context.MODE_PRIVATE);
    }

    public ActionMode getActionMode() {
        return ActionMode.stringToEnum(mPreferences.getString(PREF_ACTION_MODE, ActionMode.FullAuto.name()));
    }

    public void setActionMode(ActionMode actionMode) {
        mPreferences.edit().putString(PREF_ACTION_MODE, actionMode.name()).apply();
    }

    public PictureMode getPictureMode() {
        return PictureMode.stringToEnum(mPreferences.getString(PREF_PICTURE_MODE, PictureMode.auto.name()));
    }

    public void setPictureMode(PictureMode pictureMode) {
        mPreferences.edit().putString(PREF_PICTURE_MODE, pictureMode.name()).apply();
    }

    public PictureQuality getPictureQuality() {
        return PictureQuality.stringToEnum(mPreferences.getString(PREF_PICTURE_QUALITY, PictureQuality.LOW.name()));
    }

    public void setPictureQuality(PictureQuality pictureQuality) {
        mPreferences.edit().putString(PREF_PICTURE_QUALITY, pictureQuality.name()).apply();
    }

    public String getSaveDir() {
        File sdcard = Environment.getExternalStorageDirectory();
        return mPreferences.getString(PREF_SAVE_DIR, sdcard.getAbsolutePath() + "/PanoramaApp");
    }

    public void setSaveDir(String path) {
        mPreferences.edit().putString(PREF_SAVE_DIR, path).apply();
    }

    public void clearAll() {
        mPreferences.getAll().clear();
    }

    public int getLat() {
        return mPreferences.getInt(PREF_GRID_LAT, 10);
    }

    public int getLon() {
        return mPreferences.getInt(PREF_GRID_LON, 7);
    }

    public void setLat(int lat) {
        mPreferences.edit().putInt(PREF_GRID_LAT, lat).apply();
    }

    public void setLon(int lon) {
        mPreferences.edit().putInt(PREF_GRID_LON, lon).apply();
    }
}
