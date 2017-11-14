package study.acodexm;


import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MyGdxGame extends ApplicationAdapter {
    SpriteBatch batch;
    Texture img;

    Viewport viewport;
    OrthographicCamera camera;

    OrientationHelper orientationHelper;

    public MyGdxGame(OrientationHelper orientationHelper) {
        this.orientationHelper = orientationHelper;
    }

    @Override
    public void create() {
        batch = new SpriteBatch();
        img = new Texture("badlogic.jpg");
        camera = new OrthographicCamera();
        viewport = new FitViewport(800, 480, camera);
    }

    @Override
    public void render() {
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        batch.draw(img, 0, 0);
        batch.end();

        if (Gdx.input.justTouched()) {
            switch (orientationHelper.getOrientation()) {
                case LANDSCAPE: {
                    orientationHelper.requestOrientation(OrientationHelper.Orientation.PORTRAIT);
                    break;
                }
                case PORTRAIT: {
                    orientationHelper.requestOrientation(OrientationHelper.Orientation.LANDSCAPE);
                    break;
                }
            }

        }
    }

    @Override
    public void resize(int width, int height) {
        if (width > height && viewport.getWorldWidth() < viewport.getWorldHeight()) {
            viewport.setWorldSize(800, 480);
        }
        if (width < height && viewport.getWorldWidth() > viewport.getWorldHeight()) {
            viewport.setWorldSize(480, 800);
        }
        viewport.update(width, height, true);
    }

    public static interface OrientationHelper {

        public static enum Orientation {
            LANDSCAPE,
            PORTRAIT
        }

        public boolean requestOrientation(Orientation orientation);

        public Orientation getOrientation();
    }

}