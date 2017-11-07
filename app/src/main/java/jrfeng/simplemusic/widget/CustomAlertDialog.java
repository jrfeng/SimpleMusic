package jrfeng.simplemusic.widget;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.app.AppCompatDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import jrfeng.simplemusic.R;
import jrfeng.simplemusic.utils.softinput.SoftInputObserver;

/**
 * 专为 {@link jrfeng.simplemusic.adapter.vlayout.musiclist.MusicListAdapter} 定制的 Dialog，
 * 可能不具有一般性。
 */
public class CustomAlertDialog {
    private Context mContext;
    private Style mStyle;
    private AppCompatDialog mDialog;

    private View vgContent;
    private TextView tvDialogTitle;
    private TextView tvDialogMessage;
    private View lvDialogOption;
    private ImageButton ibDialogCheckBox;
    private TextView tvDialogOptionText;
    private EditText etDialogInput;

    private Button btnDialogNegative;
    private Button btnDialogPositive;

    private boolean mOptionChecked;

    private int mArg;
    private OnButtonClickListener mPositiveButtonListener;

    public CustomAlertDialog(Context context) {
        mContext = context;
        mStyle = Style.JUST_MESSAGE;
        initDialog();
    }

    public void setStyle(Style style) {
        mStyle = style;
        switch (mStyle) {
            case JUST_MESSAGE:
                tvDialogMessage.setVisibility(View.VISIBLE);
                lvDialogOption.setVisibility(View.GONE);
                etDialogInput.setVisibility(View.GONE);
                enablePositiveButton();
                break;
            case MESSAGE_AND_OPTION:
                tvDialogMessage.setVisibility(View.VISIBLE);
                lvDialogOption.setVisibility(View.VISIBLE);
                etDialogInput.setVisibility(View.GONE);
                enablePositiveButton();
                break;
            case INPUT:
                tvDialogMessage.setVisibility(View.GONE);
                lvDialogOption.setVisibility(View.GONE);
                etDialogInput.setVisibility(View.VISIBLE);
                disablePositiveButton();
                break;
        }
    }

    public void setTitle(String title) {
        tvDialogTitle.setText(title);
    }

    public void setMessage(String message) {
        tvDialogMessage.setText(message);
    }

    public void setInputHint(String hint) {
        etDialogInput.setHint(hint);
    }

    public void setOptionText(String text) {
        tvDialogOptionText.setText(text);
    }

    public void setPositiveButtonListener(OnButtonClickListener listener) {
        mPositiveButtonListener = listener;
    }

    public void show() {
        mArg = -1;
        mDialog.show();
    }

    public void show(int arg) {
        mArg = arg;
        mDialog.show();
    }

    //***************Observer***************

    public interface OnButtonClickListener {
        void onButtonClicked(String input, boolean optionChecked, int arg);
    }

    //*****************Enum*****************

    public enum Style {
        JUST_MESSAGE,
        MESSAGE_AND_OPTION,
        INPUT
    }

    //***************private****************

    private void initDialog() {
        mDialog = new AppCompatDialog(mContext);
        mDialog.setContentView(R.layout.widget_custom_alert_dialog);
        Window window = mDialog.getWindow();
        if (window != null) {
            window.getDecorView().setBackgroundColor(Color.TRANSPARENT);
            window.getDecorView().setPadding(0, 0, 0, 0);
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            window.setAttributes(lp);
        }
        findViews();
        addViewListener();
        setStyle(mStyle);
    }

    private void findViews() {
        vgContent = mDialog.findViewById(R.id.vgContent);
        tvDialogTitle = (TextView) mDialog.findViewById(R.id.tvDialogTitle);
        tvDialogMessage = (TextView) mDialog.findViewById(R.id.tvDialogMessage);
        lvDialogOption = mDialog.findViewById(R.id.lvDialogOption);
        ibDialogCheckBox = (ImageButton) mDialog.findViewById(R.id.ibDialogCheckBox);
        tvDialogOptionText = (TextView) mDialog.findViewById(R.id.tvDialogOptionText);
        etDialogInput = (EditText) mDialog.findViewById(R.id.etDialogInput);

        btnDialogNegative = (Button) mDialog.findViewById(R.id.btnDialogNegative);
        btnDialogPositive = (Button) mDialog.findViewById(R.id.btnDialogPositive);
    }

    private void addViewListener() {
        ibDialogCheckBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mOptionChecked) {
                    mOptionChecked = false;
                    ibDialogCheckBox.setImageLevel(1);
                } else {
                    mOptionChecked = true;
                    ibDialogCheckBox.setImageLevel(2);
                }
            }
        });

        tvDialogOptionText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ibDialogCheckBox.callOnClick();
            }
        });

        etDialogInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    enablePositiveButton();
                } else {
                    disablePositiveButton();
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        //监听软键盘弹起/消失
        SoftInputObserver.autoAdjustEditText(mDialog.getWindow(), etDialogInput, vgContent);

        btnDialogNegative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mOptionChecked = false;
                ibDialogCheckBox.setImageLevel(1);
                etDialogInput.clearComposingText();
                mDialog.dismiss();
            }
        });

        btnDialogPositive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPositiveButtonListener != null) {
                    mPositiveButtonListener.onButtonClicked(
                            etDialogInput.getText().toString(),
                            mOptionChecked,
                            mArg);
                }
                mOptionChecked = false;
                ibDialogCheckBox.setImageLevel(1);
                etDialogInput.setText("");
                mDialog.dismiss();
            }
        });
    }

    private void disablePositiveButton() {
        btnDialogPositive.setEnabled(false);
        btnDialogPositive.setTextColor(mContext.getResources()
                .getColor(R.color.colorGrey300));
    }

    private void enablePositiveButton() {
        btnDialogPositive.setEnabled(true);
        btnDialogPositive.setTextColor(mContext.getResources()
                .getColor(R.color.colorBlue300));
    }
}
