package com.tamaq.courier.model.api.response;

import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.api.common.ObjectWithId;

public class PaymentPojo {

    @SerializedName("id")
    public String id;
    @SerializedName("created")
    public String created;
    @SerializedName("paytype")
    public String paytype;
    @SerializedName("user")
    public ObjectWithId user;
    @SerializedName("details")
    public String details;
    @SerializedName("amount")
    public double amount;
    @SerializedName("order")
    public ObjectWithId order;
    @SerializedName("order_number")
    public int orderNumber;
}
