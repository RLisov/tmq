package com.tamaq.courier.api;


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
import com.tamaq.courier.model.api.response.PaymentPojo;
import com.tamaq.courier.model.database.LocationRealm;
import com.tamaq.courier.model.database.TransportTypeRealm;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Observable;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;


public interface ApiService {

    String MAP_API_KEY = "AIzaSyCfdc4w4nw-d_5R0hBeIZm1VSQk9m_bd9E"; //"AIzaSyDy4u3lUp33U0hAqY4LxgJMegqxJHA7eAc"; //

    // new methods from real API

    @POST("registrate")
    Completable registrate(@Body RegistrateBody body);

    @POST("askapprove")
    Completable askapprove(@Body RegistrateBody body);

    @PUT("approve")
    Completable approve(@Body LoginBody body);

    @PUT("changelogin")
    Completable changelogin(@Body LoginBody body);

    @POST("login")
    Completable login(@Body LoginBody loginBody);

    @GET("dictionary/countries")
    Single<List<LocationRealm>> getCountries();

    @GET("dictionary/transport")
    Single<List<TransportTypeRealm>> getTransportTypes();

    @GET("dictionary/key/{countryKey}")
    Single<LocationRealm> getCities(@Path("countryKey") String countryKey);

    @GET("dictionary/key/{cityKey}")
    Single<LocationRealm> getRegions(@Path("cityKey") String cityKey);

    @PUT("me")
    Completable updateUser(@Body UserPojo userPojo);

    @GET("me")
    Single<UserPojo> getUser();

    @Multipart
    @POST("me/photo")
    Completable updatePhoto(@Query("tag") String tag, @Part MultipartBody.Part filePart);

    @POST("order/chats/{orderId}")
    Single<ChatMessageResponse> sendMessageToChat(@Path("orderId") String orderId,
                                                  @Body MessageSendBody body);

    @Multipart
    @POST("order/chats/with_photo")
    Single<ChatMessageResponse> sendChatPicture(@Query("order_id") String orderId,
                                                @Query("tag") String tag,
                                                @Query("msg") String string,
                                                @Part MultipartBody.Part filePart);

    @POST("askrole/Executor")
    Single<Object> askroleExecutor();

    @GET("notifications")
    Observable<List<NotificationPojo>> getNotifications();

    @PUT("notifications/read")
    Completable readNotifications(@Body ReadNotificationBody body);

    @GET("orders")
    Single<List<OrderPojo>> getOrderById(@Query("ids") String orderId);

    @GET("services")
    Single<List<ServicePojo>> getServiceById(@Query("ids") String queryId);

    @GET("users")
    Single<List<UserPojo>> getUserById(@Query("ids") String queryId);

    @PUT("orders/move")
    Single<OrderPojo> moveOrderStatus(@Body MoveOrderStatusBody body);

    @PUT("logout")
    Completable logout();

    @POST("me/payments/list")
    Single<List<PaymentPojo>> getPayments(@Body PaymentsBody body);

    @POST("location")
    Single<Object> sendUserLocation(@Body SendLocationBody body);

    @POST("orders/offer/{orderId}")
    Single<Object> offerOrder(@Path("orderId") String orderId);

    @POST("order/rates/{orderId}")
    Single<EstimatePojo> rateClient(@Path("orderId") String orderId, @Body EstimateBody body);

    @GET("servertime")
    Single<String> getServerTime();

    @POST("orders/list")
    Single<List<OrderPojo>> getOrdersForChat(@Body OrdersForCurrentUser body);


    @GET("https://maps.googleapis.com/maps/api/directions/json")
    Observable<DirectionsResponse> directions(
            @Query("key") String key,
            @Query("origin") String origin,
            @Query("destination") String destination
    );

    @DELETE("me/photo?tag=avatar")
    Completable removeUserPhoto();
}
