package com.tamaq.courier.model.database;

import com.tamaq.courier.presenters.tabs.payments.PaymentPeriod;

import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class PaymentSortedRealm extends RealmObject {

    public static final String PERIOD_ROW = "mPeriod";
    public static final String TIME_GAP_ROW = "mTimeGap";
    public static final String CHART_DATA_ROW = "mIsChartData";

    @PrimaryKey
    private String mDateString;
    private int mTimeGap;
    private String mMonthShortString;
    private double mValue;
    private String mPeriod;
    private boolean mIsChartData;
    private RealmList<PaymentRealm> mPaymentList;

    public PaymentSortedRealm() {
    }

    public PaymentSortedRealm(String dateString, double value, PaymentPeriod period, List<PaymentRealm> paymentList) {
        this(dateString, value, period, paymentList, false);
    }

    public PaymentSortedRealm(String dateString, double value, PaymentPeriod period, List<PaymentRealm> paymentList, boolean isChartData) {
        mDateString = dateString;
        mValue = value;
        mPeriod = period.getPeriodString();
        RealmList<PaymentRealm> realmList = new RealmList<>();
        realmList.addAll(paymentList);
        mPaymentList = realmList;
        mIsChartData = isChartData;
    }

    public PaymentSortedRealm(PaymentSortedRealm sortedRealm) {
        mDateString = sortedRealm.getDateString();
        mValue = sortedRealm.getValue();
        mPeriod = sortedRealm.getPeriod();
        mIsChartData = sortedRealm.isChartData();
        mPaymentList = sortedRealm.getPaymentList();
    }

    public String getDateString() {
        return mDateString;
    }

    public void setDateString(String dateString) {
        mDateString = dateString;
    }

    public double getValue() {
        return mValue;
    }

    public void setValue(double value) {
        mValue = value;
    }

    public String getPeriod() {
        return mPeriod;
    }

    public void setPeriod(String period) {
        mPeriod = period;
    }

    public RealmList<PaymentRealm> getPaymentList() {
        return mPaymentList;
    }

    public void setPaymentList(RealmList<PaymentRealm> paymentList) {
        mPaymentList = paymentList;
    }

    public int getTimeGap() {
        return mTimeGap;
    }

    public void setTimeGap(int timeGap) {
        mTimeGap = timeGap;
    }

    public String getMonthShortString() {
        return mMonthShortString;
    }

    public void setMonthShortString(String monthShortString) {
        mMonthShortString = monthShortString;
    }

    public boolean isChartData() {
        return mIsChartData;
    }

    public void setChartData(boolean chartData) {
        mIsChartData = chartData;
    }
}
