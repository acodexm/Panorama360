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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
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

public class MainActivity extends AndroidApplication implements SensorEventListener, ViewControl {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    @BindView(R.id.capture)
    Button captureBtn;
    @BindView(R.id.save)
    Button saveBtn;
    private SurfaceView mSurfaceView;
    private GLSurfaceView glView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private RotationVector rotationVector = new AndroidRotationVector();
    private float[] rotationMatrix = rotationVector.getValues();
    private List<Mat> listImage = new ArrayList<>();
    private ProgressDialog ringProgressDialog;
    private CameraControl mCameraControl;

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
//        mSurfaceView = new SurfaceView(getContext());
//        mSurfaceView.getHolder().addCallback(mSurfaceCallback);
        FrameLayout layout = new FrameLayout(getContext());
        View view2 = LayoutInflater.from(getContext()).inflate(R.layout.camera_layout, layout, false);
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
        initializeForView(new AndroidCamera(rotationVector, mCameraControl.getSphereControl()), cfg);

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

    }


    @Override
    protected void onResume() {
        super.onResume();
        mCameraControl.startPreview();

    }

    @Override
    protected void onPause() {
        mCameraControl.stopPreview();
        super.onPause();
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
            showToast("picture saved");
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


    public void showToast(final String message) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "File saved at: " +
                        message, Toast.LENGTH_LONG).show();
            }
        };
        post(r);
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

    @OnClick(R.id.capture)
    void captureOnClickListener() {
        mCameraControl.takePicture(-1);
    }

    @OnClick(R.id.save)
    void saveOnClickListener() {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                showProcessingDialog();
                processPicture();
                hideProcessingDialog();
            }
        };
        post(r);
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
}