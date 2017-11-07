package jrfeng.simplemusic.activity.search;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.widget.EditText;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.base.BaseActivity;

public class SearchActivity extends BaseActivity {
    private EditText etSearch;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        overridePendingTransition(R.anim.alpha_in, R.anim.no_anim);

        etSearch = (EditText) findViewById(R.id.tvSearch);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.alpha_out);
    }
}
