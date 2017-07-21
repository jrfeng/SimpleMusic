package jrfeng.simplemusic.utils;

public interface Durable {
    void restore();

    void restoreAsync(OnRestoredListener listener);

    void save();

    void saveAsync();

    interface OnRestoredListener {
        void onRestored();
    }
}
