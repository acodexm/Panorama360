package study.acodexm.utils;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import acodexm.panorama.BuildConfig;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class)
public class ImageChooserTest {
    @Test
    public void bitmapToMat() throws Exception {
    }

    @Test
    public void chooseBiggest() throws Exception {
    }

    @Test
    public void idsForWide() throws Exception {
        Integer[] ints = {12, 13, 14, 15, 17, 24, 25, 26, 27, 28, 34, 35, 36, 37, 38, 39, 40, 44, 46, 47, 48, 49, 55, 56, 58, 59, 60, 62, 63, 64};
        Integer[] ints2 = {12, 14, 15, 17, 24, 25, 26, 27, 28, 34, 35, 36, 37, 38, 39, 40, 44, 46, 47, 48, 49, 55, 56, 58, 59, 62, 63, 64};
        Integer[] intsRES1 = {13, 14, 15, 24, 25, 26, 35, 36, 37, 46, 47, 48};
        Integer[] intsRES2 = {25, 26, 27, 36, 37, 38, 47, 48, 49, 58, 59, 60};
        Integer[] intsRES3 = {24, 25, 26, 27, 35, 36, 37, 38, 46, 47, 48, 49};
        List<List<Integer>> intsRES = new ArrayList<>();
        intsRES.add(Arrays.asList(intsRES1));
        intsRES.add(Arrays.asList(intsRES2));
        intsRES.add(Arrays.asList(intsRES3));
        assertEquals(intsRES, ImageChooser.idsForWide(Arrays.asList(ints)));

        List<List<Integer>> ints2RES = new ArrayList<>();
        ints2RES.add(Arrays.asList(intsRES3));
        assertEquals(ints2RES, ImageChooser.idsForWide(Arrays.asList(ints2)));
    }

    @Test
    public void makeValidMatrix() throws Exception {
    }

    @Test
    public void collinearIds() throws Exception {
    }

    @Test
    public void addIfNotContains() throws Exception {
    }

    @Test
    public void idsForPanorama() throws Exception {
        Integer[] ints = {20, 22, 23, 24, 25, 26, 27};
        Integer[] intsRES = {22, 23, 24, 25, 26, 27};
        Integer[] ints1 = {2, 3, 4, 33, 34, 36, 37, 45, 46, 47, 49, 48};
        Integer[] ints1RES = {45, 46, 47, 48, 49};
        Integer[] ints2 = {};
        Integer[] ints2RES = {};

        assertEquals(Arrays.asList(intsRES), ImageChooser.idsForPanorama(Arrays.asList(ints)));
        assertEquals(Arrays.asList(ints1RES), ImageChooser.idsForPanorama(Arrays.asList(ints1)));
        assertEquals(Arrays.asList(ints2RES), ImageChooser.idsForPanorama(Arrays.asList(ints2)));
    }

    @Test
    public void chooseLongest() throws Exception {
    }

    @Test
    public void loadPictures() throws Exception {
    }
}
