package study.acodexm;


import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.badlogic.gdx.backends.android.AndroidApplicationConfiguration;
import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import acodexm.panorama.R;

import static android.content.Context.SENSOR_SERVICE;

public class CameraFragment extends AndroidFragmentApplication implements SensorEventListener {
    private static final String TAG = CameraFragment.class.getSimpleName();
    private int origWidth;
    private int origHeight;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private RotationVector rotationVector = new AndroidRotationVector();
    private float[] rotationMatrix = rotationVector.getValues();
    private CameraSurface cameraSurface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        RelativeLayout layout = new RelativeLayout(getContext());
        View view2 = LayoutInflater.from(getContext()).inflate(R.layout.camera_layout, layout, false);
//        View view = inflater.inflate(R.layout.camera_layout, container, false);
//        GLSurfaceView glSurfaceView=getActivity().findViewById(R.id.libgdx_view);
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

//        DeviceCameraControl cameraControl = new AndroidDeviceCameraController(this);

//        initializeForView(new AndroidCamera(cameraControl, rotationVector), cfg);

        if (graphics.getView() instanceof GLSurfaceView) {
            Log.d(TAG, "PACZ TU KRURWA");
            GLSurfaceView glView = (GLSurfaceView) graphics.getView();
            glView.setZOrderMediaOverlay(true);
            glView.getHolder().setFormat(PixelFormat.TRANSLUCENT);
            glView.setKeepScreenOn(true);
            layout.addView(glView);
            layout.addView(view2);
        }
        origWidth = graphics.getWidth();
        origHeight = graphics.getHeight();

        return layout;
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
