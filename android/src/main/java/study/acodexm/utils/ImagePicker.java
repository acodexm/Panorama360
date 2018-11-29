package study.acodexm.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import study.acodexm.utils.LOG;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import study.acodexm.PicturePosition;
import study.acodexm.settings.PictureMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static study.acodexm.AndroidCamera.LAT;
import static study.acodexm.AndroidCamera.LON;

public class ImagePicker {

    private static final String TAG = ImagePicker.class.getSimpleName();

    private static List<Integer> maxArea(PicturePosition position) {
        int[][] grid = position.getGrid();
        int[] temp = new int[grid[0].length];
        MaximumHistogram mh = new MaximumHistogram();
        int maxArea = 0;
        int area;
        List<Integer> pictures = new ArrayList<>();
        List<Integer> tempPictures = new ArrayList<>();
        for (int i = 0; i < grid.length; i++) {
            gridExtractor(position, grid, temp, tempPictures, i);
            area = mh.maxHistogram(temp);
            if (area > maxArea) {
                maxArea = area;
                pictures = new ArrayList<>(tempPictures);
            }
        }
        return pictures;
    }

    private static List<Integer> idsForPanorama(PicturePosition position) {
        int[][] grid = position.getGrid();
        int[] temp2 = new int[grid[0].length];
        List<Integer> pictures = new ArrayList<>();
        List<Integer> tempPictures = new ArrayList<>();
        int max = 0;
        for (int i = 0; i < grid.length; i++) {
            int temp = 0;
            gridExtractor(position, grid, temp2, tempPictures, i);
            for (int n : temp2) temp += n;
            if (max < temp) {
                max = temp;
                pictures = new ArrayList<>(tempPictures);
            }
        }
        return pictures;
    }

    private static void gridExtractor(PicturePosition position, int[][] grid, int[] temp2, List<Integer> tempPictures, int i) {
        for (int j = 0; j < grid[0].length; j++) {
            if (grid[i][j] == 0) {
                temp2[j] = 0;
                int rm = tempPictures.indexOf(position.calculatePosition(i, j));
                if (rm != -1)
                    tempPictures.remove(rm);
            } else if (i == Math.round(LON / 2)) {
                temp2[j] += 2 * grid[i][j];
                tempPictures.add(position.calculatePosition(i, j));
            } else {
                temp2[j] += grid[i][j];
                tempPictures.add(position.calculatePosition(i, j));
            }
        }
    }


    public static ArrayList<Integer> loadPanoParts(PicturePosition position, ArrayList<Integer> usedPositions) {
        ArrayList<Integer> result = new ArrayList<>();
        int[][] grid = position.getGrid();
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[0].length; j++) {
                if (grid[i][j] == 1 && !usedPositions.contains(position.calculatePosition(i, j))) {
                    result.add(position.calculatePosition(i, j));
                }
            }
        }
        return result;
    }

    public static List<Mat> loadPictureParts(ArrayList<Integer> ids) {
        return ids.stream().map(id -> bitmapToMat(ImageRW.loadImageExternal(id))).collect(Collectors.toList());
    }

    public static List<Mat> loadAllPictureParts() {
        return ImageRW.loadImagePartsExternal().stream().map(ImagePicker::bitmapToMat).collect(Collectors.toList());
    }

    public static List<Mat> loadPictures(PictureMode pictureMode, PicturePosition instance) {
        List<Mat> pictures = new ArrayList<>();
        switch (pictureMode) {
            case auto:
                for (int id : instance.getTakenPictures())
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case multithreaded:
                return loadAllPictureParts();
            case panorama:
                List<Integer> longestIDS = idsForPanorama(instance);
                if (longestIDS != null && longestIDS.size() > 0)
                    for (int id : longestIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    LOG.e(TAG, "panorama loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case widePicture:
                List<Integer> optimalIDS = maxArea(instance);
                if (optimalIDS != null && optimalIDS.size() > 0)
                    for (int id : optimalIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    LOG.e(TAG, "widePicture loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case picture360:
                //this will work only when whole sphere is filled with pictures
                if (instance.getTakenPictures().size() == LAT * LON)
                    for (int id : instance.getTakenPictures())
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                else
                    LOG.e(TAG, "Picture360 loadPictures failed: ", new Throwable("not enough pictures"));
                break;
        }

        return pictures;
    }

    /**
     * converts bitmap object to openCV Mat object
     *
     * @param bitmap
     * @return
     */
    private static Mat bitmapToMat(Bitmap bitmap) {
        Matrix matrix = new Matrix();
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
                bitmap.getHeight(), matrix, false);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        bitmap.recycle();
        return mat;
    }
}

