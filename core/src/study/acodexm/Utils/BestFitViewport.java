package study.acodexm.Utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Scaling;
import com.badlogic.gdx.utils.viewport.Viewport;

/**
 * stage.setViewport(new BestFitViewport(1000,800));
 * <p>
 * such a viewport will force landscape orientation and fit the landscape display.
 * <p>
 * stage.setViewport(new BestFitViewport(750,1030));
 * <p>
 * and this one will force portrait orientation and fit the portrait display.
 */
public class BestFitViewport extends Viewport {
    private Scaling scaling;
    private boolean rotated;
    private int screen_width, screen_height;
    private float currentRotation;
    private BestFitCamera camera;
    private float worldWidth;
    private float worldHeight;
    private int viewportWidth;
    private int viewportHeight;
    private int viewportY;
    private int viewportX;

    public BestFitViewport(float wWidth, float wHeight) {
        super();
        this.scaling = Scaling.fit;
        this.worldWidth = wWidth;
        this.worldHeight = wHeight;
        this.camera = new BestFitCamera();
    }

    private void updateRotationState(int screenW, int screenH) {
        this.screen_width = screenW;
        this.screen_height = screenH;
        rotated = (worldWidth > worldHeight && screenW < screenH) ||
                (worldWidth < worldHeight && screenW > screenH);
    }

    @Override
    public void setWorldHeight(float worldHeight) {
        setWorldSize(getWorldWidth(), worldHeight);
    }

    @Override
    public void setWorldSize(float worldWidth, float worldHeight) {
        this.worldWidth = worldWidth;
        this.worldHeight = worldHeight;
        updateRotationState(screen_width, screen_height);
    }

    @Override
    public void setWorldWidth(float worldWidth) {
        setWorldSize(worldWidth, getWorldHeight());
    }

    @Override
    public void update(int screenWidth, int screenHeight, boolean centerCamera) {
        if (screenHeight != 0 && screenWidth != 0 &&
                (screenHeight != this.screen_height || screenWidth != this.screen_width)
                ) {
            updateRotationState(screenWidth, screenHeight);
            Vector2 scaled;
            if (rotated) {
                scaled = scaling.apply(worldWidth, worldHeight, screenHeight, screenWidth);
                viewportWidth = Math.round(scaled.y);
                viewportHeight = Math.round(scaled.x);
            } else {
                scaled = scaling.apply(worldWidth, worldHeight, screenWidth, screenHeight);
                viewportWidth = Math.round(scaled.x);
                viewportHeight = Math.round(scaled.y);
            }

            // center the viewport in the middle of the screen
            viewportX = (screenWidth - viewportWidth) / 2;
            viewportY = (screenHeight - viewportHeight) / 2;

            Gdx.gl.glViewport(viewportY, viewportX, viewportWidth, viewportHeight);
            camera.viewportWidth = worldWidth;
            camera.viewportHeight = worldHeight;
            if (centerCamera)
                camera.position.set(camera.viewportWidth / 2, camera.viewportHeight / 2, 0);

            float previousRotation = currentRotation;
            currentRotation = rotated ? 270 : 0;
            camera.rotate(currentRotation - previousRotation, 0, 0, 1);
            if (rotated) {
                camera.stretchFactorX = (worldHeight / viewportWidth) / (worldWidth / viewportHeight);
                camera.stretchFactorY = 1 / camera.stretchFactorX;
            }
            camera.rotated = rotated;
        }
        camera.update();
    }
}