package study.acodexm;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Quaternion;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Button;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.viewport.StretchViewport;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import study.acodexm.Utils.LOG;

public class AndroidCamera implements ApplicationListener {
    public static final float WORLD_WIDTH = 800;
    public static final float WORLD_HEIGHT = 480;
    private static final String TAG = AndroidCamera.class.getSimpleName();
    private final DeviceCameraControl deviceCameraControl;
    public SpriteBatch batch;
    ClickListener mClickListener = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            LOG.d(TAG, "x: " + x + " y: " + y);

        }
    };
    private Stage stage;
    private Texture shootTexUp;
    private Texture shootTexDown;
    private Button mShootBtn;
    private SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private String date = sDateFormat.format(new java.util.Date());
    private PerspectiveCamera camera;
    private Mode mode = Mode.normal;
    ClickListener takePicture = new ClickListener() {
        @Override
        public void clicked(InputEvent event, float x, float y) {
            LOG.d(TAG, "take picture btn pressed");
            if (mode == Mode.preview) {
                mode = Mode.takePicture;
            }
        }
    };
    //merge
    private ModelInstance instance;
    private ModelBatch modelBatch;
    //    private PerspectiveCamera cam;
    private CameraInputController camController;
    private Shader shader;
    private RenderContext renderContext;
    private Model sphereTemplate;
    private Model photoSphere;
    private Renderable renderable;
    private ModelBuilder mModelBuilder;
    private int cells;
    private List<Vector3> vector3s;
    private boolean isUpdated;

    public AndroidCamera(DeviceCameraControl cameraControl) {
        this.deviceCameraControl = cameraControl;
    }

    @Override
    public void create() {
        LOG.d(TAG, "create method begin");
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        stage = new Stage(new StretchViewport(WORLD_WIDTH, WORLD_HEIGHT));
        Gdx.input.setInputProcessor(stage);
        shootTexUp = new Texture(Gdx.files.internal("data/shutter.png"));
        shootTexDown = new Texture(Gdx.files.internal("data/shutter2.png"));
        Button.ButtonStyle shootBtnStyle = new Button.ButtonStyle();
        shootBtnStyle.up = new TextureRegionDrawable(new TextureRegion(shootTexUp));
        shootBtnStyle.down = new TextureRegionDrawable(new TextureRegion(shootTexDown));
        mShootBtn = new Button(shootBtnStyle);
        mShootBtn.setPosition(WORLD_WIDTH - 150, ((WORLD_HEIGHT / 2) - (mShootBtn.getHeight() / 2)));
        mShootBtn.addListener(takePicture);
        stage.addActor(mShootBtn);
        stage.addListener(mClickListener);
        batch = new SpriteBatch();
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(67, 2f * aspectRatio, 2f);
//        camera = new PerspectiveCamera(
//                67.0f,
//                Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.far = 300.0f;
        camera.near = 1f;
        camera.position.set(0.3183349f, 0.0061908923f, 0.33766082f);
        camera.lookAt(-0.68591654f, -0.013339217f, -0.72755706f);

        //merge
        cells = 8;
        isUpdated = false;
        modelBatch = new ModelBatch();
//        cam = new PerspectiveCamera(67, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
//        cam.position.set(0.3183349f, 0.0061908923f, 0.33766082f);
//        cam.lookAt(-0.68591654f, -0.013339217f, -0.72755706f);
//        cam.near = 1f;
//        cam.far = 300f;
//        cam.update();
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
        //sphere model template
        mModelBuilder = new ModelBuilder();
        sphereTemplate = mModelBuilder.createSphere(2f, 4f, 2f, cells, 7,
                new Material(),
                VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates);
        NodePart blockPart = sphereTemplate.nodes.get(0).parts.get(0);
        renderable = new Renderable();
        blockPart.setRenderable(renderable);
        renderable.meshPart.primitiveType = GL20.GL_POINTS;
        renderable.environment = null;
        renderable.worldTransform.idt();
        renderContext = new RenderContext(
                new DefaultTextureBinder(DefaultTextureBinder.WEIGHTED, 1));
        shader = new DefaultShader(renderable);
        shader.init();

        //vertices of template sphere model
        int verticesAmount = renderable.meshPart.mesh.getNumVertices()
                * renderable.meshPart.mesh.getVertexSize() / 4;
        float vertices[] = new float[verticesAmount];
        renderable.meshPart.mesh.getVertices(vertices);
        int vsize = renderable.meshPart.mesh.getVertexSize() / 4;
        vector3s = new ArrayList<Vector3>();
        for (int i = 0; i < vertices.length; i += vsize) {
            vector3s.add(meshToWorld(
                    vertices[i],
                    vertices[i + 1],
                    vertices[i + 2],
                    renderable.worldTransform));
        }
//        photoSphere = setTexOnSphere(vector3s, mModelBuilder);
//        instance = new ModelInstance(photoSphere);
    }

    @Override
    public void dispose() {
        LOG.d(TAG, "dispose method begin");
        if (shootTexUp != null) {
            shootTexUp.dispose();
        }
        if (shootTexDown != null) {
            shootTexDown.dispose();
        }
        if (stage != null) {
            stage.dispose();
        }
        batch.dispose();
        shader.dispose();
        sphereTemplate.dispose();
        photoSphere.dispose();
        modelBatch.dispose();
    }

    @Override
    public void render() {
        if (mode == Mode.normal) {
            mode = Mode.prepare;
            if (deviceCameraControl != null) {
                deviceCameraControl.prepareCameraAsync();
            }
        }
        Gdx.gl.glClearColor(0.57f, 0.40f, 0.55f, 1.0f);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        render_preview();
//merge
        camController.update();
//        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (!isUpdated) {
            LOG.d(TAG, "update sphere textures");
            renderPhotos();
            isUpdated = true;
        }
        try {
            instance.transform.setFromEulerAngles(
                    -deviceCameraControl.getChangeX(), 0, 0);
//                    translateRotate1(instance.transform),
//                    -translateRotate2(instance.transform));
//            instance.transform.setFromEulerAngles(
//                    deviceCameraControl.getChangeX(),
//                    deviceCameraControl.getChangeY(),
//                    deviceCameraControl.getChangeZ());
        } catch (Exception e) {
            LOG.e(TAG, "nope", e);
        }
        modelBatch.begin(camera);
        modelBatch.render(instance);
        modelBatch.end();

        renderContext.begin();
        shader.begin(camera, renderContext);
        renderable.worldTransform.setFromEulerAngles(
                -deviceCameraControl.getChangeX(), 0, 0);
//                translateRotate1(renderable.worldTransform),
//                -translateRotate2(renderable.worldTransform));
        shader.render(renderable);
        shader.end();
        renderContext.end();
    }

    public float translateRotate1(Matrix4 transform) {
        float axis;
        Matrix4 modelTransform = new Matrix4();
        modelTransform.set(transform);
        modelTransform.mul(camera.combined);
        Quaternion q = modelTransform.getRotation(new Quaternion());
        axis = deviceCameraControl.getChangeY() * (1 - q.getYaw());
        return axis;
    }

    public float translateRotate2(Matrix4 transform) {
        float axis;
        Matrix4 modelTransform = new Matrix4();
        modelTransform.set(transform);
        modelTransform.mul(camera.combined);
        Quaternion q = modelTransform.getRotation(new Quaternion());
        axis = deviceCameraControl.getChangeY() * q.getYaw();
        return axis;
    }

    private void renderPhotos() {
        try {
            List<Integer> ids = new ArrayList<Integer>();
            ids.add(30);
            ids.add(31);
            ids.add(32);
            ids.add(33);
            Map<Integer, String> stringSet = new HashMap<Integer, String>();
            stringSet.put(30, "room.jpg");
            stringSet.put(31, "room.jpg");
            stringSet.put(32, "room.jpg");
            stringSet.put(33, "room.jpg");
            photoSphere = setPhotoOnSphere(vector3s, mModelBuilder, stringSet, ids);
            instance = new ModelInstance(photoSphere);
            instance.transform.setFromEulerAngles(0f, -90f, 0f);
        } catch (Exception e) {
            LOG.e(TAG, "no chyba nie:", e);
        }
    }

    private Model setPhotoOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, String> fileName, List<Integer> ids) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        modelBuilder.begin();
        for (int id : ids) {
            Texture texture = new Texture(Gdx.files.internal("data/texture/" + fileName.get(id)));
            Material material = new Material(TextureAttribute.createDiffuse(texture),
                    ColorAttribute.createSpecular(1, 1, 1, 1),
                    FloatAttribute.createShininess(8f));
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(id + cells + 1).x, fourVertices.get(id + cells + 1).y, fourVertices.get(id + cells + 1).z,
                            fourVertices.get(id + cells + 2).x, fourVertices.get(id + cells + 2).y, fourVertices.get(id + cells + 2).z,
                            fourVertices.get(id + 1).x, fourVertices.get(id + 1).y, fourVertices.get(id + 1).z,
                            fourVertices.get(id).x, fourVertices.get(id).y, fourVertices.get(id).z,
                            -1, -1, -1);
        }
        return modelBuilder.end();
    }

    private Model setTexOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        Texture texture = new Texture("badlogic.jpg");
        Material material = new Material(TextureAttribute.createDiffuse(texture),
                ColorAttribute.createSpecular(1, 1, 1, 1),
                FloatAttribute.createShininess(8f));
        modelBuilder.begin();
        for (int i = cells + 1; i < fourVertices.size() - (cells * 2 + 3); i++) {
            modelBuilder.part("id" + i, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(i + cells + 1).x, fourVertices.get(i + cells + 1).y, fourVertices.get(i + cells + 1).z,
                            fourVertices.get(i + cells + 2).x, fourVertices.get(i + cells + 2).y, fourVertices.get(i + cells + 2).z,
                            fourVertices.get(i + 1).x, fourVertices.get(i + 1).y, fourVertices.get(i + 1).z,
                            fourVertices.get(i).x, fourVertices.get(i).y, fourVertices.get(i).z,
                            -1, -1, -1);

        }
        return modelBuilder.end();
    }

    private Vector3 meshToWorld(float x, float y, float z, Matrix4 transformation) {
        float[] q = transformation.getValues();
        Vector3 r = new Vector3();
        r.x = q[Matrix4.M00] * x + q[Matrix4.M01] * y + q[Matrix4.M02] * z + q[Matrix4.M03];
        r.y = q[Matrix4.M10] * x + q[Matrix4.M11] * y + q[Matrix4.M12] * z + q[Matrix4.M13];
        r.z = q[Matrix4.M20] * x + q[Matrix4.M21] * y + q[Matrix4.M22] * z + q[Matrix4.M23];
        return r;
    }

    private void calculateCenter() {

    }

    public void render_preview() {
        Gdx.gl20.glHint(GL20.GL_GENERATE_MIPMAP_HINT, GL20.GL_NICEST);
        if (mode == Mode.takePicture) {
            Gdx.gl20.glClearColor(0f, 0.0f, 0.0f, 0.0f);
            if (deviceCameraControl != null) {
                LOG.d(TAG, "takePicture method begin");
                deviceCameraControl.takePicture();
            }
            mode = Mode.waitForPictureReady;
        } else if (mode == Mode.waitForPictureReady) {
            Gdx.gl20.glClearColor(0.0f, 0f, 0.0f, 0.0f);
        } else if (mode == Mode.prepare) {
            Gdx.gl20.glClearColor(0.0f, 0.0f, 0f, 0.6f);
            if (deviceCameraControl != null) {
                if (deviceCameraControl.isReady()) {
                    deviceCameraControl.startPreviewAsync();
                    mode = Mode.preview;
                }
            }
        } else if (mode == Mode.preview) {
            Gdx.gl20.glClearColor(0.0f, 0.0f, 0.0f, 0f);
        } else {
            Gdx.gl20.glClearColor(0.0f, 0.0f, 0.6f, 1.0f);
        }
        Gdx.gl20.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        batch.begin();
        stage.act();
        stage.draw();
        batch.end();
        Gdx.gl20.glEnable(GL20.GL_DEPTH_TEST);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE);
        Gdx.gl20.glEnable(GL20.GL_TEXTURE_2D);
        Gdx.gl20.glEnable(GL20.GL_LINE_LOOP);
        Gdx.gl20.glDepthFunc(GL20.GL_LEQUAL);
        Gdx.gl20.glClearDepthf(1.0F);
        camera.update(true);
        if (mode == Mode.waitForPictureReady) {

            if (deviceCameraControl != null && deviceCameraControl.getPictureData() != null) {
                deviceCameraControl.saveAsJpeg();
                deviceCameraControl.stopPreviewAsync();
                mode = Mode.normal;
            }
        }
    }

    @Override
    public void resize(int width, int height) {
//        camera = new PerspectiveCamera(67.0f, width, height);
//        camera.far = 300.0f;
//        camera.near = 1f;
//        camera.position.set(0.3183349f, 0.0061908923f, 0.33766082f);
//        camera.lookAt(-0.68591654f, -0.013339217f, -0.72755706f);

    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }

    public enum Mode {
        normal, prepare, preview, takePicture, waitForPictureReady,
    }

}
