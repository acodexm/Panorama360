package study.acodexm;


import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.VertexAttributes.Usage;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import study.acodexm.Utils.LOG;

public class ShaderTest implements ApplicationListener {
    private static final String TAG = ShaderTest.class.getSimpleName();
    private ModelInstance instance;
    private ModelBatch modelBatch;
    private PerspectiveCamera cam;
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
    private Vector3 position;
    private float x, y, z, change, tmp;
    private float temp;
    private Matrix4 modelTransform;
    private Matrix4 instMat;
    private Matrix4 camMat;


    @Override
    public void create() {
        isUpdated = false;
        cells = 8;
        modelBatch = new ModelBatch();
        float aspectRatio = (float) Gdx.graphics.getWidth() / (float) Gdx.graphics.getHeight();
        cam = new PerspectiveCamera(50, 2f * aspectRatio, 2f);

//        cam.position.set(0.3183349f, 0.0061908923f, 0.33766082f);
//        cam.lookAt(-0.68591654f, -0.013339217f, -0.72755706f);
        cam.position.set(0f, 0f, 1f);
        cam.lookAt(0, 0f, 0);
        cam.near = 1f;
        cam.far = 300f;

        cam.update();
        camController = new CameraInputController(cam);
        Gdx.input.setInputProcessor(camController);
        //sphere model template
        mModelBuilder = new ModelBuilder();
        sphereTemplate = mModelBuilder.createSphere(2f, 4f, 2f, cells, 7,
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
//        System.out.println(vector3s.size());
//        System.out.println(vector3s);
        //new sphere model with textures
        photoSphere = setTexOnSphere(vector3s, mModelBuilder);
        instance = new ModelInstance(photoSphere);
        x = 1f;
        y = 0f;
        z = 0f;
        change = 0;
//        y=0.001f;
//        z=0.001f;
        modelTransform = new Matrix4();
        instMat = new Matrix4();
        camMat = new Matrix4();
    }

    public float calcRotation(float angle) {
        double result;
        if (angle >= 0 && temp < angle)
            result = Math.sqrt(0.977 - Math.pow(angle, 2));
        else if (angle >= 0 && temp > angle)
            result = -Math.sqrt(0.977 - Math.pow(angle, 2));
        else if (angle < 0 && temp > angle)
            result = -Math.sqrt(0.977 - Math.pow(angle, 2));
        else
            result = Math.sqrt(0.977 - Math.pow(angle, 2));
        if (Double.isNaN(result))
            result = 0;
        temp = angle;
        return (float) result;
    }

    @Override
    public void render() {
        camController.update();
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        if (!isUpdated) {
//            renderPhotos();
            isUpdated = true;
        }

        camMat = cam.combined;
        instMat = instance.transform;
//        camMat.toNormalMatrix();
//        instMat.toNormalMatrix();
        modelTransform.set(instMat);
/* Multiply the transform with the combined matrix of the camera. */
        modelTransform.mul(camMat);
/* Extract the position as usual. */
        Quaternion q = modelTransform.getRotation(new Quaternion(), true);
        Quaternion camQ = cam.projection.getRotation(new Quaternion(), true);
        Quaternion modelQ = instance.transform.getRotation(new Quaternion(), true);
//        position = modelTransform.getTranslation(new Vector3());
//        Vector3 center=new Vector3(0,0,0);
//        Vector3 direction = (center - position).normalized;
//        position = instance.transform.getTranslation(new Vector3());
//        System.out.println(position);
//        instance.transform.setFromEulerAngles(20f,y,z);
//        System.out.println(x);
        instance.transform.setFromEulerAngles(x, translateRotatePitch(), -translateRotatePitch());
        System.out.println(x + " " + camQ.getYaw() + " " + camQ.getRoll() + " " + camQ.getPitch() + " " + modelQ.getYaw() + " " + modelQ.getRoll() + " " + modelQ.getPitch());
//        System.out.println(x + " " + calcRotation(q.getYaw() / 18) * 18 + " " + q.getYaw());
        x++;
        if (change < 50 && tmp < change) {
            tmp = change;
            change++;
        } else {
            tmp = change;
            change--;
            if (change < -49)
                change = tmp + 1;
        }
//        y++;
//        z++;
//        System.out.println(atan2(position.x, position.z));
//        System.out.println(asin(-position.y));


//
//        float[] mat = new float[4 * 4];
//        Gdx.input.getRotationMatrix(mat);
//
//        Matrix4 m = new Matrix4(mat);

        modelBatch.begin(cam);
        modelBatch.render(instance);
        modelBatch.end();
        renderContext.begin();
        shader.begin(cam, renderContext);
        shader.render(renderable);
        shader.end();
        renderContext.end();
    }

    public float translateRotatePitch() {
        float axis;
        Quaternion modelQ = instance.transform.getRotation(new Quaternion(), true);
        axis = change * (float) Math.cos(Math.toRadians(modelQ.getYaw()));
        LOG.d(TAG, modelQ.getYaw() + " " + Math.cos(Math.toRadians(modelQ.getYaw())) + " " + axis + " " + change);
        return axis;
    }

    public float translateRotateRoll() {
        float axis;
        Quaternion modelQ = instance.transform.getRotation(new Quaternion(), true);
        axis = change * (float) Math.sin(Math.toRadians(modelQ.getYaw()));
        return axis;
    }

    @Override
    public void dispose() {
        shader.dispose();
        sphereTemplate.dispose();
        photoSphere.dispose();
        modelBatch.dispose();

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
        } catch (Exception e) {
            LOG.e(TAG, "no chyba nie:", e);
        }
    }

    private Model setPhotoOnSphere(List<Vector3> fourVertices, ModelBuilder modelBuilder, Map<Integer, String> fileName, List<Integer> ids) {
        int attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        modelBuilder.begin();
        for (int id : ids) {
            Texture texture = new Texture("data/texture/" + fileName.get(id));
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
        int attr = Usage.Position | Usage.Normal | Usage.TextureCoordinates;
        // Initially, the mask should have an alpha of 1
        Pixmap mask = new Pixmap(256, 256, Pixmap.Format.Alpha);

        // Cut a rectangle of alpha value 0
        mask.setBlending(Pixmap.Blending.None);
        mask.setColor(new Color(0f, 0f, 0f, 1f));
        mask.fillRectangle(0, 0, 0, 0);

        Pixmap fg = new Pixmap(Gdx.files.internal("badlogic2.png"));
        fg.drawPixmap(mask, fg.getWidth(), fg.getHeight());
        mask.setBlending(Pixmap.Blending.SourceOver);
        Texture texture = new Texture(fg);
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

    @Override
    public void resize(int width, int height) {
//        float aspectRatio = (float) width / (float) height;
//        cam= new PerspectiveCamera(67, 2f * aspectRatio, 2f);
    }

    @Override
    public void pause() {
    }

    @Override
    public void resume() {
    }
}