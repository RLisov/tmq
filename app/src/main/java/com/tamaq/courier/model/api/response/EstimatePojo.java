package com.tamaq.courier.model.api.response;


import com.google.gson.annotations.SerializedName;
import com.tamaq.courier.model.api.common.KeyObject;
import com.tamaq.courier.model.api.common.ObjectWithId;

public class EstimatePojo {

    @SerializedName("id")
    public String id;
    @SerializedName("created")
    public String created;
    @SerializedName("user")
    public ObjectWithId user;
    @SerializedName("order")
    public ObjectWithId order;
    @SerializedName("msg_valuation")
    public String msgValuation;
    @SerializedName("tips")
    public boolean tips;
    @SerializedName("valuation")
    public boolean valuation;
    @SerializedName("typerate")
    public KeyObject typerate;

}
