package com.tamaq.courier.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CheckBox;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tamaq.courier.R;


public class CheckBoxAdvanced extends FrameLayout implements Checkable {

    private TextView textView;
    private CheckBox checkBox;
    private LinearLayout parentLayout;
    private CompoundButton.OnCheckedChangeListener checkedChangeListener;

    public CheckBoxAdvanced(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.CheckBoxAdvanced,
                0, 0);

        try {
            CharSequence text = a.getText(R.styleable.CheckBoxAdvanced_text);
            textView.setText(text);
            textView.setCompoundDrawablesWithIntrinsicBounds(a.getResourceId(R.styleable.CheckBoxAdvanced_drawableLeft, 0), 0, 0, 0);
            textView.setCompoundDrawablePadding(a.getDimensionPixelSize(R.styleable.CheckBoxAdvanced_drawableLeftPadding, 0));
            String typefaceName = a.getString(R.styleable.CheckBoxAdvanced_typeface);
            if (typefaceName != null) {
                Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), typefaceName);
                textView.setTypeface(typeFace);
            }

            parentLayout.setPadding(
                    a.getDimensionPixelSize(R.styleable.CheckBoxAdvanced_contentPaddingLeft, 0),
                    0,
                    a.getDimensionPixelSize(R.styleable.CheckBoxAdvanced_contentPaddingRight, 0),
                    0);
        } finally {
            a.recycle();
        }
    }

    private void init() {
        parentLayout = (LinearLayout) LayoutInflater.from(getContext()).inflate(R.layout.check_box_advanced, this, false);
        this.addView(parentLayout);
        textView = (TextView) parentLayout.findViewById(R.id.textView);
        checkBox = (CheckBox) parentLayout.findViewById(R.id.checkBox);
        parentLayout.setOnClickListener(view -> {
            if (!isEnabled())
                return;
            checkBox.setChecked(!checkBox.isChecked());
            if (checkedChangeListener != null)
                checkedChangeListener.onCheckedChanged(checkBox, checkBox.isChecked());
        });
    }

    public void setCheckedChangeListener(CompoundButton.OnCheckedChangeListener checkedChangeListener) {
        this.checkedChangeListener = checkedChangeListener;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        parentLayout.setEnabled(enabled);
        textView.setEnabled(enabled);
        checkBox.setEnabled(enabled);
    }

    @Override
    public void toggle() {
        setChecked(!isChecked());
    }

    public boolean isChecked() {
        return checkBox.isChecked();
    }

    public void setChecked(boolean checked) {
        checkBox.setChecked(checked);
    }
}
