package com.tamaq.courier.presenters.tabs.orders.map_route;

import android.location.Location;

import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenter;
import com.tamaq.courier.presenters.base.BaseView;


public interface MapRouteContract {

    interface View extends BaseView {

        void displayUserMarker(Location location);

        void applyPolyline(String polyline, int color);

        void onPolylinesLoaded(LatLngBounds latLngBounds);

        Marker provideUserMarker();

        void onOrderStatusChanged();

        void displayPreviousCoordinates(double lat, double lng, float zoom);

        void displayDistanceIsTooLong();
    }

    interface Presenter extends BasePresenter<View> {

        OrderRealm getOrderById(String orderId);

        Location getLastUserLocation();

        void loadPolylines(double latClient, double lngClient,
                           double latRestaurant, double lngRestaurant);

        void changeOrderStatus(String orderId, OrderStatus orderStatus);

        void checkHasPreviousCoordinates();

        void saveMapLastCoordinates(double lat, double lng, float zoom);
    }

}
