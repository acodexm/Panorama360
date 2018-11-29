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
    private static final boolean debug = true;


    public static Runnable r(String tag, String message, Throwable tr) {
        return () -> e(tag, message, tr);
    }

    public static Runnable r(String tag, String message) {
        return () -> d(tag, message);
    }

    public static void s(String tag, String message, Throwable tr) {
        if (debug) {
            File logFile = new File(Environment.getExternalStorageDirectory() + LOG_DIR + LOG_FILE);
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
                    buf.append(new Date().toString());
                    buf.append("  |  ");
                    buf.append(tag);
                    buf.newLine();
                    buf.append(message);
                    buf.newLine();
                    if (tr != null) {
                        buf.append(tr.getMessage());
                        buf.newLine();
                    }
                    buf.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
