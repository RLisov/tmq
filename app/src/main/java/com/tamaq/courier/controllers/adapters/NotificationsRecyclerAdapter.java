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
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class NotificationsRecyclerAdapter extends RecyclerView.Adapter<NotificationsRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<NotificationRealm> mOriginalList;
    private List<NotificationRealm> mList;
    private Listener mListener;
    private String mSearchString;

    public NotificationsRecyclerAdapter(Context context) {
        mOriginalList = new ArrayList<>();
        mList = new ArrayList<>();
        mContext = context;
    }

    public void setObjects(List<NotificationRealm> list) {
        mOriginalList = new ArrayList<>(list);
        mList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_notifications, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final NotificationRealm notification = mList.get(position);
        holder.titleTextView.setText(notification.getTitleUI(mContext));
        holder.descriptionTextView.setText(notification.getDescription());
        holder.descriptionTextView.setSingleLine();

        highlightSearch(notification.getTitleUI(mContext), holder.titleTextView);
        highlightSearch(notification.getDescription(), holder.descriptionTextView);

        RxView.clicks(holder.elementContainerView).subscribe(aVoid ->
                mListener.onItemClick(notification.getId()));

        String stringDate = DateHelper.getStringFromDate(
                DateHelper.parseDateFromString(notification.getDate()),
                DateHelper.NOTIFICATION_FORMAT);

        holder.timeTextView.setText(stringDate);
    }

    private void highlightSearch(String originalString, TextView textView) {
        String filter = "";
        if (mSearchString != null)
            filter = mSearchString;

        int startPos = originalString.toLowerCase().indexOf(filter.toLowerCase());
        int endPos = startPos + filter.length();

        if (startPos != -1) {
            Spannable spannable = new SpannableString(originalString);
            TextAppearanceSpan span = new TextAppearanceSpan(mContext, R.style.SearchTextSpan);
            spannable.setSpan(span, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            textView.setText(spannable);
        } else textView.setText(originalString);
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public void onSearchRequested(String query) {
        mSearchString = query;

        List<NotificationRealm> filteredList;

        if (query.length() >= 3) filteredList = filter(mOriginalList, query);
        else filteredList = mOriginalList;

        if (filteredList.isEmpty()) mListener.onSearchEmpty();
        else mListener.onSearchNotEmpty();

        mList.clear();
        mList.addAll(filteredList);
        notifyDataSetChanged();
    }

    private List<NotificationRealm> filter(List<NotificationRealm> list, String query) {
        List<NotificationRealm> filteredList = new ArrayList<>();

        for (NotificationRealm notification : list) {
            String titleUI = notification.getTitleUI(mContext);
            String description = notification.getDescription();

            if (titleUI != null && titleUI.toLowerCase().contains(query.toLowerCase())
                    || description != null && description.toLowerCase().contains(query.toLowerCase()))
                filteredList.add(notification);
        }

        return filteredList;
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public interface Listener {

        void onItemClick(String notificationId);

        void onSearchEmpty();

        void onSearchNotEmpty();

    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.elementContainerView)
        View elementContainerView;
        @BindView(R.id.titleTextView)
        TextView titleTextView;
        @BindView(R.id.descriptionTextView)
        TextView descriptionTextView;
        @BindView(R.id.timeTextView)
        TextView timeTextView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
