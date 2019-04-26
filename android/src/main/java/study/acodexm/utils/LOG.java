package study.acodexm.utils;

import android.os.Debug;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import java.util.Locale;


public class LOG {
    private static final String LOG_DIR = "/PanoramaApp/logs";
    private static final String LOG_FILE = "/logs.txt";
    private static final String LOG_PERFORMANCE_FILE = "/performance.txt";
    private static final String SEPARATOR = " <&&> ";
    private static final boolean debug = true;

    public static Runnable cpJ() {
        return () -> copyJniLogs();
    }

    private static boolean copyJniLogs(){
    File from = new File("/data/data/study.acodexm/files");
    if (from.isDirectory() && from.listFiles().length == 0) return false;
    File to = new File(Environment.getExternalStorageDirectory() + LOG_DIR);
    return from.renameTo(to);
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
            Log.d(tag, message);
            String msg = String.format(Locale.getDefault(), "%s%s%s%s%s",
                    new Date().toString(),
                    SEPARATOR,
                    message,
                    SEPARATOR,
                    value);
            p(tag, msg);
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
            Log.d(tag, message);
            String msg = String.format(Locale.getDefault(), "%s%s%s%s%s%s%f%s%f%s%f",
                    new Date().toString(),
                    SEPARATOR,
                    tag,
                    SEPARATOR,
                    message,
                    SEPARATOR + "NativeHeapAllocatedSize: ",
                    NativeHeapAllocatedSize,
                    SEPARATOR + "NativeHeapSize: ",
                    NativeHeapSize,
                    SEPARATOR + "NativeHeapFreeSize: ",
                    NativeHeapFreeSize
            );
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
                    new Date().toString(),
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
