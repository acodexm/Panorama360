package study.acodexm.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ImageRW {
    private static final String TAG = ImageRW.class.getSimpleName();

    /**
     * method saves taken individual pictures on external storage
     *
     * @param bytes
     * @param currentPictureId
     */
    public static void saveImageExternal(byte[] bytes, int currentPictureId) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp/temp");
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/" + currentPictureId + ".png";
        boolean success = true;
        isPathCreated();
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "File saving failed", e);
            }
        } else {
            Log.d(TAG, "File saving failed");
        }
    }

    private static boolean isPathCreated() {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp");
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        return success;
    }

    /**
     * method saves picture as a result from picture stitching and gives them unique names based on
     * current date time
     *
     * @param result
     * @return true if success
     */
    public static boolean saveResultImageExternal(Mat result) {
        Log.d(TAG, "saveResultImageExternal: begin saving");
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp");
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat("ddMMyyyyHHmmss", Locale.getDefault());
        final String fileName = folder.getAbsolutePath() + "/panorama_" +
                simple.format(date) + ".png";
        Log.d(TAG, "saveResultImageExternal: filename: " + fileName);
        if (isPathCreated()) {
            try {
                return Imgcodecs.imwrite(fileName, result);
            } catch (Exception e) {
                Log.e(TAG, "File saving failed", e);
            }
        } else {
            Log.d(TAG, "File saving failed");
        }
        return false;
    }

    /**
     * method deletes all pictures from temporary files if any exists
     */
    public static void deleteTempFiles() {
        isPathCreated();
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/");
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null && children.length > 0)
                for (String aChildren : children) {
                    if (new File(dir, aChildren).delete()) {
                        Log.d(TAG, "file " + aChildren.trim() + " deleted");
                    } else {
                        Log.d(TAG, "deleteTempFiles: failed");
                    }
                }
        }
    }

    /**
     * method loads images from temporary folder
     *
     * @param currentPictureId
     * @return requested image
     */
    static Bitmap loadImageExternal(int currentPictureId) {
        if (!isPathCreated())
            return null;
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/" + currentPictureId + ".png";
        Bitmap bitmap = null;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            bitmap = BitmapFactory.decodeStream(fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "File loading failed", e);
        }
        return bitmap;
    }

    static Bitmap loadImageExternal(String currentPictureId) {
        if (!isPathCreated())
            return null;
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/" + currentPictureId + ".png";
        Bitmap bitmap = null;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            bitmap = BitmapFactory.decodeStream(fos);
            fos.close();
        } catch (IOException e) {
            Log.e(TAG, "File loading failed", e);
        }
        return bitmap;
    }

}
