package study.acodexm;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = "AndroidManifest.xml")
public class JustTesting {
    @Test
    public void justTest() throws Exception {
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat("ddMMyyyyHHmmss");
        System.out.println(simple.format(date));
//        StringBuilder sb = new StringBuilder(simple.format(date));
//        sb.append(".png");
//        sb.insert(0, "panorama_");
//        String file=sb.toString();
        String file = "/storage/emulated/0/PanoramaApp/panorama_15012018215307.png";
        file = file.substring(file.indexOf("panorama_"));
        System.out.println(file);
        Pattern num = Pattern.compile("\\d+");
        Matcher mN = num.matcher(file);
        double s;
        double current = 0;
        if (mN.find()) {
            s = Double.parseDouble(mN.group());
//            Log.d(TAG,"number found: "+s);
//            imagesPath.put(s, fileCurrent.getPath());
            if (current < s)
                current = s;
        }
        System.out.println(current);
    }

}
