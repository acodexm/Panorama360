package study.acodexm.utils;


import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ImageRW {
    private static final String TAG = ImageRW.class.getSimpleName();
    private static final String MAIN_DIR = "/PanoramaApp";
    private static final String TEMP_DIR = "/PanoramaApp/temp";
    private static final String PART_DIR = "/PanoramaApp/part";
    private static final String HIST_DIR = "/PanoramaApp/archived";
    private static final String MAIN_PREFIX = "/panorama_";
    private static final String PART_PREFIX = "/part_panorama_";
    private static final String PNG = ".png";
    private static final String PATTERN = "ddMMyyyyHHmmss";

    /**
     * method saves taken individual pictures on external storage
     *
     * @param bytes
     * @param currentPictureId
     */
    public static void saveImageExternal(byte[] bytes, int currentPictureId) {
        File folder = new File(Environment.getExternalStorageDirectory() + TEMP_DIR);
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath() + TEMP_DIR + "/" + currentPictureId + PNG;
        boolean success = true;
        isPathCreated(TEMP_DIR);
        if (!folder.exists()) {
            success = folder.mkdirs();
        }
        if (success) {
            try {
                FileOutputStream fos = new FileOutputStream(fileName);
                fos.write(bytes);
                fos.close();
            } catch (IOException e) {
                LOG.e(TAG, "File saving failed", e);
            }
        } else {
            LOG.d(TAG, "File saving failed");
        }
    }

    public static boolean isPathCreated(String path) {
        File folder = new File(Environment.getExternalStorageDirectory() + path);
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
        LOG.d(TAG, "saveResultImageExternal: begin saving");
        File folder = new File(Environment.getExternalStorageDirectory() + MAIN_DIR);
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat(PATTERN, Locale.getDefault());
        final String fileName = folder.getAbsolutePath() + MAIN_PREFIX + simple.format(date) + PNG;
        LOG.d(TAG, "saveResultImageExternal: filename: " + fileName);
        if (isPathCreated(MAIN_DIR)) {
            try {
                return Imgcodecs.imwrite(fileName, result);
            } catch (Exception e) {
                LOG.e(TAG, "File saving failed", e);
            }
        } else {
            LOG.d(TAG, "File saving failed");
        }
        return false;
    }

    public static boolean savePartResultImageExternal(Mat result) {
        LOG.d(TAG, "savePartResultImageExternal: begin saving");
        File folder = new File(Environment.getExternalStorageDirectory() + PART_DIR);
        Date date = new Date();
        SimpleDateFormat simple = new SimpleDateFormat(PATTERN, Locale.getDefault());
        final String fileName = folder.getAbsolutePath() + PART_PREFIX + simple.format(date) + PNG;
        LOG.d(TAG, "saveResultImageExternal: filename: " + fileName);
        if (isPathCreated(PART_DIR)) {
            try {
                return Imgcodecs.imwrite(fileName, result);
            } catch (Exception e) {
                LOG.e(TAG, "Part File saving failed", e);
            }
        } else {
            LOG.d(TAG, "Part File saving failed");
        }
        return false;
    }

    /**
     * methods deletes all pictures from temporary files if any exists
     */
    public static void deleteTempFiles() {
        deleteFolderFiles(TEMP_DIR);
    }

    public static void deletePartFiles() {
        deleteFolderFiles(PART_DIR);
    }

    private static void deleteFolderFiles(String folder) {
        isPathCreated(folder);
        if (archive(folder))
            LOG.d(TAG, "archived " + folder);
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
        if (dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null && children.length > 0)
                for (String aChildren : children) {
                    if (new File(dir, aChildren).delete()) {
                        LOG.d(TAG, "file " + aChildren.trim() + " deleted");
                    } else {
                        LOG.d(TAG, "deleteTempFiles: failed");
                    }
                }
        }
    }

    private static boolean archive(String folder) {
        SimpleDateFormat simple = new SimpleDateFormat(PATTERN, Locale.getDefault());
        Date date = new Date();
        File from = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + folder);
        File to = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + HIST_DIR + folder + simple.format(date));
        if (isPathCreated(HIST_DIR + folder + simple.format(date)))
            return from.renameTo(to);
        return false;
    }

    /**
     * method loads images from temporary folder
     *
     * @param currentPictureId
     * @return requested image
     */
    static Bitmap loadImageExternal(int currentPictureId) {
        if (!isPathCreated(TEMP_DIR))
            return null;
        final String fileName = Environment.getExternalStorageDirectory().getAbsolutePath()
                + TEMP_DIR + "/" + currentPictureId + PNG;
        Bitmap bitmap = null;
        try {
            FileInputStream fos = new FileInputStream(fileName);
            bitmap = BitmapFactory.decodeStream(fos);
            fos.close();
        } catch (IOException e) {
            LOG.e(TAG, "File loading failed", e);
        }
        return bitmap;
    }

    public static List<Bitmap> loadImagePartsExternal() {
        isPathCreated(PART_DIR);
        List<Bitmap> result = new ArrayList<>();
        LOG.d(TAG, "load images from imagesFolder:" + PART_DIR);
        File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + PART_DIR);
        LOG.d(TAG, "loadImages file exist: " + file.exists());
        LOG.d(TAG, "loadImages file id folder: " + file.isDirectory());
        if (file.exists() && file.isDirectory()) {
            File[] listFiles = file.listFiles();
            for (File fileCurrent : listFiles) {
                if (fileCurrent.isFile()) {
                    try {
                        FileInputStream fos = new FileInputStream(fileCurrent.getPath());
                        result.add(BitmapFactory.decodeStream(fos));
                        fos.close();
                    } catch (IOException e) {
                        LOG.e(TAG, "PART File loading failed", e);
                    }
                }
            }
        }
        LOG.d(TAG, "loadImagePartsExternal parts count :" + result.size());
        return result;
    }
}
