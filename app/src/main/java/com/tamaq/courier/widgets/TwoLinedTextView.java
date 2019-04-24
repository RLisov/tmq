package com.tamaq.courier.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tamaq.courier.R;


public class TwoLinedTextView extends LinearLayout {

    private TextView textViewTitle;
    private TextView textViewSubtitle;

    public TwoLinedTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.TwoLinedTextView,
                0, 0);

        try {
            textViewTitle.setText(a.getText(R.styleable.TwoLinedTextView_titleText));
            textViewSubtitle.setText(a.getText(R.styleable.TwoLinedTextView_subtitleText));
        } finally {
            a.recycle();
        }
    }

    private void init() {
        LayoutInflater.from(getContext()).inflate(R.layout.two_lined_text_view, this, true);
        textViewTitle = (TextView) findViewById(R.id.titleTextView);
        textViewSubtitle = (TextView) findViewById(R.id.subTitleTextView);
        setOrientation(VERTICAL);
    }

    public void setTitle(CharSequence text) {
        textViewTitle.setText(text);
    }

    public void setSubTitle(CharSequence text) {
        textViewSubtitle.setText(text);
    }
}
