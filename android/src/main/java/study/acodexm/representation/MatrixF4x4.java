package study.acodexm.representation;

/**
 * The Class MatrixF4x4.
 * <p>
 * Internal the matrix is structured as
 * <p>
 * [ x0 , y0 , z0 , w0 ] [ x1 , y1 , z1 , w1 ] [ x2 , y2 , z2 , w2 ] [ x3 , y3 , z3 , w3 ]
 * <p>
 * it is recommend that when setting the matrix values individually that you use the set{x,#} methods, where 'x' is
 * either x, y, z or w and # is either 0, 1, 2 or 3, setY1 for example. The reason you should use these functions is
 * because it will map directly to that part of the matrix regardless of whether or not the internal matrix is column
 * major or not. If the matrix is either or length 9 or 16 it will be able to determine if it can set the value or not.
 * If the matrix is of size 9 but you set say w2, the value will not be set and the set method will return without any
 * error.
 */
public class MatrixF4x4 {


    /**
     * The matrix.
     */
    public float[] matrix;

    /**
     * Instantiates a new matrixf4x4. The Matrix is assumed to be Column major, however you can change this by using the
     * setColumnMajor function to false and it will operate like a row major matrix.
     */
    public MatrixF4x4() {
        this.matrix = new float[16];
        Matrix.setIdentityM(this.matrix, 0);
    }

    /**
     * Gets the matrix.
     *
     * @return the matrix, can be null if the matrix is invalid
     */
    public float[] getMatrix() {
        return this.matrix;
    }

    public void set(MatrixF4x4 source) {
        System.arraycopy(source.matrix, 0, matrix, 0, matrix.length);
    }

}
