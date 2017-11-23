package study.acodexm;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.Shader;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.shaders.DefaultShader;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.DefaultTextureBinder;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.graphics.g3d.utils.RenderContext;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import study.acodexm.Utils.LOG;
import study.acodexm.settings.ActionMode;
import study.acodexm.settings.PictureMode;

public class AndroidCamera implements ApplicationListener {
    private static final String TAG = AndroidCamera.class.getSimpleName();
    private PerspectiveCamera camera;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private CameraInputController camController;
    private Shader shader;
    private RenderContext renderContext;
    private Model sphereTemplate;
    private Model photoSphere;
    private Renderable renderable;
    private ModelBuilder mModelBuilder;
    private int lat, lon;
    private List<Vector3> vector3s;
    private boolean isUpdated;
    private Matrix4 rotationMatrix;
    private FPSLogger fpsLogger;
    private Matrix4 mat4;
    private RotationVector mRotationVector;
    private boolean calculated = false;
    private Map<Integer, Vector3> centersOfGrid;
    private ActionMode mActionMode = ActionMode.FullAuto;
    private PictureMode mPictureMode = PictureMode.panorama;
    private SphereControl mSphereControl;
    private Map<Integer, byte[]> mPictures;
    private List<Integer> ids;
    private List<Integer> takenPictures;

    public AndroidCamera(RotationVector rotationVector, SphereControl sphereControl) {
        mRotationVector = rotationVector;
        mSphereControl = sphereControl;
    }

    @Override
    public void create() {
        LOG.d(TAG, "create method begin");
        Gdx.app.setLogLevel(Application.LOG_DEBUG);
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(20, 2f * aspectRatio, 2f);
        camera.far = 300.0f;
        camera.near = 0.1f;
        camera.position.set(0f, 0f, 0f);
        isUpdated = false;
        modelBatch = new ModelBatch();
        camController = new CameraInputController(camera);
        //sphere model template
        mModelBuilder = new ModelBuilder();
        lat = 10;
        lon = 7;
        sphereTemplate = mModelBuilder.createSphere(2f, 2f, 2f, lat, lon,
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
        rotationMatrix = new Matrix4(mRotationVector.getValues());
        fpsLogger = new FPSLogger();
        mat4 = new Matrix4();
        setIdList();
        takenPictures = new ArrayList<Integer>();
    }

    @Override
    public void dispose() {
        LOG.d(TAG, "dispose method begin");
        shader.dispose();
        sphereTemplate.dispose();
        photoSphere.dispose();
        modelBatch.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glViewport(0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        Gdx.gl.glClearColor(0, 0, 0, 0);
        if (!isUpdated) {
            LOG.d(TAG, "update sphere textures");
            renderPhotos();
            isUpdated = true;
        }

        modelBatch.begin(camera);
        modelBatch.render(instance);
        modelBatch.end();

        renderContext.begin();
        shader.begin(camera, renderContext);
        shader.render(renderable);
        shader.end();
        renderContext.end();
        camController.update();

        mat4.set(mRotationVector.getValues());
        camera.up.set(mat4.val[Matrix4.M01], mat4.val[Matrix4.M02], mat4.val[Matrix4.M00]);
        camera.direction.set(-mat4.val[Matrix4.M21], -mat4.val[Matrix4.M22], -mat4.val[Matrix4.M20]);
        camera.update();
        fpsLogger.log();
        if (!calculated)
            centersOfGrid = calculateCenterList(vector3s, ids);
        int position = whereIsCameraLooking(centersOfGrid, ids);
        if (position != -1) {
            if (!takenPictures.contains(position)) {
                takenPictures.add(position);
                LOG.d(TAG, "take picture!! at: " + position);
                mSphereControl.autoTakePicture(position);
            }
        }
    }

    private void setIdList() {
        ids = new ArrayList<Integer>();
        //all row*col - last row + weird col switch hazard
        int amount = lat * lon - lat + Math.round(lat / 2);
        for (int i = lat; i <= amount; i++) {
            ids.add(i);
        }
        mSphereControl.setIdTable(ids);
    }

    private void renderPhotos() {
        try {
            mPictures = mSphereControl.getPictures();
            if (mPictures == null)
                mPictures = new HashMap<Integer, byte[]>();
            photoSphere = setPhotoOnSphere(vector3s, mModelBuilder, mPictures, ids);
            instance = new ModelInstance(photoSphere);
        } catch (Exception e) {
            LOG.e(TAG, "nope ", e);
        }
    }

    private Model setPhotoOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, byte[]> fileName, List<Integer> ids) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        modelBuilder.begin();
        for (int id : ids) {
            byte[] bytes = fileName.get(id);
            Texture texture;
            if (bytes != null) {
                try {
                    texture = new Texture(new Pixmap(bytes, 0, bytes.length));
                } catch (Exception e) {
                    LOG.e(TAG, "texture load failed, loading empty ", e);
                    texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
                }
            } else {
                LOG.d(TAG, "texture load failed, bytes=null, loading empty ");
                texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
            }
            Material material = new Material(TextureAttribute.createDiffuse(texture),
                    ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
                    new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.3f));
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(id + lat + 1).x, fourVertices.get(id + lat + 1).y, fourVertices.get(id + lat + 1).z,
                            fourVertices.get(id + lat + 2).x, fourVertices.get(id + lat + 2).y, fourVertices.get(id + lat + 2).z,
                            fourVertices.get(id + 1).x, fourVertices.get(id + 1).y, fourVertices.get(id + 1).z,
                            fourVertices.get(id).x, fourVertices.get(id).y, fourVertices.get(id).z,
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


    private Map<Integer, Vector3> calculateCenterList(List<Vector3> fourVertices, List<Integer> ids) {
        Map<Integer, Vector3> centersOfGrid = new HashMap<Integer, Vector3>();
        for (int id : ids) {
            Vector3 centerOfTex = new Vector3(
                    ((fourVertices.get(id + lat + 1).x + fourVertices.get(id + lat + 2).x) / 2 +
                            (fourVertices.get(id + 1).x + fourVertices.get(id).x) / 2),
                    ((fourVertices.get(id + lat + 1).y + fourVertices.get(id + lat + 2).y) / 2 +
                            (fourVertices.get(id + 1).y + fourVertices.get(id).y) / 2),
                    ((fourVertices.get(id + lat + 1).z + fourVertices.get(id + lat + 2).z) / 2 +
                            (fourVertices.get(id + 1).z + fourVertices.get(id).z) / 2));
            centersOfGrid.put(id, centerOfTex);
        }
        calculated = true;
        return centersOfGrid;
    }

    private int whereIsCameraLooking(Map<Integer, Vector3> centersOfGrid, List<Integer> ids) {
/**
 * we have a camera direction vector which changes with camera move
 * now we have to calculate the center point of rectangle where we what to take photo
 * if the center point and camera direction vectors are colinear than we know which rectangle
 * is in center so we can take photo and put it on as a texture
 */


/**
 * is colinear when c{x,y,z}==0
 { c_{x}=a_{y}b_{z}-a_{z}b_{y}}
 { c_{y}=a_{z}b_{x}-a_{x}b_{z}}
 { c_{z}=a_{x}b_{y}-a_{y}b_{x}}
 */
        Vector3 worldPoint = camera.direction;
        Vector3 isColinear;
        for (int i : ids) {
            isColinear = new Vector3(worldPoint.y * centersOfGrid.get(i).z - worldPoint.z * centersOfGrid.get(i).y,
                    worldPoint.z * centersOfGrid.get(i).x - worldPoint.x * centersOfGrid.get(i).z,
                    worldPoint.x * centersOfGrid.get(i).y - worldPoint.y * centersOfGrid.get(i).x);
            if ((isColinear.x < 0.5f && isColinear.x > -0.5f)
                    && (isColinear.y < 0.5f && isColinear.y > -0.5f)
                    && (isColinear.z < 0.5f && isColinear.z > -0.5f)) {
                return i;
            }
        }
        return -1;
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


}
