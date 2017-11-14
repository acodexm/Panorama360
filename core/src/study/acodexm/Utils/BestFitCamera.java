package study.acodexm.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

public class BestFitCamera extends Camera {
    public float zoom = 1, stretchFactorX = 1, stretchFactorY = 1;
    public boolean rotated;

    public BestFitCamera() {
        this.near = 0;
    }

    public BestFitCamera(float viewportWidth, float viewportHeight) {
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.near = 0;
        update();
    }

    private final Vector3 tmp = new Vector3();

    @Override
    public void update() {
        update(true);
    }

    @Override
    public void update(boolean updateFrustum) {
        float left = zoom * stretchFactorX * -(viewportWidth / 2);
        float right = zoom * stretchFactorX * (viewportWidth / 2);
        float top = zoom * stretchFactorY * -(viewportHeight / 2);
        float bottom = zoom * stretchFactorY * viewportHeight / 2;
        if (rotated) {
            float tmp = left;
            left = top;
            top = tmp;
            tmp = right;
            right = bottom;
            bottom = tmp;
        }
        projection.setToOrtho(left, right, top, bottom, near, far);
        view.setToLookAt(position, tmp.set(position).add(direction), up);
        combined.set(projection);
        Matrix4.mul(combined.val, view.val);

        if (updateFrustum) {
            invProjectionView.set(combined);
            Matrix4.inv(invProjectionView.val);
            frustum.update(invProjectionView);
        }
    }

    /**
     * Sets this camera to an orthographic projection using a viewport fitting the screen resolution,
     * centered at (Gdx.graphics.getWidth()/2, Gdx.graphics.getHeight()/2), with the y-axis pointing
     * up or down.
     *
     * @param yDown whether y should be pointing down
     */
    public void setToOrtho(boolean yDown) {
        setToOrtho(yDown, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
    }

    /**
     * Sets this camera to an orthographic projection, centered at (viewportWidth/2,
     * viewportHeight/2), with the y-axis pointing up or down.
     *
     * @param yDown          whether y should be pointing down.
     * @param viewportWidth
     * @param viewportHeight
     */
    public void setToOrtho(boolean yDown, float viewportWidth,
                           float viewportHeight) {
        if (yDown) {
            up.set(0, -1, 0);
            direction.set(0, 0, 1);
        } else {
            up.set(0, 1, 0);
            direction.set(0, 0, -1);
        }
        position
                .set(zoom * viewportWidth / 2.0f, zoom * viewportHeight / 2.0f, 0);
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        update();
    }

    /**
     * Rotates the camera by the given angle around the direction vector. The direction and up vector
     * will not be orthogonalized.
     *
     * @param angle
     */
    public void rotate(float angle) {
        rotate(direction, angle);
    }

    /**
     * Moves the camera by the given amount on each axis.
     *
     * @param x the displacement on the x-axis
     * @param y the displacement on the y-axis
     */
    public void translate(float x, float y) {
        translate(x, y, 0);
    }

    /**
     * Moves the camera by the given vector.
     *
     * @param vec the displacement vector
     */
    public void translate(Vector2 vec) {
        translate(vec.x, vec.y, 0);
    }

}