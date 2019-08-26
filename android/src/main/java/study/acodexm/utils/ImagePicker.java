package study.acodexm.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import study.acodexm.PicturePosition;
import study.acodexm.settings.PictureMode;

import static study.acodexm.utils.MaximumHistogram.maxArea;
import static study.acodexm.utils.MaximumHistogram.maxLength;


public class ImagePicker {
    private static final String TAG = ImagePicker.class.getSimpleName();


    public static List<Integer> loadPanoParts(PicturePosition position) {
        ArrayList<Integer> result = new ArrayList<>();
        int[][] grid = position.getGrid();
        for (int x = 0; x < grid.length; x++) {
            for (int y = 0; y < grid[0].length; y++) {
                if (grid[x][y] == 1 && !position.getUsedPositions().contains(position.calculatePosition(x, y))) {
                    result.add(position.calculatePosition(x, y));
                }
            }
        }
        return result;
    }

    public static List<Mat> loadPictureParts(ArrayList<Integer> ids) {
        return ids.stream().map(id -> bitmapToMat(ImageRW.loadImageExternal(id))).collect(Collectors.toList());
    }

    private static List<Mat> loadAllPictureParts(PicturePosition position) {
        List<Mat> parts = ImageRW.loadImagePartsExternal().stream()
                .map(ImagePicker::bitmapToMat)
                .collect(Collectors.toList());
        parts.addAll(loadPictureParts((ArrayList<Integer>) loadPanoParts(position)));
        return parts;
    }

    private static List<Mat> loadTestPictures() {
        return ImageRW.loadTestImagesExternal().stream()
                .map(ImagePicker::bitmapToMat)
                .collect(Collectors.toList());
    }

    public static List<Mat> loadPictures(PictureMode pictureMode, PicturePosition instance) {
        List<Mat> pictures = new ArrayList<>();
        switch (pictureMode) {
            case auto:
                for (int id : instance.getTakenPictures())
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case multithreaded:
                return loadAllPictureParts(instance);
            case panorama:
                Set<Integer> longestIDS = maxLength(instance);
                if (longestIDS != null && longestIDS.size() > 0)
                    for (int id : longestIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    LOG.e(TAG, "panorama loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case widePicture:
                Set<Integer> optimalIDS = maxArea(instance);
                if (optimalIDS != null && optimalIDS.size() > 0)
                    for (int id : optimalIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    LOG.e(TAG, "widePicture loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case picture360:
                //this will work only when whole sphere is filled with pictures
                for (int id : instance.getTakenPictures())
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case test: {
                return loadTestPictures();
            }
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
        bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, false);
        Mat mat = new Mat();
        Utils.bitmapToMat(bitmap, mat);
        bitmap.recycle();
        return mat;
    }
}

