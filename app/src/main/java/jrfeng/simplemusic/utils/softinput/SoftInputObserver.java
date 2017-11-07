package jrfeng.simplemusic.utils.softinput;

import android.animation.ValueAnimator;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;

public class SoftInputObserver {

    /**
     * 监听软键盘的弹出/消失，并自动调整 EditText 的位置（以 EditText 本身作为基准）。
     *
     * @param window   EditText 所在窗口的 Window 对象。
     * @param editText EditText。
     */
    public static void autoAdjustEditText(final Window window, final View editText) {
        autoAdjustEditText(window, editText, editText);
    }

    /**
     * 监听软键盘的弹出/消失，并自动调整 EditText 的位置（可以指定一个基准 View，这样
     * 的话，调整位置时将以这个基准 View 的位置作为参照）。
     *
     * @param window   EditText 所在窗口的 Window 对象。
     * @param editText EditText。
     * @param limit    基准 View（调整位置时将以基准 View 作为参照）。
     */
    public static void autoAdjustEditText(final Window window, @NonNull final View editText, @NonNull final View limit) {
        editText.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            private int mOffset;

            @Override
            public void onGlobalLayout() {
                if (window != null) {
                    Rect rect = new Rect();
                    View decorView = window.getDecorView();
                    decorView.getWindowVisibleDisplayFrame(rect);
                    //获取屏幕的高度
                    int screenHeight = decorView.getRootView().getHeight();

                    //获取软件盘高度
                    int softInputHeight = screenHeight - rect.bottom;

                    //获取参照组件的Bottom
                    int[] locate = new int[2];
                    limit.getLocationOnScreen(locate);
                    final int contentBottom = locate[1] + limit.getHeight();

                    int contentHeight = screenHeight - contentBottom;

                    if (softInputHeight > 0) {
                        mOffset = softInputHeight - contentHeight;
                    }

                    //测试用
                    Log.d("Dialog", "***************************");
                    Log.d("Dialog", "SoftInputHeight      : " + softInputHeight);
                    Log.d("Dialog", "ContentBottom(final) : " + contentBottom);
                    Log.d("Dialog", "Offset               : " + mOffset);
                    Log.d("Dialog", "---------------------------");
                    Log.d("Dialog", "RectBottom           : " + rect.bottom);
                    Log.d("Dialog", "ScreenHeight         : " + screenHeight);
                    Log.d("Dialog", "Rect - Screen        : " + (rect.bottom - screenHeight));
                    Log.d("Dialog", "***************************");

                    if ((rect.bottom - screenHeight) < 0 && mOffset > 0) {
                        final View view = editText.getRootView();
                        ValueAnimator animator = ValueAnimator.ofInt(0, (mOffset + 16/*增量，增加组件与软键盘间的空隙*/));
                        animator.setDuration(150);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int offset = (int)valueAnimator.getAnimatedValue();
                                view.scrollTo(0, offset);
                                Log.d("Dialog", "scrollTo : " + offset);
                            }
                        });
                        animator.start();
                    } else if (mOffset > 0) {
                        final View view = editText.getRootView();
                        ValueAnimator animator = ValueAnimator.ofInt(view.getScrollY(), 0);
                        animator.setDuration(150);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                int offset = (int)valueAnimator.getAnimatedValue();
                                view.scrollTo(0, offset);
                                Log.d("Dialog", "scrollTo : " + offset);
                            }
                        });
                        animator.start();
                    }
                }
            }
        });
    }
}
