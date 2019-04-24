package com.tamaq.courier.model.database;


import android.content.Context;
import android.support.annotation.Nullable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.R;
import com.tamaq.courier.model.ui.OrderUIItem;
import com.tamaq.courier.model.ui.TimeWithEventItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

import static com.tamaq.courier.utils.DateHelper.SERVER_DATE_FORMAT;
import static com.tamaq.courier.utils.DateHelper.TIME_FORMAT;
import static com.tamaq.courier.utils.DateHelper.getStringFromDate;
import static com.tamaq.courier.utils.DateHelper.parseDateFromString;

public class OrderRealm extends RealmObject implements OrderUIItem {

    public static final String ID_ROW = "mOrderId";
    public static final String STATISTICS_ROW = "mStatisticList";

    @PrimaryKey
    @SerializedName("order_id")
    @Expose
    private String mOrderId;
    @SerializedName("created")
    @Expose
    private String mCreatedDate;
    @SerializedName("order_status")
    @Expose
    private String mOrderStatus;
    @SerializedName("requested_time")
    @Expose
    private String mRequestedTime;
    @SerializedName("accepted_time")
    @Expose
    private String mAcceptedTime;
    @SerializedName("send_to_restaurant_time")
    @Expose
    private String mSendToRestaurantTime;
    @SerializedName("arrive_to_restaurant_time")
    @Expose
    private String mArriveToRestaurantTime;
    @SerializedName("order_took_from_restaurant_time")
    @Expose
    private String mOrderTookFromRestaurantTime;
    @SerializedName("arrive_to_client_time")
    @Expose
    private String mArriveToClientTime;
    @SerializedName("order_completed")
    @Expose
    private String mOrderCompleted;
    @SerializedName("restaurant_name")
    @Expose
    private String mRestaurantName;
    @SerializedName("address_restaurant")
    @Expose
    private String mAddressRestaurant;
    @SerializedName("restaurant_address_lat")
    @Expose
    private Double mRestaurantAddressLat;
    @SerializedName("restaurant_address_lng")
    @Expose
    private Double mRestaurantAddressLng;
    @SerializedName("address_client")
    @Expose
    private String mAddressClient;
    @SerializedName("client_address_lat")
    @Expose
    private Double mClientAddressLat;
    @SerializedName("client_address_lng")
    @Expose
    private Double mClientAddressLng;
    @SerializedName("client_name")
    @Expose
    private String mClientName;
    @SerializedName("client_phone")
    @Expose
    private String mClientPhone;
    @SerializedName("client_rating")
    @Expose
    private Integer mClientRating;

    @SerializedName("payment_change")
    @Expose
    private Double mNeedChangeFrom;
    @SerializedName("payment")
    @Expose
    private Double mClientNeedToPay;
    @SerializedName("profit")
    @Expose
    private Double mProfitForDelivery;

    @SerializedName("dispatcher_phone")
    @Expose
    private String mDispatcherPhone;
    @SerializedName("restaurant_phone")
    @Expose
    private String mRestaurantPhone;
    @SerializedName("info_url")
    @Expose
    private String mInfoHTML;
    private Integer mNumber;
    private String mAddressForArchive;

    private Double mDeliveryAddressLat;
    private Double mDeliveryAddressLng;

    //  *** estimate fields begin
    /**
     * order estimate. My by one on ordinal values of {@link EstimateType}
     */
    @SerializedName("review_type")
    private Integer mReviewType;

    @SerializedName("review_comment")
    private String mReviewComment;

    @SerializedName("lived_tips")
    private boolean mLivedTips;
    //  *** estimate fields end

    private RealmList<StatisticItemRealm> mStatisticList;
    private int mProvideTime;

    /**
     * @param context     any Context
     * @param orderStatus mStatus of order, may be one of {@link OrderStatus }
     * @return The name of the next possible in order of mStatus.
     * For example, if the current mStatus is ARRIVED_AT_RESTAURANT,
     * then the next one is GOING_TO_CUSTOMER
     */
    public static String getStatusAsStringForNext(Context context, String orderStatus) {
        if (orderStatus == null)
            return context.getString(R.string.arrived_in_restaurant);
        switch (OrderStatus.fromString(orderStatus)) {
            case FIND_EXECUTOR:
            case EXECUTOR_SELECTED:
                return context.getString(R.string.go_to_restaurant);
            case GOING_TO_RESTAURANT:
                return context.getString(R.string.arrived_in_restaurant);
            case ARRIVED_AT_RESTAURANT:
                return context.getString(R.string.go_to_customer);
            case GOING_TO_CUSTOMER:
                return context.getString(R.string.arrived_to_customer);
            case ARRIVED_TO_CUSTOMER:
                return context.getString(R.string.order_completed_without_dot);
            case COMPLETE:
                return "";
            default:
                return "";
        }
    }

    /**
     * @param orderStatus current mStatus of order, see {@link }
     * @return Returns the next highest order mStatus, see {@link OrderStatus}
     */
    @Nullable
    public static OrderStatus getNextStatus(String orderStatus) {
        return OrderStatus.getNextStatus(OrderStatus.fromString(orderStatus));
    }

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public String getClientName() {
        return mClientName;
    }

    public void setClientName(String clientName) {
        mClientName = clientName;
    }

    public Integer getReviewType() {
        return mReviewType;
    }

    public void setReviewType(Integer reviewType) {
        mReviewType = reviewType;
    }

    public String getReviewComment() {
        return mReviewComment;
    }

    public void setReviewComment(String reviewComment) {
        mReviewComment = reviewComment;
    }

    public boolean isLivedTips() {
        return mLivedTips;
    }

    public void setLivedTips(boolean livedTips) {
        mLivedTips = livedTips;
    }

    public String getSendToRestaurantTime() {
        return mSendToRestaurantTime;
    }

    public void setSendToRestaurantTime(String sendToRestaurantTime) {
        mSendToRestaurantTime = sendToRestaurantTime;
    }

    public String getOrderTookFromRestaurantTime() {
        return mOrderTookFromRestaurantTime;
    }

    public void setOrderTookFromRestaurantTime(String orderTookFromRestaurantTime) {
        mOrderTookFromRestaurantTime = orderTookFromRestaurantTime;
    }

    public String getOrderCompletedTime() {
        return mOrderCompleted;
    }

    public void setOrderCompleted(String orderCompleted) {
        mOrderCompleted = orderCompleted;
    }

    public Integer getClientRating() {
        return mClientRating;
    }

    public void setClientRating(Integer clientRating) {
        mClientRating = clientRating;
    }

    @Override
    public String getArriveTimeUI() {
        return getTimeArriveToRestaurant();
    }

    public String getTimeArriveToRestaurant() {
        return mArriveToRestaurantTime;
    }

    public void setTimeArriveToRestaurant(String arriveToRestaurantTime) {
        mArriveToRestaurantTime = arriveToRestaurantTime;
    }

    @Override
    public String getRestAddressUI() {
        return getRestaurantAddress();
    }

    public String getRestaurantAddress() {
        return mAddressRestaurant;
    }

    public void setRestaurantAddress(String addressRestaurant) {
        mAddressRestaurant = addressRestaurant;
    }

    @Override
    public String getRestNameUI() {
        return getRestaurantName();
    }

    public String getRestaurantName() {
        return mRestaurantName;
    }

    public void setRestaurantName(String restaurantName) {
        mRestaurantName = restaurantName;
    }

    @Override
    public double getPaymentUI() {
        return getClientNeedToPay();
    }

    public double getClientNeedToPay() {
        return mClientNeedToPay != null ? mClientNeedToPay : 0;
    }

    public void setClientNeedToPay(double clientNeedToPay) {
        mClientNeedToPay = clientNeedToPay;
    }

    @Override
    public double getPaymentChangeUI() {
        return (int) getNeedChangeFrom();
    }

    public double getNeedChangeFrom() {
        return mNeedChangeFrom != null ? mNeedChangeFrom : 0;
    }

    public void setNeedChangeFrom(double needChangeFrom) {
        mNeedChangeFrom = needChangeFrom;
    }

    @Override
    public double getProfitUI() {
        return getProfitForDelivery();
    }

    public double getProfitForDelivery() {
        return mProfitForDelivery != null ? mProfitForDelivery : 0;
    }

    public void setProfitForDelivery(double profitForDelivery) {
        mProfitForDelivery = profitForDelivery;
    }

    @Override
    public String getClientAddressUI() {
        return getClientAddress();
    }

    public String getClientAddress() {
        return mAddressClient;
    }

    public void setClientAddress(String addressClient) {
        mAddressClient = addressClient;
    }

    @Override
    public double getRestLatUI() {
        return getRestaurantAddressLat();
    }

    public double getRestaurantAddressLat() {
        return mRestaurantAddressLat;
    }

    public void setRestaurantAddressLat(double restaurantAddressLat) {
        mRestaurantAddressLat = restaurantAddressLat;
    }

    @Override
    public double getRestLngUI() {
        return getRestaurantAddressLng();
    }

    public double getRestaurantAddressLng() {
        return mRestaurantAddressLng != null ? mRestaurantAddressLng : 0;
    }

    public void setRestaurantAddressLng(double restaurantAddressLng) {
        mRestaurantAddressLng = restaurantAddressLng;
    }

    @Override
    public double getClientLatUI() {
        return getClientAddressLat();
    }

    public double getClientAddressLat() {
        return mClientAddressLat != null ? mClientAddressLat : 0;
    }

    public void setClientAddressLat(double clientAddressLat) {
        mClientAddressLat = clientAddressLat;
    }

    @Override
    public double getClientLngUI() {
        return getClientAddressLng();
    }

    public double getClientAddressLng() {
        return mClientAddressLng != null ? mClientAddressLng : 0;
    }

    public void setClientAddressLng(double clientAddressLng) {
        mClientAddressLng = clientAddressLng;
    }

    @Override
    public String getAcceptedTimeUI() {
        return getTimeAccepted();
    }

    public String getTimeAccepted() {
        return mAcceptedTime;
    }

    public void setTimeAccepted(String acceptedTime) {
        mAcceptedTime = acceptedTime;
    }

    @Override
    public String getArriveToClientTimeUI() {
        return getTimeArriveToClient();
    }

    public String getTimeArriveToClient() {
        return mArriveToClientTime;
    }

    public void setTimeArriveToClient(String arriveToClientTime) {
        mArriveToClientTime = arriveToClientTime;
    }

    @Override
    public String getClientPhoneUI() {
        return getClientPhone();
    }

    public String getClientPhone() {
        return mClientPhone;
    }

    public void setClientPhone(String clientPhone) {
        mClientPhone = clientPhone;
    }

    @Override
    public String getDispatcherPhoneUI() {
        return getDispatcherPhone();
    }

    public String getDispatcherPhone() {
        return mDispatcherPhone;
    }

    public void setDispatcherPhone(String dispatcherPhone) {
        mDispatcherPhone = dispatcherPhone;
    }

    @Override
    public String getRestaurantPhoneUI() {
        return getRestaurantPhone();
    }

    public String getRestaurantPhone() {
        return mRestaurantPhone;
    }

    public void setRestaurantPhone(String restaurantPhone) {
        mRestaurantPhone = restaurantPhone;
    }

    @Override
    public String getFormattedArriveTime(Context context) {
        Date requestedDate = parseDateFromString(getRequestedTime());
        return context.getResources().getString(
                R.string.arrive_restaurant_minutes_and_time, getStringFromDate(requestedDate, TIME_FORMAT));
    }

    public String getRequestedTime() {
        return mRequestedTime;
    }

    public void setRequestedTime(String requestedTime) {
        mRequestedTime = requestedTime;
    }

    public boolean isArchive() {
        OrderStatus orderStatus = OrderStatus.fromString(getOrderStatus());
        return (orderStatus.equals(OrderStatus.COMPLETE)
                || orderStatus.equals(OrderStatus.OVERDUE)
                || orderStatus.equals(OrderStatus.CANCEL_CUSTOMER)
                || orderStatus.equals(OrderStatus.CANCEL_EXECUTOR));
    }

    public String getOrderStatus() {
        return mOrderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        mOrderStatus = orderStatus;
    }

    public String getStatusForDisplay(Context context) {
        return OrderStatus.getTitleUI(OrderStatus.fromString(getOrderStatus()), context);
    }

    public String getCreatedDate() {
        return mCreatedDate;
    }

    public void setCreatedDate(String createdDate) {
        mCreatedDate = createdDate;
    }

    public String getInfoHTML() {
        return mInfoHTML;
    }

    public void setInfoHTML(String infoHTML) {
        mInfoHTML = infoHTML;
    }

    public Integer getNumber() {
        return mNumber;
    }

    public void setNumber(Integer number) {
        mNumber = number;
    }

    public String getAddressForArchive() {
        return mAddressForArchive;
    }

    public void setAddressForArchive(String addressForArchive) {
        mAddressForArchive = addressForArchive;
    }

    public List<TimeWithEventItem> prepareEventItems(Context context) {
        List<StatisticItemRealm> list = getStatisticList();
        if (getStatisticList() == null)
            return new ArrayList<>();
        List<TimeWithEventItem> eventsList = new ArrayList<>();
        for (StatisticItemRealm statisticItemRealm : list) {
            if (statisticItemRealm.getMoveTo() != null) {
                OrderStatus orderStatus = OrderStatus.fromString(statisticItemRealm.getMoveTo());

                eventsList.add(new TimeWithEventItem(
                        getStringFromDate(parseDateFromString(statisticItemRealm.getCreated()), SERVER_DATE_FORMAT),
                        getStringFromDate(parseDateFromString(statisticItemRealm.getCreated()), TIME_FORMAT),
                        OrderStatus.getTitleUI(orderStatus, context)));
            }
        }

        Collections.sort(eventsList, (o1, o2) -> {
            Date firstDate = parseDateFromString(o1.getFullDate());
            Date secondDate = parseDateFromString(o2.getFullDate());
            return firstDate.compareTo(secondDate);
        });

        return eventsList;
    }

    public RealmList<StatisticItemRealm> getStatisticList() {
        return mStatisticList;
    }

    public void setStatisticList(RealmList<StatisticItemRealm> statisticList) {
        mStatisticList = statisticList;
    }

    @Nullable
    public Date getCompletedStatusTime() {
        List<StatisticItemRealm> statisticItemRealms = getStatisticList();
        if (statisticItemRealms == null || statisticItemRealms.isEmpty())
            return null;
        for (StatisticItemRealm statisticItemRealm : statisticItemRealms) {
            if (statisticItemRealm.getMoveTo() != null && statisticItemRealm.getMoveTo().equals(OrderStatus.COMPLETE.getValue()))
                return parseDateFromString(statisticItemRealm.getCreated());
        }
        return null;
    }

    public int getProvideTime() {
        return mProvideTime;
    }

    public void setProvideTime(int provideTime) {
        mProvideTime = provideTime;
    }

    public Double getDeliveryAddressLat() {
        return mDeliveryAddressLat;
    }

    public void setDeliveryAddressLat(Double deliveryAddressLat) {
        mDeliveryAddressLat = deliveryAddressLat;
    }

    public Double getDeliveryAddressLng() {
        return mDeliveryAddressLng != null ? mDeliveryAddressLng : 0;
    }

    public void setDeliveryAddressLng(Double deliveryAddressLng) {
        mDeliveryAddressLng = deliveryAddressLng;
    }

    public enum EstimateType {
        ESTIMATE, COMPLAINT;

        public boolean isPositive() {
            return name().equals(ESTIMATE.name());
        }
    }

    public enum CancelReason {
        NOT_TIME("not_time"), TRANSPORT_BREAK("transport_break"), OTHER_REASON("other_reason");

        String mValue;

        CancelReason(String value) {
            mValue = value;
        }

        public String getValue() {
            return mValue;
        }
    }
}
