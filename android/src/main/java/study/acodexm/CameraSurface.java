package study.acodexm;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.ByteArrayOutputStream;
import java.util.List;

import study.acodexm.control.AndroidSphereControl;
import study.acodexm.control.CameraControl;
import study.acodexm.control.ViewControl;
import study.acodexm.settings.SettingsControl;
import study.acodexm.utils.ImageRW;

@SuppressWarnings("deprecation")
public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, CameraControl {
    private static final String TAG = CameraSurface.class.getSimpleName();
    private List<Integer> ids;
    private Camera camera;
    private byte[] mPicture;
    private boolean safeToTakePicture = false;
    private ViewControl mViewControl;
    private SphereControl mSphereControl;
    private SettingsControl mSettingsControl;
    private int currentPictureId;
    private int PHOTO_WIDTH;
    private int PHOTO_HEIGHT;

//    private List<Mat> listOfPictureLayers = new ArrayList<>();
//    private Map<Integer, Mat> listImageL1 = new TreeMap<>();
//    private Map<Integer, Mat> listImageL2 = new TreeMap<>();
//    private Map<Integer, Mat> listImageL3 = new TreeMap<>();
//    private Map<Integer, Mat> listImageL4 = new TreeMap<>();
//    private Map<Integer, Mat> listImageL5 = new TreeMap<>();
//    private int LAT = AndroidCamera.LAT;
//    private int LON = AndroidCamera.LON;

    //    @SuppressLint("UseSparseArrays")
    public CameraSurface(MainActivity activity, SettingsControl settingsControl) {
        super(activity.getContext());
        mViewControl = activity;
        mSettingsControl = settingsControl;
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSphereControl = new AndroidSphereControl(this);
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

        Runnable saveImage = new Runnable() {
            @Override
            public void run() {
                ImageRW.saveImageExternal(bytes, currentPictureId);
            }
        };
        mViewControl.post(saveImage);

        Runnable processTexture = new Runnable() {
            @Override
            public void run() {
                mViewControl.showProcessingDialog();
                mPicture = resizeImage(bytes);
                mSphereControl.setPicture(mPicture);
                mViewControl.hideProcessingDialog();
                mViewControl.updateRender();
                safeToTakePicture = true;
            }
        };
        mViewControl.post(processTexture);

        camera.startPreview();
        Log.d(TAG, "onPictureTaken process time: " + (System.currentTimeMillis() - time));

    }


    private byte[] resizeImage(byte[] bytes) {
        Bitmap original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, false);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.PNG, 0, blob);
        byte[] resizedImg = blob.toByteArray();
        resized.recycle();
        original.recycle();
        return resizedImg;
    }


    @Override
    public void takePicture(int id) {
        if (camera != null && safeToTakePicture) {
            if (ids == null)
                ids = mSphereControl.getIdTable();
            currentPictureId = id;
            mSphereControl.setLastPosition(id);
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
    public List<Integer> getIdsTable() {
        return mSphereControl.getTakenPicturesIds();
    }

    //    private void addPictures(Mat mat) {
//        switch (mSettingsControl.getPictureMode()) {
//            case auto:
//                listImageL1.put(currentPictureId, mat);
//                break;
//            case panorama:
//                if (currentPictureId > LAT && currentPictureId <= 2 * LAT)
//                    listImageL1.put(currentPictureId, mat);
//                else if (currentPictureId > 2 * LAT + 1 && currentPictureId <= 3 * LAT + 1)
//                    listImageL2.put(currentPictureId, mat);
//                else if (currentPictureId > 3 * LAT + 2 && currentPictureId <= 4 * LAT + 2)
//                    listImageL3.put(currentPictureId, mat);
//                else if (currentPictureId > 4 * LAT + 3 && currentPictureId <= 5 * LAT + 3)
//                    listImageL4.put(currentPictureId, mat);
//                else if (currentPictureId > 5 * LAT + 4 && currentPictureId <= 6 * LAT + 4)
//                    listImageL5.put(currentPictureId, mat);
//                break;
//            case widePicture:
//                if (currentPictureId > LAT && currentPictureId <= 2 * LAT)
//                    listImageL1.put(currentPictureId, mat);
//                else if (currentPictureId > 2 * LAT + 1 && currentPictureId <= 3 * LAT + 1)
//                    listImageL2.put(currentPictureId, mat);
//                else if (currentPictureId > 3 * LAT + 2 && currentPictureId <= 4 * LAT + 2)
//                    listImageL3.put(currentPictureId, mat);
//                else if (currentPictureId > 4 * LAT + 3 && currentPictureId <= 5 * LAT + 3)
//                    listImageL4.put(currentPictureId, mat);
//                else if (currentPictureId > 5 * LAT + 4 && currentPictureId <= 6 * LAT + 4)
//                    listImageL5.put(currentPictureId, mat);
//                break;
//            case picture360:
//                listImageL1.put(currentPictureId, mat);
//                break;
//        }
//    }
//    public List<Mat> getPictureList() {
//        switch (mSettingsControl.getPictureMode()) {
//            case auto:
//                listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
//                disposeImages(listImageL1);
//                break;
//            case panorama:
//                chooseLongestList();
//                break;
//            case widePicture:
//                choosePicturesForWide();
//                break;
//            case picture360:
//                if (listImageL1.size() == LAT * LON) {
//                    listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
//                    disposeImages(listImageL1);
//                }
//                break;
//        }
//        return listOfPictureLayers;
//    }
//    private void choosePicturesForWide() {
//        if (listImageL1.size() > 2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
//            disposeImages(listImageL1);
//        }
//        if (listImageL2.size() > 2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL2.values()));
//            disposeImages(listImageL2);
//        }
//        if (listImageL3.size() > 2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL3.values()));
//            disposeImages(listImageL3);
//        }
//        if (listImageL4.size() > 2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL4.values()));
//            disposeImages(listImageL4);
//        }
//        if (listImageL5.size() > 2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL5.values()));
//            disposeImages(listImageL5);
//        }
//    }
//
//    private void chooseLongestList() {
//        int L1 = listImageL1.size();
//        int L2 = listImageL2.size();
//        int L3 = listImageL3.size();
//        int L4 = listImageL4.size();
//        int L5 = listImageL5.size();
//        int max = chooseLongest(new ArrayList<>(Arrays.asList(
//                L1, L2, L3, L4, L5)));
//        if (max == L1) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL1.values()));
//            disposeImages(listImageL1);
//        }
//        if (max == L2) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL2.values()));
//            disposeImages(listImageL2);
//        }
//        if (max == L3) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL3.values()));
//            disposeImages(listImageL3);
//        }
//        if (max == L4) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL4.values()));
//            disposeImages(listImageL4);
//        }
//        if (max == L5) {
//            listOfPictureLayers.addAll(new ArrayList<>(listImageL5.values()));
//            disposeImages(listImageL5);
//        }
//    }
//    private int chooseLongest(List<Integer> integers) {
//        int max = 0;
//        for (int i : integers) {
//            if (i > max)
//                max = i;
//        }
//        return max;
//    }

//    private void disposeImages(Map<Integer, Mat> matMap) {
//        for (Mat mat : matMap.values()) mat.release();
//        matMap.clear();
//    }
}
