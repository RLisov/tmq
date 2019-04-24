package com.tamaq.courier.controllers.adapters;


import android.content.Context;
import android.support.v4.content.ContextCompat;
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

public class CityRecyclerAdapter extends RecyclerView.Adapter<CityRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<LocationRealm> mOriginalList;
    private List<LocationRealm> mList;
    private int lastSelectedIndex = -1;
    private Listener mListener;
    private String mSearchString;

    public CityRecyclerAdapter(List<LocationRealm> cityRealmList, Context context) {
        mOriginalList = new ArrayList<>(cityRealmList);
        mList = new ArrayList<>(cityRealmList);
        mContext = context;
    }

    public void setObjects(List<LocationRealm> countryList) {
        mOriginalList = countryList;
        mList = countryList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_city, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final LocationRealm cityRealm = mList.get(position);
        holder.cityName.setText(cityRealm.getValueRu());

        highlightSearch(cityRealm.getValueRu(), holder.cityName);

        RxView.clicks(holder.countryContainer).subscribe(aVoid ->
                mListener.onCityClick(cityRealm, position));

        int textColor;
        int containerColor;

        if (cityRealm.isChosen()) {
            textColor = ContextCompat.getColor(mContext, R.color.dark_sky_blue_two);
            containerColor = ContextCompat.getColor(mContext, R.color.white_four);
            holder.markCityView.setVisibility(View.VISIBLE);
        } else {
            textColor = ContextCompat.getColor(mContext, R.color.black_87);
            containerColor = ContextCompat.getColor(mContext, R.color.white);
            holder.markCityView.setVisibility(View.GONE);
        }

        holder.cityName.setTextColor(textColor);
        holder.countryContainer.setBackgroundColor(containerColor);
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

    private List<LocationRealm> filter(List<LocationRealm> list, String query) {
        List<LocationRealm> filteredList = new ArrayList<>();

        for (LocationRealm cityRealm : list) {
            if (cityRealm.getValueRu().toLowerCase().contains(query.toLowerCase()))
                filteredList.add(cityRealm);
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

        void onCityClick(LocationRealm cityRealm, int position);

        void onSearchEmpty();

        void onSearchNotEmpty();

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.cityNameTextView)
        TextView cityName;
        @BindView(R.id.cityMarkView)
        View markCityView;
        @BindView(R.id.cityElementContainerView)
        View countryContainer;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
