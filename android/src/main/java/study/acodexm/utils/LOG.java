package study.acodexm.utils;

import android.os.Environment;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;


public class LOG {
    private static final String LOG_DIR = "/PanoramaApp/logs";
    private static final String LOG_FILE = "/logs.txt";
    private static final String LOG_PERFORMANCE_FILE = "/performance.txt";
    private static final String SEPARATOR = " <&&> ";
    private static final boolean debug = true;

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

    public static Runnable r(String tag, String message, Throwable tr) {
        return () -> e(tag, message, tr);
    }

    public static Runnable r(String tag, String message) {
        return () -> d(tag, message);
    }

    public static Runnable r(String tag, String message, String value) {
        return () -> p(tag, message, value);
    }

    public static Runnable r(String tag, String message, int value) {
        return () -> p(tag, message, value);
    }

    public static void s(String tag, String message, Throwable tr) {
        if (debug) {
            String msg = new Date().toString() +
                    SEPARATOR +
                    tag +
                    SEPARATOR +
                    message +
                    SEPARATOR +
                    (tr != null ? tr.getMessage() : "");
            writeLogs(msg, LOG_FILE);
        }
    }

    public static void p(String tag, String message, String value) {
        if (debug) {
            Log.d(tag, message);
            String msg = new Date().toString() +
                    SEPARATOR +
                    tag +
                    SEPARATOR +
                    message +
                    SEPARATOR +
                    value;
            writeLogs(msg, LOG_PERFORMANCE_FILE);
        }
    }

    public static void p(String tag, String message, int value) {
        if (debug) {
            Log.d(tag, message);
            String msg = new Date().toString() +
                    SEPARATOR +
                    tag +
                    SEPARATOR +
                    message +
                    SEPARATOR +
                    value;
            writeLogs(msg, LOG_PERFORMANCE_FILE);
        }
    }

    public static void s(String tag, String message) {
        s(tag, message, null);
    }

    public static void d(String tag, String message) {
        if (debug) {
            Log.d(tag, message);
            s(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable tr) {
        if (debug) {
            Log.e(tag, message, tr);
            s(tag, message, tr);
        }
    }
}
