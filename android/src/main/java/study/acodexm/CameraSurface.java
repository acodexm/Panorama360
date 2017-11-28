package study.acodexm;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import study.acodexm.control.AndroidSphereControl;
import study.acodexm.control.CameraControl;
import study.acodexm.control.ViewControl;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, CameraControl {
    private Map<Integer, byte[]> mPictures;
    private List<Integer> ids;
    private Camera camera;
    private List<Mat> listImage = new ArrayList<>();
    private boolean safeToTakePicture = false;
    private AndroidSphereControl mSphereControl;
    private int currentPictureId;
    private int PHOTO_WIDTH;
    private int PHOTO_HEIGHT;
    private ViewControl mViewControl;

    @SuppressLint("UseSparseArrays")
    public CameraSurface(MainActivity activity) {
        super(activity.getContext());
        mViewControl = activity;
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSphereControl = new AndroidSphereControl(this);
        mPictures = new HashMap<>();
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
            PHOTO_HEIGHT = metrics.heightPixels / 4;
            PHOTO_WIDTH = metrics.widthPixels / 4;
        }
    }


    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(0);
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        Camera.Parameters myParameters = camera.getParameters();
        Camera.Size myBestSize = getBestPreviewSize(myParameters);
        if (myBestSize != null) {
            myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
//            myParameters.setPictureFormat(ImageFormat.RGB_565);
            camera.setParameters(myParameters);
            camera.setDisplayOrientation(0);
            camera.startPreview();
        }
        safeToTakePicture = true;
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

    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }


    @Override
    public void onPictureTaken(final byte[] bytes, Camera camera) {
        long time = System.currentTimeMillis();
//        mPictures.put(currentPictureId, bytes);
        Runnable r = new Runnable() {
            @Override
            public void run() {
                mViewControl.showProcessingDialog();
                mPictures.put(currentPictureId, resizeImage(bytes));
                mSphereControl.setPictures(mPictures);
                mViewControl.hideProcessingDialog();
                safeToTakePicture = true;
            }
        };
        post(r);
//        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//        Matrix matrix = new Matrix();
//        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
//                bitmap.getHeight(), matrix, false);
//        Mat mat = new Mat();
//        Utils.bitmapToMat(bitmap, mat);
//        listImage.add(mat);
//        camera.startPreview();
        System.out.println("onPictureTaken process time: " + (System.currentTimeMillis() - time));

    }

    private byte[] resizeImage(byte[] bytes) {
        Bitmap original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, false);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.PNG, 0, blob);
        return blob.toByteArray();
    }

    @Override
    public void takePicture(int id) {
        if (camera != null && safeToTakePicture) {
            if (ids == null)
                ids = mSphereControl.getIdTable();
            currentPictureId = id;
            safeToTakePicture = false;
            camera.takePicture(null, null, this);
        }
    }

    @Override
    public void startPreview() {
        if (camera != null)
            camera.startPreview();
    }

    @Override
    public void stopPreview() {
        if (camera != null)
            camera.stopPreview();
    }

    @Override
    public CameraSurface getSurface() {
        return this;
    }

    @Override
    public SphereControl getSphereControl() {
        return mSphereControl;
    }
}
