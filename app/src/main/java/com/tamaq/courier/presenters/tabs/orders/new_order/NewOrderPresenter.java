package com.tamaq.courier.presenters.tabs.orders.new_order;

import android.location.Location;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Pair;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.maps.android.PolyUtil;
import com.tamaq.courier.R;
import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.OrderRespondedInfoDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.api.response.DirectionsResponse;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.presenters.base.BasePresenterAbs;
import com.tamaq.courier.presenters.tabs.notifications.NotificationType;
import com.tamaq.courier.shared.TamaqApp;
import com.tamaq.courier.utils.LocationHelper;
import com.tamaq.courier.utils.PrefsHelper;
import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;


public class NewOrderPresenter extends BasePresenterAbs<NewOrderContract.View>
        implements NewOrderContract.Presenter {

    private RxFragment mRxFragment;
    private TamaqApp mApp;
    private ServerCommunicator mServerCommunicator;
    private Location mLastUserLocation;
    private String mLastWorkType;

    public NewOrderPresenter(TamaqApp app, ServerCommunicator serverCommunicator) {
        this.mApp = app;
        this.mServerCommunicator = serverCommunicator;
    }

    @Override
    public void attachPresenter(NewOrderContract.View view) {
        super.attachPresenter(view);
        mRxFragment = (RxFragment) view;
    }

    @Override
    public Location getLastUserLocation() {
        return mLastUserLocation;
    }

    @Override
    public void loadPolylines(double latClient, double lngClient,
                              double latRestaurant, double lngRestaurant) {
        getView().showMapLoader();
        if (mLastUserLocation != null) {
            loadPolylinesLocal(latClient, lngClient, latRestaurant, lngRestaurant);
            getView().setUserMarker(mLastUserLocation.getLatitude(), mLastUserLocation.getLongitude());
        } else {
            LocationHelper.getLastKnownLocationObservable(mRxFragment.getActivity(),
                    addressObservable -> addressObservable
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(address -> {
                                mLastUserLocation = new Location("");
                                mLastUserLocation.setLatitude(address.getLatitude());
                                mLastUserLocation.setLongitude(address.getLongitude());
                                loadPolylinesLocal(latClient, lngClient, latRestaurant,
                                        lngRestaurant);
                                getView().setUserMarker(mLastUserLocation.getLatitude(),
                                        mLastUserLocation.getLongitude());
                            }, onError));
        }
        // displaying polyline between client and restaurant, if we don`t receive any location info
        // after 5 seconds
        new Handler().postDelayed(() -> {
            if (mLastUserLocation == null)
                loadPolylineBetweenClientAndRestaurant(
                        latClient, lngClient, latRestaurant, lngRestaurant);
        }, 5 * 1000);
    }

    private void loadPolylinesLocal(double latClient, double lngClient,
                                    double latRestaurant, double lngRestaurant) {
        Observable<DirectionsResponse> observableClientRestaurant = mServerCommunicator
                .directionsObservable(
                        String.format(Locale.US, "%f,%f", latClient, lngClient),
                        String.format(Locale.US, "%f,%f", latRestaurant, lngRestaurant));

        Observable<DirectionsResponse> observableCurierRestaurant = mServerCommunicator
                .directionsObservable(
                        String.format(Locale.US, "%f,%f",
                                mLastUserLocation.getLatitude(),
                                mLastUserLocation.getLongitude()),
                        String.format(Locale.US, "%f,%f", latRestaurant, lngRestaurant));
        Observable.zip(observableClientRestaurant, observableCurierRestaurant,
                Pair::new)
                .compose(mRxFragment.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(directionsPair -> {
                    getView().hideMapLoader();
                    if (directionsPair.first.routes.size() > 0
                            && directionsPair.first.routes.get(0) != null) {
                        getView().applyPolyline(directionsPair.first.routes.get(0).getFullPolyline(),
                                R.color.tangerine);
                    }
                    if (directionsPair.first.routes.size() > 0
                            && directionsPair.second.routes.get(0) != null) {
                        getView().applyPolyline(directionsPair.second.routes.get(0).getFullPolyline(),
                                R.color.dark_sky_blue);
                    }
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    if (!directionsPair.first.routes.isEmpty()) {
                        for (LatLng latLng : PolyUtil.decode(
                                directionsPair.first.routes.get(0).getFullPolyline()))
                            boundsBuilder.include(latLng);
                    }
                    if (!directionsPair.second.routes.isEmpty()) {
                        for (LatLng latLng : PolyUtil.decode(
                                directionsPair.second.routes.get(0).getFullPolyline()))
                            boundsBuilder.include(latLng);
                    }
                    getView().applyLatLngBounds(boundsBuilder.build());
                }, onError);
    }

    private void loadPolylineBetweenClientAndRestaurant(double latClient, double lngClient,
                                                        double latRestaurant, double lngRestaurant) {
        mServerCommunicator
                .directionsObservable(
                        String.format(Locale.US, "%f,%f", latClient, lngClient),
                        String.format(Locale.US, "%f,%f", latRestaurant, lngRestaurant))
                .subscribe(directions -> {
                    getView().hideMapLoader();
                    if (directions.routes.get(0) != null) {
                        getView().applyPolyline(directions.routes.get(0).getFullPolyline(),
                                R.color.tangerine);
                    }
                    LatLngBounds.Builder boundsBuilder = new LatLngBounds.Builder();
                    if (!directions.routes.isEmpty()) {
                        for (LatLng latLng : PolyUtil.decode(directions.routes.get(0).getFullPolyline()))
                            boundsBuilder.include(latLng);
                    }
                    getView().applyLatLngBounds(boundsBuilder.build());
                }, onError);
    }

    @Override
    public void acceptOrder(String orderId) {
        mServerCommunicator.offerOrder(orderId)
                .subscribe(o -> {
                    onOfferOrderSuccess(orderId);
                    OrderRespondedInfoDAO.getInstance().setOrderAccepterByUser(true, getRealm());
                }, onError);
    }

    private void onOfferOrderSuccess(String orderId) {
        getNotificationsCheckObservable(orderId)
                .flatMap(object -> getNotificationsCheckObservable(orderId))
                .timeout(2, TimeUnit.MINUTES, observer -> {
                    new Handler(Looper.getMainLooper()).post(
                            () -> getView().onClientNotSelectedAnyExecutor());
                    observer.onComplete();
                })
                .subscribe(notificationRealm -> {
                    OrderRespondedInfoDAO.getInstance().clear(getRealm());
                    if (notificationRealm.getType().equals(
                            NotificationType.YOU_SELECTED_EXECUTOR.getTypeId())) {
                        UserDAO.getInstance().setCurrentOrderId(orderId, getRealm());
                        getView().onOrderAcceptedByClient();
                    } else if (notificationRealm.getType().equals(
                            NotificationType.YOU_NOT_SELECTED_EXECUTOR.getTypeId())) {
                        getView().onOrderNotAcceptedByClient();
                    } else if (notificationRealm.getType().equals(
                            NotificationsDAO.EMPTY_NOTIFICATION_TYPE)) {
                        getView().onClientNotSelectedAnyExecutor();
                    }
                    if (!notificationRealm.getType().equals(
                            NotificationsDAO.EMPTY_NOTIFICATION_TYPE)) {
                        NotificationsDAO.getInstance().saveNotificationWasReadLocally(
                                notificationRealm, getRealm());
                    }
                    OrderRespondedInfoDAO.getInstance().clear(getRealm());
                }, onError);
    }

    private Observable<NotificationRealm> getNotificationsCheckObservable(String orderId) {
        return Observable.interval(1, 30, TimeUnit.SECONDS)
                .compose(mRxFragment.bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .flatMap(aLong -> mServerCommunicator.getNotifications())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(notificationPojoList -> {
                    List<NotificationRealm> notificationRealm = NotificationsDAO.getInstance()
                            .addNotificationsFromPojo(notificationPojoList, getRealm(), getView() != null ? getView().getContext() : null);
                    return NotificationsDAO.getInstance().findAnswerOnOrderOffer(
                            notificationRealm, orderId);
                });
    }

    @Override
    public OrderRealm getOrderById(String orderId) {
        return OrderDAO.getInstance().getOrderById(orderId, getRealm());
    }

    @Override
    public void saveOrderRespondedId(String orderId) {
        OrderRespondedInfoDAO.getInstance().saveRespondedOrderId(orderId, getRealm());
    }

    @Override
    public void startNotificationsCheck(String orderId) {
        onOfferOrderSuccess(orderId);
    }

    @Override
    public void saveOrderRespondedTimerInfo(int timerTotalTime, int lastEmittedTimerValue) {
        OrderRespondedInfoDAO.getInstance().saveRespondedOrderTimeInfo(
                timerTotalTime, lastEmittedTimerValue, getRealm());
    }

    @Override
    public void checkNeedShowTimeIsOutDialog(String orderId) {
        String userId = UserDAO.getInstance().getUser(getRealm()).getId();

        mServerCommunicator.getOrderById(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(orderPojo -> {
                    if (orderPojo.getExecutor().getId().equals(userId))
                        getView().onOrderAcceptedByClient();
                    else getView().showTimeIsOutDialog();
                }, onError);
    }

    @Override
    public void changeWorkStatus(boolean isWorking) {
        String currentWorkType;
        if (isWorking && !TextUtils.isEmpty(mLastWorkType)) {
            currentWorkType = mLastWorkType;
            PrefsHelper.setLastWorkType(currentWorkType, getView().getContext());
        } else {
            mLastWorkType = UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()).getWorkType();
            currentWorkType = CommonOrdersSettingRealm.WorkType.NOT_WORKING.toString();
            PrefsHelper.setLastWorkType(mLastWorkType, getView().getContext());
        }

        UserDAO.getInstance().updateCommonOrderSettingsWorkType(currentWorkType, getRealm());

        mServerCommunicator
                .updateUserInfo(UserPojo.createForUpdateCommonOrderSettings(
                        UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()), getRealm()))
                .subscribe(() -> {
                }, onError);
    }
}
