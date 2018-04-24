package study.acodexm.utils;

import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.Mat;

import java.util.ArrayList;
import java.util.List;

import study.acodexm.PicturePosition;
import study.acodexm.settings.PictureMode;

import static study.acodexm.AndroidCamera.LAT;
import static study.acodexm.AndroidCamera.LON;

public class ImagePicker {

    private static final String TAG = ImagePicker.class.getSimpleName();

    private static List<Integer> maxArea(PicturePosition position) {
        int input[][] = position.getGrid();
        int temp[] = new int[input[0].length];
        MaximumHistogram mh = new MaximumHistogram();
        int maxArea = 0;
        int area;
        List<Integer> pictures = new ArrayList<>();
        List<Integer> tempPictures = new ArrayList<>();
        for (int i = 0; i < input.length; i++) {
            for (int j = 0; j < input[0].length; j++) {
                if (input[i][j] == 0) {
                    temp[j] = 0;
                    int rm = tempPictures.indexOf(position.calculatePosition(i, j));
                    if (rm != -1)
                        tempPictures.remove(rm);
                } else if (i == Math.round(LON / 2)) {
                    temp[j] += 2 * input[i][j];
                    tempPictures.add(2 * position.calculatePosition(i, j));
                } else {
                    temp[j] += input[i][j];
                    tempPictures.add(position.calculatePosition(i, j));
                }
            }
            area = mh.maxHistogram(temp);
            if (area > maxArea) {
                maxArea = area;
                pictures = new ArrayList<>(tempPictures);
            }
        }
        return pictures;
    }

    public static List<Mat> loadPictures(PictureMode pictureMode, PicturePosition instance) {
        List<Mat> pictures = new ArrayList<>();
        switch (pictureMode) {
            case auto:
                for (int id : instance.getTakenPictures())
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case panorama:
                List<Integer> longestIDS = idsForPanorama(instance.getTakenPictures());
                if (longestIDS != null && longestIDS.size() > 0)
                    for (int id : longestIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "panorama loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case widePicture:
                List<Integer> optimalIDS = maxArea(instance);
                if (optimalIDS != null && optimalIDS.size() > 0)
                    for (int id : optimalIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "widePicture loadPictures failed: ", new Throwable("empty list or null"));
                break;
            case picture360:
                //this will work only when whole sphere is filled with pictures
                if (instance.getTakenPictures().size() == LAT * LON)
                    for (int id : instance.getTakenPictures())
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                else
                    Log.e(TAG, "Picture360 loadPictures failed: ", new Throwable("not enough pictures"));
                break;
        }

        return pictures;
    }

    private static List<Integer> idsForPanorama(List<Integer> positions) {
        return null;
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

