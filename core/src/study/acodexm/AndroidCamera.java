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
import com.badlogic.gdx.graphics.g3d.Attribute;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.ModelInstance;
import com.badlogic.gdx.graphics.g3d.Renderable;
import com.badlogic.gdx.graphics.g3d.attributes.BlendingAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.TextureAttribute;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.graphics.g3d.utils.CameraInputController;
import com.badlogic.gdx.graphics.g3d.utils.ModelBuilder;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import study.acodexm.Utils.LOG;
import study.acodexm.settings.ActionMode;
import study.acodexm.settings.SettingsControl;

public class AndroidCamera implements ApplicationListener, SphereManualControl {
    public static final int LAT = 10;
    public static final int LON = 7;
    private static final String TAG = AndroidCamera.class.getSimpleName();
    private PerspectiveCamera camera;
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private CameraInputController camController;
    private Model sphereTemplate;
    private Model photoSphere;
    private Renderable renderable;
    private ModelBuilder mModelBuilder;
    private List<Vector3> vector3s;
    private boolean isUpdated;
    private FPSLogger fpsLogger;
    private Matrix4 mat4;
    private RotationVector mRotationVector;
    private SphereControl mSphereControl;
    private SettingsControl mSettingsControl;
    private Map<Integer, Vector3> centersOfGrid;
    private ActionMode mActionMode;
    private Map<Integer, byte[]> mPictures;
    private List<Integer> ids;
    private List<Integer> takenPictures;
    private Map<Integer, String> stringSet;
    private int position;
    private long time;
    private boolean canRender = false;

    public AndroidCamera(RotationVector rotationVector, SphereControl sphereControl, SettingsControl settingsControl) {
        mRotationVector = rotationVector;
        mSphereControl = sphereControl;
        mSettingsControl = settingsControl;
    }

    @Override
    public void create() {
        LOG.d(TAG, "create method begin");
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        setupCamera();
        setupSphere();
        makeListOfSphereVertices();
        //initialize variables

        fpsLogger = new FPSLogger();
        mat4 = new Matrix4();
        takenPictures = new ArrayList<Integer>();
        fpsLogger = new FPSLogger();
        stringSet = new HashMap<Integer, String>();
        isUpdated = false;
        modelBatch = new ModelBatch();
        setIdList();
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            renderPhotosAndroid();
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            renderPhotosDesktop();
        }
        time = System.currentTimeMillis();
    }

    @Override
    public void dispose() {
        LOG.d(TAG, "dispose method begin");
        photoSphere.dispose();
        modelBatch.dispose();
    }

    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        mActionMode = mSettingsControl.getActionMode();
        camController.update();
        if (canRender && !isUpdated) {
            LOG.d(TAG, "update sphere textures");
            if (Gdx.app.getType() == Application.ApplicationType.Android) {
                mPictures = mSphereControl.getPictures();
                if (mPictures == null)
                    mPictures = new HashMap<Integer, byte[]>();
                updateTextureAndroid(mPictures);
//                updateTextureDesktop(stringSet);
            } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                updateTextureDesktop(stringSet);
            }
            isUpdated = true;
        }

        modelBatch.begin(camera);
        modelBatch.render(instance);
        modelBatch.end();

        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            mat4.set(mRotationVector.getValues());
            camera.up.set(mat4.val[Matrix4.M01], mat4.val[Matrix4.M02], mat4.val[Matrix4.M00]);
            camera.direction.set(-mat4.val[Matrix4.M21], -mat4.val[Matrix4.M22], -mat4.val[Matrix4.M20]);
            camera.update();
        }
        position = whereIsCameraLooking(centersOfGrid, ids);
        if (canRender && position != -1 && mActionMode == ActionMode.FullAuto) {
            if (!takenPictures.contains(position)) {
                LOG.d(TAG, "auto take picture!! at: " + position);
                takenPictures.add(position);
                if (Gdx.app.getType() == Application.ApplicationType.Android) {
                    mSphereControl.autoTakePicture(position);
//                    stringSet.put(position, position + ".png");
                } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                    stringSet.put(position, position + ".png");
                }
            }
        }

//        fpsLogger.log();
//        forceGC();
//        System.out.println("getJavaHeap: " + Gdx.app.getJavaHeap()
//                + " getNativeHeap: " + Gdx.app.getNativeHeap());
    }

    private void forceGC() {
        if (System.currentTimeMillis() - time > 5000) {
            time = System.currentTimeMillis();
            System.gc();
        }
    }

    private void setupCamera() {
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(30, 2f * aspectRatio, 2f);
        camera.position.set(0f, 0f, 0f);
        camera.lookAt(0, 0f, -4.0f);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();
        camController = new CameraInputController(camera);
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            Gdx.input.setInputProcessor(camController);
        }
    }

    private void setupSphere() {
        mModelBuilder = new ModelBuilder();
        sphereTemplate = mModelBuilder.createSphere(4f, 4f, 4f, LAT, LON,
                new Material(),
                VertexAttributes.Usage.Position
                        | VertexAttributes.Usage.Normal
                        | VertexAttributes.Usage.TextureCoordinates);
        NodePart blockPart = sphereTemplate.nodes.get(0).parts.get(0);
        renderable = new Renderable();
        blockPart.setRenderable(renderable);
    }

    private void makeListOfSphereVertices() {
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
    }

    private void setIdList() {
        ids = new ArrayList<Integer>();
        //all row*col - last row + weird col switch hazard
        int amount = LAT * LON - LAT + Math.round(LAT / 2);
        for (int i = LAT; i <= amount; i++) {
            ids.add(i);
        }
        if (Gdx.app.getType() == Application.ApplicationType.Android && mSphereControl != null) {
            mSphereControl.setIdTable(ids);
        }
        centersOfGrid = calculateCenterList(vector3s, ids);
    }

    private void renderPhotosAndroid() {
        mPictures = mSphereControl.getPictures();
        if (mPictures == null)
            mPictures = new HashMap<Integer, byte[]>();
        if (photoSphere != null)
            photoSphere.dispose();
        photoSphere = setPhotoOnSphereAndroid(vector3s, mModelBuilder, mPictures, ids);
        instance = null;
        instance = new ModelInstance(photoSphere);
    }

    private void renderPhotosDesktop() {
        if (photoSphere != null)
            photoSphere.dispose();
        photoSphere = setPhotoOnSphereDesktop(vector3s, mModelBuilder, stringSet, ids);
        instance = null;
        instance = new ModelInstance(photoSphere);
    }

    private Model setPhotoOnSphereAndroid(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, byte[]> fileName, List<Integer> ids) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        modelBuilder.begin();
        byte[] bytes;
        Texture texture;
        Material material;
        BlendingAttribute attribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.7f);
        ColorAttribute colorAttribute = ColorAttribute.createSpecular(1f, 1f, 1f, 1f);
        for (int id : ids) {
            bytes = fileName.get(id);
            if (bytes != null) {
                try {
//                    LOG.d(TAG, "loading byte[] texture " + id);
                    texture = new Texture(new Pixmap(bytes, 0, bytes.length));
                } catch (Exception e) {
//                    LOG.e(TAG, "texture load failed, loading empty ", e);
                    texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
                }
            } else {
//                LOG.d(TAG, "texture load failed, bytes=null, loading empty ");
                texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
            }
            material = new Material(TextureAttribute.createDiffuse(texture),
                    attribute,
                    colorAttribute);
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(id + LAT + 1).x, fourVertices.get(id + LAT + 1).y, fourVertices.get(id + LAT + 1).z,
                            fourVertices.get(id + LAT + 2).x, fourVertices.get(id + LAT + 2).y, fourVertices.get(id + LAT + 2).z,
                            fourVertices.get(id + 1).x, fourVertices.get(id + 1).y, fourVertices.get(id + 1).z,
                            fourVertices.get(id).x, fourVertices.get(id).y, fourVertices.get(id).z,
                            -1, -1, -1);
        }
        return modelBuilder.end();
    }

    private void updateTextureAndroid(Map<Integer, byte[]> fileName) {
        byte[] bytes;
        Texture texture;
        for (int id : ids) {
            bytes = fileName.get(id);
            if (bytes != null) {
                try {
//                    LOG.d(TAG, "loading byte[] texture " + id);
                    texture = new Texture(new Pixmap(bytes, 0, bytes.length));
                } catch (Exception e) {
//                    LOG.e(TAG, "texture load failed, loading empty ", e);
                    texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
                }
            } else {
//                LOG.d(TAG, "texture load failed, bytes=null, loading empty ");
                texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
            }
            Material mat = instance.materials.get(id - LAT);
            for (Attribute att : mat) {
                if (att.type == TextureAttribute.Diffuse) {
                    ((TextureAttribute) att).textureDescription.set(texture,
                            Texture.TextureFilter.Linear,
                            Texture.TextureFilter.Linear,
                            Texture.TextureWrap.ClampToEdge,
                            Texture.TextureWrap.ClampToEdge);
                }
            }

        }
    }

    private void updateTextureDesktop(Map<Integer, String> fileName) {
        String bytes;
        Texture texture;
        Material mat;
        for (int id : ids) {
            bytes = fileName.get(id);
            if (bytes != null) {
                try {
//                    LOG.d(TAG, "loading texture " + id + ".png");
                    texture = new Texture(Gdx.files.internal("data/numbers/" + bytes));
                } catch (Exception e) {
//                    LOG.e(TAG, "texture load failed, loading empty ", e);
                    texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
                }
            } else {
//                LOG.d(TAG, "texture load failed, bytes=null, loading empty ");
                texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
            }
            mat = instance.materials.get(id - LAT);
            for (Attribute att : mat) {
                if (att.type == TextureAttribute.Diffuse) {
                    ((TextureAttribute) att).textureDescription.set(
                            texture,
                            Texture.TextureFilter.Linear,
                            Texture.TextureFilter.Linear,
                            Texture.TextureWrap.ClampToEdge,
                            Texture.TextureWrap.ClampToEdge);
                }
            }
        }
    }

    private Model setPhotoOnSphereDesktop(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, String> fileName, List<Integer> ids) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        modelBuilder.begin();
        String bytes;
        Texture texture;
        Material material;
        BlendingAttribute attribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.7f);
        ColorAttribute colorAttribute = ColorAttribute.createSpecular(1f, 1f, 1f, 1f);
        for (int id : ids) {
            bytes = fileName.get(id);
            if (bytes != null) {
                try {
                    LOG.d(TAG, "loading texture " + id + ".png");
                    texture = new Texture(Gdx.files.internal("data/numbers/" + bytes));
                } catch (Exception e) {
                    LOG.e(TAG, "texture load failed, loading empty ", e);
                    texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
                }
            } else {
                LOG.d(TAG, "texture load failed, bytes=null, loading empty ");
                texture = new Texture(Gdx.files.internal("data/numbers/empty.png"));
            }
            material = new Material();
            material.set(
                    TextureAttribute.createDiffuse(texture),
                    colorAttribute,
                    attribute);
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(id + LAT + 1).x, fourVertices.get(id + LAT + 1).y, fourVertices.get(id + LAT + 1).z,//00
                            fourVertices.get(id + LAT + 2).x, fourVertices.get(id + LAT + 2).y, fourVertices.get(id + LAT + 2).z,//01
                            fourVertices.get(id + 1).x, fourVertices.get(id + 1).y, fourVertices.get(id + 1).z,//11
                            fourVertices.get(id).x, fourVertices.get(id).y, fourVertices.get(id).z,//10
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
                    ((fourVertices.get(id + LAT + 1).x + fourVertices.get(id + LAT + 2).x) / 2 +
                            (fourVertices.get(id + 1).x + fourVertices.get(id).x) / 2),
                    ((fourVertices.get(id + LAT + 1).y + fourVertices.get(id + LAT + 2).y) / 2 +
                            (fourVertices.get(id + 1).y + fourVertices.get(id).y) / 2),
                    ((fourVertices.get(id + LAT + 1).z + fourVertices.get(id + LAT + 2).z) / 2 +
                            (fourVertices.get(id + 1).z + fourVertices.get(id).z) / 2));
            centersOfGrid.put(id, centerOfTex);
        }
        return centersOfGrid;
    }

    private int whereIsCameraLooking(Map<Integer, Vector3> centersOfGrid, List<Integer> ids) {
/**
 * we have a camera direction vector which changes with camera move
 * now we have to calculate the center point of all rectangles where we what to take photo
 * if the center point and camera direction vectors are collinear than we know which rectangle
 * is in center so we can take photo and put it on as a texture
 */


/**
 * is collinear when c{x,y,z}==0
 { c_{x}=a_{y}b_{z}-a_{z}b_{y}}
 { c_{y}=a_{z}b_{x}-a_{x}b_{z}}
 { c_{z}=a_{x}b_{y}-a_{y}b_{x}}
 */
        Vector3 direction = camera.direction;
        Vector3 isCollinear;
        for (int i : ids) {
            //check if the point is in front of us and not on the opposite side
            if (direction.x * centersOfGrid.get(i).x >= 0
                    && direction.y * centersOfGrid.get(i).y >= 0
                    && direction.z * centersOfGrid.get(i).z >= 0) {
                isCollinear = new Vector3(
                        direction.y * centersOfGrid.get(i).z - direction.z * centersOfGrid.get(i).y,
                        direction.z * centersOfGrid.get(i).x - direction.x * centersOfGrid.get(i).z,
                        direction.x * centersOfGrid.get(i).y - direction.y * centersOfGrid.get(i).x
                );
                if ((isCollinear.x < 0.5f && isCollinear.x > -0.5f)
                        && (isCollinear.y < 0.5f && isCollinear.y > -0.5f)
                        && (isCollinear.z < 0.5f && isCollinear.z > -0.5f)) {
                    if (i == 10 || i == 21 || i == 32 || i == 43 || i == 54 || i == 65)
                        return -1;
                    return i;
                }
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

    @Override
    public int canTakePicture() {
        return whereIsCameraLooking(centersOfGrid, ids);
    }

    @Override
    public void startRendering() {
        canRender = true;
    }

    @Override
    public void stopRendering() {
        canRender = false;
    }

    @Override
    public void updateRender() {
        isUpdated = false;
    }
}
