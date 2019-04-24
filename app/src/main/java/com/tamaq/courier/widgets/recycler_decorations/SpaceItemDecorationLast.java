package com.tamaq.courier.widgets.recycler_decorations;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

public class SpaceItemDecorationLast extends RecyclerView.ItemDecoration {

    private int spacing;

    public SpaceItemDecorationLast(int spacing) {
        this.spacing = spacing;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        if (position == parent.getAdapter().getItemCount() - 1) {
            outRect.bottom = spacing; // item bottom
        }
    }
}