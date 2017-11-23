package study.acodexm;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.FPSLogger;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
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

public class ShaderTest implements ApplicationListener {
    private static final String TAG = ShaderTest.class.getSimpleName();
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private PerspectiveCamera camera;
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
    private List<Integer> ids;
    private boolean calculated = false;
    private Map<Integer, Vector3> centersOfGrid;
    private List<Integer> takenPictures;
    private FPSLogger fpsLogger;


    @Override
    public void create() {
        isUpdated = false;
        lat = 10;
        lon = 7;
        modelBatch = new ModelBatch();
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        camera = new PerspectiveCamera(50, 2f * aspectRatio, 2f);
        camera.position.set(0f, 0f, 0f);
        camera.lookAt(0, 0f, -4.0f);
        camera.near = 1f;
        camera.far = 300f;

        camera.update();
        camController = new CameraInputController(camera);
        Gdx.input.setInputProcessor(camController);
        //sphere model template
        mModelBuilder = new ModelBuilder();
        sphereTemplate = mModelBuilder.createSphere(4f, 4f, 4f, lat, lon,
                new Material(),
                Usage.Position | Usage.Normal | Usage.TextureCoordinates);
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
        takenPictures = new ArrayList<Integer>();
        fpsLogger = new FPSLogger();
    }


    @Override
    public void render() {
        camController.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (!isUpdated) {
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
        if (!calculated)
            centersOfGrid = calculateCenterList(vector3s, ids);
        int position = whereIsCameraLooking(centersOfGrid, ids);
        if (position != -1) {
            if (!takenPictures.contains(position)) {
                takenPictures.add(position);
                LOG.d(TAG, "take picture!! at: " + position);
            }
        }
        fpsLogger.log();
    }

    @Override
    public void dispose() {
        shader.dispose();
        sphereTemplate.dispose();
        photoSphere.dispose();
        modelBatch.dispose();

    }

    private void renderPhotos() {
        int j = 0;
        Map<Integer, String> stringSet = new HashMap<Integer, String>();
        for (int i = lat; i <= 50; i++) {
            if (i % 10 == 0) {
                j++;
                stringSet.put(i + j - 1, "empty.png");
            }
            stringSet.put(i + j, (i - 10) + ".png");
        }
        ids = new ArrayList<Integer>();
        for (int i = lat; i <= (lat * lon - lat) + j; i++) {
            ids.add(i);
        }

        System.out.println(stringSet);
        photoSphere = setPhotoOnSphere(vector3s, mModelBuilder, stringSet, ids);
        instance = new ModelInstance(photoSphere);
    }

    private Model setPhotoOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, String> fileName, List<Integer> ids) {
        int attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        modelBuilder.begin();
        for (int id : ids) {
            System.out.println(fileName.get(id));
            Texture texture = new Texture("data/numbers/" + fileName.get(id));
            Material material = new Material();
            material.set(
                    TextureAttribute.createDiffuse(texture),
                    ColorAttribute.createSpecular(1f, 1f, 1f, 1f),
                    new BlendingAttribute(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA, 0.3f));
            modelBuilder.part("id" + id, GL20.GL_TRIANGLES, attr, material)
                    .rect(
                            fourVertices.get(id + lat + 1).x, fourVertices.get(id + lat + 1).y, fourVertices.get(id + lat + 1).z,//00
                            fourVertices.get(id + lat + 2).x, fourVertices.get(id + lat + 2).y, fourVertices.get(id + lat + 2).z,//01
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
            isCollinear = new Vector3(
                    direction.y * centersOfGrid.get(i).z - direction.z * centersOfGrid.get(i).y,
                    direction.z * centersOfGrid.get(i).x - direction.x * centersOfGrid.get(i).z,
                    direction.x * centersOfGrid.get(i).y - direction.y * centersOfGrid.get(i).x
            );
            if ((isCollinear.x < 0.5f && isCollinear.x > -0.5f)
                    && (isCollinear.y < 0.5f && isCollinear.y > -0.5f)
                    && (isCollinear.z < 0.5f && isCollinear.z > -0.5f)) {
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