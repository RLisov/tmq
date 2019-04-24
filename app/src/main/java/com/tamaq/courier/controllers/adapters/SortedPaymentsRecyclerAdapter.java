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
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.PaymentSortedRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;
import com.tamaq.courier.utils.DateHelper;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SortedPaymentsRecyclerAdapter extends RecyclerView.Adapter<SortedPaymentsRecyclerAdapter.ViewHolder> {

    private final Context mContext;
    private List<PaymentSortedRealm> mList;
    private Listener mListener;
    private PaymentPeriod mPaymentPeriod;
    private BasePresenter presenter;

    public SortedPaymentsRecyclerAdapter(Context context, PaymentPeriod paymentPeriod,
                                         BasePresenter presenter) {
        mList = new ArrayList<>();
        mContext = context;
        mPaymentPeriod = paymentPeriod;
        this.presenter = presenter;
    }

    public void setObjects(List<PaymentSortedRealm> list) {
        mList = new ArrayList<>(list);
        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_recycler_payment, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaymentSortedRealm sortedPayment = mList.get(position);

        if (mPaymentPeriod == PaymentPeriod.ALL_TIME) {
            RxView.clicks(holder.itemContainer).subscribe(o ->
                    mListener.onAllTimeItemClicked(sortedPayment.getPaymentList().get(0).getId()));
        } else {
            RxView.clicks(holder.itemContainer).subscribe(o ->
                    mListener.onPeriodItemClicked(sortedPayment.getDateString(),
                            sortedPayment.getTimeGap(), mPaymentPeriod));
        }

        String dateResultString = sortedPayment.getDateString();
        holder.dateTextView.setText(dateResultString);

        switch (mPaymentPeriod) {
            case WEEK:
                dateResultString += String.format(Locale.getDefault(), " (%d)", sortedPayment.getPaymentList().size());
                int startPos = dateResultString.lastIndexOf(" ");
                int endPos = dateResultString.length();

                Spannable spannable = new SpannableString(dateResultString);
                TextAppearanceSpan span = new TextAppearanceSpan(mContext, R.style.PaymentAllTimeTextSpan);
                spannable.setSpan(span, startPos, endPos, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);

                holder.dateTextView.setText(spannable);
                break;

            case ALL_TIME:
                PaymentRealm payment = sortedPayment.getPaymentList().get(0);
                holder.orderTextView.setVisibility(View.VISIBLE);
                String subtitleString;

                if (sortedPayment.getPaymentList().get(0).getOrderNumber() != 0) {
                    subtitleString = String.format(Locale.getDefault(), "%s %s",
                            mContext.getString(R.string.order),
                            sortedPayment.getPaymentList().get(0).getOrderNumber());

                    dateResultString = String.format(Locale.getDefault(), "%s. %s",
                            DateHelper.getStringFromDate(DateHelper.parseDateFromString(payment.getFullDate()),
                                    DateHelper.PAYMENT_DATE_FORMAT), payment.getTypeUI(mContext));
                } else {
                    subtitleString = payment.getTypeUI(mContext);
                    dateResultString = String.format(Locale.getDefault(), "%s. %s",
                            DateHelper.getStringFromDate(DateHelper.parseDateFromString(payment.getFullDate()),
                                    DateHelper.PAYMENT_DATE_FORMAT), sortedPayment.getValue() < 0
                                    ? mContext.getString(R.string.duty)
                                    : mContext.getString(R.string.replenishment));
                }

                holder.orderTextView.setText(subtitleString);
                holder.dateTextView.setText(dateResultString);
                break;
        }

        DecimalFormat format = new DecimalFormat("#.##");
        String resultMoneyString = String.format("%s %s", format.format(sortedPayment.getValue()), presenter.getUserCurrency());
        holder.moneyTextView.setText(resultMoneyString);
    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    public interface Listener {
        void onPeriodItemClicked(String periodString, int timeGap, PaymentPeriod paymentPeriod);

        void onAllTimeItemClicked(String paymentId);
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.itemContainer)
        View itemContainer;
        @BindView(R.id.dateAndTypeTextView)
        TextView dateTextView;
        @BindView(R.id.orderTextView)
        TextView orderTextView;
        @BindView(R.id.moneyTextView)
        TextView moneyTextView;

        ViewHolder(final View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }

}
