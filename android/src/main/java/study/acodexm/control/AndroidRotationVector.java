package study.acodexm.control;

import study.acodexm.RotationVector;

public class AndroidRotationVector implements RotationVector {

    private float[] values = new float[16];

    /**
     * constructor that creates vector that represents 4x4 matrix with ones on diagonal
     */
    public AndroidRotationVector() {
        for (int i = 0; i < values.length; i++) {
            values[i] = 0;
        }
        values[0] = 1;
        values[5] = 1;
        values[10] = 1;
        values[15] = 1;
    }

    @Override
    public void updateRotationVector(float[] values) {
        this.values = values;
    }

    @Override
    public float[] getValues() {
        return values;
    }

}
