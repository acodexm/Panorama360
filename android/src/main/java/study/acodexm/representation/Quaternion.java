package study.acodexm.representation;

/**
 * The Quaternion class. A Quaternion is a four-dimensional vector that is used to represent rotations of a rigid body
 * in the 3D space. It is very similar to a rotation vector; it contains an angle, encoded into the w component
 * and three components to describe the rotation-axis (encoded into x, y, z).
 *
 * <p>
 * Quaternions allow for elegant descriptions of 3D rotations, interpolations as well as extrapolations and compared to
 * Euler angles, they don't suffer from gimbal lock. Interpolations between two Quaternions are called SLERP (Spherical
 * Linear Interpolation).
 * </p>
 *
 * <p>
 * This class also contains the representation of the same rotation as a Quaternion and 4x4-Rotation-Matrix.
 * </p>
 *
 * @author Leigh Beattie, Alexander Pacha
 */
public class Quaternion extends Vector4f {

    private Vector4f tmpVector = new Vector4f();
    private Quaternion tmpQuaternion;

    /**
     * Creates a new Quaternion object and initialises it with the identity Quaternion
     */
    public Quaternion() {
        super();
        /**
         * Rotation matrix that contains the same rotation as the Quaternion in a 4x4 homogenised rotation matrix.
         * Remember that for performance reasons, this matrix is only updated, when it is accessed and not on every change
         * of the quaternion-values.
         */
        loadIdentityQuat();
    }


    /**
     * Copies the values from the given quaternion to this one
     *
     * @param quat The quaternion to copy from
     */
    public void set(Quaternion quat) {
        copyVec4(quat);
    }

    /**
     * Multiply this quaternion by the input quaternion and store the result in the out quaternion
     *
     * @param input
     * @param output
     */
    public void multiplyByQuat(Quaternion input, Quaternion output) {

        if (input != output) {
            output.points[3] = (points[3] * input.points[3] - points[0] * input.points[0] - points[1] * input.points[1] - points[2]
                    * input.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * input.points[0] + points[0] * input.points[3] + points[1] * input.points[2] - points[2]
                    * input.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * input.points[1] + points[1] * input.points[3] + points[2] * input.points[0] - points[0]
                    * input.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * input.points[2] + points[2] * input.points[3] + points[0] * input.points[1] - points[1]
                    * input.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        } else {
            tmpVector.points[0] = input.points[0];
            tmpVector.points[1] = input.points[1];
            tmpVector.points[2] = input.points[2];
            tmpVector.points[3] = input.points[3];

            output.points[3] = (points[3] * tmpVector.points[3] - points[0] * tmpVector.points[0] - points[1]
                    * tmpVector.points[1] - points[2] * tmpVector.points[2]); //w = w1w2 - x1x2 - y1y2 - z1z2
            output.points[0] = (points[3] * tmpVector.points[0] + points[0] * tmpVector.points[3] + points[1]
                    * tmpVector.points[2] - points[2] * tmpVector.points[1]); //x = w1x2 + x1w2 + y1z2 - z1y2
            output.points[1] = (points[3] * tmpVector.points[1] + points[1] * tmpVector.points[3] + points[2]
                    * tmpVector.points[0] - points[0] * tmpVector.points[2]); //y = w1y2 + y1w2 + z1x2 - x1z2
            output.points[2] = (points[3] * tmpVector.points[2] + points[2] * tmpVector.points[3] + points[0]
                    * tmpVector.points[1] - points[1] * tmpVector.points[0]); //z = w1z2 + z1w2 + x1y2 - y1x2
        }
    }


    /**
     * Sets the quaternion to an identity quaternion of 0,0,0,1.
     */
    public void loadIdentityQuat() {
        setX(0);
        setY(0);
        setZ(0);
        setW(1);
    }

    @Override
    public String toString() {
        return "{X: " + getX() + ", Y:" + getY() + ", Z:" + getZ() + ", W:" + getW() + "}";
    }


    /**
     * Get a linear interpolation between this quaternion and the input quaternion, storing the result in the output
     * quaternion.
     *
     * @param input  The quaternion to be slerped with this quaternion.
     * @param output The quaternion to store the result in.
     * @param t      The ratio between the two quaternions where 0 <= t <= 1.0 . Increase value of t will bring rotation
     *               closer to the input quaternion.
     */
    public void slerp(Quaternion input, Quaternion output, float t) {
        Quaternion bufferQuat;
        float cosHalftheta = this.dotProduct(input);

        if (cosHalftheta < 0) {
            if (tmpQuaternion == null) tmpQuaternion = new Quaternion();
            bufferQuat = tmpQuaternion;
            cosHalftheta = -cosHalftheta;
            bufferQuat.points[0] = (-input.points[0]);
            bufferQuat.points[1] = (-input.points[1]);
            bufferQuat.points[2] = (-input.points[2]);
            bufferQuat.points[3] = (-input.points[3]);
        } else {
            bufferQuat = input;
        }
        if (Math.abs(cosHalftheta) >= 1.0) {
            output.points[0] = (this.points[0]);
            output.points[1] = (this.points[1]);
            output.points[2] = (this.points[2]);
            output.points[3] = (this.points[3]);
        } else {
            double sinHalfTheta = Math.sqrt(1.0 - cosHalftheta * cosHalftheta);
            double halfTheta = Math.acos(cosHalftheta);

            double ratioA = Math.sin((1 - t) * halfTheta) / sinHalfTheta;
            double ratioB = Math.sin(t * halfTheta) / sinHalfTheta;

            //Calculate Quaternion
            output.points[3] = ((float) (points[3] * ratioA + bufferQuat.points[3] * ratioB));
            output.points[0] = ((float) (this.points[0] * ratioA + bufferQuat.points[0] * ratioB));
            output.points[1] = ((float) (this.points[1] * ratioA + bufferQuat.points[1] * ratioB));
            output.points[2] = ((float) (this.points[2] * ratioA + bufferQuat.points[2] * ratioB));

        }
    }

}
