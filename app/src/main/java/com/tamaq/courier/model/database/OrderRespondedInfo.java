package com.tamaq.courier.model.database;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import io.realm.RealmObject;

public class OrderRespondedInfo extends RealmObject {

    private String mOrderId;
    private boolean mAcceptedByUser;
    private int mTotalTime;
    private int mLeastTime;
    private Date mDateOfUpdateTime;

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public boolean isAcceptedByUser() {
        return mAcceptedByUser;
    }

    public void setAcceptedByUser(boolean acceptedByUser) {
        mAcceptedByUser = acceptedByUser;
    }

    public int getTotalTime() {
        return mTotalTime;
    }

    public void setTotalTime(int totalTime) {
        mTotalTime = totalTime;
    }

    public boolean isActual() {
        return getLeastSecondsToShow() > 0;
    }

    public int getLeastSecondsToShow() {
        int secondsDiff = getDifferenceBetweenUpdateTimeAndNow();
        return getLeastTime() - secondsDiff;
    }

    public int getDifferenceBetweenUpdateTimeAndNow() {
        Date date = new Date();
        Date lastUpdateDate = getDateOfUpdateTime() != null ? getDateOfUpdateTime() : new Date();
        return (int) TimeUnit.MILLISECONDS.toSeconds(date.getTime() - lastUpdateDate.getTime());
    }

    public int getLeastTime() {
        return mLeastTime;
    }

    public void setLeastTime(int leastTime) {
        mLeastTime = leastTime;
    }

    public Date getDateOfUpdateTime() {
        return mDateOfUpdateTime;
    }

    public void setDateOfUpdateTime(Date dateOfUpdateTime) {
        mDateOfUpdateTime = dateOfUpdateTime;
    }
}
