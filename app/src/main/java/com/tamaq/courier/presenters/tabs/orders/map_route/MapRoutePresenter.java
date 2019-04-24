package com.tamaq.courier.presenters.tabs.orders.map_route;

import android.annotation.SuppressLint;
import android.location.Location;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.tamaq.courier.R;
import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.model.api.response.DirectionsResponse;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.shared.google_map.MarkerAnimationController;
import com.tamaq.courier.utils.LocationChangeListener;
import com.tamaq.courier.utils.LocationHelper;
import com.tamaq.courier.utils.MapAcitivityLastCoordinatesHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.Locale;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class MapRoutePresenter extends BasePresenterAbs<MapRouteContract.View>
        implements MapRouteContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp app;
    private ServerCommunicator serverCommunicator;
    private LocationChangeListener locationChangeListener;
    private Location lastUserLocation;
    private MarkerAnimationController markerAnimationController;

    public MapRoutePresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.app = app;
        this.serverCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(MapRouteContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
        markerAnimationController = new MarkerAnimationController();
        locationChangeListener = new LocationChangeListener(app, (location, duration) -> {
            if (lastUserLocation == null) {
                getView().displayUserMarker(location);
                lastUserLocation = location;
            } else {
                moveUserMarker(location, duration);
            }
            lastUserLocation = location;
        }, 1000);
    }

    private void moveUserMarker(Location location, long duration) {
        if (getView() != null && getView().provideUserMarker() != null) {
            markerAnimationController.addAnimateMarkerByPoint(
                    new LatLng(location.getLatitude(), location.getLongitude()),
                    getView().provideUserMarker(),
                    duration);
        }
    }

    @Override
    public void detachPresenter() {
        super.detachPresenter();
        if (locationChangeListener != null)
            locationChangeListener.stop();
    }

    @Override
    public OrderRealm getOrderById(String orderId) {
        return OrderDAO.getInstance().getOrderById(orderId, getRealm());
    }

    @Override
    public Location getLastUserLocation() {
        return lastUserLocation;
    }

    @Override
    public void loadPolylines(double latClient, double lngClient,
                              double latRestaurant, double lngRestaurant) {
        if (lastUserLocation != null) {
            loadPolylinesLocal(latClient, lngClient, latRestaurant, lngRestaurant);
            getView().displayUserMarker(lastUserLocation);
        } else {
            LocationHelper.getLastKnownLocationObservable(mRxFragment.getActivity(),
                    addressObservable -> addressObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(address -> {
                                lastUserLocation = new Location("");
                                lastUserLocation.setLatitude(address.getLatitude());
                                lastUserLocation.setLongitude(address.getLongitude());

                                loadPolylinesLocal(latClient, lngClient, latRestaurant, lngRestaurant);
                                getView().displayUserMarker(lastUserLocation);
                            }, onError));
        }
    }

    @SuppressLint("CheckResult")
    private void loadPolylinesLocal(double latClient, double lngClient,
                                    double latRestaurant, double lngRestaurant) {
        final LatLng latLngClient = new LatLng(latClient, lngClient);
        final LatLng latLngRestaurant = new LatLng(latRestaurant, lngRestaurant);
        final LatLng latLngUser = new LatLng(lastUserLocation.getLatitude(), lastUserLocation.getLongitude());

        Observable<DirectionsResponse> observableClientRestaurant = serverCommunicator
                .directionsObservable(
                        String.format(Locale.US, "%f,%f", latClient, lngClient),
                        String.format(Locale.US, "%f,%f", latRestaurant, lngRestaurant));

        Observable<DirectionsResponse> observableCurierRestaurant = serverCommunicator
                .directionsObservable(
                        String.format(Locale.US, "%f,%f",
                                lastUserLocation.getLatitude(),
                                lastUserLocation.getLongitude()),
                        String.format(Locale.US, "%f,%f", latRestaurant, lngRestaurant));

        Observable.zip(observableClientRestaurant, observableCurierRestaurant, Pair::new)
                .compose(mRxFragment.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(directionsPair -> {
                    checkDistanceAndDisplayPolyline(directionsPair.first, R.color.tangerine, latLngClient, latLngRestaurant);
                    checkDistanceAndDisplayPolyline(directionsPair.second, R.color.dark_sky_blue, latLngUser, latLngRestaurant);

                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    if (!directionsPair.first.routes.isEmpty()) {
                        for (LatLng latLng : PolyUtil.decode(directionsPair.first.routes.get(0).getFullPolyline()))
                            boundsBuilder.include(latLng);
                    }
                    if (!directionsPair.second.routes.isEmpty()) {
                        for (LatLng latLng : PolyUtil.decode(directionsPair.second.routes.get(0).getFullPolyline()))
                            boundsBuilder.include(latLng);
                    }
                    getView().onPolylinesLoaded(boundsBuilder.build());
                }, onError);
    }

    @SuppressLint("CheckResult")
    private void checkDistanceAndDisplayPolyline(DirectionsResponse directions, int polylineColor, LatLng firstLatLng, LatLng secondLatLng) {
        if (!directions.routes.isEmpty() && directions.routes.get(0) != null) {
            Observable.just(new Pair<>(firstLatLng, secondLatLng))
                    .map(latLngLatLngPair -> {
                        double distanceInM = SphericalUtil.computeDistanceBetween(firstLatLng, secondLatLng);
                        double distanceInKm = distanceInM / 1000;
                        return distanceInKm < 1000;
                    })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isDistanceNormal -> {
                        if (isDistanceNormal) getView().applyPolyline(directions.routes.get(0).getFullPolyline(), polylineColor);
                        else getView().displayDistanceIsTooLong();
                    }, onError);
        }
    }

    @Override
    public void checkHasPreviousCoordinates() {
        MapAcitivityLastCoordinatesHelper.MapActivityLastCoordinates coordinates =
                MapAcitivityLastCoordinatesHelper.getLastMapActivityCoordinates(
                        getView().getContext());
        if (!coordinates.isEmpty())
            getView().displayPreviousCoordinates(coordinates.lat, coordinates.lng, coordinates.zoom);

    }

    @Override
    public void saveMapLastCoordinates(double lat, double lng, float zoom) {
        MapAcitivityLastCoordinatesHelper.saveMapActivityLastCoordinates(getView().getContext(),
                lat, lng, zoom);
    }

    @Override
    public void changeOrderStatus(String orderId, OrderStatus orderStatus) {
        getView().showCommonLoader();
        serverCommunicator
                .moveOrderStatus(orderId, orderStatus)
                .subscribe(orderPojo -> {
                    OrderDAO.getInstance().addOrUpdateOrderFromPojo(
                            orderPojo, getRealm(), getView().getContext());
                    OrderDAO.getInstance().updateOrderStatus(orderId, orderStatus, getRealm());
                    getView().hideCommonLoader();
                    getView().onOrderStatusChanged();
                }, onError);
    }
}
