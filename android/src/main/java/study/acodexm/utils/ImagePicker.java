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
    public int maximum(int input[][]) {
        int temp[] = new int[input[0].length];
        MaximumHistogram mh = new MaximumHistogram();
        int maxArea = 0;
        int area = 0;
        for (int[] anInput : input) {
            for (int j = 0; j < temp.length; j++) {
                if (anInput[j] == 0) {
                    temp[j] = 0;
                } else {
                    temp[j] += anInput[j];
                }
            }
            area = mh.maxHistogram(temp);
            if (area > maxArea) {
                maxArea = area;
            }
        }
        return maxArea;
    }

    public static void main(String args[]) {
        int input[][] = {{1, 1, 1, 0},
                {1, 1, 1, 1},
                {0, 1, 1, 0},
                {0, 1, 1, 1},
                {1, 0, 0, 1},
                {1, 1, 1, 1}};
        ImagePicker mrs = new ImagePicker();
        int maxRectangle = mrs.maximum(input);
        //System.out.println("Max rectangle is of size " + maxRectangle);
        assert maxRectangle == 8;
    }

    public static List<Mat> loadPictures(PictureMode pictureMode, PicturePosition instance) {
        List<Mat> pictures = new ArrayList<>();
        switch (pictureMode) {
            case auto:
                for (int id : instance.getTakenPictures())
                    pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                break;
            case panorama:
                List<Integer> longestIDS = idsForPanorama(positions);
                if (longestIDS != null && longestIDS.size() > 0)
                    for (int id : longestIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "panorama loadPictures failed: ",
                            new Throwable("empty list or null"));
                break;
            case widePicture:
                List<Integer> optimalIDS = idsForWide(positions).get(0);
                if (optimalIDS != null && optimalIDS.size() > 0)
                    for (int id : optimalIDS) {
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                    }
                else
                    Log.e(TAG, "widePicture loadPictures failed: ",
                            new Throwable("empty list or null"));
                break;
            case picture360:
                //this will work only when whole sphere is filled with pictures
                if (instance.getTakenPictures().size() == LAT * LON)
                    for (int id : instance.getTakenPictures())
                        pictures.add(bitmapToMat(ImageRW.loadImageExternal(id)));
                else
                    Log.e(TAG, "Picture360 loadPictures failed: ",
                            new Throwable("not enough pictures"));
                break;
        }

        return pictures;
    }

    private static List<Integer> idsForPanorama(List<PicturePosition> positions) {
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

