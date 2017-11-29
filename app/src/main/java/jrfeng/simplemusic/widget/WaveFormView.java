package jrfeng.simplemusic.widget;

import android.Manifest;
import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.audiofx.Visualizer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import jrfeng.simplemusic.R;

public class WaveFormView extends View implements LifecycleObserver {
    private Visualizer mVisualizer;
    private byte[] mData;
    private float[] mPoints;
    private int mCenterCutLength;

    private Paint mPaint = new Paint();
    private Rect mRect = new Rect();

    private int mStaticCount;
    private boolean mEnabled;

    public WaveFormView(Context context) {
        this(context, null);
    }

    public WaveFormView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mData = null;
        mEnabled = false;

        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.WaveFormView);

        int color = typedArray.getColor(R.styleable.WaveFormView_waveFormColor, Color.RED);
        int strokeWidth = typedArray.getDimensionPixelOffset(R.styleable.WaveFormView_waveFormWidth, 2);
        mCenterCutLength = typedArray.getDimensionPixelOffset(R.styleable.WaveFormView_waveFormCenterCutWidth, 0);

        mPaint.setStrokeWidth(strokeWidth);
        mPaint.setAntiAlias(true);
        mPaint.setColor(color);
        mPaint.setStyle(Paint.Style.STROKE);

        typedArray.recycle();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onStart() {
        //调试
        Log.d("WaveForm", "onStart");

        if (mVisualizer != null) {
            mVisualizer.setEnabled(true);
            mEnabled = true;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onStop() {
        //调试
        Log.d("WaveForm", "onStop");

        if (mVisualizer != null) {
            mVisualizer.setEnabled(false);
            mEnabled = false;
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mVisualizer != null) {
            mVisualizer.release();
            mVisualizer = null;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mData == null) {
            return;
        }

        if (isStatic()) {
            if (mStaticCount > 200) {
                mEnabled = false;
                mStaticCount = 0;
                mVisualizer.setEnabled(false);
            }
            return;
        }

        mRect.set(0, 0, getWidth(), getHeight());

        if (mPoints == null || mPoints.length < mData.length * 4) {
            mPoints = new float[mData.length * 4];
        }

        int half = mData.length / 2;
        int cutHalf = mCenterCutLength / 2;

        int halfHeight = mRect.height() / 2;

        for (int i = 0; i < mData.length - 1; i++) {
            if (cutHalf > 0 && i > half - cutHalf && i < half + cutHalf) {
                continue;
            }
            mPoints[i * 4] = mRect.width() * i / (mData.length - 1);
            mPoints[i * 4 + 1] = halfHeight + ((byte) (mData[i] + 128)) * 128 / halfHeight / 2;

            mPoints[i * 4 + 2] = mRect.width() * (i + 1) / (mData.length - 1);
            mPoints[i * 4 + 3] = halfHeight + ((byte) (mData[i + 1] + 128)) * 128 / halfHeight / 2;
        }
        canvas.drawLines(mPoints, mPaint);
    }

    //*****************public***************

    public boolean init(int audioSession, @NonNull Lifecycle lifecycle) {
        //调试
        Log.d("WaveForm", "初始化");

        boolean result = checkRecordAudioPermission();

        if (result) {
            lifecycle.addObserver(this);

            mVisualizer = new Visualizer(audioSession);
            mVisualizer.setDataCaptureListener(new Visualizer.OnDataCaptureListener() {
                @Override
                public void onWaveFormDataCapture(Visualizer visualizer, byte[] bytes, int i) {
                    updateVisualizer(bytes);
                }

                @Override
                public void onFftDataCapture(Visualizer visualizer, byte[] bytes, int i) {

                }
            }, Visualizer.getMaxCaptureRate(), true, false);
            setEnabled(true);
        } else {
            Log.e("WaveFormView", "需要 RECORD_AUDIO 权限。");
        }

        return result;
    }

    public void setWaveFormWidth(int width) {
        mPaint.setStrokeWidth(width);
    }

    public void setWaveFromColor(int color) {
        mPaint.setColor(color);
    }

    public void setCenterCutLength(int length) {
        mCenterCutLength = Math.max(0, length);
    }

    public void setEnabled(boolean enabled) {
        if (mVisualizer != null) {
            mEnabled = enabled;
            mVisualizer.setEnabled(enabled);
        }
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    //****************private*****************

    private void updateVisualizer(byte[] ftt) {
        mData = ftt;
        invalidate();
    }

    private boolean isStatic() {
        byte first = mData[0];
        for (byte data : mData) {
            if (data != first) {
                mStaticCount = 0;
                return false;
            }
        }
        mStaticCount++;
        return true;
    }

    private boolean checkRecordAudioPermission() {
        int result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.RECORD_AUDIO);
        return result == PackageManager.PERMISSION_GRANTED;
    }
}

