/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tamaq.courier.presenters.main;

import android.os.Handler;
import android.text.TextUtils;

import com.tamaq.courier.api.ServerCommunicator;
import com.tamaq.courier.dao.DialogDAO;
import com.tamaq.courier.dao.LocationDAO;
import com.tamaq.courier.dao.NotificationsDAO;
import com.tamaq.courier.dao.OrderDAO;
import com.tamaq.courier.dao.OrderRespondedInfoDAO;
import com.tamaq.courier.dao.UserDAO;
import com.tamaq.courier.model.api.common.OrderPojo;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.api.response.NotificationPojo;
import com.tamaq.courier.model.database.AutoRateSettingRealm;
import com.tamaq.courier.model.database.CommonOrdersSettingRealm;
import com.tamaq.courier.model.database.NotificationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderRespondedInfo;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.model.database.UserRealm;
import com.tamaq.courier.presenters.base.BaseActivityPresenterAbs;
import com.tamaq.courier.utils.DateHelper;
import com.tamaq.courier.utils.PrefsHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;

public class MainPresenter extends BaseActivityPresenterAbs<MainContract.View> implements MainContract.Presenter {

    private boolean mRecentlyUpdated = false;
    private Disposable firstChecker;
    private Disposable secondChecker;

    public MainPresenter(ServerCommunicator serverCommunicator) {
        super(serverCommunicator);
    }

    @Override
    public void attachPresenter(MainContract.View v) {
        super.attachPresenter(v);
        launchNotificationsCheck();
    }

    @Override
    public void checkNeedStartChecker() {
        launchNotificationsCheck();
    }

    private void launchNotificationsCheck() {
        if (firstChecker != null) {
            firstChecker.dispose();
            firstChecker = null;
        }
        if (secondChecker != null) {
            secondChecker.dispose();
            secondChecker = null;
        }

        firstChecker = Observable
                .interval(15, 30, TimeUnit.SECONDS)
                .filter(aLong -> getView() != null)
                .filter(aLong -> !PrefsHelper.isUserConfirmed(getView().getContext()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> checkIsUserConfirmed(), onError);

        secondChecker = Observable
                .interval(5, 15, TimeUnit.SECONDS)
                .filter(this::checkCanWork)
                .filter(this::isUserAuthorized)
                .filter(this::isNeedEstimateLastOrder)
//                .filter(this::checkStatusOnline)
                .flatMap(aLong -> getServerCommunicator().getNotifications())
                .subscribeOn(Schedulers.io())
                .map(this::convertNotificationToLocal)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(notificationRealms -> {
                    String userWorkType = UserDAO.getInstance().getCommonOrderSettings(getRealm()).getWorkType();
                    String reviewWorkType = CommonOrdersSettingRealm.WorkType.REVIEW.toString();
                    if (userWorkType.equals(reviewWorkType)) {
                        NotificationsDAO.getInstance().findNewOrderNotification(notificationRealms)
                                .doOnError(onError)
                                .subscribe((notificationRealm) -> {
                                    if (notificationRealm != null) loadOrderFromReviewTypeByNotificationId(notificationRealm);
                                }, onError);
                    }

                    NotificationsDAO.getInstance().findNewOrderAutorateNotification(notificationRealms)
                            .doOnError(onError)
                            .subscribe((notificationRealm) -> {
                                if (notificationRealm != null) loadOrderAutomticIfExist(notificationRealm);
                            }, onError);
                }, onError);
    }

    private boolean isNeedEstimateLastOrder(Long anyValue) {
        return !PrefsHelper.isNeedEstimateLastOrder(this.getView().getContext());
    }

    private boolean isUserAuthorized(Long anyValue) {
        return PrefsHelper.isUserAuthorized(this.getView().getContext());
    }

    private boolean checkCanWork(Long anyValue) {
        return this.getView() != null && !PrefsHelper.isUserBlocked(this.getView().getContext());
    }

    private List<NotificationRealm> convertNotificationToLocal(List<NotificationPojo> notificationPojos) {
        if (notificationPojos == null) return new ArrayList<>();
        return NotificationsDAO.getInstance()
                .addNotificationsFromPojo(notificationPojos, getRealm(), this.getView().getContext());
    }

    private boolean checkStatusOnline() {
        Realm defaultInstance = Realm.getDefaultInstance();
        String userWorkType = UserDAO.getInstance().getCommonOrderSettings(defaultInstance).getWorkType();
        String automaticWorkType = CommonOrdersSettingRealm.WorkType.NOT_WORKING.toString();
        defaultInstance.close();
        return !userWorkType.equals(automaticWorkType);
    }

    @Override
    public void checkIsUserConfirmed() {
        UserRealm userRealm = UserDAO.getInstance().getUser(getRealm());
        if (userRealm != null && userRealm.isConfirmed()) {
            if (getView() != null) {
                PrefsHelper.setUserConfirmed(true, getView().getContext());
                checkIsExecutingOrder();
            }
            return;
        }

        getServerCommunicator()
                .getUserInfo()
                .subscribe(userPojo -> {
                    if (userPojo.role.equals(UserRealm.EXECUTOR_ROLE)) {
                        UserDAO.getInstance().setUserStatusConfirmed(getRealm());
                        if (getView() != null) PrefsHelper.setUserConfirmed(true, getView().getContext());
                    }
                }, onError);
    }

    @Override
    public void checkNeedTrackUserLocation() {
        getView().onNeedTrackUserLocation();

//        if (true) { // // TODO: We must track the user's location whenever he has an active order
//            getView().onNeedTrackUserLocation();
//        } else {
//            getView().onNeedStopTrackUserLocation();
//        }
    }

    private void loadOrderFromReviewTypeByNotificationId(NotificationRealm notificationRealm) {
        getServerCommunicator()
                .getOrderById(notificationRealm.getOrderId())
                .filter(orderPojo -> orderPojo.getStatus().equals(OrderStatus.FIND_EXECUTOR.getValue()))
                .filter(orderPojo -> orderPojo.getEearn() >= UserDAO.getInstance().getCommonOrderSettings(getRealm()).getMinReward())
                .flatMap(orderPojo -> Maybe.fromSingle(loadOrderFullInfoSingle(orderPojo, orderPojo.getId())))
                .subscribe(orderRealm -> getView().onNewOrderNotification(orderRealm, notificationRealm),
                        onError);
    }

    private Single<OrderRealm> loadOrderFullInfoSingle(OrderPojo orderPojoSingle, String orderId) {
        return Single.just(orderPojoSingle)
                .flatMap(orderPojo -> {
                    OrderDAO.getInstance().addOrUpdateOrderFromPojo(orderPojo, getRealm(), getView().getContext());
                    OrderDAO.getInstance().updateOrderWithClientInfo(orderId, orderPojo.getCustomer(), getRealm());
                    return getServerCommunicator().getServiceById(orderPojo.getService().id);
                })
                .flatMap(servicePojo -> {
                    OrderDAO.getInstance().updateOrderWithServiceInfo(orderId, servicePojo, getRealm());
                    OrderRealm order = OrderDAO.getInstance().getOrderById(orderId, getRealm());
                    return Single.just(order);
                });
    }

    private void loadOrderAutomticIfExist(NotificationRealm notificationRealm) {
        String userWorkType = UserDAO.getInstance().getCommonOrderSettings(getRealm()).getWorkType();
        String automaticWorkType = CommonOrdersSettingRealm.WorkType.AUTOMATIC.toString();
        String reviewWorkType = CommonOrdersSettingRealm.WorkType.REVIEW.toString();

        if (userWorkType.equals(automaticWorkType)) loadOrderFromAutoRateTypeByNotificationId(notificationRealm);
        else /*if (userWorkType.equals(reviewWorkType))*/ loadOrderForcibly(notificationRealm);
    }

    private void loadOrderFromAutoRateTypeByNotificationId(NotificationRealm notificationRealm) {
        getServerCommunicator()
                .getOrderById(notificationRealm.getOrderId())
                .filter(orderPojo -> orderPojo.getStatus().equals(OrderStatus.EXECUTOR_SELECTED.getValue()))
                .filter(orderPojo -> {
                    AutoRateSettingRealm autoRateSettings = UserDAO.getInstance().getAutoRateSettings(getRealm());
                    int minimumClientRating = autoRateSettings.getMinimumClientRating();
                    int ratingPercent = orderPojo.getCustomer().getRatingPercent();
                    return ratingPercent > minimumClientRating;
                })
                .flatMap(orderPojo -> Maybe.fromSingle(loadOrderFullInfoSingle(orderPojo, orderPojo.getId())))
                .filter(orderRealm -> {
                    AutoRateSettingRealm autoRateSettings = UserDAO.getInstance().getAutoRateSettings(getRealm());
                    int maximumWorkTime = autoRateSettings.getMaximumWorkTime();
                    int provideTime = orderRealm.getProvideTime();
                    return provideTime <= maximumWorkTime;
                })
                .subscribe(orderRealm -> {
                    UserDAO.getInstance().setCurrentOrderId(orderRealm.getOrderId(), getRealm());
                    changeWorkStatusToNotWorking();
                    getView().onNewAutoRateNotification(notificationRealm, true);
                }, onError);
    }

    private void loadOrderForcibly(NotificationRealm notificationRealm) {
        getServerCommunicator()
                .getOrderById(notificationRealm.getOrderId())
                .filter(orderPojo -> orderPojo.getStatus().equals(OrderStatus.EXECUTOR_SELECTED.getValue()))
                .flatMap(orderPojo -> Maybe.fromSingle(loadOrderFullInfoSingle(orderPojo, orderPojo.getId())))
                .subscribe(orderRealm -> {
                    UserDAO.getInstance().setCurrentOrderId(orderRealm.getOrderId(), getRealm());
                    changeWorkStatusToNotWorking();
                    getView().onNewAutoRateNotification(notificationRealm, false);
                }, onError);
    }

    private void changeWorkStatusToNotWorking() {
        String lastWorkType = UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()).getWorkType();
        PrefsHelper.setLastWorkType(lastWorkType, getView().getContext());

        String workTypeNotWorking = CommonOrdersSettingRealm.WorkType.NOT_WORKING.toString();
        UserDAO.getInstance().updateCommonOrderSettingsWorkType(workTypeNotWorking, getRealm());

        getServerCommunicator()
                .updateUserInfo(UserPojo.createForUpdateCommonOrderSettings(
                        UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()),
                        getRealm()))
                .subscribe(() -> {
                }, onError);
    }

    @Override
    public void saveNotificationWasReadLocally(NotificationRealm notificationRealm) {
        NotificationsDAO.getInstance().saveNotificationWasReadLocally(notificationRealm, getRealm());
    }

    @Override
    public void checkNeedShowEstimateScreen() {
        OrderRealm orderRealm = OrderDAO.getInstance().getLastCompletedOrder(getRealm());
        if (orderRealm == null) return;
        if (orderRealm.getClientRating() == null) {
            getView().onNeedShowEstimateScreen(orderRealm.getOrderId());
        }
    }

    @Override
    public void checkIsExecutingOrder() {
        getServerCommunicator()
                .getUserInfo()
                .subscribe(userPojo -> {
                    if (userPojo.isExecutingOrder()) {
                        getServerCommunicator().getOrderById(userPojo.getExecutingOrder().getId())
                                .flatMap(orderPojo -> loadOrderFullInfoSingle(orderPojo, orderPojo.getId()))
                                .subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(orderRealm -> {
                                    UserDAO.getInstance().setCurrentOrderId(orderRealm.getOrderId(), getRealm());
                                    getView().openCurrentOrderScreen();
                                }, onError);
                    }
                }, onError);
    }

    @Override
    public void checkNeedShowNewOrderScreen() {
        OrderRespondedInfo orderRespondedInfo = OrderRespondedInfoDAO.getInstance().getOrderRespondedInfo(getRealm());
        if (orderRespondedInfo == null) return;
        getView().onNeedShowNewOrderScreen(orderRespondedInfo);
    }

    @Override
    public void getServerTime() {
        getServerCommunicator().getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe((serverTime) -> {
                    DateHelper.setDiffWithServer(serverTime);
                    getView().serverTimeUpdated();
                }, onError);
    }

    @Override
    public void loadCitiesAndDistricts() {
        String country = UserDAO.getInstance().getUser(getRealm()).getCountry().getKey();
        getServerCommunicator().getCitiesByCountry(country)
                .subscribe(citiesWithRegions -> {
                    LocationDAO.getInstance().addCountryWithCities(citiesWithRegions, getRealm());
                }, onError);
    }

    @Override
    public void updateDialogs() {
        if (mRecentlyUpdated || getView() == null) return;

        mRecentlyUpdated = true;
        new Handler().postDelayed(() -> mRecentlyUpdated = false, 3000);

        String userId = UserDAO.getInstance().getUser(getRealm()).getId();
        if (isUserAuthorized(null)) {
            getServerCommunicator().getOrdersCurrentUser(userId)
                    .map(orderPojoList ->
                            DialogDAO.getInstance().filterOrdersFor24Hours(orderPojoList))
                    .map(orderPojoList ->
                            DialogDAO.getInstance().convertOrderPojoToDialogRealm(
                                    getView().getContext(), getRealm(), orderPojoList))
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(dialogList -> {
                        if (getView() == null) return;

                        if (!dialogList.isEmpty()) {
                            DialogDAO.getInstance().addDialogs(dialogList, getRealm());
                            getView().onDialogsUpdated();
                        }

                    }, onError);
        }

    }

    @Override
    public void changeWorkStatusToLastActive() {
        String lastWorkType = PrefsHelper.getLastWorkType(getView().getContext());
        if (!TextUtils.isEmpty(lastWorkType)) {
            UserDAO.getInstance().updateCommonOrderSettingsWorkType(lastWorkType, getRealm());

            getServerCommunicator()
                    .updateUserInfo(UserPojo.createForUpdateCommonOrderSettings(
                            UserDAO.getInstance().getCommonOrderSettingsUnmanaged(getRealm()), getRealm()))
                    .subscribe(() -> {
                    }, onError);
        }
    }

    @Override
    public void detachPresenter() {
        super.detachPresenter();
    }
}
