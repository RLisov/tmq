package com.tamaq.courier.presenters.tabs.payments;

public enum PaymentPeriod {

    YEAR("year"), MONTH("month"), WEEK("week"), ALL_TIME("all");

    private String mPeriod;

    PaymentPeriod(String period) {
        mPeriod = period;
    }

    public String getPeriodString() {
        return mPeriod;
    }
}
