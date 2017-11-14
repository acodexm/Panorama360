package study.acodexm.Utils;


import com.badlogic.gdx.Gdx;

public class LOG {
    public static void d(String tag, String message) {
        Gdx.app.log(tag, message);
    }

    public static void e(String tag, String message, Throwable ex) {
        Gdx.app.log(tag, message, ex);
    }

}
