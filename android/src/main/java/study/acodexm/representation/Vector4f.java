package study.acodexm.representation;

/**
 * Representation of a four-dimensional float-vector
 */
public class Vector4f {

    /**
     * The points.
     */
    protected float points[] = {0, 0, 0, 0};

    /**
     * Instantiates a new vector4f.
     */
    public Vector4f() {
        this.points[0] = 0;
        this.points[1] = 0;
        this.points[2] = 0;
        this.points[3] = 0;
    }

    /**
     * To array.
     *
     * @return the float[]
     */
    public float[] array() {
        return points;
    }

    public void copyVec4(Vector4f vec) {
        this.points[0] = vec.points[0];
        this.points[1] = vec.points[1];
        this.points[2] = vec.points[2];
        this.points[3] = vec.points[3];
    }

    /**
     * Multiply by scalar.
     *
     * @param scalar the scalar
     */
    public void multiplyByScalar(float scalar) {
        this.points[0] *= scalar;
        this.points[1] *= scalar;
        this.points[2] *= scalar;
        this.points[3] *= scalar;
    }

    public float dotProduct(Vector4f input) {
        return this.points[0] * input.points[0] + this.points[1] * input.points[1] + this.points[2] * input.points[2]
                + this.points[3] * input.points[3];
    }

    /**
     * Gets the x.
     *
     * @return the x
     */
    public float getX() {
        return this.points[0];
    }

    /**
     * Gets the y.
     *
     * @return the y
     */
    public float getY() {
        return this.points[1];
    }

    /**
     * Gets the z.
     *
     * @return the z
     */
    public float getZ() {
        return this.points[2];
    }

    /**
     * Gets the w.
     *
     * @return the w
     */
    public float getW() {
        return this.points[3];
    }

    /**
     * Sets the x.
     *
     * @param x the new x
     */
    public void setX(float x) {
        this.points[0] = x;
    }

    /**
     * Sets the y.
     *
     * @param y the new y
     */
    public void setY(float y) {
        this.points[1] = y;
    }

    /**
     * Sets the z.
     *
     * @param z the new z
     */
    public void setZ(float z) {
        this.points[2] = z;
    }

    /**
     * Sets the w.
     *
     * @param w the new w
     */
    public void setW(float w) {
        this.points[3] = w;
    }


    public float w() {
        return this.points[3];
    }

    public void w(float w) {
        this.points[3] = w;
    }

    public void setXYZW(float x, float y, float z, float w) {
        this.points[0] = x;
        this.points[1] = y;
        this.points[2] = z;
        this.points[3] = w;
    }


    @Override
    public String toString() {
        return "X:" + points[0] + " Y:" + points[1] + " Z:" + points[2] + " W:" + points[3];
    }

}