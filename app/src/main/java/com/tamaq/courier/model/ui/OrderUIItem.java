package com.tamaq.courier.model.ui;

import android.content.Context;

public interface OrderUIItem {

    String getArriveTimeUI();

    String getRestAddressUI();

    String getRestNameUI();

    double getPaymentUI();

    double getPaymentChangeUI();

    double getProfitUI();

    String getClientAddressUI();

    double getRestLatUI();

    double getRestLngUI();

    double getClientLatUI();

    double getClientLngUI();

    String getAcceptedTimeUI();

    String getArriveToClientTimeUI();

    String getClientPhoneUI();

    String getDispatcherPhoneUI();

    String getRestaurantPhoneUI();

    String getFormattedArriveTime(Context context);
}
