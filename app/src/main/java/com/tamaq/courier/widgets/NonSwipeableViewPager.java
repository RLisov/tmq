package com.tamaq.courier.widgets;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;


public class NonSwipeableViewPager extends ViewPager {

    private boolean enabled;

    public NonSwipeableViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.enabled = true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return this.enabled && super.onTouchEvent(event);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return this.enabled && super.onInterceptTouchEvent(event);
    }

    public void setSwipePagingEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
