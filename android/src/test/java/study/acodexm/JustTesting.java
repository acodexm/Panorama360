package study.acodexm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Date;

import acodexm.panorama.BuildConfig;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class JustTesting {
    @Test
    public void justTest() throws Exception {
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat("HH-mm-ss__dd_MM_yyyy");
        System.out.println(simple.format(date));
    }
}
