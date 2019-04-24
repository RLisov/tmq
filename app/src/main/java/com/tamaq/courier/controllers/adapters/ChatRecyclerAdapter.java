package com.tamaq.courier.controllers.adapters;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.tamaq.courier.R;
import com.tamaq.courier.model.database.DialogRealm;
import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.utils.DateHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import de.hdodenhof.circleimageview.CircleImageView;

public class ChatRecyclerAdapter extends RecyclerView.Adapter<ChatRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<DialogRealm> mList;

    public ChatRecyclerAdapter(Context context) {
        mList = new ArrayList<>();
        mContext = context;
    }

    public ChatRecyclerAdapter(List<DialogRealm> list, Context context) {
        mList = new ArrayList<>(list);
        mContext = context;
    }

    public void setObjects(List<DialogRealm> list) {
        mList = list;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_chat, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final DialogRealm dialogRealm = mList.get(position);

        Glide.with(mContext)
                .load(dialogRealm.getAvatarUrl())
                .placeholder(R.drawable.user_pik_80)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .animate(android.R.anim.fade_in)
                .into(holder.avatarImage);

        holder.userNameTextView.setText(dialogRealm.getUserName());
        holder.userAddressTextView.setText(dialogRealm.getUserAddress());

        setBadgeCount(holder, dialogRealm);
        setMessagePreview(holder, dialogRealm);
        setDate(holder, dialogRealm);
    }

    private void setBadgeCount(ViewHolder holder, DialogRealm dialogRealm) {
        int notificationCount = 0;

        for (MessageRealm messageRealm : dialogRealm.getMessages()) {
            if (messageRealm.getEnumType() != MessageRealm.MessageType.ME && !messageRealm.isRead()) notificationCount++;
        }

        if (notificationCount > 0) {
            holder.notificationCountTextView.setVisibility(View.VISIBLE);
            holder.notificationCountTextView.setText(String.valueOf(notificationCount));
        } else {
            holder.notificationCountTextView.setVisibility(View.GONE);
        }
    }

    private void setMessagePreview(ViewHolder holder, DialogRealm dialogRealm) {
        String previewText = mContext.getString(R.string.no_messages);
        if (dialogRealm.getMessages() != null && !dialogRealm.getMessages().isEmpty()) {

            MessageRealm lastMessage = getLastMessage(dialogRealm);
            previewText = lastMessage.getMessage();
            if (lastMessage.getImageUrl() != null && !lastMessage.getImageUrl().isEmpty()) {
                previewText = mContext.getString(R.string.image);
            }
        }
        holder.messagePreviewTextView.setText(previewText);
    }

    private void setDate(ViewHolder holder, DialogRealm dialogRealm) {
        Date currentDate = DateHelper.parseDateFromString(DateHelper.getCurrentDateString());
        Date lastMessageDate;
        if (dialogRealm.getMessages() != null && !dialogRealm.getMessages().isEmpty()) {
            MessageRealm lastMessage = getLastMessage(dialogRealm);
            lastMessageDate = DateHelper.parseDateFromString(lastMessage.getTime());
        } else lastMessageDate = DateHelper.parseDateFromString(dialogRealm.getCreatedDate());

        long duration = -(lastMessageDate.getTime() - currentDate.getTime());
        long diffInMinutes = TimeUnit.MILLISECONDS.toMinutes(duration);

        int timeInMinutes = (int) diffInMinutes;
        int timeInHours = timeInMinutes / 60;
        int timeInDays = timeInHours / 24;

        String resultTimeString;
        if (timeInMinutes < 60) {
            resultTimeString = timeInMinutes <= 0
                    ? mContext.getString(R.string.right_now)
                    : getDateStringFormat(timeInMinutes, mContext.getString(R.string.minutes));
        } else if (timeInHours > 23) resultTimeString = getDateStringFormat(timeInDays, mContext.getString(R.string.days));
        else resultTimeString = getDateStringFormat(timeInHours, mContext.getString(R.string.hours));

        holder.timeTextView.setText(resultTimeString);
    }

    private String getDateStringFormat(int timeInHours, String postfix) {
        return String.format(Locale.getDefault(), "%d %s", timeInHours, postfix);
    }

    private MessageRealm getLastMessage(DialogRealm dialogRealm) {
        List<MessageRealm> messageList = new ArrayList<>(dialogRealm.getMessages());
        Collections.sort(messageList, (o1, o2) -> {
            Date firstDate = DateHelper.parseDateFromString(o1.getTime());
            Date secondDate = DateHelper.parseDateFromString(o2.getTime());
            return firstDate.compareTo(secondDate);
        });
        return messageList.get(messageList.size() - 1);
    }

    public DialogRealm getDialog(int position) {
        if (position < 0) position = 0;
        return mList.get(position);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void removeItemByPosition(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.elementContainerView)
        View elementContainerView;
        @BindView(R.id.avatarImage)
        CircleImageView avatarImage;
        @BindView(R.id.userNameTextView)
        TextView userNameTextView;
        @BindView(R.id.userAddressTextView)
        TextView userAddressTextView;
        @BindView(R.id.timeTextView)
        TextView timeTextView;
        @BindView(R.id.notificationCountTextView)
        TextView notificationCountTextView;
        @BindView(R.id.messagePreviewTextView)
        TextView messagePreviewTextView;
        @BindView(R.id.undoTextView)
        TextView undoTextView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
