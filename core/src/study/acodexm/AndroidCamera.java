package study.acodexm;


import com.badlogic.gdx.Application;
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
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
    private static final String TAG = AndroidCamera.class.getSimpleName();
    private static final float offset = 0.3f;
    private int LAT;
    private int LON;
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
    private Matrix4 mat4;
    private RotationVector mRotationVector;
    private SphereControl mSphereControl;
    private SettingsControl mSettingsControl;
    private Map<Integer, Vector3> centersOfGrid;
    private ActionMode mActionMode = ActionMode.FullAuto;
    private List<Integer> ids;
    private PicturePosition mPosition;
    private boolean canRender = false;

    public AndroidCamera(RotationVector rotationVector, SphereControl sphereControl, SettingsControl settingsControl) {
        mRotationVector = rotationVector;
        mSphereControl = sphereControl;
        mSettingsControl = settingsControl;
        LAT = settingsControl.getGridSize().getLAT();
        LON = settingsControl.getGridSize().getLON();
    }

    @Override
    public void create() {
        LOG.d(TAG, "create method begin");
        Gdx.app.setLogLevel(Application.LOG_DEBUG);

        setupCamera();
        setupSphere();
        makeListOfSphereVertices();
        //initialize variables
        mat4 = new Matrix4();
        mPosition = PicturePosition.getInstance(LAT, LON, false);
        isUpdated = false;
        modelBatch = new ModelBatch();
        setIdList();
        renderPhotos();
    }

    @Override
    public void dispose() {
        LOG.d(TAG, "dispose method begin");
        if (photoSphere != null) photoSphere.dispose();
        if (modelBatch != null) modelBatch.dispose();
    }

    /**
     * this method runs in continuous loop so the changes to all objects are visible
     * every iteration view is cleared and redrawn
     */
    @Override
    public void render() {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        //update action mode in case user changed it in settings
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            mActionMode = mSettingsControl.getActionMode();
        } else if (Gdx.app.getType() == Application.ApplicationType.Desktop)
            camController.update();

        //update textures on sphere if user took a picture
        if (!isUpdated) {
            LOG.d(TAG, "update sphere textures");
            if (canRender && Gdx.app.getType() == Application.ApplicationType.Android) {
                if (mPosition.isCurrentPositionPossible()) {
                    updateSingleTextureAndroid(mPosition.calculateCurrentPosition(), mSphereControl.getPicture());
                    mPosition.saveCurrentPosition();
                }
            } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                if (mPosition.isCurrentPositionPossible()) {
                    updateSingleTextureDesktop(mPosition.calculateCurrentPosition());
                    mPosition.saveCurrentPosition();
                }
            }
            isUpdated = true;
        }
        //render sphere
        modelBatch.begin(camera);
        modelBatch.render(instance);
        modelBatch.end();

        //rotate camera as the user rotates device
        if (Gdx.app.getType() == Application.ApplicationType.Android) {
            mat4.set(mRotationVector.getValues());
            camera.up.set(mat4.val[Matrix4.M01], mat4.val[Matrix4.M02], mat4.val[Matrix4.M00]);
            camera.direction.set(-mat4.val[Matrix4.M21], -mat4.val[Matrix4.M22], -mat4.val[Matrix4.M20]);
            camera.update();
        }
        //if the action mode is set to auto than take picture in center of grid cell when camera is steady
        whereIsCameraLooking(centersOfGrid);
        if (mPosition.isCurrentPositionPossible()) {
            if (canRender && Gdx.app.getType() == Application.ApplicationType.Android && mActionMode == ActionMode.FullAuto) {
                mSphereControl.autoTakePicture();
            } else if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
                isUpdated = false;
                LOG.d(TAG, "position: " + mPosition);
            }
        }
    }

    /**
     * this method set setting for the field of view, camera position and camera controller (Desktop)
     */
    private void setupCamera() {
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(30, 2f * aspectRatio, 2f);
        camera.position.set(0f, 0f, 0f);
        camera.lookAt(0, 0f, -4.0f);
        camera.near = 0.1f;
        camera.far = 300f;
        camera.update();
        if (Gdx.app.getType() == Application.ApplicationType.Desktop) {
            camController = new CameraInputController(camera);
            Gdx.input.setInputProcessor(camController);
        }
    }

    /**
     * this method builds a temporary sphere which will be used to build sphere from rectangles
     * using nodes coordinates of this temp sphere model
     */
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

    /**
     * extract all coordinates (and bunch of more less useful values) from temp sphere model vertices
     */
    private void makeListOfSphereVertices() {
        int verticesAmount = renderable.meshPart.mesh.getNumVertices()
                * renderable.meshPart.mesh.getVertexSize() / 4;
        float vertices[] = new float[verticesAmount];
        renderable.meshPart.mesh.getVertices(vertices);
        int vSize = renderable.meshPart.mesh.getVertexSize() / 4;
        vector3s = new ArrayList<Vector3>();
        Matrix4 wt = renderable.worldTransform;
        for (int i = 0; i < vertices.length; i += vSize) {
            vector3s.add(meshToWorld(
                    vertices[i],
                    vertices[i + 1],
                    vertices[i + 2],
                    wt));
        }
        sphereTemplate.dispose();
    }

    /**
     * this method converts 4 dimensional matrix to vector of coordinates x, y, z
     * this is possible due to built in matrix properties in LibGDX lib.
     *
     * @param x
     * @param y
     * @param z
     * @param transformation
     * @return
     */
    private Vector3 meshToWorld(float x, float y, float z, Matrix4 transformation) {
        float[] q = transformation.getValues();
        Vector3 r = new Vector3();
        r.x = q[Matrix4.M00] * x + q[Matrix4.M01] * y + q[Matrix4.M02] * z + q[Matrix4.M03];
        r.y = q[Matrix4.M10] * x + q[Matrix4.M11] * y + q[Matrix4.M12] * z + q[Matrix4.M13];
        r.z = q[Matrix4.M20] * x + q[Matrix4.M21] * y + q[Matrix4.M22] * z + q[Matrix4.M23];
        return r;
    }

    /**
     * this method create collection of ids that will represent individual positions of sphere grid
     * single cells, so that they can be precisely identified
     */
    private void setIdList() {
        ids = new ArrayList<Integer>();
        //all row*col - last row + weird col switch hazard
        int amount = LAT * LON - LAT + Math.round(LAT / 2);
        for (int i = LAT; i <= amount; i++) {
            ids.add(i);
        }
        centersOfGrid = calculateCenterList(vector3s);
    }

    private void renderPhotos() {
        if (photoSphere != null)
            photoSphere.dispose();
        photoSphere = setPhotoOnSphere(vector3s, mModelBuilder, ids);
        instance = new ModelInstance(photoSphere);
    }

    /**
     * this method creates a spherical model build from rectangles. Each rectangle takes position of
     * its four corners from vertices of temp sphere model.
     * empty texture is loaded for every cell/rectangle
     * every model part has 70% of opacity
     *
     * @param fourVertices list of vertices from temp sphere model
     * @param modelBuilder
     * @param ids          indexes of each cell
     * @return new Spherical model with custom textures on its grid
     */
    private Model setPhotoOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder, List<Integer> ids) {
        int attr = VertexAttributes.Usage.Position | VertexAttributes.Usage.Normal | VertexAttributes.Usage.TextureCoordinates;
        modelBuilder.begin();
        Texture texture = null;
        BlendingAttribute attribute = new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.7f);
        ColorAttribute colorAttribute = ColorAttribute.createSpecular(1f, 1f, 1f, 1f);
        for (int id : ids) {
            try {
                texture = new Texture(Gdx.files.internal("data/empty.png"));
            } catch (Exception e) {
                LOG.e(TAG, "loading texture failed", e);
            }
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, new Material(TextureAttribute.createDiffuse(texture),
                    attribute,
                    colorAttribute))
                    .rect(
                            fourVertices.get(id + LAT + 1).x, fourVertices.get(id + LAT + 1).y, fourVertices.get(id + LAT + 1).z,
                            fourVertices.get(id + LAT + 2).x, fourVertices.get(id + LAT + 2).y, fourVertices.get(id + LAT + 2).z,
                            fourVertices.get(id + 1).x, fourVertices.get(id + 1).y, fourVertices.get(id + 1).z,
                            fourVertices.get(id).x, fourVertices.get(id).y, fourVertices.get(id).z,
                            -1, -1, -1);
        }
        return modelBuilder.end();
    }

    /**
     * this method updates spherical model with given pictures in raw data and for given position on
     * grid
     *
     * @param id
     * @param bytes
     */
    private void updateSingleTextureAndroid(int id, byte[] bytes) {
        Texture texture;
        try {
            Pixmap pixmap = new Pixmap(bytes, 0, bytes.length);
            texture = new Texture(pixmap);
        } catch (Exception e) {
            LOG.e(TAG, "texture load failed, loading empty ", e);
            texture = new Texture(Gdx.files.internal("data/empty.png"));
        }
        if (id != -1)
            updateTextures(id, texture);
    }

    /**
     * same functionality as updateSingleTextureAndroid method but for desktop
     */
    private void updateSingleTextureDesktop(int id) {
        if (id != -1 && instance.materials.size > id - LAT && id > LAT) {
            Texture texture;

            try {
                LOG.d(TAG, "loading texture " + id + ".png");
                texture = new Texture(Gdx.files.local("data/" + id + ".png"));
            } catch (Exception e) {
                LOG.e(TAG, "texture load failed, loading empty ", e);
                texture = new Texture(Gdx.files.local("data/empty.png"));
            }
            updateTextures(id, texture);
        }
    }

    private void updateTextures(int id, Texture texture) {
        try {
            for (Attribute att : instance.materials.get(id - LAT)) {
                if (att.type == TextureAttribute.Diffuse) {
                    ((TextureAttribute) att).textureDescription.texture.dispose();
                    ((TextureAttribute) att).textureDescription.set(texture,
                            Texture.TextureFilter.Linear,
                            Texture.TextureFilter.Linear,
                            Texture.TextureWrap.ClampToEdge,
                            Texture.TextureWrap.ClampToEdge);
                }
            }
        } catch (IndexOutOfBoundsException e) {
            LOG.e(TAG, "updateTextures ", e);
        }
    }


    /**
     * this method calculates a 3D position of center of each cell in sphere grid depending on vertices
     * from temp sphere model
     *
     * @param fourVertices
     * @return map of centers
     */
    private Map<Integer, Vector3> calculateCenterList(List<Vector3> fourVertices) {
        Map<Integer, Vector3> centersOfGrid = new HashMap<Integer, Vector3>();
        for (int i = 0; i < LON; i++) {
            for (int j = 0; j < LAT; j++) {
                int id = mPosition.calculatePosition(i, j);
                LOG.d(TAG, "id: " + id);
                Vector3 centerOfTex = new Vector3(
                        ((fourVertices.get(id + LAT + 1).x + fourVertices.get(id + LAT + 2).x) / 2 +
                                (fourVertices.get(id + 1).x + fourVertices.get(id).x) / 2),
                        ((fourVertices.get(id + LAT + 1).y + fourVertices.get(id + LAT + 2).y) / 2 +
                                (fourVertices.get(id + 1).y + fourVertices.get(id).y) / 2),
                        ((fourVertices.get(id + LAT + 1).z + fourVertices.get(id + LAT + 2).z) / 2 +
                                (fourVertices.get(id + 1).z + fourVertices.get(id).z) / 2));
                centersOfGrid.put(id, centerOfTex);
            }
        }
        LOG.d(TAG, "centersOfGrid" + centersOfGrid);
        return centersOfGrid;
    }

    /**
     * this method determines if vector from (0,0,0) position to point where is environment
     * camera is looking is collinear with vector from (0,0,0) to center of cell on sphere grid
     *
     * @param centersOfGrid
     * @return
     */
    private void whereIsCameraLooking(Map<Integer, Vector3> centersOfGrid) {

        /*
         * we have a camera direction vector which changes with camera move,
         * now we have to calculate the center point of all rectangles where we want to take photo,
         * if the center point and camera direction vectors are collinear than we know which rectangle
         * is in center so we can take photo in that position and name it as the position id
         *
         * is collinear when c{x,y,z}==0
         { c_{x}=a_{y}b_{z}-a_{z}b_{y}}
         { c_{y}=a_{z}b_{x}-a_{x}b_{z}}
         { c_{z}=a_{x}b_{y}-a_{y}b_{x}}
         */

        Vector3 direction = camera.direction;
        Vector3 isCollinear;
        for (int i = 0; i < LON; i++) {
            for (int j = 0; j < LAT; j++) {
                //check if the point is in front of us and not on the opposite side
                int pos = mPosition.calculatePosition(i, j);
                if (pos != -1)
                    if (direction.x * centersOfGrid.get(pos).x >= 0
                            && direction.y * centersOfGrid.get(pos).y >= 0
                            && direction.z * centersOfGrid.get(pos).z >= 0) {
                        isCollinear = new Vector3(
                                direction.y * centersOfGrid.get(pos).z - direction.z * centersOfGrid.get(pos).y,
                                direction.z * centersOfGrid.get(pos).x - direction.x * centersOfGrid.get(pos).z,
                                direction.x * centersOfGrid.get(pos).y - direction.y * centersOfGrid.get(pos).x
                        );
                        if ((isCollinear.x < offset && isCollinear.x > -offset)
                                && (isCollinear.y < offset && isCollinear.y > -offset)
                                && (isCollinear.z < offset && isCollinear.z > -offset)) {
                            mPosition.setCurrentPosition(i, j);
//                            LOG.d(TAG, "pos" + pos);
                            return;
                        }
                    }
            }
        }
    }

    /**
     * this method checks if the camera is pointing on the center of nearest texture
     *
     * @return
     */
    private boolean isCameraPointingCenter() {
        if (camera == null || mPosition == null || centersOfGrid == null) return false;
        Vector3 direction = camera.direction;
        Vector3 isCollinear;
        for (int i = 0; i < LON; i++) {
            for (int j = 0; j < LAT; j++) {
                int pos = mPosition.calculatePosition(i, j);
                if (pos != -1)
                    if (direction.x * centersOfGrid.get(pos).x >= 0
                            && direction.y * centersOfGrid.get(pos).y >= 0
                            && direction.z * centersOfGrid.get(pos).z >= 0) {
                        isCollinear = new Vector3(
                                direction.y * centersOfGrid.get(pos).z - direction.z * centersOfGrid.get(pos).y,
                                direction.z * centersOfGrid.get(pos).x - direction.x * centersOfGrid.get(pos).z,
                                direction.x * centersOfGrid.get(pos).y - direction.y * centersOfGrid.get(pos).x
                        );
                        if ((isCollinear.x < offset && isCollinear.x > -offset)
                                && (isCollinear.y < offset && isCollinear.y > -offset)
                                && (isCollinear.z < offset && isCollinear.z > -offset)) {
                            return true;
                        }
                    }
            }
        }
        return false;
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
    public boolean isCameraSteady() {
        return isCameraPointingCenter();
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
