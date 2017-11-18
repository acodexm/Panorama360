package acodexm.panorama;


import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Button;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();

    static {
        System.loadLibrary("opencv_java3");
        System.loadLibrary("MyLib");
    }


    @BindView(R.id.capture)
    Button captureBtn;
    @BindView(R.id.save)
    Button saveBtn;
    //    @BindView(R.id.surfaceView)
    SurfaceView mSurfaceView;
    //    @BindView(R.id.surfaceViewOnTop)
    SurfaceView mSurfaceViewOnTop;
    private List<Mat> listImage = new ArrayList<>();
    private ProgressDialog ringProgressDialog;
    private boolean safeToTakePicture = true;
    private Camera mCam;
    Camera.PictureCallback jpegCallback = new Camera.PictureCallback() {
        public void onPictureTaken(byte[] data, Camera camera) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
            Matrix matrix = new Matrix();
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                    bitmap.getHeight(), matrix, false);
            Mat mat = new Mat();
            Utils.bitmapToMat(bitmap, mat);
            listImage.add(mat);
            Canvas canvas = null;
            try {
                canvas = mSurfaceViewOnTop.getHolder().lockCanvas(null);
                synchronized (mSurfaceViewOnTop.getHolder()) {
                    canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                    float scale = 1.0f * mSurfaceView.getHeight() / bitmap.getHeight();
                    Bitmap scaleImage = Bitmap.createScaledBitmap(bitmap,
                            (int) (scale * bitmap.getWidth()), mSurfaceView.getHeight(), false);
                    Paint paint = new Paint();
                    paint.setAlpha(200);
                    canvas.drawBitmap(scaleImage, -scaleImage.getWidth() * 2 / 3, 0, paint);
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (canvas != null) {
                    mSurfaceViewOnTop.getHolder().unlockCanvasAndPost(canvas);
                }
            }
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
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        isPreview = false;
        mSurfaceView.getHolder().addCallback(mSurfaceCallback);
        mSurfaceViewOnTop.setZOrderOnTop(true);
        mSurfaceViewOnTop.getHolder().setFormat(PixelFormat.TRANSPARENT);
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
}