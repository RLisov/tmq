package com.tamaq.courier.controllers.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.model.ui.TimeWithEventUIItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TimeWithEventAdapter<T extends TimeWithEventUIItem> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mList = new ArrayList<>();
    private Context context;


    public TimeWithEventAdapter(Context context) {
        this.context = context;
    }

    public void setObjects(List<T> objects) {
        mList = new ArrayList<>(objects);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.time_with_event_recycler_item,
                viewGroup, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final T item = mList.get(position);
        final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        itemViewHolder.textViewFirst.setText(item.getEventTitleUI());
        itemViewHolder.textViewSecond.setText(item.getEventTimeUI());
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }


    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.textViewFirst)
        TextView textViewFirst;
        @BindView(R.id.textViewSecond)
        TextView textViewSecond;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
