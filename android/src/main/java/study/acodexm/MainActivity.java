package study.acodexm;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.badlogic.gdx.backends.android.AndroidApplication;
import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import acodexm.panorama.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AndroidApplication implements SensorEventListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }

    @BindView(R.id.capture)
    Button captureBtn;
    @BindView(R.id.save)
    Button saveBtn;
    //    @BindView(R.id.camera_surface)
    SurfaceView mSurfaceView;
    //    @BindView(R.id.libgdx_surface)
    GLSurfaceView glView;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private RotationVector rotationVector = new AndroidRotationVector();
    private float[] rotationMatrix = rotationVector.getValues();
    private List<Mat> listImage = new ArrayList<>();
    private ProgressDialog ringProgressDialog;
    private boolean safeToTakePicture = true;
    private Camera mCam;
    private int origWidth;
    private int origHeight;
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            listImage.add(mat);
//            Canvas canvas = null;
//            try {
//                canvas = glView.getHolder().lockCanvas(null);
//                synchronized (glView.getHolder()) {
//                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
//                    float scale = 1.0f * mSurfaceView.getHeight() / bitmap.getHeight();
//                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap,
//                            (int) (scale * bitmap.getWidth()), mSurfaceView.getHeight(), false);
//                    Paint paint = new Paint();
//                    paint.setAlpha(200);
//                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
//                }
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (canvas != null) {
//                    glView.getHolder().unlockCanvasAndPost(canvas);
//                }
//            }
            mCam.startPreview();
            safeToTakePicture = true;
        }
    };
    private boolean isPreview;
    SurfaceHolder.Callback mSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                mCam.setPreviewDisplay(holder);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            Camera.Parameters myParameters = mCam.getParameters();
            Camera.Size myBestSize = getBestPreviewSize(myParameters);
            if (myBestSize != null) {
                myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
                mCam.setParameters(myParameters);
                mCam.setDisplayOrientation(0);
                mCam.startPreview();
                isPreview = true;
            }
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
        }
    };

    private Runnable imageProcessingRunnable = new Runnable() {
        @Override
        public void run() {
            showProcessingDialog();
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
                File sdcard = Environment.getExternalStorageDirectory();
                final String fileName = sdcard.getAbsolutePath() + "/PanoramaApp/opencv_" +
                        System.currentTimeMillis() + ".png";
                Imgcodecs.imwrite(fileName, result);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "File saved at: " +
                                fileName, Toast.LENGTH_LONG).show();
                    }
                });
                listImage.clear();
            } catch (Exception e) {
                e.printStackTrace();
            }
            closeProcessingDialog();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isPreview = false;
        mSurfaceView = new SurfaceView(getContext());
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);
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

        DeviceCameraControl cameraControl = new AndroidDeviceCameraController(this);

        initializeForView(new AndroidCamera(cameraControl, rotationVector), cfg);

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
        origWidth = graphics.getWidth();
        origHeight = graphics.getHeight();
        setContentView(layout);
        ButterKnife.bind(this);
    }

    public void post(Runnable r) {
        handler.post(r);
    }

    public void setFixedSize(int width, int height) {
        Log.d("setFixedSize", "began");
        if (graphics.getView() instanceof GLSurfaceView) {
            Log.d("setFixedSize", "if statement");
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.getHolder().setFixedSize(width, height);
            glView.setZOrderMediaOverlay(true);
        }
    }

    public void restoreFixedSize() {
        Log.d("restoreFixedSize", "began");
        if (graphics.getView() instanceof GLSurfaceView) {
            Log.d("restoreFixedSize", "if statement");
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.getHolder().setFixedSize(origWidth, origHeight);
            glView.setZOrderMediaOverlay(true);
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        mCam = Camera.open(0);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isPreview) {
            mCam.stopPreview();
        }
        mCam.release();
        mCam = null;
        isPreview = false;
    }

    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize;
        List<Camera.Size> sizeList = parameters.getSupportedPreviewSizes();
        bestSize = sizeList.get(0);
        for (int i = 1; i < sizeList.size(); i++) {
            if ((sizeList.get(i).width * sizeList.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = sizeList.get(i);
            }
        }
        return bestSize;
    }

    private void showProcessingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCam.stopPreview();
                ringProgressDialog = ProgressDialog.show(MainActivity.this, "",
                        "Panorama", true);
                ringProgressDialog.setCancelable(false);
            }
        });
    }

    private void closeProcessingDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mCam.startPreview();
                ringProgressDialog.dismiss();
            }
        });
    }

    @OnClick(R.id.capture)
    void captureOnClickListener() {
        if (mCam != null && safeToTakePicture) {
            safeToTakePicture = false;
            mCam.takePicture(null, null, jpegCallback);
        }
    }

    @OnClick(R.id.save)
    void saveOnClickListener() {
        Thread thread = new Thread(imageProcessingRunnable);
        thread.start();
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