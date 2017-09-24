package jrfeng.simplemusic.utils.durable;

import java.io.Serializable;

public interface Durable extends Serializable {
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
