package jrfeng.simplemusic.activity.choice;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;

import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.player.base.BaseActivity;

public class MultiChoiceActivity extends BaseActivity {
    public static final String KEY_GROUP_TYPE = "groupType";
    public static final String KEY_GROUP_NAME = "groupName";
    public static final String KEY_CHECKED_ITEM = "checkedItem";

    private static final String TAG = "MultiChoiceActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multi_choice);
        overridePendingTransition(R.anim.slide_in_up, R.anim.alpha_out);

        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.fmContainer);

        if (fragment == null) {
            Fragment multiChoiceFragment = new MultiChoiceFragment();
            Bundle args = getIntent().getExtras();
            multiChoiceFragment.setArguments(args);
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.fmContainer, multiChoiceFragment)
                    .commit();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_down);
    }

    //*******************调试用******************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
