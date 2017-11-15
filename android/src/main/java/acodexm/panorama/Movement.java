package acodexm.panorama;


import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class Movement {
    Context mContext;
    float mLastX = 0, mLastY = 0, mLastZ = 0;
    private SensorManager mSensorManager;
    private Sensor mSensor;

    public Movement(Context context) {
        mContext = context;

        mSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        mSensorManager.registerListener(new SensorEventListener() {

            @Override
            public void onSensorChanged(SensorEvent event) {
                float x = event.values[0];
                float y = event.values[1];
                float z = event.values[2];
                mLastX += x;
                mLastY += y;
                mLastZ += z;
//                Log.d("SENSOR", "X:" + String.format("%.4f", mLastX) + " Y:" + String.format("%.4f", mLastY));
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
            }
        }, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }
}
