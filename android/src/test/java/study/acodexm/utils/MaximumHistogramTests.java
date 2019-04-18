package study.acodexm.utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import study.acodexm.PicturePosition;

import static study.acodexm.utils.MaximumHistogram.maxArea;
import static study.acodexm.utils.MaximumHistogram.maxLength;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MaximumHistogramTests {

    @Test
    public void testImagePicker() {
        PicturePosition position = PicturePosition.getInstance(4, 5, true);
        /* original         extended
         * 1  0  0  1  | 1  0  0  1
         * 0  1  0  0  | 0  1  0  0
         * 1! 0  1! 1! | 1  0  1  1
         * 1  0  0  0  | 1  0  0  0
         * 0  1  1  0  | 0  1  1  0
         * ! -> should be in result
         * */
        int[][] grid = {{1, 0, 1, 1, 0}, {0, 1, 0, 0, 1}, {0, 0, 1, 0, 1}, {1, 0, 1, 0, 0}};
//        int[][] grid = {{0, 0, 1, 0, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 0, 0}, {0, 0, 1, 0, 0}};// all has to appear
        position.setGrid(grid);
        Set<Integer> panoIds = maxLength(position);
        /* original         extended
         * 0  0  0  1  | 0  0  0  1
         * 0  0  0  0  | 0  0  0  0
         * 1! 0  1! 1! | 1  0  1  1
         * 1! 0  1! 1! | 1  0  1  1
         * 1  0  0  1  | 1  0  0  1
         * ! -> should be in result
         * */

        int[][] grid2 = {{0, 0, 1, 1, 1}, {0, 0, 0, 0, 0}, {0, 0, 1, 1, 0}, {1, 0, 1, 1, 1}};
//        int[][] grid2 = {{0, 0, 1, 1, 0}, {0, 0, 0, 0, 0}, {0, 0, 1, 1, 0}, {0, 0, 1, 1, 0}};// all has to appear
        position.setGrid(grid2);
        Set<Integer> maxAreaIds = maxArea(position);

        List<Integer> panoResult = Arrays.asList(17, 2, 12);
        List<Integer> maxAreaResult = Arrays.asList(17, 2, 18, 3, 12, 13);

        System.out.println(panoIds + "\n ==? \n" + panoResult + "\n\n");
        System.out.println(maxAreaIds + "\n ==? \n" + maxAreaResult);
//        assert panoIds == panoResult;
//        assert maxAreaIds == maxAreaResult;
    }

    @Test
    public void testMaxHist() {
        MaximumHistogram mh = new MaximumHistogram();
        int input[] = {2, 2, 2, 6, 1, 5, 4, 2, 2, 2, 2};
        int maxArea = mh.maxHistogram(input);
        System.out.println(maxArea);
        System.out.println(Math.round(7 / 2));
    }
}
