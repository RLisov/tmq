package com.tamaq.courier.widgets;

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;

import jp.wasabeef.recyclerview.animators.SlideInUpAnimator;


public class SlideUpAnimator extends SlideInUpAnimator {

    @Override
    protected void animateAddImpl(RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .translationY(0)
                .alpha(1)
                .setDuration(getAddDuration())
                .setInterpolator(mInterpolator)
                .setListener(new DefaultAddVpaListener(holder))
                .setStartDelay(0)
                .start();
    }
}
