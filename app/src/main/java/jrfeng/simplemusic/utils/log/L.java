package jrfeng.simplemusic.utils.log;


import android.util.Log;

public class L {
    private static boolean turnOnAll = true;
    private static boolean turnOnDebug = true;

    public static void d(String tag, String msg) {
        if (turnOnAll && turnOnDebug) {
            Log.d(tag, msg);
        }
    }

    public static void e(String tag, String msg) {
        if (turnOnAll) {
            Log.e(tag, msg);
        }
    }

    public static void turnOffAll() {
        turnOnAll = false;
    }

    public static void turnOnAll() {
        turnOnAll = true;
    }

    public static void turnOffDebug() {
        turnOnDebug = false;
    }

    public static void turnOnDebug() {
        turnOnDebug = true;
    }
}
