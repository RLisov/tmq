package com.tamaq.courier.controllers.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.tamaq.courier.R;
import com.tamaq.courier.model.ui.SingleTextUIItem;

import java.util.ArrayList;
import java.util.List;

import rx.functions.Action1;

public class SpinnerTwoLinedAdapter<T extends SingleTextUIItem> extends ArrayAdapter<T> {

    private Spinner mSpinner;
    private String title;
    private List<T> mList = new ArrayList<>();
    private Action1<T> selectedAction;
    private T selectedItem;

    public SpinnerTwoLinedAdapter(Context context) {
        super(context, 0);

    }

    public void setObjects(List<T> items) {
        mList = new ArrayList<>(items);
        setSelectedItem(items.get(0));
        notifyDataSetChanged();
        if (mSpinner != null)
            initSpinner();
    }

    private void initSpinner() {
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (!mList.isEmpty()) {
                    selectedItem = mList.get(position);
                    notifyDataSetChanged();
                    if (selectedAction != null)
                        selectedAction.call(selectedItem);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @SuppressLint({"ViewHolder", "InflateParams"})
    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (mSpinner == null) {
            mSpinner = (Spinner) parent;
            initSpinner();
        }

        convertView = LayoutInflater.from(getContext()).inflate(R.layout.two_lined_text_view, null);
        if (convertView != null) {
            TextView tvTitle = (TextView) convertView.findViewById(R.id.titleTextView);
            tvTitle.setText(title);
            TextView tvSubTitle = (TextView) convertView.findViewById(R.id.subTitleTextView);
            tvSubTitle.setText(selectedItem.getTitleUI());
        }
        return convertView;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        ItemViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.simple_spinner_dropdown_item_layout, null);
            viewHolder = new ItemViewHolder();
            viewHolder.textView = (TextView) convertView.findViewById(R.id.textView);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ItemViewHolder) convertView.getTag();
        }

        SingleTextUIItem item = mList.get(position);
        viewHolder.textView.setText(item.getTitleUI());
        boolean isSelected = selectedItem != null && selectedItem.equals(item);
        viewHolder.textView.setSelected(isSelected);

        return convertView;
    }

    public void setSelectedAction(Action1<T> selectedAction) {
        this.selectedAction = selectedAction;
    }

    public void selectItemById(int id) {
        String value = String.valueOf(id);
        selectItemById(value);
    }

    public void selectItemById(String id) {
        for (T checkableSingleTextItem : mList) {
            if (checkableSingleTextItem.getIdUI().equals(id)) {
                setSelectedItem(checkableSingleTextItem);
                mSpinner.setSelection(mList.indexOf(checkableSingleTextItem));
                notifyDataSetChanged();
                return;
            }
        }
    }

    public SingleTextUIItem getSelectedItem() {
        return selectedItem;
    }

    public void setSelectedItem(T selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSpinner(Spinner spinner) {
        this.mSpinner = spinner;
    }

    static class ItemViewHolder {
        TextView textView;
    }
}
