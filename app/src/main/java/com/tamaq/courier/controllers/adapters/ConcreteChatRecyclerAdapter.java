package com.tamaq.courier.controllers.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.Target;
import com.jakewharton.rxbinding2.view.RxView;
import com.tamaq.courier.R;
import com.tamaq.courier.model.database.MessageRealm;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.HelperCommon;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.functions.Action2;


public class ConcreteChatRecyclerAdapter
        extends RecyclerView.Adapter<ConcreteChatRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<MessageRealm> mList;
    private Action2<Bitmap, ImageView> mOnPhotoClickAction;
    private Action2<String, Integer> mOnMessageClick;

    public ConcreteChatRecyclerAdapter(Context context) {
        mList = new ArrayList<>();
        mContext = context;
    }

    public void setObjects(List<MessageRealm> countryRealmList) {
        mList = countryRealmList;
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_concrete_chat, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final MessageRealm messageRealm = mList.get(position);
        changeMessageType(messageRealm, holder);
        displayContent(messageRealm, holder);
    }

    private void changeMessageType(MessageRealm message, ViewHolder holder) {
        holder.managerIcon.setVisibility(View.GONE);
        LinearLayout.LayoutParams layoutParams =
                (LinearLayout.LayoutParams) holder.elementContainerView.getLayoutParams();

        Drawable drawable;
        switch (message.getType()) {
            case 1:
            case 2:
                layoutParams.gravity = Gravity.START;
                layoutParams.rightMargin = HelperCommon.dpToPx(32);
                layoutParams.leftMargin = HelperCommon.dpToPx(16);
                drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_message_14x);
                if (message.getType() == 2) {
                    holder.managerIcon.setVisibility(View.VISIBLE);
                    drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_message_34x);
                }

                break;
            default:
                layoutParams.gravity = Gravity.END;
                layoutParams.rightMargin = HelperCommon.dpToPx(16);
                layoutParams.leftMargin = HelperCommon.dpToPx(32);
                drawable = ContextCompat.getDrawable(mContext, R.drawable.bg_message_24x);
                break;
        }

        int currentPosition = holder.getAdapterPosition();
        int previousPosition = currentPosition - 1;
        if ((previousPosition >= 0
                && Objects.equals(mList.get(previousPosition).getType(), message.getType()))) {
            layoutParams.topMargin = HelperCommon.dpToPx(8);
        } else if (currentPosition != 0) layoutParams.topMargin = HelperCommon.dpToPx(24);

        holder.elementContainerView.setBackground(drawable);
        holder.elementContainerView.setLayoutParams(layoutParams);
    }

    private void displayContent(MessageRealm message, ViewHolder holder) {
        if (!message.isSent()) {
            holder.unsuccessfulIcon.setVisibility(View.VISIBLE);
            RxView.clicks(holder.elementContainerView).subscribe(o ->
                    mOnMessageClick.call(message.getId(), holder.getAdapterPosition()));
        } else {
            holder.unsuccessfulIcon.setVisibility(View.GONE);
        }

        holder.timeTextView.setText(convertTimeInChatFragment(message.getTime()));
        if (message.getMessage() != null)
            holder.messageText.setText(message.getMessage());
        if (message.getImageUrl() != null && !message.getImageUrl().isEmpty()) {
            setImage(message, holder);
        } else {
            holder.messageText.setVisibility(View.VISIBLE);
            holder.image.setVisibility(View.GONE);
            holder.backgroundForImage.setVisibility(View.GONE);
            holder.progressImage.setVisibility(View.GONE);
        }
    }

    private String convertTimeInChatFragment(String timeInServerFormat) {
        String resultString;

        int messageDay;
        int yesterday;

        Calendar calendar = Calendar.getInstance();

        calendar.add(Calendar.DATE, -1);
        yesterday = calendar.get(Calendar.DAY_OF_YEAR);

        Date date = DateHelper.parseDateFromString(timeInServerFormat);
        calendar.setTime(date);

        messageDay = calendar.get(Calendar.DAY_OF_YEAR);

        //We cant use switch here. Calendar values are constants and compiler isn't happy.
        // http://i.imgur.com/vHIk6cy.png
        // http://i.imgur.com/x7VOAcN.png

        if (messageDay > yesterday) {
            resultString = String.format("%s, %s", mContext.getString(R.string.today), DateHelper.getStringFromDate(date, DateHelper.TIME_FORMAT));
        } else if (messageDay == yesterday) {
            resultString = String.format("%s, %s", mContext.getString(R.string.yesterday), DateHelper.getStringFromDate(date, DateHelper.TIME_FORMAT));
        } else {
            resultString = DateHelper.getStringFromDate(date, DateHelper.PAYMENT_DATE_FORMAT);
        }

        return resultString;
    }

    private void setImage(MessageRealm message, final ViewHolder holder) {
        holder.messageText.setVisibility(View.GONE);
        holder.image.setVisibility(View.VISIBLE);
        holder.backgroundForImage.setVisibility(View.VISIBLE);
        holder.progressImage.setVisibility(View.VISIBLE);

        int resultWidth = message.getWidth() != 0 ? message.getWidth() : 175;
        int resultHeight = message.getHeight() != 0 ? message.getHeight() : 120;

        int resultWidthForViewInPx = HelperCommon.dpToPx(resultWidth > resultHeight ? 175 : 120);
        int resultHeightForViewInPx = HelperCommon.dpToPx(resultWidth > resultHeight ? 120 : 175);

        ViewGroup.LayoutParams imageLayoutParams = holder.image.getLayoutParams();
        imageLayoutParams.width = resultWidthForViewInPx;
        imageLayoutParams.height = resultHeightForViewInPx;
        holder.image.setLayoutParams(imageLayoutParams);

        ViewGroup.LayoutParams backgroundLayoutParams = holder.backgroundForImage.getLayoutParams();
        backgroundLayoutParams.width = resultWidthForViewInPx;
        backgroundLayoutParams.height = resultHeightForViewInPx;
        holder.backgroundForImage.setLayoutParams(backgroundLayoutParams);

        ViewCompat.setTransitionName(holder.image, message.getId());
        Glide.with(holder.image.getContext())
                .load(message.getImageUrl())
                .asBitmap()
                .override(resultWidthForViewInPx, resultHeightForViewInPx)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .centerCrop()
                .animate(R.anim.slow_fade_in)
                .listener(new RequestListener<String, Bitmap>() {
                    @Override
                    public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache,
                                                   boolean isFirstResource) {
                        if (message.isSent()) {
                            RxView.clicks(holder.image).subscribe(o -> mOnPhotoClickAction.call(
                                    ((RoundedBitmapDrawable) holder.image.getDrawable()).getBitmap(), holder.image));
                        }
                        holder.progressImage.setVisibility(View.GONE);
                        holder.backgroundForImage.setVisibility(View.GONE);
                        return false;
                    }
                })
                .into(new BitmapImageViewTarget(holder.image) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        RoundedBitmapDrawable roundedBitmapDrawable = RoundedBitmapDrawableFactory.create(mContext.getResources(), resource);
                        roundedBitmapDrawable.setCornerRadius(8f);
                        holder.image.setImageDrawable(roundedBitmapDrawable);
                    }
                });

        if (message.getType() == 2) holder.managerIcon.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void addMessage(MessageRealm message) {
        mList.add(message);
        notifyItemInserted(mList.size() - 1);
    }

    public void removeMessage(int position) {
        mList.remove(position);
        notifyItemRemoved(position);
    }

    public void setOnPhotoClickAction(Action2<Bitmap, ImageView> onPhotoClickAction) {
        mOnPhotoClickAction = onPhotoClickAction;
    }

    public void setOnMessageClick(Action2<String, Integer> onMessageClick) {
        mOnMessageClick = onMessageClick;
    }

    public void updateMessageUnsent(int position) {
        mList.get(position).setSent(false);
        notifyItemChanged(position);
    }

    private static class SizeColorDrawable extends ColorDrawable {

        private int width;
        private int height;

        SizeColorDrawable(int color, int w, int h) {
            super(color);
            this.width = w;
            this.height = h;
        }

        @Override
        public int getIntrinsicHeight() {
            return height;
        }

        @Override
        public int getIntrinsicWidth() {
            return width;
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.elementContainerView)
        LinearLayout elementContainerView;

        @BindView(R.id.messageText)
        TextView messageText;

        @BindView(R.id.timeTextView)
        TextView timeTextView;

        @BindView(R.id.managerIcon)
        ImageView managerIcon;

        @BindView(R.id.unsuccessfulIcon)
        ImageView unsuccessfulIcon;

        @BindView(R.id.image)
        ImageView image;
        @BindView(R.id.backgroundForImage)
        View backgroundForImage;

        @BindView(R.id.progressImage)
        ProgressBar progressImage;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
