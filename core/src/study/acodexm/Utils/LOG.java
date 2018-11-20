package study.acodexm.Utils;


import com.badlogic.gdx.Gdx;

/**
 * simple log utility class
 */
public class LOG {
    private static boolean debug = false;

    public static void d(String tag, String message) {
        if (debug) Gdx.app.log(tag, message);
    }

    public static void e(String tag, String message, Throwable ex) {
        if (debug) Gdx.app.log(tag, message, ex);
    }

}
