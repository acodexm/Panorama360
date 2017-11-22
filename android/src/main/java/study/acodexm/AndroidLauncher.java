package study.acodexm;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;

import com.badlogic.gdx.backends.android.AndroidFragmentApplication;

import acodexm.panorama.R;

public class AndroidLauncher extends FragmentActivity implements AndroidFragmentApplication.Callbacks {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        CameraFragment fragment = new CameraFragment();
        setContentView(R.layout.libgdx_layout);
        FragmentTransaction trans = getSupportFragmentManager().beginTransaction();
        trans.replace(R.id.libgdx_container, fragment);
        trans.commit();

    }

    @Override
    public void exit() {
    }
}