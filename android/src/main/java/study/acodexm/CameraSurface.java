package study.acodexm;


import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Surface;
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
import study.acodexm.utils.LOG;

@SuppressWarnings("deprecation")
public class CameraSurface extends SurfaceView implements SurfaceHolder.Callback, Camera.PictureCallback, Camera.AutoFocusCallback, CameraControl {
    private static final String TAG = CameraSurface.class.getSimpleName();
    private Handler handler;
    private Camera camera;
    private CameraSurface thisCamera;
    private byte[] mPicture;
    private boolean safeToTakePicture = false;
    private ViewControl mViewControl;
    private SphereControl mSphereControl;
    private SettingsControl mSettingsControl;
    private int PHOTO_WIDTH;
    private int PHOTO_HEIGHT;
    private Camera.Size highestRes;
    private Camera.Size lowRes;
    private Camera.Size lowestRes;
    private Context context;

    public CameraSurface(Context context) {
        super(context);
        thisCamera = this;
    }

    public CameraSurface(Context context, AttributeSet attrs) {
        super(context, attrs);
        thisCamera = this;
    }

    public CameraSurface(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        thisCamera = this;
    }

    public CameraSurface(MainActivity activity, SettingsControl settingsControl) {
        super(activity.getContext());
        context = activity.getContext();
        mViewControl = activity;
        mSettingsControl = settingsControl;
        getHolder().addCallback(this);
        getHolder().setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        mSphereControl = new AndroidSphereControl(this);
        //this section sets height and width variables for resizing image for textures on sphere
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        if (windowManager != null) {
            windowManager.getDefaultDisplay().getMetrics(metrics);
            PHOTO_HEIGHT = metrics.heightPixels / 4;
            PHOTO_WIDTH = metrics.widthPixels / 4;
        }
        handler = new Handler();
        thisCamera = this;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        camera = Camera.open(0);
        try {
            camera.setPreviewDisplay(holder);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * here are the camera settings such as preview size or picture resolution and quality
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        LOG.d(TAG, "surfaceChanged called");
        Camera.Parameters myParameters = camera.getParameters();
        Camera.Size myBestSize = getBestPreviewSize(myParameters);
        getBestPictureSize(myParameters);
        int orientation = setCameraDisplayOrientation(context);
        if (myBestSize != null) {
            myParameters.setPreviewSize(myBestSize.width, myBestSize.height);
            myParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
            switch (mSettingsControl.getPictureQuality()) {
                case NORMAL:
                    myParameters.set("jpeg-quality", 70);
                    myParameters.setRotation(orientation);
                    myParameters.setPictureFormat(PixelFormat.JPEG);
                    myParameters.setPictureSize(lowRes.width, lowRes.height);
                    break;
                case LOW:
                    myParameters.set("jpeg-quality", 70);
                    myParameters.setRotation(orientation);
                    myParameters.setPictureFormat(PixelFormat.JPEG);
                    myParameters.setPictureSize(lowestRes.width, lowestRes.height);
                    break;
                case HIGH:
                    myParameters.set("jpeg-quality", 70);
                    myParameters.setRotation(orientation);
                    myParameters.setPictureFormat(PixelFormat.JPEG);
                    myParameters.setPictureSize(highestRes.width, highestRes.height);
                    break;
            }

            camera.setParameters(myParameters);
            camera.setDisplayOrientation(orientation);
            camera.startPreview();
        }
        safeToTakePicture = true;
    }

    private static int getDeviceCurrentOrientation(Context context) {
        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int rotation = windowManager.getDefaultDisplay().getRotation();
        LOG.d("Utils", "Current orientation = " + rotation);
        return rotation;
    }

    private int setCameraDisplayOrientation(Context context) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(0, info); // Use the first rear-facing camera
        int rotation = getDeviceCurrentOrientation(context);

        int degrees;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
                degrees = 0;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }
        return result;
    }


    /**
     * this method finds the best resolution for preview for current device
     */
    private Camera.Size getBestPreviewSize(Camera.Parameters parameters) {
        Camera.Size bestSize;
        List<Camera.Size> previewSizes = parameters.getSupportedPreviewSizes();
        bestSize = previewSizes.get(0);
        for (int i = 1; i < previewSizes.size(); i++) {
            if ((previewSizes.get(i).width * previewSizes.get(i).height) >
                    (bestSize.width * bestSize.height)) {
                bestSize = previewSizes.get(i);
            }
        }
        return bestSize;
    }

    /**
     * this method finds highest, low, and lowest picture resolutions
     */
    private void getBestPictureSize(Camera.Parameters parameters) {
        List<Camera.Size> pictureSizes = parameters.getSupportedPictureSizes();
        highestRes = pictureSizes.get(0);
        lowRes = pictureSizes.get(pictureSizes.size() / 2);
        lowestRes = pictureSizes.get(pictureSizes.size() - 1);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        camera.stopPreview();
        camera.release();
        camera = null;
    }

    /**
     * this is a trigger method for taking picture
     */
    @Override
    public void takePicture() {
        if (camera != null && safeToTakePicture) {
            safeToTakePicture = false;
            safeAutoFocus();
        }
    }


    public void safeAutoFocus() {
        try {
            camera.autoFocus(thisCamera);
        } catch (RuntimeException e) {
            scheduleAutoFocus();
            safeToTakePicture = true;
            LOG.s(TAG, "Take picture failed", e);
        }
    }

    private void scheduleAutoFocus() {
        handler.postDelayed(() -> {
            if (camera != null && safeToTakePicture) {
                safeToTakePicture = false;
                safeAutoFocus();
            }
        }, 500);
    }

    /**
     * this method makes sure that taken picture is in focus
     */
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        LOG.d(TAG, "onAutoFocus: " + success);
        if (success && camera != null)
            camera.takePicture(null, null, this);
    }

    /**
     * this method saves taken picture to external storage and sends it to sphere for a texture needs
     */
    @Override
    public void onPictureTaken(final byte[] bytes, Camera camera) {
        long time = System.currentTimeMillis();

        Runnable saveImage = () -> ImageRW.saveImageExternal(bytes, PicturePosition.getInstance(mSettingsControl.getGridSize().getLAT(), mSettingsControl.getGridSize().getLON(), false).calculateCurrentPosition());
        mViewControl.post(saveImage);

        Runnable processTexture = () -> {
            mPicture = resizeImage(bytes);
            mSphereControl.setPicture(mPicture);
            mViewControl.updateRender();
            safeToTakePicture = true;
        };
        mViewControl.post(processTexture);

        camera.startPreview();
        LOG.s(TAG, "onPictureTaken process time: " + (System.currentTimeMillis() - time));

    }


    /**
     * this method resize image to a given resolution
     */
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
