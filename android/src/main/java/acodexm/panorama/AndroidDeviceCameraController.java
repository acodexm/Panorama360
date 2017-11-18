package acodexm.panorama;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.util.Log;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.Toast;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.List;

import study.acodexm.DeviceCameraControl;

public class AndroidDeviceCameraController implements DeviceCameraControl,
        PictureCallback, Camera.AutoFocusCallback {
    private static final String TAG = AndroidDeviceCameraController.class.getSimpleName();
    private static final int ONE_SECOND_IN_MILI = 1000;
    private final CameraFragment mCameraFragment;
    private CameraSurface cameraSurface;
    private byte[] pictureData;
    private boolean safeToTakePicture = true;
//    private Movement mMovement;

    public AndroidDeviceCameraController(CameraFragment mCameraFragment) {
        this.mCameraFragment = mCameraFragment;
//        mMovement = new Movement();
    }

    public void setCameraParametersForPicture(Camera camera) {
        Log.d(TAG, "setCameraParametersForPicture");
        Camera.Parameters p = camera.getParameters();
        List<Camera.Size> supportedSizes = p.getSupportedPictureSizes();
        int maxSupportedWidth = -1;
        int maxSupportedHeight = -1;
        for (Camera.Size size : supportedSizes) {
            if (size.width > maxSupportedWidth) {
                maxSupportedWidth = size.width;
                maxSupportedHeight = size.height;
            }
        }
        p.setPictureSize(maxSupportedWidth, maxSupportedHeight);
        p.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        camera.setParameters(p);
    }

    public synchronized void showToast(final String message) {
        Log.d(TAG, "showToast");
        Runnable r = new Runnable() {
            public void run() {
                Toast.makeText(mCameraFragment.getContext(), message, Toast.LENGTH_SHORT).show();
            }
        };
        mCameraFragment.post(r);
    }

    //AutoFocusCallback
    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        // Focus process finished, we now have focus (or not)
        if (success) {
            if (camera != null && safeToTakePicture) {
                Log.d(TAG, "autofocus take picture ");
                safeToTakePicture = false;

                camera.takePicture(null, null, null, this);

            }
        }
    }

    //PictureCallback
    @Override
    public void onPictureTaken(byte[] pictureData, Camera camera) {
        Log.d(TAG, "onPictureTaken");
        this.pictureData = pictureData;
    }

    //DeviceCameraControl
    @Override
    public synchronized void prepareCamera() {
        mCameraFragment.setFixedSize(960, 640);
        if (cameraSurface == null) {
            cameraSurface = new CameraSurface(mCameraFragment.getContext());
        }
        mCameraFragment.getActivity().addContentView(cameraSurface, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

    }

    @Override
    public synchronized void startPreview() {
        if (cameraSurface != null && cameraSurface.getCamera() != null) {
            cameraSurface.getCamera().startPreview();
        }
    }

    @Override
    public synchronized void stopPreview() {
        if (cameraSurface != null) {
            ViewParent parentView = cameraSurface.getParent();
            if (parentView instanceof ViewGroup) {
                ViewGroup viewGroup = (ViewGroup) parentView;
                viewGroup.removeView(cameraSurface);
            }
            if (cameraSurface.getCamera() != null) {
                cameraSurface.getCamera().stopPreview();
            }
        }
        mCameraFragment.restoreFixedSize();
    }

    @Override
    public synchronized void startPreviewAsync() {
        Log.d(TAG, "startPreviewAsync");
        Runnable r = new Runnable() {
            public void run() {
                startPreview();
            }
        };
        mCameraFragment.post(r);
    }

    @Override
    public synchronized void stopPreviewAsync() {
        Log.d(TAG, "stopPreviewAsync");
        Runnable r = new Runnable() {
            public void run() {
                stopPreview();
            }
        };
        mCameraFragment.post(r);
    }

    @Override
    public synchronized void prepareCameraAsync() {
        Log.d(TAG, "prepareCameraAsync");
        Runnable r = new Runnable() {
            public void run() {
                prepareCamera();
            }
        };
        mCameraFragment.post(r);
    }

//    @Override
//    public float getChangeX() {
//        return (float) Math.round(mMovement.mLastX * 10) / 10;
//    }
//
//    @Override
//    public float getChangeY() {
//        return (float) Math.round(mMovement.mLastY * 10) / 10;
//    }
//
//    @Override
//    public float getChangeZ() {
//        return (float) Math.round(mMovement.mLastZ * 10) / 10;
//    }

    @Override
    public void takePicture() {
        setCameraParametersForPicture(cameraSurface.getCamera());
        cameraSurface.getCamera().autoFocus(this);
    }

    @Override
    public synchronized byte[] getPictureData() {
        return pictureData;
    }

    @Override
    public void saveAsJpeg() {

        SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        FileHandle pictureFile = Gdx.files.external(
                "PanoramaApp/"
                        + sDateFormat.format(new java.util.Date())
                        + "_cameraPhoto.jpg");

        Bitmap bitmap = BitmapFactory.decodeByteArray(pictureData, 0, pictureData.length);
        Matrix matrix = new Matrix();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);

        long time = System.currentTimeMillis();
        FileOutputStream fos;
        try {
            fos = new FileOutputStream(pictureFile.file());
            bitmap.compress(CompressFormat.JPEG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        showToast("Picture saved");
        safeToTakePicture = true;
        Log.d(TAG, "saveAsJpeg processing time: " + (System.currentTimeMillis() - time));

    }

    @Override
    public boolean isReady() {
        return cameraSurface != null && cameraSurface.getCamera() != null;
    }
}
