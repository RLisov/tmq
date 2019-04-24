package com.tamaq.courier.widgets;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Typeface;
import android.support.v7.widget.AppCompatTextView;
import android.util.AttributeSet;

import com.tamaq.courier.R;


public class TypeFaceTextView extends AppCompatTextView {

    public TypeFaceTextView(Context context) {
        super(context);
    }

    public TypeFaceTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        applyParams(attrs);
    }

    public void applyParams(AttributeSet attributeSet) {
//        if (isInEditMode())
//            return;
        TypedArray attrArray = getContext().obtainStyledAttributes(attributeSet, R.styleable.TypeFaceTextView);
        try {
            String typefaceName = attrArray.getString(R.styleable.TypeFaceTextView_typeface);
            setFontFromAsset(typefaceName);
        } finally {
            attrArray.recycle();
        }
    }

    private void setFontFromAsset(String typefaceName) {
        if (typefaceName != null) {
            Typeface typeFace = Typeface.createFromAsset(getContext().getAssets(), typefaceName);
            setTypeface(typeFace);
        }
    }

    public TypeFaceTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        applyParams(attrs);
    }

}
