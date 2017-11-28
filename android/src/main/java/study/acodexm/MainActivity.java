package study.acodexm;


import android.app.ProgressDialog;
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
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import acodexm.panorama.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import study.acodexm.Utils.LOG;
import study.acodexm.control.AndroidRotationVector;
import study.acodexm.control.AndroidSettingsControl;
import study.acodexm.control.CameraControl;
import study.acodexm.control.ViewControl;
import study.acodexm.settings.ActionMode;
import study.acodexm.settings.PictureMode;
import study.acodexm.settings.PictureQuality;
import study.acodexm.settings.SettingsControl;
import study.acodexm.settings.UserPreferences;

public class MainActivity extends AndroidApplication implements SensorEventListener, ViewControl, NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    @BindView(R.id.capture)
    ImageView captureBtn;
    @BindView(R.id.open_gallery)
    ImageView galleryBtn;
    //    @BindView(R.id.save)
    Button saveBtn;
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
    @BindView(R.id.save_dir)
    TextView mSaveDir;
    private SurfaceView mSurfaceView;
    private GLSurfaceView glView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private RotationVector rotationVector = new AndroidRotationVector();
    private SettingsControl mSettingsControl = new AndroidSettingsControl();
    private float[] rotationMatrix = rotationVector.getValues();
    private List<Mat> listImage = new ArrayList<>();
    private ProgressDialog ringProgressDialog;
    private CameraControl mCameraControl;
    private ShutterState mShutterState;
    private UserPreferences mPreferences;

//    };

    //    private Camera mCam;
//    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
//        public void onPictureTaken(byte[] data, Camera camera) {
//            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
//            Matrix matrix = new Matrix();
//            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//                    bitmap.getHeight(), matrix, false);
//            Mat mat = new Mat();
//            Utils.bitmapToMat(bitmap, mat);
//            listImage.add(mat);
////            Canvas canvas = null;
////            try {
////                canvas = glView.getHolder().lockCanvas(null);
////                synchronized (glView.getHolder()) {
////                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
////                    float scale = 1.0f * mSurfaceView.getHeight() / bitmap.getHeight();
////                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap,
////                            (int) (scale * bitmap.getWidth()), mSurfaceView.getHeight(), false);
////                    Paint paint = new Paint();
////                    paint.setAlpha(200);
////                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
////                }
////            } catch (Exception e) {
////                e.printStackTrace();
////            } finally {
////                if (canvas != null) {
////                    glView.getHolder().unlockCanvasAndPost(canvas);
////                }
////            }
//            mCam.startPreview();
//            safeToTakePicture = true;
//        }
//    };
//    private boolean isPreview;
//    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
//        @Override
//        public void surfaceCreated(SurfaceHolder holder) {
//            try {
//                mCam.setPreviewDisplay(holder);
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        @Override
//        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//            Camera.Parameters myParameters = mCam.getParameters();
//            Camera.Size myBestSize = getBestPreviewSize(myParameters);
//            if (myBestSize != null) {
//                myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
//                mCam.setParameters(myParameters);
//                mCam.setDisplayOrientation(0);
//                mCam.startPreview();
//                isPreview = true;
//            }
//        }
//
//        @Override
//        public void surfaceDestroyed(SurfaceHolder holder) {
//        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mCameraControl = new CameraSurface(this);
        mSurfaceView = mCameraControl.getSurface();
        FrameLayout layout = new FrameLayout(getContext());
        View view2 = LayoutInflater.from(getContext()).inflate(R.layout.activity_main, layout, false);
        mSensorManager = (SensorManager) getContext().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        AndroidApplicationConfiguration cfg = new AndroidApplicationConfiguration();
        cfg.useGyroscope = true;
        cfg.useAccelerometer = false;
        cfg.useCompass = false;
        cfg.r = 8;
        cfg.g = 8;
        cfg.b = 8;
        cfg.a = 8;
        initializeForView(new AndroidCamera(rotationVector, mCameraControl.getSphereControl(), mSettingsControl), cfg);

        if (graphics.getView() instanceof GLSurfaceView) {
            Log.d(TAG, "creating layout");
            glView = (GLSurfaceView) graphics.getView();
            glView.setZOrderMediaOverlay(true);
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setKeepScreenOn(true);
            layout.addView(mSurfaceView);
            layout.addView(glView);
            layout.addView(view2);
        }
        setContentView(layout);
        ButterKnife.bind(this);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        mPreferences = new UserPreferences(this);
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
        } else {
            super.onBackPressed();
        }
    }

    public void post(Runnable r) {
        handler.post(r);
    }

    private void processPicture() {
        try {
            int images = listImage.size();
            Log.d(TAG, "Pictures taken:" + images);
            long[] tempObjAddress = new long[images];
            for (int i = 0; i < images; i++) {
                tempObjAddress[i] = listImage.get(i).getNativeObjAddr();
            }
            Mat result = new Mat();
            // Call the OpenCV C++ Code to perform stitching process
            NativePanorama.processPanorama(tempObjAddress, result.getNativeObjAddr());
            // Save the image to external storage
            savePicture(result);
            showToastRunnable("picture saved");
            listImage.clear();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void savePicture(Mat result) {
        File sdcard = Environment.getExternalStorageDirectory();
        final String fileName = sdcard.getAbsolutePath() + "/PanoramaApp/opencv_" +
                System.currentTimeMillis() + ".png";
        Imgcodecs.imwrite(fileName, result);
    }

    public void showToastRunnable(final String message) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "File saved at: " +
                        message, Toast.LENGTH_LONG).show();
            }
        };
        post(r);
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void showProcessingDialog() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mCameraControl.stopPreview();
                ringProgressDialog = ProgressDialog.show(MainActivity.this, "",
                        "Panorama", true);
                ringProgressDialog.setCancelable(false);
            }
        };
        post(r);
    }

    public void hideProcessingDialog() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mCameraControl.startPreview();
                ringProgressDialog.dismiss();
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
                break;
            case HIGH:
                mSwitchHigh.setChecked(true);
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
            case capture:
                captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.capture));
                break;
        }
    }

    private void onCaptureBtnClickAction() {
        switch (mShutterState) {
            case ready:
                showToast("ready");
                switch (mSettingsControl.getActionMode()) {
                    case FullAuto:
                        captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.rec));
                        showToast("FullAuto");
                        break;
                    case Manual:
                        showToast("Manual");
                        captureBtn.setBackground(ContextCompat.getDrawable(this, R.drawable.capture));
                        break;
                }
                mShutterState = ShutterState.capture;
                break;

            case recording:
                showToast("recording");
                switch (mSettingsControl.getPictureMode()) {
                    case auto:
                        showToast("auto");
                        break;
                    case panorama:
                        showToast("panorama");
                        break;
                    case widePicture:
                        showToast("widePicture");
                        break;
                    case picture360:
                        showToast("picture360");
                        break;
                }
                mShutterState = ShutterState.ready;
                break;
            case capture:
                showToast("capture");
//                takePhotoManualy();
                mShutterState = ShutterState.ready;
                break;
        }
        setCaptureBtnImage();
    }

    @OnClick(R.id.capture)
    void captureOnClickListener() {
        onCaptureBtnClickAction();
//        mCameraControl.takePicture(-1);
    }

    @OnClick(R.id.open_gallery)
    void onGalleryClickAction() {
        showToast("open gallery");
    }

    @OnClick(R.id.save_picture)
    void saveOnClickListener() {
        showToast("save");
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                showProcessingDialog();
//                processPicture();
//                hideProcessingDialog();
//            }
//        };
//        post(r);
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

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        return false;
    }

    @OnClick(R.id.mode_auto)
    void onSwitchAuto() {
        mPreferences.setActionMode(ActionMode.FullAuto);
        mSettingsControl.setActionMode(ActionMode.FullAuto);
        mSwitchManual.setChecked(false);
        setCaptureBtnImage();
    }

    @OnClick(R.id.mode_manual)
    void onSwitchManual() {
        mPreferences.setActionMode(ActionMode.Manual);
        mSettingsControl.setActionMode(ActionMode.Manual);
        mSwitchAuto.setChecked(false);
        setCaptureBtnImage();
    }

    @OnClick(R.id.picture_auto)
    void onSwitchAutoPanorama() {
        mPreferences.setPictureMode(PictureMode.auto);
        mSettingsControl.setPictureMode(PictureMode.auto);
        mSwitchPanorama.setChecked(false);
        mSwitchWide.setChecked(false);
        mSwitch360.setChecked(false);
    }

    @OnClick(R.id.picture_panorama)
    void onSwitchPanorama() {
        mPreferences.setPictureMode(PictureMode.panorama);
        mSettingsControl.setPictureMode(PictureMode.panorama);
        mSwitchAutoPicture.setChecked(false);
        mSwitchWide.setChecked(false);
        mSwitch360.setChecked(false);
    }

    @OnClick(R.id.picture_wide)
    void onSwitchWide() {
        mPreferences.setPictureMode(PictureMode.widePicture);
        mSettingsControl.setPictureMode(PictureMode.widePicture);
        mSwitchAutoPicture.setChecked(false);
        mSwitchPanorama.setChecked(false);
        mSwitch360.setChecked(false);
    }

    @OnClick(R.id.picture_360)
    void onSwitch360() {
        mPreferences.setPictureMode(PictureMode.picture360);
        mSettingsControl.setPictureMode(PictureMode.picture360);
        mSwitchAutoPicture.setChecked(false);
        mSwitchWide.setChecked(false);
        mSwitchPanorama.setChecked(false);
    }

    @OnClick(R.id.quality_high)
    void onSwitchHigh() {
        mPreferences.setPictureQuality(PictureQuality.HIGH);
        mSettingsControl.setPictureQuality(PictureQuality.HIGH);
        mSwitchLow.setChecked(false);
    }

    @OnClick(R.id.quality_low)
    void onSwitchLow() {
        mPreferences.setPictureQuality(PictureQuality.LOW);
        mSettingsControl.setPictureQuality(PictureQuality.LOW);
        mSwitchHigh.setChecked(false);
    }

    private enum ShutterState {
        ready, capture, recording;

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