package jrfeng.simplemusic.utils.wrapper;


import android.view.View;

public class ViewHeightWrapper {
    private View mView;

    public ViewHeightWrapper(View view) {
        mView = view;
    }

    public void setHeight(int height) {
        mView.getLayoutParams().height = height;
        mView.requestLayout();
    }

    public int getHeight() {
        return mView.getHeight();
    }
}
