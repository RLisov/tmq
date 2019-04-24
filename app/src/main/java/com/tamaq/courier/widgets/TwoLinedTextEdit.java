package com.tamaq.courier.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.text.InputFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.utils.HelperCommon;

import rx.functions.Action0;


public class TwoLinedTextEdit extends LinearLayout {

    private TextView textViewTitle;
    private EditText editTextSubtitle;
    private Action0 onEditTextInputDoneAction;

    public TwoLinedTextEdit(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TwoLinedTextEdit,
                0, 0);

        try {
            textViewTitle.setText(a.getText(R.styleable.TwoLinedTextEdit_titleText));
            editTextSubtitle.setText(a.getText(R.styleable.TwoLinedTextEdit_subtitleText));
        } finally {
            a.recycle();
        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.two_lined_text_edit, this, true);
        textViewTitle = (TextView) findViewById(R.id.titleTextView);
        editTextSubtitle = (EditText) findViewById(R.id.subTitleEditText);
        setOrientation(VERTICAL);
        editTextSubtitle.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                if (onEditTextInputDoneAction != null) {
                    onEditTextInputDoneAction.call();
                    return true;
                }
            }
            return false;
        });
    }

    public void setTitle(CharSequence text) {
        textViewTitle.setText(text);
    }

    public void setSubTitle(CharSequence text) {
        editTextSubtitle.setText(text);
    }

    public void requestEditTextFocus() {
        editTextSubtitle.requestFocus();
        HelperCommon.showKeyboard(getContext(), editTextSubtitle);
        editTextSubtitle.setSelection(editTextSubtitle.length());
    }

    public void clearEditTextFocus(Activity activity) {
        editTextSubtitle.clearFocus();
        HelperCommon.hideKeyboard(activity);
    }

    public void setOnEditTextInputDoneAction(Action0 onEditTextInputDoneAction) {
        this.onEditTextInputDoneAction = onEditTextInputDoneAction;
    }

    public void setUpEditTextMaxLength(int maxLength) {
        InputFilter[] fArray = new InputFilter[1];
        fArray[0] = new InputFilter.LengthFilter(maxLength);
        editTextSubtitle.setFilters(fArray);
    }

    public EditText getEditTextSubtitle() {
        return editTextSubtitle;
    }
}
