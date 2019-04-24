package com.tamaq.courier.controllers.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.model.database.LocationRealm;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class CountryRecyclerAdapter extends RecyclerView.Adapter<CountryRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<LocationRealm> mOriginalList;
    private List<LocationRealm> mList;
    private int lastSelectedIndex = -1;
    private Listener mListener;
    private String mSearchString;

    public CountryRecyclerAdapter(List<LocationRealm> locationRealmList, Context context) {
        mOriginalList = new ArrayList<>(locationRealmList);
        mList = new ArrayList<>(locationRealmList);
        mContext = context;
    }

    public void setObjects(List<LocationRealm> locationRealmList) {
        mOriginalList = locationRealmList;
        mList = locationRealmList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_country, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocationRealm locationRealm = mList.get(position);

        holder.countryName.setText(locationRealm.getValueRu());
        holder.countryCode.setText(locationRealm.getCountryCodeString());

        highlightSearch(locationRealm.getValueRu(), holder.countryName);
        highlightSearch(locationRealm.getCountryCodeString(), holder.countryCode);

        RxView.clicks(holder.countryContainer).subscribe(aVoid ->
                mListener.onCountryClick(locationRealm, position));

        holder.countryName.setSelected(locationRealm.isChosen());
        holder.countryCode.setSelected(locationRealm.isChosen());
        holder.countryContainer.setSelected(locationRealm.isChosen());
    }

    private void highlightSearch(String originalString, TextView textView) {
        String filter = "";
        if (mSearchString != null)
            filter = mSearchString;

        String itemValue = originalString;

        int startPos = itemValue.toLowerCase().indexOf(filter.toLowerCase());
        int endPos = startPos + filter.length();

        if (startPos != -1) {
            Spannable spannable = new SpannableString(itemValue);
            TextAppearanceSpan span = new TextAppearanceSpan(mContext, R.style.SearchTextSpan);
            spannable.setSpan(span, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else
            textView.setText(itemValue);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void onSearchRequested(String query) {
        mSearchString = query;

        List<LocationRealm> filteredList;

        if (query.length() >= 2) filteredList = filter(mOriginalList, query);
        else filteredList = mOriginalList;

        if (filteredList.isEmpty()) mListener.onSearchEmpty();
        else mListener.onSearchNotEmpty();

        mList.clear();
        mList.addAll(filteredList);
        notifyDataSetChanged();
    }

    private List<LocationRealm> filter(List<LocationRealm> locationRealmList, String query) {
        List<LocationRealm> filteredList = new ArrayList<>();

        for (LocationRealm locationRealm : locationRealmList) {
            if (locationRealm.getValueRu().toLowerCase().contains(query.toLowerCase())
                    || locationRealm.getCountryCodeString().toLowerCase().contains(query.toLowerCase()))
                filteredList.add(locationRealm);
        }

        return filteredList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setSelected(int position) {
        if (lastSelectedIndex >= 0) {
            mList.get(lastSelectedIndex).setChosen(false);
            notifyItemChanged(lastSelectedIndex);
        }

        mList.get(position).setChosen(true);
        lastSelectedIndex = position;
        notifyItemChanged(position);
    }

    public interface Listener {

        void onCountryClick(LocationRealm locationRealm, int position);

        void onSearchEmpty();

        void onSearchNotEmpty();

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.countryNameTextView)
        TextView countryName;
        @BindView(R.id.countryCodeTextView)
        TextView countryCode;
        @BindView(R.id.countryElementContainerView)
        View countryContainer;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
