package study.acodexm.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;

import java.util.List;

import static android.content.Context.ACTIVITY_SERVICE;

public class PerformanceLogger {
    private ActivityManager mgr;

    public PerformanceLogger(Context context) {
        this.mgr = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
    }

    public void snap() {
        List<ActivityManager.RunningAppProcessInfo> processes = mgr.getRunningAppProcesses();
        LOG.p("DEBUG", "Running processes:", processes.toString());
        for (ActivityManager.RunningAppProcessInfo p : processes) {
            LOG.p("DEBUG", "  process name: ", p.processName);
            LOG.p("DEBUG", "pid: ", p.pid);
            int[] pids = new int[1];
            pids[0] = p.pid;
            Debug.MemoryInfo[] MI = mgr.getProcessMemoryInfo(pids);
            LOG.p("memory", "dalvik private: ", MI[0].dalvikPrivateDirty);
            LOG.p("memory", "dalvik shared: ", MI[0].dalvikSharedDirty);
            LOG.p("memory", "dalvik pss: ", MI[0].dalvikPss);
            LOG.p("memory", "native private: ", MI[0].nativePrivateDirty);
            LOG.p("memory", "native shared: ", MI[0].nativeSharedDirty);
            LOG.p("memory", "native pss: ", MI[0].nativePss);
            LOG.p("memory", "other private: ", MI[0].otherPrivateDirty);
            LOG.p("memory", "other shared: ", MI[0].otherSharedDirty);
            LOG.p("memory", "other pss: ", MI[0].otherPss);

            LOG.p("memory", "total private dirty memory (KB): ", MI[0].getTotalPrivateDirty());
            LOG.p("memory", "total shared (KB): ", MI[0].getTotalSharedDirty());
            LOG.p("memory", "total pss: ", MI[0].getTotalPss());
        }
    }
}
