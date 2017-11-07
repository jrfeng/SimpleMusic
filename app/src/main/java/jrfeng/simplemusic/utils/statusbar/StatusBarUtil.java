package jrfeng.simplemusic.utils.statusbar;

import android.app.Activity;
import android.os.Build;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class StatusBarUtil {
    public static void lightMode(Activity activity) {
        if (toggleMIUIStatusMode(activity.getWindow(), false)) {
            return;
        }

        if (toggleFlymeStatusMode(activity.getWindow(), false)) {
            return;
        }

        toggleAndroid6StatusMode(activity.getWindow(), false);
    }

    public static void darkMode(Activity activity) {
        if (toggleMIUIStatusMode(activity.getWindow(), true)) {
            return;
        }

        if (toggleFlymeStatusMode(activity.getWindow(), true)) {
            return;
        }

        toggleAndroid6StatusMode(activity.getWindow(), true);
    }

    //*************private*************

    private static boolean toggleAndroid6StatusMode(Window window, boolean dark) {
        boolean result = false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (dark) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
            result = true;
        }
        return result;
    }

    private static boolean toggleMIUIStatusMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            Class<Window> clazz = Window.class;
            try {
                int darkModeFlag = 0;
                Class layoutParams = Class.forName("android.view.MiuiWindowManager$LayoutParams");
                Field field = layoutParams.getField("EXTRA_FLAG_STATUS_BAR_DARK_MODE");
                darkModeFlag = field.getInt(layoutParams);
                if (dark) {
                    Method extraFlagField = clazz.getMethod("addExtraFlags", int.class);
                    extraFlagField.invoke(window, darkModeFlag);//状态栏透明且黑色字体
                } else {
                    Method extraFlagField = clazz.getMethod("clearExtraFlags", int.class);
                    extraFlagField.invoke(window, darkModeFlag);//清除黑色字体
                }
                result = true;
            } catch (Exception e) {
                System.err.println("Not MIUI, " + e.toString());
            }
        }
        return result;
    }

    public static boolean toggleFlymeStatusMode(Window window, boolean dark) {
        boolean result = false;
        if (window != null) {
            try {
                WindowManager.LayoutParams lp = window.getAttributes();
                Field darkFlag = WindowManager.LayoutParams.class
                        .getDeclaredField("MEIZU_FLAG_DARK_STATUS_BAR_ICON");
                Field meizuFlags = WindowManager.LayoutParams.class
                        .getDeclaredField("meizuFlags");
                darkFlag.setAccessible(true);
                meizuFlags.setAccessible(true);
                int bit = darkFlag.getInt(null);
                int value = meizuFlags.getInt(lp);
                if (dark) {
                    value |= bit;
                } else {
                    value &= ~bit;
                }
                meizuFlags.setInt(lp, value);
                window.setAttributes(lp);
                result = true;
            } catch (Exception e) {
                System.err.println("Not Flyme, " + e.toString());
            }
        }
        return result;
    }
}

