package study.acodexm;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import acodexm.panorama.RotationVector;

public class Guidance implements ApplicationListener {


    private final Vector3 up = new Vector3();
    private final Vector3 out = new Vector3();
    private final Vector3 position = new Vector3();
    //
    Matrix4 mat4;
    private PerspectiveCamera camera;
    private Model model;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private FPSLogger fpsLogger;
    private Environment environment;
    private RotationVector rotationVector;
    private Matrix4 rotationMatrix;
    private CameraInputController camController;
    private Shader shader;
    private RenderContext renderContext;
    private Model sphereTemplate;
    private Model photoSphere;
    private Renderable renderable;
    private ModelBuilder mModelBuilder;

    public Guidance(RotationVector r) {
        rotationVector = r;
    }


    //
    @Override
    public void create() {
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(50, 2f * aspectRatio, 2f);
        camera.position.set(0, 0, 5f);
        camera.lookAt(0, 0, 0);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();
        camController = new CameraInputController(camera);
        ModelBuilder modelBuilder = new ModelBuilder();
        model = modelBuilder.createBox(5f, 5f, 5f, new Material(ColorAttribute.createDiffuse(Color.GREEN)), VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal);
        instance = new ModelInstance(model);
        modelBatch = new ModelBatch();

        environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
        mModelBuilder = new ModelBuilder();
        sphereTemplate = mModelBuilder.createSphere(2f, 4f, 2f, 8, 7,
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
//        Gdx.input.setInputProcessor(camController);

        instance.transform.translate(new Vector3(0, 0, -10));
        rotationMatrix = new Matrix4(rotationVector.getValues());
        fpsLogger = new FPSLogger();
        mat4 = new Matrix4();
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);

        modelBatch.begin(camera);
        modelBatch.render(instance, environment);
        modelBatch.end();
        renderContext.begin();
        shader.begin(camera, renderContext);
        shader.render(renderable);
        shader.end();
        renderContext.end();
//        rotationMatrix.set(rotationVector.getValues());
//        camera.rotate(rotationMatrix);
//        camera.update();

//        Gdx.app.log("Rotation Matrix", rotationMatrix.toString());
        camController.update();


        fpsLogger.log();

//        rotationMatrix.set(rotationVector.getValues());

//        this.up.set(rotationMatrix.val[0], rotationMatrix.val[1], rotationMatrix.val[2]);
//        this.out.set(rotationMatrix.val[4], rotationMatrix.val[5], rotationMatrix.val[6]);
//        this.position.set(rotationMatrix.val[8], rotationMatrix.val[9], rotationMatrix.val[10]);
//
//        //this.camera.position.set(position);
//        this.camera.direction.set(out);
//        this.camera.up.set(up);

//        Gdx.input.getRotationMatrix(mat4.val);
        mat4.set(rotationVector.getValues());
        camera.up.set(mat4.val[Matrix4.M01], mat4.val[Matrix4.M02], mat4.val[Matrix4.M00]);
        camera.direction.set(-mat4.val[Matrix4.M21], -mat4.val[Matrix4.M22], -mat4.val[Matrix4.M20]);

        camera.update();


    }

    @Override
    public void resize(int width, int height) {

    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void dispose() {

    }

}