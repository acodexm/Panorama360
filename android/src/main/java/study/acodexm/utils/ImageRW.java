package study.acodexm.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ImageRW {
    private static final String TAG = ImageRW.class.getSimpleName();

    public static void saveImageExternal(byte[] bytes, int currentPictureId) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp/temp");
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/" + currentPictureId + ".png";
        boolean success = true;
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

    public static boolean saveResultImageExternal(Mat result) {
        Log.d(TAG, "saveResultImageExternal: begin saving");
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp");
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat("HH-mm-ss__dd_MM_yyyy");
        final String fileName = folder.getAbsolutePath() + "/panorama_" +
                simple.format(date) + ".png";
        Log.d(TAG, "saveResultImageExternal: filename: " + fileName);
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
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

    public static void saveImageForTextureExternal(byte[] bytes, int currentPictureId,
                                                   int PHOTO_WIDTH, int PHOTO_HEIGHT) {
        File folder = new File(Environment.getExternalStorageDirectory()
                + "/PanoramaApp/temp/texture/");
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + "/PanoramaApp/temp/texture/" + currentPictureId + ".png";
        boolean success = true;
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(resizeImage(bytes, PHOTO_WIDTH, PHOTO_HEIGHT));
                fos.close();
            } catch (IOException e) {
                Log.e(TAG, "File saving failed", e);
            }
        } else {
            Log.d(TAG, "File saving failed");
        }
    }

    public static void deleteTempFiles() {
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

    static Bitmap loadImageExternal(int currentPictureId) {
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

    static byte[] resizeImage(byte[] bytes, int PHOTO_WIDTH, int PHOTO_HEIGHT) {
        Bitmap original = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
        Bitmap resized = Bitmap.createScaledBitmap(original, PHOTO_WIDTH, PHOTO_HEIGHT, false);
        ByteArrayOutputStream blob = new ByteArrayOutputStream();
        resized.compress(Bitmap.CompressFormat.PNG, 0, blob);
        byte[] resizedImg = blob.toByteArray();
        resized.recycle();
        original.recycle();
        return resizedImg;
    }
}
