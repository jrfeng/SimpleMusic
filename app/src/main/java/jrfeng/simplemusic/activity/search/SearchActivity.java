package jrfeng.simplemusic.activity.search;

import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

import jrfeng.player.data.Music;
import jrfeng.player.mode.MusicStorage;
import jrfeng.simplemusic.MyApplication;
import jrfeng.simplemusic.R;
import jrfeng.simplemusic.activity.search.adpter.SearchResultAdapter;
import jrfeng.player.base.BaseActivity;

public class SearchActivity extends BaseActivity implements SearchContract.View {
    public static final String KEY_GROUP_TYPE = "groupType";
    public static final String KEY_GROUP_NAME = "groupName";

    private static final String TAG = "SearchActivity";

    private MusicStorage.GroupType mGroupType;
    private String mGroupName;

    private SearchContract.Presenter mPresenter;

    private EditText etSearchInput;
    private ImageButton ibClear;
    private TextView tvResultHint;
    private RecyclerView rvListContainer;

    private SearchResultAdapter mAdapter;

    private ViewTreeObserver.OnGlobalLayoutListener mLayoutListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        overridePendingTransition(R.anim.opacity_in, R.anim.no_anim);

        Bundle args = getIntent().getExtras();
        mGroupType = MusicStorage.GroupType.valueOf(args.getString(KEY_GROUP_TYPE, MusicStorage.GroupType.MUSIC_LIST.name()));
        mGroupName = args.getString(KEY_GROUP_NAME, MusicStorage.MUSIC_LIST_ALL_MUSIC);
        mPresenter = new SearchPresenter(this, mGroupType, mGroupName);

        findViews();
        initViews();
        addViewListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(R.anim.no_anim, R.anim.opacity_out);
    }

    @Override
    public void setPresenter(SearchContract.Presenter presenter) {
        //该方法没有实际用处
        mPresenter = presenter;
    }

    @Override
    public void updateSearchResult(List<Music> result) {
        tvResultHint.setText("搜索到" + result.size() + "条结果");
        mAdapter.updateSearchResult(result);
    }

    @Override
    public void close() {
        finish();
    }

    //******************private***************

    private void findViews() {
        etSearchInput = findViewById(R.id.etSearchInput);
        ibClear = findViewById(R.id.ibClear);
        tvResultHint = findViewById(R.id.tvResultHint);
        rvListContainer = findViewById(R.id.rvListContainer);

    }

    private void initViews() {
        etSearchInput.setHint(getDescribe());

        mAdapter = new SearchResultAdapter(this, mPresenter);
        rvListContainer.setAdapter(mAdapter);
        rvListContainer.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));

        initGlobalLayoutListener();
    }

    private void initGlobalLayoutListener() {
        final Window window = getWindow();
        mLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            private boolean softInputShow;

            @Override
            public void onGlobalLayout() {
                Rect rect = new Rect();
                View decorView = window.getDecorView();
                decorView.getWindowVisibleDisplayFrame(rect);
                //获取屏幕的高度
                int screenHeight = decorView.getRootView().getHeight();

                //获取软件盘高度
                int softInputHeight = screenHeight - rect.bottom;

                //调试
                log("softInputHeight : " + softInputHeight);

                //调试
                log("List Height(前) : " + rvListContainer.getHeight());

                if (softInputHeight == 0 && softInputShow) {
                    softInputShow = false;
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rvListContainer.getLayoutParams();
                    lp.height = RecyclerView.LayoutParams.MATCH_PARENT;
                    rvListContainer.setLayoutParams(lp);
                    rvListContainer.requestLayout();

                    //调试
                    log("List : 恢复高度");
                } else if (softInputHeight > 0 && !softInputShow) {
                    softInputShow = true;
                    LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) rvListContainer.getLayoutParams();
                    lp.height = rvListContainer.getHeight() - softInputHeight;
                    rvListContainer.setLayoutParams(lp);
                    rvListContainer.requestLayout();

                    //调试
                    log("List : 调整高度");
                }

                //调试
                log("List Height(后) : " + rvListContainer.getHeight());
            }
        };
    }

    private void addViewListener() {
        etSearchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0 && ibClear.getVisibility() != View.VISIBLE) {
                    ibClear.setVisibility(View.VISIBLE);
                }

                if (charSequence.length() < 1) {
                    ibClear.setVisibility(View.GONE);
                }

                mPresenter.search(charSequence.toString());
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        ibClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                etSearchInput.setText("");
                ibClear.setVisibility(View.GONE);
            }
        });

        initSoftInputListener();
    }

    private String getDescribe() {
        StringBuilder describe = new StringBuilder();
        describe.append("搜索 - ");
        switch (mGroupType) {
            case MUSIC_LIST:
                switch (mGroupName) {
                    case MusicStorage.MUSIC_LIST_ALL_MUSIC:
                        describe.append("所有音乐");
                        break;
                    case MusicStorage.MUSIC_LIST_I_LOVE:
                        describe.append("我喜欢");
                        break;
                    case MusicStorage.MUSIC_LIST_RECENT_PLAY:
                        describe.append("最近播放");
                        break;
                    default:
                        describe.append("歌单 - ").append(mGroupName);
                        break;
                }
                break;
            case ARTIST_LIST:
                describe.append("歌手 - ").append(mGroupName);
                break;
            case ALBUM_LIST:
                describe.append("专辑 - ").append(mGroupName);
                break;
        }

        return describe.toString();
    }

    private void initSoftInputListener() {
        etSearchInput.getViewTreeObserver().addOnGlobalLayoutListener(mLayoutListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        etSearchInput.getViewTreeObserver().removeOnGlobalLayoutListener(mLayoutListener);
    }

    //****************调试用***************

    private static void log(String msg) {
        if (MyApplication.DEBUG) {
            Log.d(TAG, msg);
        }
    }
}
