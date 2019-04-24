package com.tamaq.courier.model.database;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ArchiveOrderRealm extends RealmObject {

    @SerializedName("id")
    @Expose
    @PrimaryKey
    private Integer mId;
    @SerializedName("orderStatus")
    @Expose
    private String mOrderStatus;
    @SerializedName("orderId")
    @Expose
    private String mOrderId;
    @SerializedName("date")
    @Expose
    private String mDate;
    @SerializedName("mAddress")
    @Expose
    private String mAddress;

    public Integer getId() {
        return mId;
    }

    public void setId(Integer id) {
        mId = id;
    }

    public String getOrderStatus() {
        return mOrderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        mOrderStatus = orderStatus;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public void setOrderId(String orderId) {
        mOrderId = orderId;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String date) {
        mDate = date;
    }

    public String getAddress() {
        return mAddress;
    }

    public void setAddress(String address) {
        mAddress = address;
    }
}
