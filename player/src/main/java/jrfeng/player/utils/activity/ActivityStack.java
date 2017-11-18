package jrfeng.player.utils.activity;

import java.util.LinkedList;
import java.util.List;

import jrfeng.player.base.BaseActivity;

public class ActivityStack {
    private static List<BaseActivity> mStack = new LinkedList<>();

    private ActivityStack() {
    }

    public static void push(BaseActivity activity) {
        mStack.add(activity);
    }

    public static void pop(BaseActivity activity) {
        mStack.remove(activity);
    }

    public static void finishAll() {
        for (BaseActivity activity : mStack) {
            if (activity != null && !activity.isFinishing()) {
                activity.finish();
            }
        }
        mStack.clear();
    }
}
