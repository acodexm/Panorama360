package study.acodexm.utils;

import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class LOG {
    private static final String LOG_DIR = "/PanoramaApp/logs";
    private static final String LOG_FILE = "/logs.txt";
    private static final String JNI_LOG_FILE = "/jlogs.txt";
    private static final String JNI_PERFORMANCE_FILE = "/jperformance.txt";
    private static final String LOG_PERFORMANCE_FILE = "/performance.txt";
    private static final String SEPARATOR = "|";
    private static final String PATTERN = "ddMMyyyyHHmmssSSS";
    private static final boolean debug = true;

    public static Runnable cpJ() {
        return () -> {
            d("copyJniLogs success: ", String.valueOf(copyJniLogs(JNI_LOG_FILE)));
            d("copyJniLogs success: ", String.valueOf(copyJniLogs(JNI_PERFORMANCE_FILE)));
        };
    }

    private static boolean copyJniLogs(String filename) {
        File from = new File(Environment.getDataDirectory() + "/data/study.acodexm/files" + filename);
        if (!from.isFile()) {
            d("copyJniLogs", " no such file /data/study.acodexm/files" + filename);
            return false;
        }
        File file = new File(Environment.getExternalStorageDirectory() + LOG_DIR + filename);
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e("LOG", "copyJniLogs", e);
                return false;
            }
        }
        try {
            FileOutputStream fos = new FileOutputStream(file, true);
            FileInputStream fis = new FileInputStream(from);
            int c;
            while ((c = fis.read()) != -1) {
                fos.write(c);
            }
            fos.close();
            fis.close();
        } catch (FileNotFoundException e) {
            e("LOG", "copyJniLogs", e);
            return false;
        } catch (IOException e) {
            e("LOG", "copyJniLogs", e);
            return false;
        }
        return from.delete();
    }

    /**
     * WRITE MESSAGE TO SPECIFIED FILE
     *
     * @param message
     * @param filename
     */
    private static void writeLogs(String message, String filename) {
        File logFile = new File(Environment.getExternalStorageDirectory() + LOG_DIR + filename);
        boolean success = true;
        if (ImageRW.isPathCreated(LOG_DIR))
            if (!logFile.exists()) {
                try {
                    success = logFile.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        if (success)
            try {
                BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
                buf.append(message);
                buf.newLine();
                buf.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
    }

    /**
     * SAVE ERROR LOG FROM OTHER TREAD
     *
     * @param tag
     * @param message
     * @param tr
     * @return
     */
    public static Runnable r(String tag, String message, Throwable tr) {
        return () -> s(tag, message, tr);
    }

    /**
     * SAVE LOG FROM OTHER TREAD
     *
     * @param tag
     * @param message
     * @return
     */
    public static Runnable r(String tag, String message) {
        return () -> s(tag, message);
    }

    /**
     * SAVE PERFORMANCE LOG FROM OTHER TREAD
     *
     * @param tag
     * @param message
     * @param value
     * @return
     */
    public static Runnable r(String tag, String message, String value) {
        return () -> p(tag, message, value);
    }

    public static Runnable r(String tag, String message, long value) {
        return () -> p(tag, message, value + "");
    }

    /**
     * SAVE PERFORMANCE LOG VALUE
     *
     * @param tag
     * @param message
     * @param value
     * @return
     */
    public static void p(String tag, String message, String value) {
        if (debug) {
            p(tag, String.format(Locale.getDefault(), "%s%s%s",
                    message,
                    SEPARATOR,
                    value));
        }
    }

    /**
     * SAVE PERFORMANCE LOG
     *
     * @param tag
     * @param message
     * @return
     */
    public static void p(String tag, String message) {
        double NativeHeapAllocatedSize = (double) Debug.getNativeHeapAllocatedSize();
        double NativeHeapSize = (double) Debug.getNativeHeapSize();
        double NativeHeapFreeSize = (double) Debug.getNativeHeapFreeSize();
        if (debug) {
            String msg = String.format(Locale.getDefault(), "%s%s%s%s%s%s%f%s%f%s%f",
                    new SimpleDateFormat(PATTERN, Locale.getDefault()).format(new Date()),
                    SEPARATOR,
                    tag,
                    SEPARATOR,
                    message,
                    SEPARATOR + "NativeHeapAllocatedSize:" + SEPARATOR,
                    NativeHeapAllocatedSize,
                    SEPARATOR + "NativeHeapSize:" + SEPARATOR,
                    NativeHeapSize,
                    SEPARATOR + "NativeHeapFreeSize:" + SEPARATOR,
                    NativeHeapFreeSize
            );
            Log.d(tag, msg);
            writeLogs(msg, LOG_PERFORMANCE_FILE);
        }
    }

    /**
     * SAVE MESSAGE
     *
     * @param tag
     * @param message
     * @param tr      OPTIONAL
     */
    public static void s(String tag, String message, Throwable tr) {
        if (debug) {
            String msg = String.format(Locale.getDefault(), "%s%s%s%s%s%s%s",
                    new SimpleDateFormat(PATTERN, Locale.getDefault()).format(new Date()),
                    SEPARATOR,
                    tag,
                    SEPARATOR,
                    message,
                    SEPARATOR,
                    (tr != null ? tr.getMessage() : ""));
            writeLogs(msg, LOG_FILE);
        }
    }

    /**
     * SAVE MESSAGE
     *
     * @param tag
     * @param message
     */
    public static void s(String tag, String message) {
        s(tag, message, null);
    }

    /**
     * DEBUG
     *
     * @param tag
     * @param message
     */
    public static void d(String tag, String message) {
        if (debug) {
            Log.d(tag, message);
        }
    }

    /**
     * DEBUG ERROR
     *
     * @param tag
     * @param message
     * @param tr
     */
    public static void e(String tag, String message, Throwable tr) {
        if (debug) {
            Log.e(tag, message, tr);
        }
    }
}
