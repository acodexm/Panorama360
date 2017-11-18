package acodexm.panorama;


import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import study.acodexm.DeviceCameraControl;

import static android.content.Context.SENSOR_SERVICE;

public class Movement implements SensorEventListener {
    CameraFragment mCameraFragment;
    float mLastX = 0, mLastY = 0, mLastZ = 0;
    private SensorManager mSensorManager;
    private Sensor mSensor;
    private DeviceCameraControl rotationVector = new AndroidDeviceCameraController(mCameraFragment);
//    private float[] rotationMatrix = rotationVector.getValues();

    public Movement(CameraFragment cameraFragment) {
        mCameraFragment = cameraFragment;
//        mSensorManager = (SensorManager) mContext.getSystemService(SENSOR_SERVICE);
//        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mSensorManager = (SensorManager) mCameraFragment.getContext().getSystemService(SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);
////        mSensorManager.registerListener(new SensorEventListener() {
//
//            @Override
//            public void onSensorChanged(SensorEvent event) {
//                if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR){
//                    SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values);
//                    rotationVector.updateRotationVector(rotationMatrix);
//                }
////                float x = event.values[0];
////                float y = event.values[1];
////                float z = event.values[2];
////                mLastX += x;
////                mLastY += y;
////                mLastZ += z;
////                Log.d("SENSOR", "X:" + String.format("%.4f", mLastX) + " Y:" + String.format("%.4f", mLastY));
//            }
//
//            @Override
//            public void onAccuracyChanged(Sensor sensor, int accuracy) {
//            }
//        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//            SensorManager.getRotationMatrixFromVector(rotationMatrix, sensorEvent.values);
//            rotationVector.updateRotationVector(rotationMatrix);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
