package jrfeng.player.utils.activity;

import java.util.LinkedList;
import java.util.List;

import jrfeng.player.base.BaseActivity;

public class ActivityStack {
    private static List<BaseActivity> mStack;

    private ActivityStack() {
    }

    public synchronized static void add(BaseActivity activity) {
        if (mStack == null) {
            mStack = new LinkedList<>();
        }
        mStack.add(activity);
    }

    public static void remove(BaseActivity activity) {
        mStack.remove(activity);
    }

    public static void finishAll() {
        for (BaseActivity activity : mStack) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
        mStack.clear();
        mStack = null;
    }
}
