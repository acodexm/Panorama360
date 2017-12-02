package study.acodexm;


import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import study.acodexm.control.AndroidSphereControl;
import study.acodexm.control.CameraControl;
import study.acodexm.control.ViewControl;
import study.acodexm.settings.SettingsControl;

public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, CameraControl {
    private static final String TAG = CameraSurface.class.getSimpleName();
    List<Mat> listOfPictureLayers = new ArrayList<>();
    private Map<Integer, byte[]> mPictures;
    private List<Integer> ids;
    private Camera camera;
    private Map<Integer, Mat> listImageL1 = new TreeMap<>();
    private Map<Integer, Mat> listImageL2 = new TreeMap<>();
    private Map<Integer, Mat> listImageL3 = new TreeMap<>();
    private Map<Integer, Mat> listImageL4 = new TreeMap<>();
    private Map<Integer, Mat> listImageL5 = new TreeMap<>();
    private boolean safeToTakePicture = false;
    private SphereControl mSphereControl;
    private SettingsControl mSettingsControl;
    private int currentPictureId;
    private int PHOTO_WIDTH;
    private int PHOTO_HEIGHT;
    private ViewControl mViewControl;
    private int LAT = AndroidCamera.LAT;
    private int LON = AndroidCamera.LON;

    @SuppressLint("UseSparseArrays")
    public CameraSurface(MainActivity activity, SettingsControl settingsControl) {
        super(activity.getContext());
        mViewControl = activity;
        mSettingsControl = settingsControl;
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
        Log.d(TAG, "surfaceChanged called");
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
        Runnable processTexture = new Runnable() {
            @Override
            public void run() {
                mViewControl.showProcessingDialog();
                mPictures.put(currentPictureId, resizeImage(bytes));
                mSphereControl.setPictures(mPictures);
                mViewControl.hideProcessingDialog();
                mViewControl.updateRender();
                safeToTakePicture = true;
            }
        };
        mViewControl.post(processTexture);

        Runnable processPicture = new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Matrix matrix = new Matrix();
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                        bitmap.getHeight(), matrix, false);
                Mat mat = new Mat();
                Utils.bitmapToMat(bitmap, mat);
                addPictures(mat);
            }
        };

        mViewControl.post(processPicture);
        camera.startPreview();
        Log.d(TAG, "onPictureTaken process time: " + (System.currentTimeMillis() - time));

    }

    private void addPictures(Mat mat) {
        switch (mSettingsControl.getPictureMode()) {
            case auto:
                listImageL1.put(currentPictureId, mat);
                break;
            case panorama:
                if (currentPictureId > LAT && currentPictureId <= 2 * LAT)
                    listImageL1.put(currentPictureId, mat);
                else if (currentPictureId > 2 * LAT + 1 && currentPictureId <= 3 * LAT + 1)
                    listImageL2.put(currentPictureId, mat);
                else if (currentPictureId > 3 * LAT + 2 && currentPictureId <= 4 * LAT + 2)
                    listImageL3.put(currentPictureId, mat);
                else if (currentPictureId > 4 * LAT + 3 && currentPictureId <= 5 * LAT + 3)
                    listImageL4.put(currentPictureId, mat);
                else if (currentPictureId > 5 * LAT + 4 && currentPictureId <= 6 * LAT + 4)
                    listImageL5.put(currentPictureId, mat);
                break;
            case widePicture:
                if (currentPictureId > LAT && currentPictureId <= 2 * LAT)
                    listImageL1.put(currentPictureId, mat);
                else if (currentPictureId > 2 * LAT + 1 && currentPictureId <= 3 * LAT + 1)
                    listImageL2.put(currentPictureId, mat);
                else if (currentPictureId > 3 * LAT + 2 && currentPictureId <= 4 * LAT + 2)
                    listImageL3.put(currentPictureId, mat);
                else if (currentPictureId > 4 * LAT + 3 && currentPictureId <= 5 * LAT + 3)
                    listImageL4.put(currentPictureId, mat);
                else if (currentPictureId > 5 * LAT + 4 && currentPictureId <= 6 * LAT + 4)
                    listImageL5.put(currentPictureId, mat);
                break;
            case picture360:
                listImageL1.put(currentPictureId, mat);
                break;
        }
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

    @Override
    public List<Mat> getPictureList() {
        switch (mSettingsControl.getPictureMode()) {
            case auto:
                listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
                break;
            case panorama:
                chooseLongestList();
                break;
            case widePicture:
                choosePicturesForWide();
                break;
            case picture360:
                if (listImageL1.size() == LAT * LON)
                    listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
                break;
        }
        return listOfPictureLayers;
    }

    private void choosePicturesForWide() {
        if (listImageL1.size() > 2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
        if (listImageL2.size() > 2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL2.values()));
        if (listImageL3.size() > 2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL3.values()));
        if (listImageL4.size() > 2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL4.values()));
        if (listImageL5.size() > 2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL5.values()));
    }

    private void chooseLongestList() {
        int L1 = listImageL1.size();
        int L2 = listImageL2.size();
        int L3 = listImageL3.size();
        int L4 = listImageL4.size();
        int L5 = listImageL5.size();
        int max = chooseLongest(new ArrayList<>(Arrays.asList(
                L1, L2, L3, L4, L5)));
        if (max == L1)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
        if (max == L2)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL2.values()));
        if (max == L3)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL3.values()));
        if (max == L4)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL4.values()));
        if (max == L5)
            listOfPictureLayers.addAll(new ArrayList<>(listImageL5.values()));
    }

    private int chooseLongest(List<Integer> integers) {
        int max = 0;
        for (int i : integers) {
            if (i > max)
                max = i;
        }
        return max;
    }

}
