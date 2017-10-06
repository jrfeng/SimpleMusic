package jrfeng.musicplayer.utils.durable;

public interface Durable {
    void restore();

    void restoreAsync(OnRestoredListener listener);

    boolean isRestored();

    void save();

    void saveAsync(OnSavedListener listener);

    interface OnRestoredListener {
        void onRestored();
    }

    interface OnSavedListener {
        void onSaved();
    }
}
