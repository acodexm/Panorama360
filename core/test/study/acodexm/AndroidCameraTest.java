package study.acodexm;

import com.badlogic.gdx.math.Vector3;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import static org.mockito.Mockito.mock;

@RunWith(MockitoJUnitRunner.class)
public class AndroidCameraTest {


    @Test
    public void create() throws Exception {
    }

    @Test
    public void dispose() throws Exception {
    }

    @Test
    public void render() throws Exception {
    }

    @Test
    public void isCameraSteady() throws Exception {
        AndroidCamera androidCamera = mock(AndroidCamera.class);
        Vector3 cameraOld = new Vector3(0, 0, 0);
        Vector3 camera1 = new Vector3(0, 0, 0);
        Vector3 camera2 = new Vector3(1, 0, 0);
        Vector3 camera3 = new Vector3(0, 1, 0);
        Vector3 camera4 = new Vector3(0, 0, 1);
        Vector3 camera5 = new Vector3(-1, 0, 0);
        Vector3 camera6 = new Vector3(0, -1, 0);
        Vector3 camera7 = new Vector3(0, 0, -1);
//        System.out.println(androidCamera.isCameraSteady(cameraOld,camera1));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera2));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera3));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera4));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera5));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera6));
//        System.out.println(!androidCamera.isCameraSteady(cameraOld,camera7));

    }

    @Test
    public void resize() throws Exception {
    }

    @Test
    public void pause() throws Exception {
    }

    @Test
    public void resume() throws Exception {
    }

    @Test
    public void canTakePicture() throws Exception {
    }

    @Test
    public void startRendering() throws Exception {
    }

    @Test
    public void stopRendering() throws Exception {
    }

    @Test
    public void updateRender() throws Exception {
    }

}
