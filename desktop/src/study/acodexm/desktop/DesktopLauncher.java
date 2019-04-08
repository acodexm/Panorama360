package study.acodexm.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;

import study.acodexm.AndroidCamera;
import study.acodexm.settings.SettingsControl;

public class DesktopLauncher {
    public static void main(String[] arg) {
        LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
        SettingsControl settingsControl = new DesktopSettingsControl();
        new LwjglApplication(new AndroidCamera(null, null, settingsControl), config);
    }
}
