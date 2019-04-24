package com.tamaq.courier.model.api.common;


import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.api.response.ChatMessageResponse;
import com.tamaq.courier.model.api.response.EstimatePojo;
import com.tamaq.courier.model.database.OrderStatus;
import com.tamaq.courier.utils.DateHelper;

import java.util.List;

public class OrderPojo {

    @SerializedName("statistics")
    public List<StatisticsPojo> statistics;
    @SerializedName("customer")
    public UserWithLocation mCustomer;
    @SerializedName("id")
    private String mId;
    @SerializedName("created")
    private String mCreated;
    @SerializedName("second_find_executor")
    private boolean mSecondFindExecutor;
    @SerializedName("number")
    private int mNumber;
    @SerializedName("status")
    private String mStatus;
    @SerializedName("executor")
    private UserWithLocation mExecutor;
    @SerializedName("address")
    private AddressPojo mAddress;
    @SerializedName("service")
    private ObjectWithId mService;
    @SerializedName("pay_bonus")
    private int mPayBonus;
    @SerializedName("to_time")
    private String mToTime;
    @SerializedName("need_change_from")
    private int mNeedChangeFrom;
    @SerializedName("comment_to_executor")
    private String mCommentToExecutor;
    @SerializedName("description")
    private String mDescription;
    @SerializedName("will_bonus")
    private int mWillBonus;

    @SerializedName("sum")
    private double mSum;
    @SerializedName("esum")
    private double mEsum;
    @SerializedName("eearn")
    private double mEearn;

    @SerializedName("service_address")
    private AddressPojo mServiceAddress;
    @SerializedName("track_service_to_customer")
    private TrackPojo mTrackServiceToCustomer;
    @SerializedName("chats")
    private List<ChatMessageResponse> mChats;
    @SerializedName("order_products")
    private List<?> mOrderProducts;
    @SerializedName("rates")
    private List<EstimatePojo> mRates;

    public String getId() {
        return mId;
    }

    public void setId(String id) {
        mId = id;
    }

    public String getCreated() {
        return mCreated;
    }

    public void setCreated(String created) {
        mCreated = created;
    }

    public boolean isSecondFindExecutor() {
        return mSecondFindExecutor;
    }

    public void setSecondFindExecutor(boolean secondFindExecutor) {
        mSecondFindExecutor = secondFindExecutor;
    }

    public int getNumber() {
        return mNumber;
    }

    public void setNumber(int number) {
        mNumber = number;
    }

    public String getStatus() {
        return mStatus;
    }

    public void setStatus(String status) {
        mStatus = status;
    }

    public UserWithLocation getCustomer() {
        return mCustomer;
    }

    public void setCustomer(UserWithLocation customer) {
        mCustomer = customer;
    }

    public UserWithLocation getExecutor() {
        return mExecutor;
    }

    public void setExecutor(UserWithLocation executor) {
        mExecutor = executor;
    }

    public AddressPojo getAddress() {
        return mAddress;
    }

    public void setAddress(AddressPojo address) {
        mAddress = address;
    }

    public ObjectWithId getService() {
        return mService;
    }

    public void setService(ObjectWithId service) {
        mService = service;
    }

    public int getPayBonus() {
        return mPayBonus;
    }

    public void setPayBonus(int payBonus) {
        mPayBonus = payBonus;
    }

    public String getToTime() {
        return mToTime;
    }

    public void setToTime(String toTime) {
        mToTime = toTime;
    }

    public int getNeedChangeFrom() {
        return mNeedChangeFrom;
    }

    public void setNeedChangeFrom(int needChangeFrom) {
        mNeedChangeFrom = needChangeFrom;
    }

    public String getCommentToExecutor() {
        return mCommentToExecutor;
    }

    public void setCommentToExecutor(String commentToExecutor) {
        mCommentToExecutor = commentToExecutor;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String description) {
        mDescription = description;
    }

    public int getWillBonus() {
        return mWillBonus;
    }

    public void setWillBonus(int willBonus) {
        mWillBonus = willBonus;
    }

    public double getSum() {
        return mSum;
    }

    public void setSum(double sum) {
        mSum = sum;
    }

    public double getEsum() {
        return mEsum;
    }

    public void setEsum(double esum) {
        mEsum = esum;
    }

    public AddressPojo getServiceAddress() {
        return mServiceAddress;
    }

    public void setServiceAddress(AddressPojo serviceAddress) {
        mServiceAddress = serviceAddress;
    }

    public double getEearn() {
        return mEearn;
    }

    public void setEearn(double eearn) {
        mEearn = eearn;
    }

    public TrackPojo getTrackServiceToCustomer() {
        return mTrackServiceToCustomer;
    }

    public void setTrackServiceToCustomer(TrackPojo trackServiceToCustomer) {
        mTrackServiceToCustomer = trackServiceToCustomer;
    }

    public List<ChatMessageResponse> getChats() {
        return mChats;
    }

    public void setChats(List<ChatMessageResponse> chats) {
        mChats = chats;
    }

    public List<?> getOrderProducts() {
        return mOrderProducts;
    }

    public void setOrderProducts(List<?> orderProducts) {
        mOrderProducts = orderProducts;
    }

    public List<EstimatePojo> getRates() {
        return mRates;
    }

    public void setRates(List<EstimatePojo> rates) {
        mRates = rates;
    }

    @Nullable
    public String getDateStringForDialogCreated() {
        if (getStatistics() == null || statistics.isEmpty()) return null;
        for (StatisticsPojo statisticsPojo : getStatistics()) {
            if (statisticsPojo.getMoveTo() != null && statisticsPojo.getMoveTo().equals(OrderStatus.EXECUTOR_SELECTED.getValue()))
                return statisticsPojo.getCreatedInLocalTimeZone();
        }
        return null;
    }

    public List<StatisticsPojo> getStatistics() {
        return statistics;
    }

    public void setStatistics(List<StatisticsPojo> statistics) {
        this.statistics = statistics;
    }

    public static class UserWithLocation extends UserPojo {

        @SerializedName("last_location")
        public LastLocationPojo lastLocation;

        public LastLocationPojo getLastLocation() {
            return lastLocation;
        }
    }

    public static class TrackPojo {

        @SerializedName("distance")
        public int distance;
        @SerializedName("duration")
        public int duration;
        @SerializedName("empty")
        public boolean empty;
        @SerializedName("points")
        public String points;

        public int getDistance() {
            return distance;
        }

        public void setDistance(int distance) {
            this.distance = distance;
        }

        public int getDuration() {
            return duration;
        }

        public void setDuration(int duration) {
            this.duration = duration;
        }

        public boolean isEmpty() {
            return empty;
        }

        public void setEmpty(boolean empty) {
            this.empty = empty;
        }

        public String getPoints() {
            return points;
        }

        public void setPoints(String points) {
            this.points = points;
        }
    }

    public static class StatisticsPojo {

        @SerializedName("id")
        public String id;
        @SerializedName("created")
        public String created;
        @SerializedName("user")
        public ObjectWithId user;
        @SerializedName("msg")
        public String msg;
        @SerializedName("move_from")
        public String moveFrom;
        @SerializedName("move_to")
        public String moveTo;
        @SerializedName("order")
        public ObjectWithId order;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public ObjectWithId getUser() {
            return user;
        }

        public void setUser(ObjectWithId user) {
            this.user = user;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }

        public String getMoveFrom() {
            return moveFrom;
        }

        public void setMoveFrom(String moveFrom) {
            this.moveFrom = moveFrom;
        }

        public String getMoveTo() {
            return moveTo;
        }

        public void setMoveTo(String moveTo) {
            this.moveTo = moveTo;
        }

        public ObjectWithId getOrder() {
            return order;
        }

        public void setOrder(ObjectWithId order) {
            this.order = order;
        }

        public String getCreatedInLocalTimeZone() {
            return DateHelper.convertStringDateFromServerToLocalTimeZone(getCreated());
        }

        public String getCreated() {
            return created;
        }

        public void setCreated(String created) {
            this.created = created;
        }
    }
}
