package study.acodexm;


import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.opencv.core.Mat;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import acodexm.panorama.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import study.acodexm.Utils.LOG;
import study.acodexm.control.AndroidRotationVector;
import study.acodexm.control.AndroidSettingsControl;
import study.acodexm.control.CameraControl;
import study.acodexm.control.ViewControl;
import study.acodexm.gallery.GalleryActivity;
import study.acodexm.settings.ActionMode;
import study.acodexm.settings.PictureMode;
import study.acodexm.settings.PictureQuality;
import study.acodexm.settings.SettingsControl;
import study.acodexm.settings.UserPreferences;
import study.acodexm.utils.ImageChooser;
import study.acodexm.utils.ImageRW;

public class MainActivity extends AndroidApplication implements SensorEventListener, ViewControl, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    @BindView(R.id.capture)
    ImageView captureBtn;
    @BindView(R.id.refresh_picture)
    ImageView refreshBtn;
    @BindView(R.id.open_gallery)
    ImageView galleryBtn;
    @BindView(R.id.mode_auto)
    Switch mSwitchAuto;
    @BindView(R.id.mode_manual)
    Switch mSwitchManual;
    @BindView(R.id.picture_panorama)
    Switch mSwitchPanorama;
    @BindView(R.id.picture_auto)
    Switch mSwitchAutoPicture;
    @BindView(R.id.picture_wide)
    Switch mSwitchWide;
    @BindView(R.id.picture_360)
    Switch mSwitch360;
    @BindView(R.id.quality_high)
    Switch mSwitchHigh;
    @BindView(R.id.quality_low)
    Switch mSwitchLow;
    @BindView(R.id.quality_very_low)
    Switch mSwitchVeryLow;
    @BindView(R.id.save_dir)
    TextView mSaveDir;
    @BindView(R.id.steady_shot)
    ProgressBar mProgressBar;
    private SurfaceView mSurfaceView;
    private GLSurfaceView glView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private RotationVector rotationVector = new AndroidRotationVector();
    private SettingsControl mSettingsControl = new AndroidSettingsControl();
    private float[] rotationMatrix = rotationVector.getValues();
    private CameraControl mCameraControl;
    private ShutterState mShutterState;
    private UserPreferences mPreferences;
    private SphereManualControl mManualControl;
    private boolean test = true;
    private boolean isCompressed;
    private boolean onBackBtnPressed = false;
    private int DOUBLE_BACK_PRESSED_DELAY = 2500;
    private boolean isNotSaving = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraControl = new CameraSurface(this, mSettingsControl);
        //getting camera surface view
        mSurfaceView = mCameraControl.getSurface();
        FrameLayout layout = new FrameLayout(getContext());
        //crating main view from activity_main layout
        View mainView = LayoutInflater.from(getContext()).inflate(R.layout.activity_main, layout, false);
        // setting up gyroscope
        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
        //creating and configuring new instance of LibGDX spherical view
        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGyroscope = true;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;
        AndroidCamera androidCamera = new AndroidCamera(rotationVector, mCameraControl.getSphereControl(),
                mSettingsControl);
        mManualControl = androidCamera;
        //initializing LibGDX spherical view
        initializeForView(androidCamera, cfg);
        if (graphics.getView() instanceof GLSurfaceView) {
            Log.d(TAG, "creating layout");
            glView = (GLSurfaceView) graphics.getView();
            glView.setZOrderMediaOverlay(true);
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setKeepScreenOn(true);
            layout.addView(mSurfaceView);
            layout.addView(glView);
            layout.addView(mainView);
        }
        //attach layout to view
        setContentView(layout);
        //injecting view components
        ButterKnife.bind(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //load preferences
        mPreferences = new UserPreferences(this);
        //delete files from temporary picture folder
        ImageRW.deleteTempFiles();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCameraControl.startPreview();
        mShutterState = ShutterState.ready;
        loadPreferences();
        setCaptureBtnImage();
    }

    @Override
    protected void onPause() {
        mCameraControl.stopPreview();
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (onBackBtnPressed) {
            Intent intent = new Intent(getApplicationContext(), WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("EXIT", true);
            startActivity(intent);
        } else {
            onBackBtnPressed = true;
            showToast(R.string.msg_exit);
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {
                    onBackBtnPressed = false;
                }
            }, DOUBLE_BACK_PRESSED_DELAY);
        }
    }


    public void post(Runnable r) {
        handler.post(r);
    }

    @Override
    public void updateRender() {
        mManualControl.updateRender();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
            rotationVector.updateRotationVector(rotationMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }

    /**
     * this method is executed on new Thread.
     * first depending on selected picture mode method loads selected pictures to be processed.
     * Next pictures are passed to native openCV stitcher to be processed.
     * If the stitching process is successful the picture is saved
     *
     * @param pictureMode
     */
    void processPicture(final PictureMode pictureMode) {
        showProcessingDialog();
        final Runnable r = new Runnable() {
            @Override
            public void run() {
                isNotSaving = false;
                final List<Mat> listImage;
                try {
                    listImage = ImageChooser.loadPictures(pictureMode,
                            mCameraControl.getIdsTable());
                } catch (Exception e) {
                    Log.e(TAG, "run: loadPictures failed", e);
                    return;
                }
                try {
                    int images = listImage.size();
                    if (images > 0) {
                        Log.d(TAG, "Pictures taken:" + images);
                        long[] tempObjAddress = new long[images];
                        for (int i = 0; i < images; i++) {
                            tempObjAddress[i] = listImage.get(i).getNativeObjAddr();
                        }

                        Mat result = new Mat();
                        // Call the OpenCV C++ Code to perform stitching process
                        try {
                            NativePanorama.processPanorama(tempObjAddress, result.getNativeObjAddr(), !isCompressed);
                            //save to external storage
                            boolean isSaved = false;
                            if (!result.empty())
                                isSaved = ImageRW.saveResultImageExternal(result);
                            showToastRunnable(getString(R.string.msg_is_saved) + isSaved);
                        } catch (Exception e) {
                            Log.e(TAG, "native processPanorama not working ", e);
                        }

                        for (Mat mat : listImage) mat.release();
                        listImage.clear();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                hideProcessingDialog();
                isNotSaving = true;
            }
        };
        new Thread(r).start();

    }

    public void showToastRunnable(final String message) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
            }
        };
        post(r);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    private void showToast(int message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /***
     * when picture is processed, to release more cpu and gpu power camera preview is stopped, and
     * additionally process circle is shown
     */
    public synchronized void showProcessingDialog() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mCameraControl.stopPreview();
                mProgressBar.setVisibility(View.VISIBLE);
            }
        };
        post(r);
    }

    public synchronized void hideProcessingDialog() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mCameraControl.startPreview();
                mProgressBar.setVisibility(View.GONE);
            }
        };
        post(r);
    }

    private void loadPreferences() {
        switch (mPreferences.getActionMode()) {
            case Manual:
                mSwitchManual.setChecked(true);
                break;
            case FullAuto:
                mSwitchAuto.setChecked(true);
                break;
        }
        switch (mPreferences.getPictureMode()) {
            case auto:
                mSwitchAutoPicture.setChecked(true);
                break;
            case panorama:
                mSwitchPanorama.setChecked(true);
                break;
            case widePicture:
                mSwitchWide.setChecked(true);
                break;
            case picture360:
                mSwitch360.setChecked(true);
                break;
        }
        switch (mPreferences.getPictureQuality()) {
            case LOW:
                mSwitchLow.setChecked(true);
                isCompressed = true;
                break;
            case VERY_LOW:
                mSwitchVeryLow.setChecked(true);
                isCompressed = true;
                break;
            case HIGH:
                mSwitchHigh.setChecked(true);
                isCompressed = false;
                break;
        }
        mSettingsControl.setActionMode(mPreferences.getActionMode());
        mSettingsControl.setPictureMode(mPreferences.getPictureMode());
        mSettingsControl.setPictureQuality(mPreferences.getPictureQuality());
        mSaveDir.setText(mPreferences.getSaveDir());
    }

    private void setCaptureBtnImage() {
        switch (mShutterState) {
            case ready:
                switch (mSettingsControl.getActionMode()) {
                    case FullAuto:
                        captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.ready_auto));
                        break;
                    case Manual:
                        captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.ready));
                        break;
                }
                break;
            case recording:
                captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.rec));
                break;

        }
    }

    private void onCaptureBtnClickAction() {
        switch (mShutterState) {
            case ready:
                switch (mSettingsControl.getActionMode()) {
                    case FullAuto:
                        mManualControl.startRendering();
                        mShutterState = ShutterState.recording;
                        break;
                    case Manual:
                        mManualControl.startRendering();
                        int position = mManualControl.canTakePicture();
                        if (position != -1)
                            mCameraControl.takePicture(position);
                        else showToast(getString(R.string.msg_take_picture_not_allowed));
                        break;
                }
                break;

            case recording:
                mManualControl.stopRendering();
                mShutterState = ShutterState.ready;
                break;

        }
        setCaptureBtnImage();
    }

    @OnClick(R.id.capture)
    void onCaptureClickListener() {
        if (isNotSaving)
            if (mShutterState == ShutterState.recording
                    || (mShutterState == ShutterState.ready
                    && mSettingsControl.getActionMode() == ActionMode.FullAuto))
                onCaptureBtnClickAction();
            else if (mManualControl.isCameraSteady()) {
                onCaptureBtnClickAction();
            } else showToast(getString(R.string.msg_do_not_move));
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.refresh_picture)
    void onRefreshClickListener() {
        if (isNotSaving) recreate();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.open_gallery)
    void onGalleryClickAction() {
        if (isNotSaving) {
            Intent intent = new Intent(MainActivity.this, GalleryActivity.class);
            String folder = Environment.getExternalStorageDirectory() + "/PanoramaApp";
            intent.putExtra(GalleryActivity.INTENT_EXTRAS_FOLDER, folder);
            startActivity(intent);
            showToast(getString(R.string.msg_open_gallery));
        } else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.save_picture)
    void saveOnClickListener() {
        if (isNotSaving) {
            showToast(getString(R.string.msg_save));
            switch (mSettingsControl.getPictureMode()) {
                case auto:
                    processPicture(PictureMode.auto);
                    break;
                case panorama:
                    processPicture(PictureMode.panorama);
                    break;
                case widePicture:
                    processPicture(PictureMode.widePicture);
                    break;
                case picture360:
                    processPicture(PictureMode.picture360);
                    break;
            }
        } else showToast(R.string.msg_wait);
    }

    /**
     * this section is responsible to manage side navigation settings list
     *
     * @param item
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @OnClick(R.id.mode_auto)
    void onSwitchAuto() {
        if (isNotSaving) {
            mPreferences.setActionMode(ActionMode.FullAuto);
            mSettingsControl.setActionMode(ActionMode.FullAuto);
            mSwitchManual.setChecked(false);
            setCaptureBtnImage();
            if (!mSwitchAuto.isChecked())
                mSwitchAuto.setChecked(true);
        } else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.mode_manual)
    void onSwitchManual() {
        if (isNotSaving)
            if (mSwitchAuto.isChecked()) {
                mPreferences.setActionMode(ActionMode.Manual);
                mSettingsControl.setActionMode(ActionMode.Manual);
                mSwitchAuto.setChecked(false);
                setCaptureBtnImage();
            } else onSwitchAuto();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.picture_auto)
    void onSwitchAutoPanorama() {
        if (isNotSaving) {
            mPreferences.setPictureMode(PictureMode.auto);
            mSettingsControl.setPictureMode(PictureMode.auto);
            mSwitchPanorama.setChecked(false);
            mSwitchWide.setChecked(false);
            mSwitch360.setChecked(false);
            if (!mSwitchAutoPicture.isChecked())
                mSwitchAutoPicture.setChecked(true);
        } else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.picture_panorama)
    void onSwitchPanorama() {
        if (isNotSaving)
            if (mSwitchPanorama.isChecked() || mSwitch360.isChecked() || mSwitchWide.isChecked()) {
                mPreferences.setPictureMode(PictureMode.panorama);
                mSettingsControl.setPictureMode(PictureMode.panorama);
                mSwitchAutoPicture.setChecked(false);
                mSwitchWide.setChecked(false);
                mSwitch360.setChecked(false);
            } else onSwitchAutoPanorama();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.picture_wide)
    void onSwitchWide() {
        if (isNotSaving)
            if (mSwitchPanorama.isChecked() || mSwitch360.isChecked() || mSwitchWide.isChecked()) {
                mPreferences.setPictureMode(PictureMode.widePicture);
                mSettingsControl.setPictureMode(PictureMode.widePicture);
                mSwitchAutoPicture.setChecked(false);
                mSwitchPanorama.setChecked(false);
                mSwitch360.setChecked(false);
            } else onSwitchAutoPanorama();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.picture_360)
    void onSwitch360() {
        if (isNotSaving)
            if (mSwitchPanorama.isChecked() || mSwitch360.isChecked() || mSwitchWide.isChecked()) {
                mPreferences.setPictureMode(PictureMode.picture360);
                mSettingsControl.setPictureMode(PictureMode.picture360);
                mSwitchAutoPicture.setChecked(false);
                mSwitchWide.setChecked(false);
                mSwitchPanorama.setChecked(false);
            } else onSwitchAutoPanorama();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.quality_high)
    void onSwitchHigh() {
        if (isNotSaving)
            if (mSwitchLow.isChecked() || mSwitchVeryLow.isChecked()) {
                mPreferences.setPictureQuality(PictureQuality.HIGH);
                mSettingsControl.setPictureQuality(PictureQuality.HIGH);
                mSwitchLow.setChecked(false);
                mSwitchVeryLow.setChecked(false);
                isCompressed = false;
                recreate();
            } else onSwitchLow();
        else showToast(R.string.msg_wait);
    }

    @OnClick(R.id.quality_low)
    void onSwitchLow() {
        if (isNotSaving) {
            mPreferences.setPictureQuality(PictureQuality.LOW);
            mSettingsControl.setPictureQuality(PictureQuality.LOW);
            mSwitchHigh.setChecked(false);
            mSwitchVeryLow.setChecked(false);
            isCompressed = true;
            if (!mSwitchLow.isChecked())
                mSwitchLow.setChecked(true);
            recreate();
        } else showToast(R.string.msg_wait);

    }

    @OnClick(R.id.quality_very_low)
    void onSwitchVeryLow() {
        if (isNotSaving) {
            if (mSwitchLow.isChecked() || mSwitchHigh.isChecked()) {
                mPreferences.setPictureQuality(PictureQuality.VERY_LOW);
                mSettingsControl.setPictureQuality(PictureQuality.VERY_LOW);
                mSwitchHigh.setChecked(false);
                mSwitchLow.setChecked(false);
                isCompressed = true;
                recreate();
            } else onSwitchLow();
        } else showToast(R.string.msg_wait);


    }

    private enum ShutterState {
        ready, recording;

        public static ShutterState stringToEnum(String s) {
            try {
                return valueOf(s);
            } catch (Exception e) {
                LOG.e("ShutterState", "string casting failed", e);
                return ready;
            }
        }
    }
}