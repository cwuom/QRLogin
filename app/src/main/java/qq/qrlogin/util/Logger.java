package qq.qrlogin.util;

public class Logger {

    private Logger() {}

    private static final String TAG = "[QRLogin]";

    public static void e(String msg) {
        android.util.Log.e(TAG, msg);
    }

    public static void e(String tag, String msg) {
        android.util.Log.e(TAG, tag + ": "+ msg);
    }

    public static void w(String msg) {
        android.util.Log.w(TAG, msg);
    }
    public static void w(String tag, String msg) {
        android.util.Log.w(TAG, tag + ": "+ msg);
    }

    public static void i(String msg) {
        android.util.Log.i(TAG, msg);
    }
    public static void i(String tag, String msg) {
        android.util.Log.i(TAG, tag + ": "+ msg);
    }


    public static void d(String msg) {
        android.util.Log.d(TAG, msg);
    }
    public static void d(String tag, String msg) {
        android.util.Log.d(TAG, tag + ": "+ msg);
    }

    public static void v(String msg) {
        android.util.Log.v(TAG, msg);
    }
    public static void v(String tag, String msg) {
        android.util.Log.v(TAG, tag + ": "+ msg);
    }

    public static void e(Throwable e) {
        android.util.Log.e(TAG, e.toString(), e);
    }

    public static void w(Throwable e) {
        android.util.Log.w(TAG, e.toString(), e);
    }

    public static void i(Throwable e) {
        android.util.Log.i(TAG, e.toString(), e);
    }

    public static void i(Throwable e, boolean output) {
        android.util.Log.i(TAG, e.toString(), e);
    }

    public static void d(Throwable e) {
        android.util.Log.d(TAG, e.toString(), e);
    }

    public static void e(String msg, Throwable e) {
        android.util.Log.e(TAG, msg, e);
    }

    public static void w(String msg, Throwable e) {
        android.util.Log.w(TAG, msg, e);
    }

    public static void i(String msg, Throwable e) {
        android.util.Log.i(TAG, msg, e);
    }

    public static void d(String msg, Throwable e) {
        android.util.Log.d(TAG, msg, e);
    }

    public static String getStackTraceString(Throwable th) {
        return android.util.Log.getStackTraceString(th);
    }
}
