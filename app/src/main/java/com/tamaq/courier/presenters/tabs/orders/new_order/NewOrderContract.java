package com.tamaq.courier.presenters.tabs.orders.new_order;

import android.location.Location;

import com.google.android.gms.maps.model.LatLngBounds;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface NewOrderContract {

    interface View extends BaseView {

        void switchViewsToAcceptedState();

        void showTimeIsOutDialog();

        void applyPolyline(String polyline, int color);

        void applyLatLngBounds(LatLngBounds latLngBounds);

        void setUserMarker(double lat, double lng);

        void onOrderAcceptedByClient();

        void onOrderNotAcceptedByClient();

        void onClientNotSelectedAnyExecutor();

        void showMapLoader();

        void hideMapLoader();
    }

    interface Presenter extends BasePresenter<View> {

        void loadPolylines(double latClient, double lngClient,
                           double latRestaurant, double lngRestaurant);

        void acceptOrder(String orderId);

        OrderRealm getOrderById(String orderId);

        Location getLastUserLocation();

        void saveOrderRespondedId(String orderId);

        void startNotificationsCheck(String orderId);

        void saveOrderRespondedTimerInfo(int timerTotalTime, int lastEmittedTimerValue);

        void checkNeedShowTimeIsOutDialog(String orderId);

        void changeWorkStatus(boolean isWorking);
    }

}
