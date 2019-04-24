package com.tamaq.courier.controllers.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.model.ui.CheckableUIItem;
import com.tamaq.courier.model.ui.SingleTextUIItem;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action1;

public class CheckableSingleTextItemAdapter<T extends CheckableUIItem & SingleTextUIItem> extends
        RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<T> mList = new ArrayList<>();
    private Context context;
    private Action1<T> clickListener;
    private T selectedItem;

    public CheckableSingleTextItemAdapter(Context context) {
        this.context = context;
    }

    public void setObjects(List<T> objects) {
        mList = new ArrayList<>(objects);
        notifyDataSetChanged();
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.checkable_single_recycler_item,
                viewGroup, false);
        return new ItemViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, final int position) {
        final T item = mList.get(position);
        final ItemViewHolder itemViewHolder = (ItemViewHolder) viewHolder;
        if (selectedItem != null) {
            boolean isSelected = selectedItem.equals(item) && selectedItem.isCheckedUI();
            itemViewHolder.clickableLayout.setSelected(isSelected);
            itemViewHolder.checkImage.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            itemViewHolder.textView.setSelected(isSelected);
        }
        itemViewHolder.textView.setText(item.getTitleUI());
        itemViewHolder.clickableLayout.setOnClickListener(v -> selectItem(item, true));
    }

    private void selectItem(T item, boolean callListener) {
        if (selectedItem == null || !selectedItem.equals(item)) {
            if (selectedItem != null)
                selectedItem.setCheckedUI(false);
            selectedItem = item;
            selectedItem.setCheckedUI(true);
        } else
            return;
        if (clickListener != null && callListener)
            clickListener.call(item);
        notifyDataSetChanged();
    }

    public void setSelectedItem(int itemPosition, boolean callListener) {
        T object = mList.get(itemPosition);
        selectItem(object, callListener);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public T getItem(int positionInAdapter) {
        return mList.get(positionInAdapter);
    }


    public void setClickListener(Action1<T> clickListener) {
        this.clickListener = clickListener;
    }

    public List<T> getmList() {
        return mList;
    }

    public void selectItemById(String id, boolean callListener) {
        for (T checkableSingleTextItem : mList) {
            if (checkableSingleTextItem.getIdUI().equals(id)) {
                selectItem(checkableSingleTextItem, callListener);
                notifyDataSetChanged();
                return;
            }
        }
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.clickableLayout)
        View clickableLayout;
        @BindView(R.id.textView)
        TextView textView;
        @BindView(R.id.checkImage)
        View checkImage;

        ItemViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
