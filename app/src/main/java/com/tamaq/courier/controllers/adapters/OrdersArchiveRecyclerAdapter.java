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
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class OrdersArchiveRecyclerAdapter extends RecyclerView.Adapter<OrdersArchiveRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<OrderRealm> mOriginalList;
    private List<OrderRealm> mList;
    private Listener mListener;
    private String mSearchString;

    public OrdersArchiveRecyclerAdapter(Context context) {
        mOriginalList = new ArrayList<>();
        mList = new ArrayList<>();
        mContext = context;
    }

    public void setObjects(List<OrderRealm> list) {
        mOriginalList = new ArrayList<>(list);
        mList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_archive_order, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final OrderRealm order = mList.get(position);

        holder.typeTextView.setText(order.getStatusForDisplay(mContext));
        holder.orderTextView.setText(mContext.getString(R.string.order_number, order.getNumber()));
        holder.addressTextView.setText(order.getAddressForArchive());

        Date dateFromString = DateHelper.parseDateFromString(order.getCreatedDate());
        String stringFromDate = DateHelper.getStringFromDate(
                dateFromString, DateHelper.NOTIFICATION_FORMAT);
        holder.dateTextView.setText(stringFromDate);

        highlightSearch(holder.typeTextView);
        highlightSearch(holder.orderTextView);
        highlightSearch(holder.addressTextView);

        RxView.clicks(holder.elementContainerView).subscribe(aVoid ->
                mListener.onItemClick(order));
    }

    private void highlightSearch(TextView textView) {
        String originalString = textView.getText().toString();
        String filter = "";
        if (mSearchString != null) filter = mSearchString;

        int startPos = originalString.toLowerCase().indexOf(filter.toLowerCase());
        int endPos = startPos + filter.length();

        if (startPos != -1) {
            Spannable spannable = new SpannableString(originalString);
            TextAppearanceSpan span = new TextAppearanceSpan(mContext, R.style.SearchTextSpan);
            spannable.setSpan(span, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else
            textView.setText(originalString);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void onSearchRequested(String query) {
        mSearchString = query;

        List<OrderRealm> filteredList;

        if (query.length() >= 1) filteredList = filter(mOriginalList, query);
        else filteredList = mOriginalList;

        if (filteredList.isEmpty()) mListener.onSearchEmpty();
        else mListener.onSearchNotEmpty();

        mList.clear();
        mList.addAll(filteredList);
        notifyDataSetChanged();
    }

    private List<OrderRealm> filter(List<OrderRealm> list, String query) {
        List<OrderRealm> filteredList = new ArrayList<>();
        String lowerCaseQuery = query.toLowerCase();

        for (OrderRealm order : list) {

            String statusForDisplay = order.getStatusForDisplay(mContext);
            Integer number = order.getNumber();
            String addressForArchive = order.getAddressForArchive();

            if (statusForDisplay != null && statusForDisplay.toLowerCase().contains(lowerCaseQuery)
                    || number != null && number.toString().toLowerCase().contains(lowerCaseQuery)
                    || addressForArchive != null && addressForArchive.toLowerCase().contains(lowerCaseQuery)
                    ) {
                filteredList.add(order);
            }
        }

        return filteredList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface Listener {

        void onItemClick(OrderRealm orderRealm);

        void onSearchEmpty();

        void onSearchNotEmpty();

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.elementContainerView)
        View elementContainerView;

        @BindView(R.id.typeTextView)
        TextView typeTextView;
        @BindView(R.id.orderTextView)
        TextView orderTextView;
        @BindView(R.id.addressTextView)
        TextView addressTextView;
        @BindView(R.id.dateTextView)
        TextView dateTextView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
