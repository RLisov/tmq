package com.tamaq.courier.api;

import android.text.TextUtils;

import com.tamaq.courier.dao.PaymentDAO;
import com.tamaq.courier.events.LogoutEvent;
import com.tamaq.courier.model.api.common.OrderPojo;
import com.tamaq.courier.model.api.common.ServicePojo;
import com.tamaq.courier.model.api.common.UserPojo;
import com.tamaq.courier.model.api.request_bodies.EstimateBody;
import com.tamaq.courier.model.api.request_bodies.LoginBody;
import com.tamaq.courier.model.api.request_bodies.MessageSendBody;
import com.tamaq.courier.model.api.request_bodies.MoveOrderStatusBody;
import com.tamaq.courier.model.api.request_bodies.OrdersForCurrentUser;
import com.tamaq.courier.model.api.request_bodies.PaymentsBody;
import com.tamaq.courier.model.api.request_bodies.ReadNotificationBody;
import com.tamaq.courier.model.api.request_bodies.RegistrateBody;
import com.tamaq.courier.model.api.request_bodies.SendLocationBody;
import com.tamaq.courier.model.api.response.ChatMessageResponse;
import com.tamaq.courier.model.api.response.DirectionsResponse;
import com.tamaq.courier.model.api.response.EstimatePojo;
import com.tamaq.courier.model.api.response.NotificationPojo;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.OrderRealm;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.model.database.PaymentRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;
import com.tamaq.courier.utils.DateHelper;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


public class ServerCommunicator {

    private static final int DEFAULT_TIMEOUT = 10; // seconds
    private static final long DEFAULT_RETRY_ATTEMPTS = 4;
    private static final String TAG = ServerCommunicator.class.getSimpleName();
    private ApiService service;

    public ServerCommunicator(ApiService service) {
        this.service = service;
    }

    public Completable registrate(String phoneNumber) {
        return service.registrate(new RegistrateBody(phoneNumber,
                RegistrateBody.PROVIDER_KEY_PHONE_PASSWORD))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable askapprove(String phoneNumber) {
        return service.askapprove(new RegistrateBody(phoneNumber,
                RegistrateBody.PROVIDER_KEY_PHONE_PASSWORD))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable approve(String phoneNumber, String password) {
        return service.approve(new LoginBody(phoneNumber,
                RegistrateBody.PROVIDER_KEY_PHONE_PASSWORD,
                password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable changelogin(String phoneNumber, String password) {
        return service.changelogin(new LoginBody(phoneNumber,
                RegistrateBody.PROVIDER_KEY_PHONE_PASSWORD,
                password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    // new methods from real API

    public Completable login(String phoneNumber, String password) {
        return service.login(new LoginBody(phoneNumber,
                RegistrateBody.PROVIDER_KEY_PHONE_PASSWORD,
                password))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<List<LocationRealm>> getCountries() {
        return service.getCountries()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<LocationRealm> getCitiesByCountry(String countryKey) {
        return service.getCities(countryKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<LocationRealm> getRegionsByCity(String cityKey) {
        return service.getRegions(cityKey)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable updateUserInfo(UserPojo userPojo) {
        return service.updateUser(userPojo)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable removeUserPhoto() {
        return service.removeUserPhoto()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<UserPojo> getUserInfo() {
        return service.getUser()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS)
                .map(userPojo -> {
                    boolean isUserDeleted = false;
                    if (TextUtils.isEmpty(userPojo.getId())) isUserDeleted = true;
                    else if (userPojo.isDeleted()) isUserDeleted = true;

                    if (isUserDeleted) EventBus.getDefault().post(new LogoutEvent(true));

                    return userPojo;
                });
    }

    public Completable updateUserPhoto(PhotoTag photoTag, File file) {
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        return service.updatePhoto(photoTag.toString(), part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<ChatMessageResponse> sendMessageToChat(String orderId, String text) {
        return service
                .sendMessageToChat(orderId, new MessageSendBody(text))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<ChatMessageResponse> sendChatPicture(String orderId, File file) {
        MultipartBody.Part part = MultipartBody.Part.createFormData(
                "file", file.getName(), RequestBody.create(MediaType.parse("image/*"), file));
        return service.sendChatPicture(orderId, PhotoTag.main.toString(), "", part)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<Object> askroleExecutor() {
        return service.askroleExecutor()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Observable<List<NotificationPojo>> getNotifications() {
        return service
                .getNotifications()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable readNotifications() {
        return service
                .readNotifications(ReadNotificationBody.fromDate(new Date()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<OrderPojo> getOrderById(String orderId) {
        return service
                .getOrderById(orderId)
                .map(list -> list.get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<ServicePojo> getServiceById(String serviceId) {
        return service
                .getServiceById(serviceId)
                .map(list -> list.get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<UserPojo> getUserById(String userId) {
        return service
                .getUserById(userId)
                .map(list -> list.get(0))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<OrderPojo> moveOrderStatus(String orderId, OrderStatus orderStatus) {
        return moveOrderStatus(orderId, orderStatus, "");
    }

    public Single<OrderPojo> moveOrderStatus(String orderId, OrderStatus orderStatus,
                                             String message) {
        return moveOrderStatus(orderId, orderStatus, message, null);
    }

    public Single<OrderPojo> moveOrderStatus(String orderId, OrderStatus orderStatus,
                                             String message, List<OrderRealm.CancelReason> reasonList) {
        return service
                .moveOrderStatus(new MoveOrderStatusBody(orderId, orderStatus.getValue(), message, reasonList))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Completable logout() {
        return service
                .logout()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<Object> sendUserLocation(double lat, double lng) {
        return service
                .sendUserLocation(new SendLocationBody(
                        DateHelper.getStringFromDateLocalToServerTimeZone(new Date(), DateHelper.SERVER_DATE_FORMAT),
                        String.valueOf(lat),
                        String.valueOf(lng)
                ))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<Object> offerOrder(String orderId) {
        return service
                .offerOrder(orderId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<EstimatePojo> rateClient(String orderId, String message,
                                           boolean positive, boolean tips) {
        return service
                .rateClient(orderId, new EstimateBody(
                        message, positive, tips))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

//    public Observable<List<LocationRealm>> getCountries() {
//        return service.getCountries()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                .retry(DEFAULT_RETRY_ATTEMPTS);
//    }

    public Single<List<TransportTypeRealm>> getTransportTypes() {
        return service.getTransportTypes()
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }


    public Observable<DirectionsResponse> directionsObservable(String origin, String destination) {
        return service.directions(ApiService.MAP_API_KEY, origin, destination)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

//    public Observable<OrderRealm> getOrder() {
//        return service.getOrder()
//                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
//                .retry(DEFAULT_RETRY_ATTEMPTS);
//    }

    public Single<List<PaymentRealm>> getPayments(int from, int count) {
        return service.getPayments(new PaymentsBody(from, count))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapObservable(Observable::fromIterable)
                .map(paymentPojo -> PaymentDAO.getInstance().fillPaymentFromPojo(
                        new PaymentRealm(), paymentPojo))
                .toList()
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<String> getServerTime() {
        return service.getServerTime()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public Single<List<OrderPojo>> getOrdersCurrentUser(String userId) {
        return service
                .getOrdersForChat(new OrdersForCurrentUser(userId))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .timeout(DEFAULT_TIMEOUT, TimeUnit.SECONDS)
                .retry(DEFAULT_RETRY_ATTEMPTS);
    }

    public enum PhotoTag {passport1, passport2, avatar, main}
}


